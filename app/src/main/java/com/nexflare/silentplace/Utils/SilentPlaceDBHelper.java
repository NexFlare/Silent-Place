package com.nexflare.silentplace.Utils;

/**
 * Created by 15103068 on 22-07-2017.
 */

public interface SilentPlaceDBHelper{

    String CREATE_TABLE="CREATE TABLE ";
    String TABLE_NAME="silentplace";
    String LBR=" ( ";
    String RBR=" ) ";
    String COLUMN_ID="id";
    String COLUMN_NAME="name";
    String COLUMN_LATITUDE="latitude";
    String COLUMN_LONGITUDE="longitude";
    String COMMA=" , ";
    String TYPE_INT=" INTEGER ";
    String TYPE_REAL=" REAL ";
    String TYPE_TEXT=" TEXT ";
    String SEMICOLON=" ;";
    String SELECT_ALL="SELECT * ";
    String INT_PK_AUTOIC = " INTEGER PRIMARY KEY AUTOINCREMENT ";
    String WHERE=" WHERE ";
    String FROM=" FROM ";
}
