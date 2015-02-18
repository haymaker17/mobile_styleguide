package com.concur.mobile.platform.request.location;

import com.google.gson.annotations.SerializedName;

/**
 * Created by OlivierB on 17/02/2015.
 */
public class Location {

    /*
    AdministrativeRegion	string	optional	Administrative Region.
Country	string	optional	2-letter ISO 3166-1 country code.
CountrySubdivision	string	optional	ISO 3166-2:2007 country subdivision
IATACode	string	optional	IATA airport code
ID	string	optional	The unique identifier of the resource.
IsAirport	Boolean	optional	Whether the location is an Airport. format: true or false
IsBookingTool	Boolean	optional	Whether the location is used by the booking tool. format: true or false.
Latitude	Decimal	optional	The latitude for the geocode for the location.
Longitude	Decimal	optional	The longitude for the geocode for the location.
Name	string	optional	The location name. The maximum is 64 characters
URI	string	optional	The URI to the resource.
     */
    @SerializedName("AdministrativeRegion")
    private String administrativeRegion;
    @SerializedName("Country")
    private String country;
    @SerializedName("CountrySubdivision")
    private String countrySubdivision;
    @SerializedName("IATACode")
    private String IATACode;
    @SerializedName("ID")
    private String id;
    @SerializedName("IsAirport")
    private Boolean isAirport;
    @SerializedName("IsBookingTool")
    private Boolean isBookingTool;
    @SerializedName("Latitude")
    private Double latitude;
    @SerializedName("Longitude")
    private Double longitude;
    @SerializedName("Name")
    private String name;

    public String getAdministrativeRegion() {
        return administrativeRegion;
    }

    public void setAdministrativeRegion(String administrativeRegion) {
        this.administrativeRegion = administrativeRegion;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountrySubdivision() {
        return countrySubdivision;
    }

    public void setCountrySubdivision(String countrySubdivision) {
        this.countrySubdivision = countrySubdivision;
    }

    public String getIATACode() {
        return IATACode;
    }

    public void setIATACode(String IATACode) {
        this.IATACode = IATACode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getIsAirport() {
        return isAirport;
    }

    public void setIsAirport(Boolean isAirport) {
        this.isAirport = isAirport;
    }

    public Boolean getIsBookingTool() {
        return isBookingTool;
    }

    public void setIsBookingTool(Boolean isBookingTool) {
        this.isBookingTool = isBookingTool;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
