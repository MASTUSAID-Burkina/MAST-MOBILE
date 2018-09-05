package com.rmsi.android.mast.domain;

import java.io.Serializable;

public class ShareType extends RefData implements Serializable {
    public static String TABLE_NAME = "SHARE_TYPE";
    public static int TYPE_MUTIPLE_OCCUPANCY_IN_COMMON = 10;
    public static int TYPE_TENANCY_IN_PROBATE = 40;
    public static int TYPE_GUARDIAN = 50;
    public static int TYPE_NON_NATURAL = 60;
    public static int TYPE_Single_Tenancy = 6;
    public static int TYPE_INDIVIDUAL = 7;
    public static int TYPE_COLLECTIVE = 8;
    public static int TYPE_Collective_Tenancy = 9;

    public String getTableName(){
        return TABLE_NAME;
    }

    public ShareType(){
        super();
    }
}
