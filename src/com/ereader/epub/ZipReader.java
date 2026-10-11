package com.ereader.epub;

import com.ereader.util.Utf8;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import javax.microedition.io.InputConnection;

public final class ZipReader {
    private static final int LOCAL_SIGNATURE = 0x04034b50;
    private static final int DESCRIPTOR_SIGNATURE = 0x08074b50;

    private ZipReader() {
    }

    public static byte[] readEntry(String epubPath, String wantedName) throws IOException {
        InputConnection connection = (InputConnection) Connector.open(epubPath, Connector.READ);
        try {
            return readEntry(connection, wantedName);
        } finally {
            connection.close();
        }
    }

    public static byte[] readEntry(InputConnection connection, String wantedName)
            throws IOException {
        Cursor input = new Cursor(connection.openInputStream());
        try {
            while (true) {
                EntryHeader header = readHeader(input);
                if (header == null) {
                    return null;
                }
                if (wantedName.equals(header.name)) {
                    byte[] value = readValue(input, header);
                    if (header.hasDescriptor()) {
                        skipDataDescriptor(input);
                    }
                    return value;
                }
                skipEntry(input, header);
            }
        } finally {
            input.close();
        }
    }

    public static Hashtable readEntries(InputConnection connection, String[] wantedNames)
            throws IOException {
        Hashtable wanted = new Hashtable();
        Hashtable result = new Hashtable();
        for (int i = 0; i < wantedNames.length; i++) {
            wanted.put(wantedNames[i], wantedNames[i]);
        }
        Cursor input = new Cursor(connection.openInputStream());
        try {
            while (result.size() < wanted.size()) {
                EntryHeader header = readHeader(input);
                if (header == null) {
                    break;
                }
                if (wanted.get(header.name) != null) {
                    result.put(header.name, readValue(input, header));
                    if (header.hasDescriptor()) {
                        skipDataDescriptor(input);
                    }
                } else {
                    skipEntry(input, header);
                }
            }
            return result;
        } finally {
            input.close();
        }
    }

    private static EntryHeader readHeader(InputStream data) throws IOException {
        int signature;
        try {
            signature = readLittleEndianInt(data);
        } catch (java.io.EOFException endOfFile) {
            return null;
        }
        if (signature != LOCAL_SIGNATURE) {
            return null;
        }
        readByte(data);
        readByte(data);
        int flags = readLittleEndianShort(data);
        int method = readLittleEndianShort(data);
        skipFully(data, 8);
        long compressedSize = readLittleEndianInt(data) & 0xffffffffL;
        long uncompressedSize = readLittleEndianInt(data) & 0xffffffffL;
        int nameLength = readLittleEndianShort(data);
        int extraLength = readLittleEndianShort(data);
        byte[] nameBytes = new byte[nameLength];
        readFully(data, nameBytes);
        skipFully(data, extraLength);
        return new EntryHeader(Utf8.decode(nameBytes), flags, method,
                compressedSize, uncompressedSize);
    }

    private static byte[] readValue(Cursor input, EntryHeader header) throws IOException {
        if (header.hasDescriptor()) {
            if (header.method != 8) {
                throw new IOException("ZIP stored entries with data descriptors are unsupported");
            }
            return inflateDeflated(input, header.uncompressedSize, input);
        }
        return inflate(new LimitedInputStream(input, header.compressedSize),
                header.method, header.uncompressedSize);
    }

    private static void skipEntry(Cursor input, EntryHeader header) throws IOException {
        if (header.hasDescriptor()) {
            if (header.method != 8) {
                throw new IOException("ZIP stored entries with data descriptors are unsupported");
            }
            inflateDeflated(input, header.uncompressedSize, input);
            skipDataDescriptor(input);
        } else {
            skipFully(input, header.compressedSize);
        }
    }

