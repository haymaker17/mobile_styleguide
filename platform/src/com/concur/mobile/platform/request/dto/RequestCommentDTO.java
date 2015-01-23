package com.concur.mobile.platform.request.dto;

/**
 * Created by ecollomb on 21/01/2015.
 */

import java.util.Date;

public class RequestCommentDTO {

    private String value = null;
    private String AuthorFirstName = null;
    private String AuthorLastName = null;
    private Date Date = null;
    private Boolean isLatest = null;


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAuthorFirstName() {
        return AuthorFirstName;
    }

    public void setAuthorFirstName(String authorFirstName) {
        AuthorFirstName = authorFirstName;
    }

    public String getAuthorLastName() {
        return AuthorLastName;
    }

    public void setAuthorLastName(String authorLastName) {
        AuthorLastName = authorLastName;
    }

    public Date getDate() {
        return Date;
    }

    public void setDate(Date date) {
        Date = date;
    }

    public Boolean getIsLatest() {
        return isLatest;
    }

    public void setIsLatest(Boolean isLatest) {
        this.isLatest = isLatest;
    }

}
