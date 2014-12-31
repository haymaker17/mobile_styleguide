package com.concur.mobile.core.travel.car.data;

public class CarType {

    public String code;
    public String description;
    public boolean isDefault;

    public CarType() {
        isDefault = false;
    }

    public CarType(String code, String description, boolean isDefault) {
        this.code = code;
        this.description = description;
        this.isDefault = isDefault;
    }

    public CarType(String code, String description) {
        this(code, description, false);
    }

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("Code")) {
            code = cleanChars;
        } else if (localName.equalsIgnoreCase("Description")) {
            description = cleanChars;
        } else if (localName.equalsIgnoreCase("IsDefault")) {
            isDefault = Boolean.parseBoolean(cleanChars);
        }

    }

}
