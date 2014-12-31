package com.concur.mobile.core.travel.data;

public class TravelPoint {

    private String benchmark;
    private String benchmarkCurrency;
    private String pointsPosted;
    private String pointsPending;
    private String totalPoints;

    public String getBenchmark() {
        return benchmark;
    }

    public void setBenchmark(String benchmark) {
        this.benchmark = benchmark;
    }

    public String getBenchmarkCurrency() {
        return benchmarkCurrency;
    }

    public void setBenchmarkCurrency(String benchmarkCurrency) {
        this.benchmarkCurrency = benchmarkCurrency;
    }

    public String getPointsPosted() {
        return pointsPosted;
    }

    public void setPointsPosted(String pointsPosted) {
        this.pointsPosted = pointsPosted;
    }

    public String getPointsPending() {
        return pointsPending;
    }

    public void setPointsPending(String pointsPending) {
        this.pointsPending = pointsPending;
    }

    public String getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(String totalPoints) {
        this.totalPoints = totalPoints;
    }

}
