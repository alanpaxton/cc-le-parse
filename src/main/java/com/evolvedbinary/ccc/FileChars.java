package com.evolvedbinary.ccc;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileChars {

    static class ChunkBuffer {

        record Chunk(char[] content, int len) {}

        final List<Chunk> chunks = new ArrayList<>();

        void add(final char[] chars, final int size) {
            chunks.add(new Chunk(chars, size));
        }

        int length() {
            int result = 0;
            for (Chunk chunk : chunks) {
                result += chunk.len;
            }
            return result;
        }

        /**
         * Turn the efficiently collected chunk buffer into a single array
         */
        private void compress() {
            // The final "compressed" version has a single array
            if (chunks.size() == 1) {
                final Chunk zero = chunks.getFirst();
                if (zero.len == zero.content.length) {
                    return;
                }
            }

            // Even if there are 0 chunks, this works
            int len = length();
            char[] onebuf = new char[len];
            int pos = 0;
            for (Chunk chunk : chunks) {
                System.arraycopy(chunk.content, 0, onebuf, pos, chunk.len);
                pos += chunk.len;
            }
            chunks.clear();
            add(onebuf, len);
        }

        /**
         * Return the contents as a single array
         * Will compress into a single array if it is not that way already
         *
         * @return a char[] of the entire contents
         */
        char[] charArray() {
            compress();
            return chunks.getFirst().content;
        }
    }


    private final static int CHUNK_SIZE = 65536;

    /**
     * Efficiently read a large file into a character array
     * Allocate a number of fixed size, large buffers (chunks)
     * Fill each of those in turn
     * Then create a single <code>char[]</code> of the required size
     * and fill it from the chunks
     *
     * @param file to be read
     * @return an array of the contents of the file
     * @throws IOException if a problem occurs while reading the file
     */
    public static char[] read(File file) throws IOException {
        final ChunkBuffer chunks = new ChunkBuffer();
        try (FileReader reader = new FileReader(file)) {
            char[] chunk = new char[CHUNK_SIZE];
            int pos = 0;
            int read = 0;
            while (read >= 0) {
                while (read >= 0 && pos < chunk.length) {
                    read = reader.read(chunk, pos, chunk.length - pos);
                    if (read > 0) pos += read;
                }
                if (pos > 0) {
                    chunks.add(chunk, pos);
                }
                // Don't allocate a new char[] if we are about to exit
                if (pos > 0 && read >= 0) {
                    chunk = new char[CHUNK_SIZE];
                }
                pos = 0;
            }
        }
        return chunks.charArray();
    }
}
