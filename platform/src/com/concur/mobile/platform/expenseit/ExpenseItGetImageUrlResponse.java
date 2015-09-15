/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.platform.expenseit;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class ExpenseItGetImageUrlResponse extends ErrorResponse implements Serializable {
    @SerializedName("images")
    private List<ExpenseImageUrl> images;

    public List<ExpenseImageUrl> getImages() {
        return images;
    }

    public void setImages(List<ExpenseImageUrl> images) {
        this.images = images;
    }

    public static class ExpenseImageUrl extends ErrorResponse {

        @SerializedName("expenseId")
        private long expenseId;

        @SerializedName("imageDataUrl")
        private String imageDataUrl;

        public long getExpenseId() {
            return expenseId;
        }

        public void setExpenseId(long expenseId) {
            this.expenseId = expenseId;
        }

        public String getImageDataUrl() {
            return imageDataUrl;
        }

        public void setImageDataUrl(String imageDataUrl) {
            this.imageDataUrl = imageDataUrl;
        }
    }
}
