package com.concur.mobile.corp.image.detection;

import com.concur.receipt.detection.interfaces.BlurInfoModel;

public class BlurInfo {

    private double score;

    private boolean blurred;

    private String method;

    public BlurInfo(double score, boolean blurry, String method) {
        this.score = score;
        this.blurred = blurry;
        this.method = method;
    }

    public BlurInfo(BlurInfoModel qualityScore) {
        this.score = qualityScore.getScore();
        this.blurred = qualityScore.isBlurred();
        this.method = qualityScore.getMethod();
    }

    public boolean isBlurred() {
        return blurred;
    }

    public double getScore() {
        return score;
    }
}
