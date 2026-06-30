package com.myapp.backend.domain.model;

public class Address {

    private String streetNumber;
    private String streetName;
    private String additionalInfo;
    private String postalCode;
    private String city;
    private String country;

    public Address() {}

    public Address(String streetNumber, String streetName, String additionalInfo,
                   String postalCode, String city, String country) {
        this.streetNumber = streetNumber;
        this.streetName = streetName;
        this.additionalInfo = additionalInfo;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    public String getStreetNumber() { return streetNumber; }
    public void setStreetNumber(String streetNumber) { this.streetNumber = streetNumber; }

    public String getStreetName() { return streetName; }
    public void setStreetName(String streetName) { this.streetName = streetName; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}