    private static byte[] inflate(InputStream input, int method, long expectedSize)
            throws IOException {
        if (method == 8) {
            return inflateDeflated(input, expectedSize, null);
        }
        if (method != 0) {
            throw new IOException("Unsupported ZIP compression method: " + method);
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream(
                (int) Math.min(expectedSize, 32768));
        byte[] buffer = new byte[512];
        int count;
        while ((count = input.read(buffer)) != -1) {
            output.write(buffer, 0, count);
        }
        return output.toByteArray();
    }

    private static byte[] inflateDeflated(InputStream input, long expectedSize,
            Cursor cursor)
            throws IOException {
        com.jcraft.jzlib.Inflater inflater = new com.jcraft.jzlib.Inflater(
                com.jcraft.jzlib.JZlib.MAX_WBITS, true);
        ByteArrayOutputStream output = new ByteArrayOutputStream(
                (int) Math.min(expectedSize, 32768));
        byte[] compressed = new byte[512];
        byte[] decompressed = new byte[512];
        int count = -1;
        boolean inputEnded = false;
        try {
            while (!inflater.finished()) {
                if (inflater.getAvailIn() == 0) {
                    if (inputEnded) {
                        throw new IOException("Truncated deflated EPUB entry");
                    }
                    count = input.read(compressed);
                    if (count < 0) {
                        inputEnded = true;
                        count = 0;
                        inflater.setInput(compressed, 0, 0, false);
                    } else {
                        inflater.setInput(compressed, 0, count, false);
                    }
                }
                inflater.setOutput(decompressed, 0, decompressed.length);
                int result = inflater.inflate(com.jcraft.jzlib.JZlib.Z_NO_FLUSH);
                int produced = decompressed.length - inflater.getAvailOut();
                if (produced > 0) {
                    output.write(decompressed, 0, produced);
                }
                if (result != com.jcraft.jzlib.JZlib.Z_OK
                        && result != com.jcraft.jzlib.JZlib.Z_STREAM_END
                        && result != com.jcraft.jzlib.JZlib.Z_BUF_ERROR) {
                    throw new IOException("Invalid deflated EPUB entry");
                }
                if (result == com.jcraft.jzlib.JZlib.Z_BUF_ERROR
                        && inflater.getAvailIn() == 0) {
                    continue;
                }
                if (result == com.jcraft.jzlib.JZlib.Z_BUF_ERROR
                        && produced == 0) {
                    throw new IOException("Invalid deflated EPUB entry");
                }
            }
            if (cursor != null) {
                int remaining = inflater.getAvailIn();
                if (remaining > 0) {
                    cursor.unread(compressed, count - remaining, remaining);
                }
            }
            return output.toByteArray();
        } finally {
            inflater.end();
        }
    }

    private static void skipDataDescriptor(InputStream input) throws IOException {
        int first = readLittleEndianInt(input);
        skipFully(input, first == DESCRIPTOR_SIGNATURE ? 12 : 8);
    }

    private static int readLittleEndianShort(InputStream input) throws IOException {
        return readByte(input) | (readByte(input) << 8);
    }

    private static int readLittleEndianInt(InputStream input) throws IOException {
        return readLittleEndianShort(input) | (readLittleEndianShort(input) << 16);
    }

    private static int readByte(InputStream input) throws IOException {
        int value = input.read();
        if (value < 0) {
            throw new java.io.EOFException();
        }
        return value;
    }

    private static void readFully(InputStream input, byte[] buffer) throws IOException {
        int offset = 0;
        while (offset < buffer.length) {
            int count = input.read(buffer, offset, buffer.length - offset);
            if (count < 0) {
                throw new java.io.EOFException();
            }
            offset += count;
        }
    }

    private static void skipFully(InputStream input, long count) throws IOException {
        while (count > 0) {
            long skipped = input.skip(count);
            if (skipped <= 0) {
                if (input.read() == -1) {
                    throw new IOException("Unexpected end of ZIP entry");
                }
                skipped = 1;
            }
            count -= skipped;
        }
    }

    private static final class EntryHeader {
        final String name;
        final int flags;
        final int method;
        final long compressedSize;
        final long uncompressedSize;

        EntryHeader(String name, int flags, int method, long compressedSize,
                long uncompressedSize) {
            this.name = name;
            this.flags = flags;
            this.method = method;
            this.compressedSize = compressedSize;
            this.uncompressedSize = uncompressedSize;
        }

        boolean hasDescriptor() {
            return (flags & 0x08) != 0;
        }
    }

    private static final class Cursor extends InputStream {
        private final InputStream input;
        private byte[] pending;
        private int pendingOffset;
        private int pendingLength;

        Cursor(InputStream input) {
            this.input = input;
        }

        public int read() throws IOException {
            if (pendingLength > 0) {
                int value = pending[pendingOffset] & 0xff;
                pendingOffset++;
                pendingLength--;
                return value;
            }
            return input.read();
        }

        public int read(byte[] buffer, int offset, int length) throws IOException {
            if (length == 0) {
                return 0;
            }
            if (pendingLength > 0) {
                int count = Math.min(length, pendingLength);
                System.arraycopy(pending, pendingOffset, buffer, offset, count);
                pendingOffset += count;
                pendingLength -= count;
                return count;
            }
            return input.read(buffer, offset, length);
        }

        void unread(byte[] buffer, int offset, int length) {
            if (length == 0) {
                return;
            }
            if (pendingLength == 0) {
                pending = new byte[length];
                System.arraycopy(buffer, offset, pending, 0, length);
                pendingOffset = 0;
                pendingLength = length;
                return;
            }
            byte[] combined = new byte[length + pendingLength];
            System.arraycopy(buffer, offset, combined, 0, length);
            System.arraycopy(pending, pendingOffset, combined, length, pendingLength);
            pending = combined;
            pendingOffset = 0;
            pendingLength = combined.length;
        }
    }

    private static final class LimitedInputStream extends InputStream {
        private final InputStream input;
        private long remaining;

        LimitedInputStream(InputStream input, long remaining) {
            this.input = input;
            this.remaining = remaining;
        }

        public int read() throws IOException {
            if (remaining == 0) {
                return -1;
            }
            int value = input.read();
            if (value >= 0) {
                remaining--;
            }
            return value;
        }

        public int read(byte[] buffer, int offset, int length) throws IOException {
            if (remaining == 0) {
                return -1;
            }
            int requested = (int) Math.min((long) length, remaining);
            int count = input.read(buffer, offset, requested);
            if (count > 0) {
                remaining -= count;
            }
            return count;
        }
    }
}
