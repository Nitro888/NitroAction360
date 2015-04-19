/*
 * Copyright 2012 Kevin Sawicki <kevinsawicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.kevinsawicki.etag;

import static com.github.kevinsawicki.http.HttpRequest.CHARSET_UTF8;
import static com.github.kevinsawicki.http.HttpRequest.ENCODING_GZIP;
import static com.github.kevinsawicki.http.HttpRequest.HEADER_CONTENT_ENCODING;
import static com.github.kevinsawicki.http.HttpRequest.HEADER_ETAG;
import static com.github.kevinsawicki.http.HttpRequest.METHOD_GET;
import static java.net.HttpURLConnection.HTTP_OK;

import com.jakewharton.disklrucache.DiskLruCache;
import com.jakewharton.disklrucache.DiskLruCache.Editor;
import com.jakewharton.disklrucache.DiskLruCache.Snapshot;

import java.io.Closeable;
import java.io.File;
import java.io.FilterInputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Cache based solely on the ETag/If-None-Match request/response headers
 */
public class EtagCache implements Flushable {

    /**
     * One megabyte
     */
    public static final long ONE_MB = 1024L * 1024L;

    /**
     * Five megabytes
     */
    public static final long FIVE_MB = 5L * ONE_MB;

    /**
     * Ten megabytes
     */
    public static final long TEN_MB = 10L * ONE_MB;

