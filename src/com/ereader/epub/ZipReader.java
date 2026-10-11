package com.ereader.epub;

import com.ereader.util.Utf8;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
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
        InputStream input = null;
        try {
            input = connection.openInputStream();
            DataInputStream data = new DataInputStream(input);
            while (true) {
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
                String name = Utf8.decode(nameBytes);

                if (wantedName.equals(name)) {
                    if ((flags & 0x08) != 0) {
                        byte[] result = inflateUnknownSize(data, method, uncompressedSize);
                        skipDataDescriptor(data);
                        return result;
                    }
                    LimitedInputStream entry = new LimitedInputStream(data, compressedSize);
                    return inflate(entry, method, uncompressedSize);
                }
                if ((flags & 0x08) != 0) {
                    skipUnknownSizeEntry(data, method, uncompressedSize);
                    skipDataDescriptor(data);
                } else {
                    LimitedInputStream entry = new LimitedInputStream(data, compressedSize);
                    skipFully(entry, compressedSize);
                }
            }
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    private static byte[] inflate(InputStream input, int method, long expectedSize) throws IOException {
        InputStream decoded = input;
        if (method == 8) {
            return inflateDeflated(input, expectedSize, 1);
        } else if (method != 0) {
            throw new IOException("Unsupported ZIP compression method: " + method);
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream(
                (int) Math.min(expectedSize, 32768));
        byte[] buffer = new byte[512];
        int count;
        while ((count = decoded.read(buffer)) != -1) {
            output.write(buffer, 0, count);
        }
        return output.toByteArray();
    }

    private static byte[] inflateUnknownSize(InputStream input, int method,
            long expectedSize) throws IOException {
        if (method != 8) {
            throw new IOException("ZIP stored entries with data descriptors are unsupported");
        }
        return inflateDeflated(input, expectedSize);
    }

    private static void skipUnknownSizeEntry(InputStream input, int method,
            long expectedSize) throws IOException {
        if (method == 8) {
            inflateDeflated(input, expectedSize, 1);
            return;
        }
        throw new IOException("ZIP stored entries with data descriptors are unsupported");
    }

    private static byte[] inflateDeflated(InputStream input, long expectedSize) throws IOException {
        return inflateDeflated(input, expectedSize, 512);
    }

    private static byte[] inflateDeflated(InputStream input, long expectedSize,
            int compressedBufferSize) throws IOException {
        com.jcraft.jzlib.Inflater inflater = new com.jcraft.jzlib.Inflater(
                com.jcraft.jzlib.JZlib.MAX_WBITS, true);
        ByteArrayOutputStream output = new ByteArrayOutputStream(
                (int) Math.min(expectedSize, 32768));
        byte[] compressed = new byte[compressedBufferSize];
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
        if (first == DESCRIPTOR_SIGNATURE) {
            skipFully(input, 12);
        } else {
            skipFully(input, 8);
        }
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
            long requested = Math.min(count, remaining);
            long skipped = input.skip(requested);
            remaining -= skipped;
            return skipped;
        }
    }
}
