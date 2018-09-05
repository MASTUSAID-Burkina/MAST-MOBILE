package com.rmsi.android.mast.domain;

import java.io.Serializable;

public class PersonOfInterest implements Serializable {
    private Long id;
    transient private Long featureId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String address;
    private String idNumber;
    private String dob;
    private int genderId;
    private int relationshipId;

    public static String TABLE_NAME = "POI";
    public static String COL_ID = "ID";
    public static String COL_FIRST_NAME = "FIRST_NAME";
    public static String COL_MIDDLE_NAME = "MIDDLE_NAME";
    public static String COL_LAST_NAME = "LAST_NAME";
    public static String COL_ADDRESS = "ADDRESS";
    public static String COL_ID_NUMBER = "ID_NUMBER";
    public static String COL_DOB = "DOB";
    public static String COL_GENDER_ID = "GENDER_ID";
    public static String COL_FEATURE_ID = "FEATURE_ID";
    public static String COL_RELATIONSHIP_ID = "RELATIONSHIP_ID";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFeatureId() {
        return featureId;
    }

    public void setFeatureId(Long featureId) {
        this.featureId = featureId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public int getGenderId() {
        return genderId;
    }

    public void setGenderId(int genderId) {
        this.genderId = genderId;
    }

    public int getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(int relationshipId) {
        this.relationshipId = relationshipId;
    }

    public PersonOfInterest(){

    }
}