    /**
     * Create cache
     *
     * @param file
     * @param size
     * @return cache or null if creation failed
     */
    public static EtagCache create(final File file, final long size) {
        try {
            return new EtagCache(file, size);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Get cached response
     */
    public static class CacheResponse implements Closeable {

        /**
         * ETag of response, never null
         */
        public final String eTag;

        /**
         * Body of response, never null
         */
        public final InputStream body;

        private final Snapshot snapshot;

        private CacheResponse(final String eTag, final InputStream body,
                              final Snapshot snapshot) {
            this.eTag = eTag;
            this.body = body;
            this.snapshot = snapshot;
        }

        public void close() {
            snapshot.close();
        }
    }

    private static final int ETAG = 0;

    private static final int BODY = 1;

    private static final MessageDigest DIGEST;

    static {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            digest = null;
        }
        DIGEST = digest;
    }

    private static class CacheStream extends FilterInputStream {

        private final Editor editor;

        private final OutputStream output;

        private final EtagCache cache;

        private final boolean flushOnClose;

        private boolean done;

        CacheStream(final InputStream input, final OutputStream output,
                    final Editor editor, final EtagCache cache, final boolean flushOnClose) {
            super(input);

            this.editor = editor;
            this.cache = cache;
            this.output = output;
            this.flushOnClose = flushOnClose;
        }

        private void abort() {
            synchronized (cache) {
                if (done)
                    return;
                done = true;
            }
            try {
                output.close();
            } catch (IOException ignored) {
                // Ignored
            }
            try {
                editor.abort();
            } catch (IOException ignored) {
                // Ignored
            }
        }

        @Override
        public int read() throws IOException {
            final int read = super.read();
            if (read != -1)
                try {
                    output.write(read);
                } catch (IOException e) {
                    abort();
                }
            return read;
        }

        @Override
        public int read(byte[] buffer, int offset, int count) throws IOException {
            final int read = super.read(buffer, offset, count);
            if (read > 0)
                try {
                    output.write(buffer, offset, read);
                } catch (IOException e) {
                    abort();
                }
            return read;
        }

        @Override
        public void close() throws IOException {
            synchronized (cache) {
                if (done)
                    return;
                done = true;
            }

            super.close();

            editor.commit();

            if (flushOnClose)
                try {
                    cache.flush();
                } catch (IOException ignored) {
                    // Ignored
                }
        }
    }

    private static String getKey(final String uri) {
        final byte[] input;
        try {
            input = uri.getBytes(CHARSET_UTF8);
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        final byte[] digested;
        synchronized (DIGEST) {
            DIGEST.reset();
            digested = DIGEST.digest(input);
        }

        final String hashed = new BigInteger(1, digested).toString(16);
        final int padding = 40 - hashed.length();
        if (padding == 0)
            return hashed;

        final char[] zeros = new char[padding];
        Arrays.fill(zeros, '0');
        return new StringBuilder(40).append(zeros).append(hashed).toString();
    }

    private static String getKey(final URLConnection connection) {
        if (connection instanceof HttpURLConnection)
            return getKey(connection.getURL().toExternalForm());
        else
            return null;
    }

    private static boolean isCacheable(final HttpURLConnection connection) {
        try {
            return METHOD_GET.equals(connection.getRequestMethod())
                    && HTTP_OK == connection.getResponseCode();
        } catch (IOException e) {
            return false;
        }
    }

    private final DiskLruCache cache;

    private final AtomicInteger hits = new AtomicInteger(0);

    private final AtomicInteger misses = new AtomicInteger(0);

    /**
     * Create cache
     *
     * @param file
     * @param size
     * @throws IOException
     */
    public EtagCache(final File file, final long size) throws IOException {
        if (DIGEST == null)
            throw new IOException("No SHA-1 algorithm available");

        cache = DiskLruCache.open(file, 1, 2, size);
    }

    /**
     * Register a hit to this cache
     *
     * @return hit count
     */
    int registerHit() {
        return hits.incrementAndGet();
    }

    /**
     * Register a miss to this cache
     *
     * @return miss count
     */
    int registerMiss() {
        return misses.incrementAndGet();
    }

    /**
     * @return hits
     */
    public int getHits() {
        return hits.intValue();
    }

    /**
     * @return misses
     */
    public int getMisses() {
        return misses.intValue();
    }

    /**
     * Get current size of the cache
     *
     * @return size
     */
    public long getSize() {
        return cache.size();
    }

    /**
     * Reset stats tracked for cache hits and misses
     *
     * @return this cache
     */
    public EtagCache resetStats() {
        hits.set(0);
        misses.set(0);
        return this;
    }

    /**
     * Get cached response for connection
     *
     * @param connection
     * @return etag or null if not in cache or connection isn't cacheable
     */
    public CacheResponse get(final URLConnection connection) {
        final String key = getKey(connection);
        if (key == null)
            return null;

        Snapshot snapshot;
        try {
            snapshot = cache.get(key);
        } catch (IOException e) {
            return null;
        }
        if (snapshot == null)
            return null;

        try {
            final String etag = snapshot.getString(ETAG);
            if (etag != null && etag.length() > 0) {
                final InputStream body = snapshot.getInputStream(BODY);
                if (body != null)
                    return new CacheResponse(etag, body, snapshot);
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * Create stream that will be cached after it is read
     *
     * @param connection
     * @return input stream that will be cached, null if cannot be cached
     */
    public InputStream put(final URLConnection connection) {
        return put(connection, false);
    }

    /**
     * Create stream that will be cached after it is read
     *
     * @param connection
     * @param flushCacheOnClose
     *          true to flush the cache when the returned response is closed
     * @return input stream that will be cached, null if cannot be cached
     */
    public InputStream put(final URLConnection connection,
                           final boolean flushCacheOnClose) {
        final String key = getKey(connection);
        if (key == null)
            return null;

        if (!isCacheable((HttpURLConnection) connection))
            try {
                cache.remove(key);
                return null;
            } catch (IOException e) {
                return null;
            }

        String etag = connection.getHeaderField(HEADER_ETAG);
        if (etag == null || etag.length() == 0)
            return null;

        Editor editor;
        try {
            editor = cache.edit(key);
        } catch (IOException e) {
            return null;
        }
        if (editor == null)
            return null;

        try {
            editor.set(ETAG, etag);
        } catch (IOException e) {
            try {
                editor.abort();
            } catch (IOException ignored) {
                // Ignored
            }
            return null;
        }

        InputStream input;
        try {
            input = connection.getInputStream();
            if (!(input instanceof InflaterInputStream)
                    && ENCODING_GZIP.equals(connection
                    .getHeaderField(HEADER_CONTENT_ENCODING)))
                input = new GZIPInputStream(input);
        } catch (IOException e) {
            return null;
        }
        OutputStream output;
        try {
            output = editor.newOutputStream(BODY);
        } catch (IOException e) {
            return null;
        }

        if (output != null)
            return new CacheStream(input, output, editor, this, flushCacheOnClose);
        else
            return null;
    }

    public void flush() throws IOException {
        cache.flush();
    }
}