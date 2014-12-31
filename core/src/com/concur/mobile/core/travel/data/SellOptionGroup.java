package com.concur.mobile.core.travel.data;

import java.util.List;

public class SellOptionGroup {

    // private List<SellOptionItem> optionItems;
    private List<SellOptionField> sellOptionFields;

    public List<SellOptionField> getSellOptionFields() {
        return sellOptionFields;
    }

    public void setSellOptionFields(List<SellOptionField> tcfs) {
        this.sellOptionFields = tcfs;
    }

    // public List<SellOptionItem> getOptionItems() {
    // return optionItems;
    // }
    //
    // public void setOptionItems(List<SellOptionItem> optionItems) {
    // this.optionItems = optionItems;
    // }

}
