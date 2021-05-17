package com.limos.fr.queries.topk;

import java.util.ArrayList;
import java.util.List;

public class QueryEval {
	
	private List<String> alias;
	private String originalQuery;
	private String query;
	private boolean most;
	private int k;
	
	public void decomposeQuery(String oQuery){
		if (alias==null)
			alias = new ArrayList<String>();
		alias.clear();
		originalQuery = oQuery;
		//SELECT Attrs FROM R1 a1, ..., Rn an WHERE phi -=10
		//SELECT Attrs FROM R1 a1, ..., Rn an WHERE phi +=10
		String rel = oQuery.split("(?i)from")[1];
		rel = rel.split("(?i)where")[0];
		String rels [] = rel.split("( )*,( )*");
		for(String a:rels) {
			String b [] = a.split("( )+");
			if (b[0].equals("")) 
				alias.add(b[2]);
			else
				alias.add(b[1]);
		}
		String [] str = null;
		
		if (originalQuery.contains("-=")) {
			str = originalQuery.split("-=");
			most = false;
		}else {
			str = originalQuery.split("\\+=");
			most = true;
		}	
		k = Integer.parseInt(str[1].replace(" ", ""));
		query = str[0];
	}
	

	public String doPatern(Long [] values) throws Exception{
		
		String res = "";
		
		try {
			for(int i = 0; i<values.length-1; i++) 
				res += alias.get(i)+".vio = "+values[i]+" And ";
			res += alias.get(values.length-1)+".vio = "+values[(values.length-1)];
		}catch(Exception e) {
			throw new Exception("May be problem of array length: "+e.getMessage());
		}
		
		return res;
	}

	public String doPatern(List<Long> values) throws Exception{
		return doPatern((Long [])values.toArray());
	}
	
	public int getK() {
		return k;
	}
	
	public boolean isMost() {
		return most;
	}
	
	public List<String> getAlias() {
		return alias;
	}
	public String getOriginalQuery() {
		return originalQuery;
	}
	public String getQuery() {
		return query;
	}

	private QueryEval() {
		
	}
	
	public static QueryEval getInstance() {
		QueryEval eval = new QueryEval();
		
		return eval;
	}
	
}
