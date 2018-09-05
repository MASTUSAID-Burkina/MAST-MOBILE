package com.rmsi.android.mast.domain;

import com.rmsi.android.mast.util.CommonFunctions;
import com.rmsi.android.mast.util.StringUtility;

import java.io.Serializable;

public class TitleType implements Serializable {
    private int id;
    private String name;
    private String nameOtherLang;

    public static String TABLE_NAME = "TITLE_TYPE";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameOtherLang() {
        return nameOtherLang;
    }

    public void setNameOtherLang(String nameOtherLang) {
        this.nameOtherLang = nameOtherLang;
    }

    public TitleType(){

    }

    @Override
    public String toString(){
        if(CommonFunctions.getInstance().getLocale().equalsIgnoreCase("en")){
            return StringUtility.empty(getName());
        }
        return StringUtility.empty(getNameOtherLang());
    }
}
