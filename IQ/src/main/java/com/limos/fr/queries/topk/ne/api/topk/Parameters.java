package com.limos.fr.queries.topk.ne.api.topk;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.limos.fr.mod.Config;

//import uca.limos.api.DatasetConfig;
//import uca.limos.api.Utils;

public class Parameters {
	protected int k;

	protected List<String> typeRes;
	protected List<List<String>> results;
	protected List<String> attrsRes;
	protected List<String> attrsLabelsRes;
	protected List<String> relations_names;
	protected String predicate;
	protected String attributs;
	
	protected long valableConstraints;
	
//	protected List<Set<Long>> buffers; 
	protected long dcs;
	protected List<List<Long>> lattices;
	
	
	protected Map<Long, Set<Long>> correspondantConstraints;
	
	protected int algo;
	protected boolean most;
	
	public List<String> getAttrsRes() {
		return attrsRes;
	}
	
	public void setValableConstraints(long valableConstraints) {
		this.valableConstraints = valableConstraints;
	}
	
	protected Parameters() {
		results = new ArrayList<List<String>>();
		attrsRes = new ArrayList<String>();
		attrsLabelsRes = new ArrayList<String>();
		typeRes = new ArrayList<String>();

		relations_names = new ArrayList<String>();
		lattices = new ArrayList<List<Long>>();
//		buffers = new ArrayList<Set<Long>>();
		
		correspondantConstraints = new HashMap<Long, Set<Long>>();
		//readBlocks = new ArrayList<Set<Long>>();
	}
	
	
	public String aliasVioset(String left, long set) {
		String res = "";
		for(long l:correspondantConstraints.get(set)) {
			res = left+"="+l+" OR ";
		}
		if (!res.isEmpty())
			res = res.substring(0, res.length()-4);
		return res;
	}
	
	
	protected void loadLattices() throws SQLException {
		long dc  = 0;
		for (int i=0; i<relations_names.size(); i++) {
			String relation = relations_names.get(i);
			String query = "SELECT distinct vioset FROM "+relation.split("_")[0];
			ResultSet res = Config.getCon().createStatement().executeQuery(query);
			List<Long> v = new ArrayList<Long>();
			while(res.next()) {
				long l = res.getLong(1)&valableConstraints;
				v.add(l);
				if (!correspondantConstraints.containsKey(l))
					correspondantConstraints.put(l, new HashSet<Long>());
				correspondantConstraints.get(l).add(res.getLong(1));
				dc = dc | l;
			}
			res.close();
			lattices.add(v);
		}
		dcs = dc;
	}
	
	public static Parameters getInstance() {
		Parameters p = new Parameters();
		return p;
	}
	
	public static Parameters getInstance(String query) {
		Parameters p = new Parameters();
		p.setQuery(query);
		return p;
	}
	
	public void setQuery(String query) {
		//SELECT Attrs FROM R1 a1, ..., Rn an WHERE phi -=10#0 OR SELECT Attrs FROM R1 a1, ..., Rn an WHERE phi -=10#1
		//SELECT Attrs FROM R1 a1, ..., Rn an WHERE phi +=10#0 OR SELECT Attrs FROM R1 a1, ..., Rn an WHERE phi +=10#1 
		String froms[] = query.split("(?i)from");
		attributs = (froms[0].replaceAll("(?i)select", "")).replace(" ", "");
		String from[] = froms[1].split("(?i)where");
		String rel = from[0];
		String rels [] = rel.split("( )*,( )*");
		// for each relation, we keep relation name plus underscore plus the label of the relation
		for(String a:rels) {
			String b [] = a.split("( )+");
			if (b[0].equals(""))
				relations_names.add(b[1]+"_"+b[2]);
			else
				relations_names.add(b[0]+"_"+b[1]);
		}
		String phiAndK[] = null;
		
		if (from.length==2){
			if (from[1].contains("-=")) {
				phiAndK = from[1].split("-=");
				most = false;
			}else {
				phiAndK = from[1].split("\\+=");
				most = true;
			}
			predicate = phiAndK[0];
		}else {
			predicate = "True";
			if (from[0].contains("-=")) {
				phiAndK = from[0].split("-=");
				most = false;
			}else {
				phiAndK = from[0].split("\\+=");
				most = true;
			}
		}
		String kAndAlgo[] = phiAndK[1].split("#");
		k = Integer.parseInt(kAndAlgo[0]);
		algo = Integer.parseInt(kAndAlgo[1]);
	} 

	public void run() throws Exception {
		if (most) {
			if (algo==0) {
				MostConsistentFirstSingleOccurence algorithm = new MostConsistentFirstSingleOccurence();
				algorithm.setParam(this);
				algorithm.algorithm();
			}else {
				MostConsistentFirstMultiOccurence algorithm = new MostConsistentFirstMultiOccurence();
				algorithm.setParam(this);
				algorithm.algorithm();				
			}
		}else {
			if (algo==0) {
				MostInconsistentFirstSingleOccurence algorithm = new MostInconsistentFirstSingleOccurence();
				algorithm.setParam(this);
				algorithm.algorithm();
			}else {
				MostInconsistentFirstMultiOccurence algorithm = new MostInconsistentFirstMultiOccurence();
				algorithm.setParam(this);
				algorithm.algorithm();				
			}
		}
	}
	
	public void setAttrsLabelsRes(List<String> attrsLabelsRes) {
		this.attrsLabelsRes = attrsLabelsRes;
	}
	public void setAttrsRes(List<String> attrsRes) {
		this.attrsRes = attrsRes;
	}
	
	public void setTypeRes(List<String> typeRes) {
		this.typeRes = typeRes;
	}
			
	public List<List<String>> getResults() {
		return results;
	}
	
	public void setK(int k) {
		this.k = k;
	}
		
	protected String aggregeBit(List<Long> ll) {
		String res = "";
		for(long l:ll)
			res = res+l+"#";
		return res;
	}
	
}
