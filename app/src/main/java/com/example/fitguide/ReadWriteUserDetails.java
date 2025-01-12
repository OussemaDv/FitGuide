package com.example.fitguide;

public class ReadWriteUserDetails {
    public String fullName, doB, gender, moblie;

    public ReadWriteUserDetails(){}

    public ReadWriteUserDetails( String textDoB, String textGender, String textMobile ){
        this.doB = textDoB;
        this.gender = textGender;
        this.moblie = textMobile;
    }
}
