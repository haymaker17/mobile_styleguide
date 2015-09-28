/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.platform.expenseit;

import android.util.Base64;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("UnusedDeclaration")
public class ExpenseItImage implements Serializable {

    @SerializedName("imageData")
    private ImageData imageData;

    @SerializedName("validPayload")
    private boolean validPayload;

    @SerializedName("expense")
    private ExpenseImage expense;

    public ImageData getImageData() {
        return imageData;
    }

    public void setImageData(ImageData imageData) {
        this.imageData = imageData;
    }

    public void setData(byte[] bytes, String mimeType) {
        ImageData data = new ImageData();
        data.setMimeType(mimeType);
        data.setImageOrder(1);

        String content;
        content = Base64.encodeToString(bytes, Base64.DEFAULT);
        data.setContent(content);
        setImageData(data);
        setValidPayload();
        expense = new ExpenseImage();
    }

    public void setValidPayload() {
        validPayload = isValidPayload();
    }

    private boolean isValidPayload() {
        return imageData != null && imageData.getMimeType() != null && imageData.getContent() != null;
    }

    public class ImageData {
        @SerializedName("mime-type")
        private String mimeType;

        @SerializedName("imageOrder")
        private int imageOrder;

        @SerializedName("content")
        private String content;

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public int getImageOrder() {
            return imageOrder;
        }

        public void setImageOrder(int imageOrder) {
            this.imageOrder = imageOrder;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public class ExpenseImage {
        @SerializedName("id")
        private String id;
        @SerializedName("totalImageCount")
        private int totalImageCount;
        @SerializedName("totalImagesUploaded")
        private int totalImagesUploaded;

        public ExpenseImage() {
            totalImageCount = 1;
            totalImagesUploaded = 1;
            long milliSecs = new Date().getTime();
            id = Long.toString(milliSecs);
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getTotalImageCount() {
            return totalImageCount;
        }

        public void setTotalImageCount(int totalImageCount) {
            this.totalImageCount = totalImageCount;
        }

        public int getTotalImagesUploaded() {
            return totalImagesUploaded;
        }

        public void setTotalImagesUploaded(int totalImagesUploaded) {
            this.totalImagesUploaded = totalImagesUploaded;
        }
    }
}
