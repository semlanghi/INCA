package com.limos.fr.mod;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.json.JSONArray;

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
		return con;
	}
	
	public static void load() throws SQLException {
		con = DriverManager.getConnection(getUrl());//getCon();
	}
	
	
	public static String getSet(Long key, Map<Integer, String> constraints) {
		Set<String> set = new HashSet<String>();
		for(Integer p:constraints.keySet()) {
			if (((key>>p)&1)!=0)
				set.add(constraints.get(p));
		}
		return set.toString().replace("[", "{").replace("]", "}");
	}
	
	public static JSONArray getData_vio(Map<Integer, Double> map, Set<Integer> vioX, double all) {
		JSONArray getData_vio = new JSONArray();
		for(Integer i:vioX) {
			if (map.containsKey(i))
				getData_vio.put((map.get(i)/all)*100);
			else
				getData_vio.put(0);
		}
		return getData_vio;
	}

	
	public static JSONArray getData_sub(Map<Long, Double> map, Set<Long> subX, double all) {
		JSONArray getData_vio = new JSONArray();
		for(Long i:subX) {
			if (map.containsKey(i))
				getData_vio.put((map.get(i)/all)*100);
			else
				getData_vio.put(0);
		}
		return getData_vio;
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
//		long res= 1;
//		for(int j=0; j<i; j++)
//			res*=2;
//		return res;
		return 1<<i;
	}
	
	public static  Map<Integer, String> getConstraintPos() throws Exception{
		String query = "SELECT * FROM c.c;";
		ResultSet r = Config.con.createStatement().executeQuery(query);
		Map<Integer, String> cst = new HashMap<Integer, String>();
		while(r.next())
			cst.put(r.getInt("position"), r.getString("id"));
		r.close();
		return cst;  
	}
	
	public static String loadMeanFunctions() {
		String req = "";
		  InputStream is = Config.class.getClassLoader().getResourceAsStream("functions.sql");
		Scanner sc = new Scanner(is);
		while(sc.hasNext())
			req += sc.nextLine();
		sc.close();
		return req;
	}

	
}
