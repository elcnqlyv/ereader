package com.ereader.epub;

import com.ereader.util.Utf8;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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
        InputStream input = connection.openInputStream();
        try {
            DataInputStream data = new DataInputStream(input);
            while (true) {
                EntryHeader header = readHeader(data);
                if (header == null) {
                    return null;
                }
                if (wantedName.equals(header.name)) {
                    byte[] value = readValue(data, header);
                    if (header.hasDescriptor()) {
                        skipDataDescriptor(data);
                    }
                    return value;
                }
                skipEntry(data, header);
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
        InputStream input = connection.openInputStream();
        try {
            DataInputStream data = new DataInputStream(input);
            while (result.size() < wanted.size()) {
                EntryHeader header = readHeader(data);
                if (header == null) {
                    break;
                }
                if (wanted.get(header.name) != null) {
                    result.put(header.name, readValue(data, header));
                    if (header.hasDescriptor()) {
                        skipDataDescriptor(data);
                    }
                } else {
                    skipEntry(data, header);
                }
            }
            return result;
        } finally {
            input.close();
        }
    }

    private static EntryHeader readHeader(DataInputStream data) throws IOException {
        int signature;
        try {
            signature = readLittleEndianInt(data);
        } catch (java.io.EOFException endOfFile) {
            return null;
        }
        if (signature != LOCAL_SIGNATURE) {
            return null;
        }
        data.readUnsignedByte();
        data.readUnsignedByte();
        int flags = readLittleEndianShort(data);
        int method = readLittleEndianShort(data);
        skipFully(data, 8);
        long compressedSize = readLittleEndianInt(data) & 0xffffffffL;
        long uncompressedSize = readLittleEndianInt(data) & 0xffffffffL;
        int nameLength = readLittleEndianShort(data);
        int extraLength = readLittleEndianShort(data);
        byte[] nameBytes = new byte[nameLength];
        data.readFully(nameBytes);
        skipFully(data, extraLength);
        return new EntryHeader(Utf8.decode(nameBytes), flags, method,
                compressedSize, uncompressedSize);
    }

    private static byte[] readValue(InputStream input, EntryHeader header) throws IOException {
        if (header.hasDescriptor()) {
            if (header.method != 8) {
                throw new IOException("ZIP stored entries with data descriptors are unsupported");
            }
            return inflateDeflated(input, header.uncompressedSize);
        }
        return inflate(new LimitedInputStream(input, header.compressedSize),
                header.method, header.uncompressedSize);
    }

    private static void skipEntry(InputStream input, EntryHeader header) throws IOException {
        if (header.hasDescriptor()) {
            if (header.method != 8) {
                throw new IOException("ZIP stored entries with data descriptors are unsupported");
            }
            inflateDeflated(input, header.uncompressedSize);
            skipDataDescriptor((DataInputStream) input);
        } else {
            skipFully(input, header.compressedSize);
        }
    }

    private static byte[] inflate(InputStream input, int method, long expectedSize)
            throws IOException {
        if (method == 8) {
            return inflateDeflated(input, expectedSize);
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

    private static byte[] inflateDeflated(InputStream input, long expectedSize)
            throws IOException {
        com.jcraft.jzlib.Inflater inflater = new com.jcraft.jzlib.Inflater(
                com.jcraft.jzlib.JZlib.MAX_WBITS, true);
        ByteArrayOutputStream output = new ByteArrayOutputStream(
                (int) Math.min(expectedSize, 32768));
        byte[] compressed = new byte[512];
        byte[] decompressed = new byte[512];
        int count;
        try {
            while ((count = input.read(compressed)) != -1) {
                inflater.setInput(compressed, 0, count, false);
                while (inflater.getAvailIn() > 0) {
                    inflater.setOutput(decompressed, 0, decompressed.length);
                    int result = inflater.inflate(com.jcraft.jzlib.JZlib.Z_NO_FLUSH);
                    int produced = decompressed.length - inflater.getAvailOut();
                    if (produced > 0) {
                        output.write(decompressed, 0, produced);
                    }
                    if (result == com.jcraft.jzlib.JZlib.Z_STREAM_END) {
                        return output.toByteArray();
                    }
                    if (result != com.jcraft.jzlib.JZlib.Z_OK
                            && result != com.jcraft.jzlib.JZlib.Z_BUF_ERROR) {
                        throw new IOException("Invalid deflated EPUB entry");
                    }
                }
            }
            throw new IOException("Truncated deflated EPUB entry");
        } finally {
            inflater.end();
        }
    }

    private static void skipDataDescriptor(DataInputStream input) throws IOException {
        int first = readLittleEndianInt(input);
        skipFully(input, first == DESCRIPTOR_SIGNATURE ? 12 : 8);
    }

    private static int readLittleEndianShort(DataInputStream input) throws IOException {
        return input.readUnsignedByte() | (input.readUnsignedByte() << 8);
    }

    private static int readLittleEndianInt(DataInputStream input) throws IOException {
        return readLittleEndianShort(input) | (readLittleEndianShort(input) << 16);
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

        public long skip(long count) throws IOException {
            long skipped = input.skip(Math.min(count, remaining));
            remaining -= skipped;
            return skipped;
        }
    }
}
