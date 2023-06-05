package com.limos.fr;

import java.util.Properties;

public class WithoutJSONDBConfig {

    // Sample Address: jdbc:postgresql://localhost:5432/samuelelanghi
    //Components
    public final static String DB_HOST_CONFIG = "dbhost"; // e.g. localhost
    public final static String DB_PORT_CONFIG = "dbport"; // e.g. 5432
    public final static String DB_TYPE_CONFIG = "dbtype"; // e.g. postgresql
    public final static String DB_NAME_CONFIG = "dbname"; // e.g. samuelelanghi
    public final static String DB_USER_CONFIG = "username";
    public final static String DB_PASSWORD_CONFIG = "pass";
    public final static String DB_CONSTRAINTS_CONFIG = "constraints";
    public static final String DB_CONSTRAINTS_TIMESTAMP_CONFIG = "constrainttimestamps";

    public static String getURL(Properties props){

       return "jdbc:" +
               props.getProperty(WithoutJSONDBConfig.DB_TYPE_CONFIG, "postgresql") + "://" +
               props.getProperty(WithoutJSONDBConfig.DB_HOST_CONFIG, "localhost") + ":" +
               props.getProperty(WithoutJSONDBConfig.DB_PORT_CONFIG, "5432") + "/" +
               props.getProperty(WithoutJSONDBConfig.DB_NAME_CONFIG, "samuelelanghi");
   }
}
