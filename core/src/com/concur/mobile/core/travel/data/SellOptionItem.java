package com.concur.mobile.core.travel.data;

import java.util.List;

public class SellOptionItem {

    private String amount;
    private String currCode;
    private String id;
    private boolean optional;
    private boolean perPassenger;
    private boolean visible;
    private String itemCode;
    private String name;
    private String optionItemId;
    private List<String> remarks;
    private boolean selected;
    private String value;
    private String uiType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isPerPassenger() {
        return perPassenger;
    }

    public void setPerPassenger(boolean perPassenger) {
        this.perPassenger = perPassenger;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOptionItemId() {
        return optionItemId;
    }

    public void setOptionItemId(String optionItemId) {
        this.optionItemId = optionItemId;
    }

    public List<String> getRemarks() {
        return remarks;
    }

    public void setRemarks(List<String> remarks) {
        this.remarks = remarks;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUiType() {
        return uiType;
    }

    public void setUiType(String uiType) {
        this.uiType = uiType;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrCode() {
        return currCode;
    }

    public void setCurrCode(String currCode) {
        this.currCode = currCode;
    }
}
