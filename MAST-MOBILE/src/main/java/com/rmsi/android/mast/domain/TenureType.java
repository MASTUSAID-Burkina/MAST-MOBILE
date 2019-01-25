package com.rmsi.android.mast.domain;

import java.io.Serializable;

/**
 * Created by Ambar.Srivastava on 1/12/2018.
 */

public class TenureType  extends ResourceAttribute implements Serializable {
    public static String TABLE_NAME = "TENURE_TYPE";
    public static String CODE_OPEN = "9";
    public static String CODE_PRIVATE_INDIVIDUAL = "10";
    public static String CODE_COLLECTIVE = "11";
    public static String CODE_COMMUNITY = "12";
    public static String CODE_PUBLIC = "13";
    public static String CODE_ORGANIZATION_FORMAL = "14";
    public static String CODE_PRIVATE_JOINT = "17";
    public static String CODE_ORGANIZATION_INFORMAL = "18";

    public String getTableName(){
        return TABLE_NAME;
    }

    public TenureType(){
        super();
    }
}