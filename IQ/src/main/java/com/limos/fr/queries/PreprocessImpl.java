package com.limos.fr.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.limos.fr.mod.Config;

@Component
public class PreprocessImpl implements PreprocessService{

	@Override
	public String doPreprocess(String param) throws Exception {
			
		JSONObject parametters = new JSONObject(param);
		
		String dbName = parametters.getString("dbname");
		String dbType = parametters.getString("dbtype");
		String dbHost = parametters.getString("dbhost");
		String dbport = parametters.getString("dbport");
		String userName = parametters.getString("username");
		String pass = parametters.getString("pass");
		
		Connection con = Config.getCon(dbType, dbHost, dbport, dbName, userName, pass);
		JSONArray constraints = parametters.getJSONArray("constraints");

		JSONObject json = new JSONObject();
		
		con.setAutoCommit(false);
		
		try {
			//
			con.createStatement().executeUpdate(Config.loadMeanFunctions());
			// create constraints table
			con.createStatement().executeUpdate("CREATE SCHEMA IF NOT EXISTS C");
			con.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS C.C (id varchar(20) primary key, position int unique, description text, f varchar(500), w varchar(500))");
			con.createStatement().executeUpdate("DELETE FROM C.C ");
			
			
			//add column id
			String queryID = "SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname='public'";
			ResultSet res = con.createStatement().executeQuery(queryID);
			while(res.next()) {
				String tableName = res.getString(1);
				con.createStatement().executeUpdate("ALTER TABLE "+tableName+" ADD COLUMN IF NOT EXISTS ID_ID serial unique");
				con.createStatement().executeUpdate("ALTER TABLE "+tableName+" ADD COLUMN IF NOT EXISTS vioset bigint default 0");
				con.createStatement().executeUpdate("ALTER TABLE "+tableName+" ADD COLUMN IF NOT EXISTS violation int default 0");
				con.createStatement().executeUpdate("UPDATE "+tableName+" SET vioset = 0");
				con.createStatement().executeUpdate("UPDATE "+tableName+" SET violation = 0");
			}
			res.close();			
			
			for(int i=0; i<constraints.length(); i++) {
				String query = constraints.getString(i);
				
				try {
					String strs[] = query.split("( )*:( )*");
					String from = strs[0];
					String where = strs[1];
					
					String str = "INSERT INTO C.C(id, position, description, f, w) values ('DC_"+(i+1)+"', "+i+", '', '"+from+"', '"+where+"')";
					con.createStatement().executeUpdate(str);
					String rels [] = from.split("( )*,( )*"); 
					List<String> tables = new ArrayList<String>();
					String sel = "";
					for(String rel:rels) {
						String rr[] = rel.split("( )+");
						if (rr.length==3) {
							tables.add(rr[1]+":"+rr[2]);
							sel+=rr[2]+".ID_ID, ";
						}else {
							tables.add(rr[0]+":"+rr[1]);
							sel+=rr[1]+".ID_ID, ";
						}
					}
					sel = sel.substring(0, sel.length()-2);
					ResultSet inconsistentTuples = con.createStatement().executeQuery("SELECT "+sel+" FROM "+ from+ ((where.equals(""))?"":" WHERE "+where));
					while(inconsistentTuples.next()) {
						for(int j= 0; j<tables.size(); j++) {
							String sql = "UPDATE "+tables.get(j).split(":")[0]+ " SET vioset = vioset | "+Config.position(i)+", violation = violation + 1 WHERE ID_ID="+inconsistentTuples.getLong(j+1);
							con.createStatement().executeUpdate(sql);
						}
					}
					inconsistentTuples.close();
				}catch(Exception e) {
					e.printStackTrace();
					json.put("result", "failed");
					json.put("message", "Error in constraints file in constraint at position "+(i+1)+"\n"+e.getMessage());
					return json.toString();
				}
				
				
			}
			
			
			con.commit();
			con.close();
			json.put("result", "success");
			json.put("message", "Database processing is done");
			
		}catch(Exception e) {
			e.printStackTrace();
			con.rollback();
			con.close();
			json.put("result", "failled");
			json.put("message", "Database preprocessing is not done !!!");
			
		}
		return json.toString();
	}

	
}
