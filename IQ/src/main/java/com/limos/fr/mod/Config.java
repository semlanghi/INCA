package com.limos.fr.mod;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Config {
	
	public static String passWord = "65515525";
	public static String userName = "postgres";
	
	public static String port = "5432";
	public static String host = "localhost";
	public static String type = "postgresql";
	public static String databaseName = "foodinspection";

	
	public static Connection con = null;
	
	
	public static String getUrl() {
		return "jdbc:"+type+"://"+host+":"+port+"/"+databaseName+"?user="+userName+"&password="+passWord;
	}

	public static Connection getCon(String t, String h, String p, String dbn, String usrn, String pass) throws SQLException {
		String param = "jdbc:"+t+"://"+h+":"+p+"/"+dbn+"?user="+usrn+"&password="+pass;
		return DriverManager.getConnection(param);
	}
	
	public static Connection getCon() throws SQLException {
		return DriverManager.getConnection(getUrl());
	}
	
	public static void load() throws SQLException {
		con = getCon();
	}
	

	public static List<String> getTables() throws SQLException {
		List<String> getTables = new ArrayList<String>();
		String query = "SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname='public'";
		ResultSet res = con.createStatement().executeQuery(query);
		while(res.next()) 
			getTables.add(res.getString(1));
		return getTables;
	}

	public static long position(int i) {
		if (i<0)
			return -1;
		long res= 1;
		for(int j=0; j<i; i++)
			res*=2;
		return res;
	}
	
}
