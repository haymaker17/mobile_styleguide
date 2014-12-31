/**
 * 
 */
package com.concur.mobile.core.util.net;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.entity.FileEntity;

/**
 * An extension of <code>InputStreamEntity</code> for the purposes of providing a byte count update.
 */
public class CountingFileEntity extends FileEntity {

    private UploadListener listener;

    public CountingFileEntity(File file, String contentType) {
        super(file, contentType);
    }

    public void setUploadListener(UploadListener listener) {
        this.listener = listener;
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        super.writeTo(new CountingOutputStream(outstream));
    }

    class CountingOutputStream extends OutputStream {

        private long counter = 0l;
        private OutputStream outputStream;

        public CountingOutputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void write(int oneByte) throws IOException {
            this.outputStream.write(oneByte);
            counter++;
            if (listener != null) {
                int percent = (int) ((counter * 100) / getContentLength());
                listener.onChange(percent);
            }
        }
    }

    public interface UploadListener {

        public void onChange(int percent);
    }

}
