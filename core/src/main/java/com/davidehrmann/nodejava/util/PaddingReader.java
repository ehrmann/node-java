/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.davidehrmann.nodejava.util;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.Arrays;

/**
 * PaddingReader adds a header and trailer to a backing Reader if the the first bytes read from the backing Reader
 * match the match prefix.
 * <p/>
 * The typical use case is massaging input to wrap scripts starting with shebangs with (function() { ... })();
 * so they evaluate when run.
 */
public class PaddingReader extends PushbackReader {

    protected final char[] header;
    protected final char[] trailer;
    protected final char[] matchPrefix;
    protected State state = State.UNREAD;

    public PaddingReader(Reader in, char[] header, char[] trailer, char[] matchPrefix) {
        super(in, Math.max(header.length + matchPrefix.length, trailer.length));

        this.header = header;
        this.trailer = trailer;
        this.matchPrefix = matchPrefix;
    }

    protected void firstRead() throws IOException {
        char[] readPrefix = new char[this.matchPrefix.length];

        int read;
        int totalRead = 0;
        while (totalRead < readPrefix.length && (read = super.read(readPrefix, totalRead, readPrefix.length - totalRead)) >= 0) {
            totalRead += read;
        }

        super.unread(readPrefix, 0, totalRead);

        if (totalRead == readPrefix.length && Arrays.equals(readPrefix, this.matchPrefix)) {
            super.unread(this.header);
            state = State.PADDING_ENABLED;
        } else {
            state = State.PADDING_DISABLED;
        }
    }

    @Override
    public int read() throws IOException {
        synchronized (super.lock) {
            if (this.state == State.UNREAD) {
                this.firstRead();
            }

            if (this.state == State.PADDING_ENABLED) {
                int r = super.read();
                if (r < 0) {
                    super.unread(this.trailer);
                    this.state = State.DONE;
                } else {
                    return r;
                }
            }

            return super.read();
        }
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        synchronized (super.lock) {
            if (state == State.UNREAD) {
                this.firstRead();
            }

            if (state == State.PADDING_ENABLED) {
                int r = super.read(cbuf, off, len);
                if (r < 0) {
                    super.unread(this.trailer);
                    state = State.DONE;
                } else {
                    return r;
                }
            }

            return super.read(cbuf, off, len);
        }
    }

    protected enum State {
        UNREAD,
        PADDING_ENABLED,
        PADDING_DISABLED,
        DONE,
    }
}