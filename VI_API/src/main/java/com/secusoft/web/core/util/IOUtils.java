package com.secusoft.web.core.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * IO Utils
 */
public class IOUtils {

    public static final int READ_BUFFER_SIZE = 4096;

    private static final int EOF = -1;

    /**
     * Read fully from the InputStream
     *
     * @param in
     * @return
     * @throws IOException
     */
    public static byte[] readFully(InputStream in) throws IOException {
        try {
            byte[] buf = new byte[READ_BUFFER_SIZE];
            int read = -1;
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            while ((read = in.read(buf)) != -1) {
                os.write(buf, 0, read);
            }
            os.close();
            return os.toByteArray();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Get InputStream bytes count without reading
     *
     * @param in
     * @return
     * @throws IOException
     */
    public static long getInputStreamLength(InputStream in) throws IOException {
        if (in instanceof FileInputStream) {
            return ((FileInputStream) in).getChannel().size();
        }
        return in.available();
    }

    /**
     * Reset the InputStream to the beginning/mark position without a large buffer
     *
     * @param in
     * @throws IOException
     */
    public static void resetInputStream(InputStream in) throws IOException {
        if (in.markSupported()) {
            in.reset();
        }
        if (in instanceof FileInputStream) {
            ((FileInputStream) in).getChannel().position(0);
        }
    }

    /**
     * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * 
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * 
     * The buffer size is given by  #DEFAULT_BUFFER_SIZE}.
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since Shamelessly cloned from Apache Commons IO 1.3 IOUtils
     */
    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        return copyLarge(input, output, new byte[READ_BUFFER_SIZE]);
    }

    /**
     * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * 
     * This method uses the provided buffer, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * 
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @param buffer the buffer to use for the copy
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since Shamelessly cloned from Apache Commons IO 2.2 IOUtils
     */
    public static long copyLarge(InputStream input, OutputStream output, byte[] buffer)
            throws IOException {
        long count = 0;
        int n = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static String readStreamAsString(InputStream in, String charset) throws IOException {
        return new String(readFully(in), charset);
    }

    public static String readStreamAsString(InputStream in) throws IOException {
        return new String(readFully(in), "utf-8");
    }
}
