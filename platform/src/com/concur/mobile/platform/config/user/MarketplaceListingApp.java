package com.concur.mobile.platform.config.user;

/**
 * Class to hold the parsed data from the Market Place API
 * 
 * @author RatanK
 * 
 */

// After the Market Place App Center Workflow requirements are finalised, this
// class will be moved to appropriate package
public class MarketplaceListingApp {

    private String listingID;
    private String name;
    private String partnerName;
    private String connectURL;
    private String contactPhone;
    private String partnerAppId;
    private String partnerAppConsumerKey;
    private String packageName;
    private String mobileSiteURL;
    private String appStoreURL;
    private boolean isUserConnected;
    private boolean launchAppIfAvailable;

    public String getListingID() {
        return listingID;
    }

    public void setListingID(String listingID) {
        this.listingID = listingID;
    }

    public String getConnectURL() {
        return connectURL;
    }

    public void setConnectURL(String connectURL) {
        this.connectURL = connectURL;
    }

    public boolean isUserConnected() {
        return isUserConnected;
    }

    public void setUserConnected(boolean isUserConnected) {
        this.isUserConnected = isUserConnected;
    }

    public boolean isLaunchAppIfAvailable() {
        return launchAppIfAvailable;
    }

    public void setLaunchAppIfAvailable(boolean launchAppIfAvailable) {
        this.launchAppIfAvailable = launchAppIfAvailable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getPartnerAppId() {
        return partnerAppId;
    }

    public void setPartnerAppId(String partnerAppId) {
        this.partnerAppId = partnerAppId;
    }

    public String getPartnerAppConsumerKey() {
        return partnerAppConsumerKey;
    }

    public void setPartnerAppConsumerKey(String partnerAppConsumerKey) {
        this.partnerAppConsumerKey = partnerAppConsumerKey;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getMobileSiteURL() {
        return mobileSiteURL;
    }

    public void setMobileSiteURL(String mobileSiteURL) {
        this.mobileSiteURL = mobileSiteURL;
    }

    public String getAppStoreURL() {
        return appStoreURL;
    }

    public void setAppStoreURL(String appStoreURL) {
        this.appStoreURL = appStoreURL;
    }

}
