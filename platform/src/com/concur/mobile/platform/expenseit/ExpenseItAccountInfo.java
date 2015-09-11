/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.platform.expenseit;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("UnusedDeclaration")
public class ExpenseItAccountInfo {
    @SerializedName("id")
    private long id;
    @SerializedName("title")
    private String title;
    @SerializedName("givenName")
    private String givenName;
    @SerializedName("middleName")
    private String middleName;
    @SerializedName("surname")
    private String surname;
    @SerializedName("password")
    private String password;
    @SerializedName("current_password")
    private String currentPassword;
    @SerializedName("createdAt")
    private String createdAt;
    @SerializedName("isAutoCTE")
    private boolean isAutoCTE;
    @SerializedName("isEmailNotificationEnabled")
    private boolean isEmailNotificationEnabled;
    @SerializedName("isNewsAndUpdatesEnabled")
    private boolean isNewsAndUpdatesEnabled;
    @SerializedName("emailNotificationMode")
    private int emailNotificationMode;
    @SerializedName("pushNotificationMode")
    private int pushNotificationMode;
    @SerializedName("canSignin")
    private boolean canSignin;
    @SerializedName("hasConcurLinkage")
    private boolean hasConcurLinkage;
    @SerializedName("hasConcurExpense")
    private boolean hasConcurExpense;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAutoCTE() {
        return isAutoCTE;
    }

    public void setAutoCTE(boolean isAutoCTE) {
        this.isAutoCTE = isAutoCTE;
    }

    public boolean isEmailNotificationEnabled() {
        return isEmailNotificationEnabled;
    }

    public void setEmailNotificationEnabled(boolean isEmailNotificationEnabled) {
        this.isEmailNotificationEnabled = isEmailNotificationEnabled;
    }

    public boolean isNewsAndUpdatesEnabled() {
        return isNewsAndUpdatesEnabled;
    }

    public void setNewsAndUpdatesEnabled(boolean isNewsAndUpdatesEnabled) {
        this.isNewsAndUpdatesEnabled = isNewsAndUpdatesEnabled;
    }

    public int getEmailNotificationMode() {
        return emailNotificationMode;
    }

    public void setEmailNotificationMode(int emailNotificationMode) {
        this.emailNotificationMode = emailNotificationMode;
    }

    public int getPushNotificationMode() {
        return pushNotificationMode;
    }

    public void setPushNotificationMode(int pushNotificationMode) {
        this.pushNotificationMode = pushNotificationMode;
    }

    public boolean isCanSignin() {
        return canSignin;
    }

    public void setCanSignin(boolean canSignin) {
        this.canSignin = canSignin;
    }

    public boolean isHasConcurLinkage() {
        return hasConcurLinkage;
    }

    public void setHasConcurLinkage(boolean hasConcurLinkage) {
        this.hasConcurLinkage = hasConcurLinkage;
    }

    public boolean isHasConcurExpense() {
        return hasConcurExpense;
    }

    public void setHasConcurExpense(boolean hasConcurExpense) {
        this.hasConcurExpense = hasConcurExpense;
    }
}
