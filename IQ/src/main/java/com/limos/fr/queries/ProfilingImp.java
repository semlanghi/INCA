package com.limos.fr.queries;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.limos.fr.mod.Config;


@Component
public class ProfilingImp implements ProfilingService{

	public String getVioAndNoVio() throws Exception{
		return byVio(-1);
	}

	public String getVioAndNoVio(long l) throws Exception{
		return byVio(l);
	}

	public String byVio(long considered) throws Exception{
		List<String> tables = Config.getTables();
		double vio = 0d;
		double novio = 0d;
		for(String tab:tables) {
			ResultSet s1 = null;
			ResultSet s2 = null;
			if (considered>=0) {
				s1 = Config.con.createStatement().executeQuery("SELECT count(*) FROM "+tab+" WHERE vioset = 0");
				s2 = Config.con.createStatement().executeQuery("SELECT count(*), vioset FROM "+tab+" WHERE vioset <> 0 GROUP BY vioset");
				s1.next();
				novio += s1.getInt(1);
				while(s2.next()){
					if ( (s2.getLong(2) & considered) == 0)
						novio += s2.getInt(1);
					else
						vio += s2.getInt(1);
				}
			}else {
				s1 = Config.con.createStatement().executeQuery("SELECT count(*) FROM "+tab+" WHERE vioset <> 0");
				s2 = Config.con.createStatement().executeQuery("SELECT count(*) FROM "+tab+" WHERE vioset = 0");
				s1.next();s2.next();
				vio += s1.getInt(1);
				novio += s2.getInt(1);
			}
			
			s1.close();
			s2.close();
		}
		double all = vio+novio;
		vio = (vio/all)*100;
		novio = (novio/all)*100;
		
//		System.out.println("::::::::::::::::::::::::::::::: "+vio);
		
		return "{\"novio\":"+novio+", \"vio\":"+vio+"}";

	}
	
	public String distributionViolation() throws Exception {
		return byViolation(-1);
	}

	public String distributionViolation(long l) throws Exception {
		return byViolation(l);
	}
	
	public String byViolation(long considered) throws Exception{
		String res = "";
		List<String> tables = Config.getTables();
		Map<Integer, Integer> maps = new HashMap<Integer, Integer>();	
		for(String tab:tables) {
			ResultSet s1 = Config.con.createStatement().executeQuery("SELECT vioset, count(*) FROM "+tab+" GROUP BY vioset");
			while(s1.next()) {
				long vioset = s1.getLong(1);
				if (considered>=0)
					vioset = vioset & considered;
				int count = s1.getInt(2);
				int key = Long.bitCount(vioset);
				if (!maps.containsKey(key))
					maps.put(key, 0);
				maps.put(key, count+maps.get(key));
			}
			s1.close();
		}
		int all = 0;
		for(int key:maps.keySet())
			all += maps.get(key);
		List<Integer> X = new ArrayList<Integer>();
		List<Double> Y = new ArrayList<Double>();
		for(int key:maps.keySet()) {
			//res+="\""+key+"\":"+(maps.get(key)/all)+", ";
			if (key!=0) {
				X.add(key);
				Y.add(((maps.get(key)*1d)/(all*1d))*100);
			}
		}
		res = "{\"Violations\":"+X.toString()+", \"percent\":"+Y.toString()+"}";
		return res;
	}
	
	public String distributionviolationssubset() throws Exception {		
		return bySubset(-1);
	}
	
	public String distributionviolationssubset(long l) throws Exception {		
		return bySubset(l);
	}
	
	public String bySubset(long considered) throws Exception{
		double all = 0;
		List<String> tables = Config.getTables();		
		Map<Long, Double> tempRes = new HashMap<Long, Double>();
		for(String tab:tables) {
			String query = "SELECT vioset, count(*) FROM "+tab+" GROUP BY vioset";
			ResultSet r = Config.con.createStatement().executeQuery(query);
			while(r.next()) {
				long vioset = r.getLong(1);
				if (considered>=0)
					vioset = vioset & considered;
				int count = r.getInt(2);
				if (!tempRes.containsKey(vioset))
					tempRes.put(vioset, 0d);
				tempRes.put(vioset, count+tempRes.get(vioset));
				all += count;
			}
			r.close();
		}
		List<String> positions = new ArrayList<String>();
		List<Double> counts = new ArrayList<Double>();
		Map<Integer, String> maps = new HashMap<Integer, String>();
		
		String query = "SELECT * FROM c.c;";
		ResultSet r = Config.con.createStatement().executeQuery(query);
		while(r.next())
			maps.put(r.getInt("position"), r.getString("id"));	
		r.close();
		
		for(long l:tempRes.keySet()) {
			String val = "{";
			long t = l;
			int pos = 0;
			while(t!=0) {
				if (t%2==1)
					val += maps.get(pos)+", ";
				t = t/2;
				pos++;
			}
			if (val.length()>1)
				val = val.substring(0,  val.length()-2);
			val = val+"}";
			if (!val.equals("{}")) {
				positions.add("\""+val+"\"");
				counts.add((tempRes.get(l)/all)*100);
			}
		}
		return "{\"position\":"+positions.toString()+", \"count\":"+counts.toString()+"}";
	}
	  

	public String distributionviolationsbyconstraint() throws Exception {
		return byConstraints(-1);
	}
	
	public String distributionviolationsbyconstraint(long l) throws Exception {
		return byConstraints(l);
	}
	
	public String byConstraints(long considered) throws Exception{
		//String res = "";
		double all = 0;
		List<String> tables = Config.getTables();		
		Map<Integer, Double> tempRes = new HashMap<Integer, Double>();
		for(String tab:tables) {
			String query = "SELECT vioset, count(*) FROM "+tab+"  GROUP BY vioset";
			ResultSet r = Config.con.createStatement().executeQuery(query);
			while(r.next()) {
				long vioset = r.getLong(1);
				if (considered>=0)
					vioset = vioset & considered;
				List<Integer> positions = new ArrayList<Integer>();
				int i =0;
				while(vioset!=0) {
					long poss = vioset%2;
					if (poss==1)
						positions.add(i);
					vioset = vioset / 2;
					i++;
				}				
				int count = r.getInt(2);
				for(int pos:positions){
					if (!tempRes.containsKey(pos))
						tempRes.put(pos, 0d);
					tempRes.put(pos, count+tempRes.get(pos));
				}
				all += count; 
			}
			r.close();
		}
		List<String> positions = new ArrayList<String>();
		List<Double> counts = new ArrayList<Double>();
		
		String query = "SELECT * FROM c.c;";
		ResultSet r = Config.con.createStatement().executeQuery(query);
		while(r.next()) {
			int pos = r.getInt("position");
			if (tempRes.containsKey(pos)) {
				positions.add("\""+r.getString("id")+"\"");
				counts.add((tempRes.get(pos)/all)*100);
			}
		}
		r.close();
		return "{\"position\":"+positions.toString()+", \"count\":"+counts.toString()+"}";
	}
	

	public String getConstraints() throws Exception {
		String query = "SELECT * FROM c.c;";
		ResultSet r = Config.con.createStatement().executeQuery(query);
		String res = "["; 
		while(r.next())
			res+="{\"f\":\"" + r.getString("f") + "\",  \"w\":\""+ r.getString("w")+"\", \"position\":"+r.getInt("position")+", \"id\":\""+r.getString("id")+"\", \"description\":\""+r.getString("description")+"\"}, ";
		if (res.length()>1)
			res = res.substring(0, res.length()-2);
		r.close(); 
		return res+"]";  
	} 

	public String explorationByConstraintsHelp(String from, String where, String limit, Set<String> select, Set<String> group) throws Exception {
		JSONObject result = new JSONObject();
		
		Set<String> otherGroup = giveOtherAttributs(group, where);
		
		String selectToString = "";//select.toString().replace("[", "").replace("]", "");
		String groupToString = "";//group.toString().replace("[", "").replace("]", "")+((!otherGroup.isEmpty())?","+otherGroup.toString().replace("[", "").replace("]", ""):"");		
		
		String selectToString_ = select.toString().replace("[", "").replace("]", "");
		String groupToString_ = group.toString().replace("[", "").replace("]", "")+((!otherGroup.isEmpty())?","+otherGroup.toString().replace("[", "").replace("]", ""):"");		
		
		
		for(String s:select) 
			selectToString += s+" AS "+s.replace(".", "_")+", ";
		for(String g:group) 
			groupToString += g+" AS "+g.replace(".", "_")+", ";
		for(String o:otherGroup) 
			groupToString += o+" AS "+o.replace(".", "_")+", ";
		
		selectToString = selectToString.substring(0, selectToString.length()-2);
		groupToString = groupToString.substring(0, groupToString.length()-2);
		
		String sql = "SELECT "+selectToString+","+groupToString+", Count(*)  FROM "+from+((!where.equals(""))?(" WHERE " + where.replace(",", " AND ")):"") +" GROUP BY "+selectToString_+","+groupToString_ +  ((!limit.equals(""))?" LIMIT "+limit :"");          
	
		System.out.println(sql);
		
		//System.exit(0);
		
		ResultSet resultSet = Config.con.createStatement().executeQuery(sql);
		Map<String, Map<String, Integer>> firstResult = new HashMap<String, Map<String,Integer>>();
		
		int last = resultSet.getMetaData().getColumnCount();
	
		while(resultSet.next()) {
			String selectValue = "(";
			String groupValue = "(";
			String otherGroupValue = "(";
			for(String s:select) {
				selectValue += s+":"+resultSet.getString(s.replace(".", "_"))+", ";
			}
			for(String g:group) {
				groupValue += g+":"+resultSet.getString(g.replace(".", "_"))+", ";
			}
			for(String o:otherGroup) {
				otherGroupValue += o+":"+resultSet.getString(o.replace(".", "_"))+", ";
			}
			selectValue = selectValue.substring(0, selectValue.length()-2)+")";
			groupValue = groupValue.substring(0, groupValue.length()-2)+")";
			otherGroupValue = otherGroupValue.substring(0, otherGroupValue.length()-2)+")";
			
			if (!firstResult.containsKey(selectValue))
				firstResult.put(selectValue, new HashMap<String, Integer>());
			Map<String, Integer> map = firstResult.get(selectValue);
			if (!map.containsKey(groupValue))
				map.put(groupValue, 0);
			map.put(groupValue, map.get(groupValue)+resultSet.getInt(last));
			if (!map.containsKey(otherGroupValue))
				map.put(otherGroupValue, 0);
			map.put(otherGroupValue, map.get(otherGroupValue)+resultSet.getInt(last));	
		}
		resultSet.close();
		
		start();
		JSONArray labels = new JSONArray();
		JSONArray datasets = new JSONArray();
		int count = 0;
		for(String xValue: firstResult.keySet()) {
			labels.put(xValue);
			count++;
			Map<String, Integer> localMap = firstResult.get(xValue);
			for(String yValue:localMap.keySet()) {

				//List<Integer> data = getZero(count);
				//data.set(data.size()-1, localMap.get(yValue));
				
				JSONArray tempData = new JSONArray();
				for(int i=0;i<count-1; i++)
					tempData.put(0);
				tempData.put(localMap.get(yValue));
				 
				JSONObject tempObj = new JSONObject();
				tempObj.put("label", yValue);
				tempObj.put("data", tempData);
				tempObj.put("backgroundColor", getNextColor());
				
				datasets.put(tempObj);		
			} 
		}    
		result.put("labels", labels);
		result.put("datasets", datasets);
		
		return result.toString();
	}
	
	private Set<String> giveOtherAttributs(Set<String> group, String where) {
		Set<String> res = new HashSet<String>();
		String [] temp = where.split("( )*,( )*");
		for(String str:temp) {
			String t[] = str.split(">=|<=|<>|>|<");
			if (t.length>1) {
				if (group.contains(t[0]))
					res.add(t[1]);
				if (group.contains(t[1]))
					res.add(t[0]);
			}
		}
		return res;
	}

	public String explorationByConstraints(String param) throws Exception {
		JSONObject jo =  new JSONObject(param);
		String from = jo.getString("from");
		String where = jo.getString("where");
		String limit = jo.getString("limit");
		
		where = where.replace(" ", "");
		
		JSONArray group = jo.getJSONArray("group");
		JSONArray select = jo.getJSONArray("select");
		
		Set<String> groups = new HashSet<String>();
		Set<String> selects = new HashSet<String>();
		
		for(int i=0; i<group.length(); i++)
			groups.add(group.getString(i));
		for(int i=0; i<select.length(); i++)
			selects.add(select.getString(i));
		
		try {
			limit = limit.replace(" ", "");
			Integer.parseInt(limit);
		}catch(Exception e) {limit="";}
		
		String res = explorationByConstraintsHelp(from, where, limit, selects, groups);
		
		System.out.println(res);
		
		return res;
	}

//	private List<Integer> getZero(int n) {
//		List<Integer> getZero = new ArrayList<Integer>();
//		for(int i=0; i<n;i++)
//			getZero.add(0);
//		return getZero;
//	}
	
	class Color {
		int red, green, blue;

		int n;
		
		public Color(int red, int green, int blue) {
			super();
			this.red = red;
			this.green = green;
			this.blue = blue;
		}
	
		
		
		public String toString(){
			return "\"rgb("+red+", "+green+", "+blue+")\"";
		}
		
		public String next(int p) {
			String current = toString();
			if (n == 0)
				red = ((red+p)%255);
			if (n == 1)
				green = ((green+p)%255);
			if (n == 0)
				blue = ((blue+p)%255);
			n = ( n + 1 )%3;
			return current;
		}
		
	}

	public String getConstraintsCorrelation(String param) throws Exception {
		JSONObject jo =  new JSONObject(param);
		
		String pos = jo.getString("Constraint_Position");
		int position = -1;
		
		String query = "SELECT * FROM c.c;";
		ResultSet r = Config.con.createStatement().executeQuery(query);
		Map<Integer, String> cst = new HashMap<Integer, String>();
		while(r.next()) {
			cst.put(r.getInt("position"), r.getString("id"));
			if (pos.equalsIgnoreCase(r.getString("id")))
				position = r.getInt("position");
		}
		r.close();

		long ll =puissance(2, position);

		int count = 0;
		Map<Integer, Integer> reps = new HashMap<Integer, Integer>();
		List<String> tables = Config.getTables();
		for(String tab:tables) {
			ResultSet s1 = Config.con.createStatement().executeQuery("SELECT vioset FROM "+tab);
			while(s1.next()) {
				if ((s1.getLong(1)&ll) != 0) {
					count++;
					long vioset = s1.getLong(1);
					int i =0;
					while(vioset!=0) {
						long poss = vioset%2;
						if (poss==1) {
							
							if (position!=i){
								if (!reps.containsKey(i))
									reps.put(i, 0);
								reps.put(i, reps.get(i)+1);
							}
						}
						vioset = vioset / 2;
						i++;
					}
				}
			}
			s1.close();
		}

		List<String> ids = new ArrayList<String>();
		List<Double> cs = new ArrayList<Double>();
		for(Integer i:reps.keySet()) {
			ids.add("\""+cst.get(i)+"\"");
			cs.add(((reps.get(i)*1d)/(count*1d))*100);
		}		
		return "{\"id\":"+ids.toString()+", \"count\":"+cs.toString()+"}";
	} 
 
	private long puissance(int i, int position) {
		long res = 1;
		for(int j=0; j<position; j++)
			res *= i;      
		return res;                     
	}

	public String gettupleProportion(String param) throws Exception {
		JSONObject jo =  new JSONObject(param);
		long pos = jo.getLong("constraints");
		List<String> tables = Config.getTables();
		JSONArray viosets = new JSONArray();
		JSONArray ids = new JSONArray();
		JSONArray violations = new JSONArray();
		
		for(String tab:tables) {
			String query = "SELECT id, vioset & "+pos+", violation  FROM "+tab+" WHERE vioset & "+pos+"<> 0";
			ResultSet s1 = Config.con.createStatement().executeQuery(query);
			while(s1.next()) {
				ids.put(s1.getString(1));
				viosets.put(Long.bitCount(s1.getLong(2)));
				violations.put(s1.getInt(3));
			}
			s1.close();
		}
		JSONObject res = new JSONObject();
		res.put("X", ids);
		res.put("Y2", violations);
		res.put("Y", viosets);
		return res.toString();
	}    
   
	public String gettupleViolations(String param) throws Exception {
		JSONObject jo =  new JSONObject(param);
		long pos = jo.getLong("constraints"); 
		String rel = "SELECT tups, constr FROM vio.violations WHERE pos & "+pos+"<>0";
		
		
		List<String> res = new ArrayList<String>();
		
		ResultSet rs = Config.con.createStatement().executeQuery(rel);
		while(rs.next()) {
			String tups = rs.getString("tups");
			String c = rs.getString("constr");
			res.add("{\"tups\":\""+tups+"\", \"cons\":\""+c+"\"}");
		} 
		rs.close();   
		return res.toString();    
	}

	int limit = 50;
	
	String [] colorsQuery = {"blue", "green", "maroon", "black"}; 
	
	public String helpQueryExecution(String param) throws Exception {
		
		JSONObject jo =  new JSONObject(param);
		String query = jo.getString("query");
		String operator = jo.getString("operator");
		JSONArray measures = jo.getJSONArray("measure");	
		
		int filter = 0;
		try {
			filter = jo.getInt("filterValue");
		}catch(Exception e) {}
		long cstrs = jo.getLong("selectedConstraints");
	
		Map<String, String> newQueries = getQueries(query, operator, measures, filter, cstrs);

		Map<String, Map<Integer, Double>> resViolations = new HashMap<String, Map<Integer, Double>>();
		Map<String, Map<Long, Double>> resSubVio = new HashMap<String, Map<Long, Double>>();
		
		Map<String, Double> alls = new HashMap<String, Double>();

		JSONArray datas = new JSONArray();
		
		Map<Integer, String> positions = getConstraintPos();
		
		for(String measure:newQueries.keySet()) {
			ResultSet rs = Config.con.createStatement().executeQuery(newQueries.get(measure));
			int j = 0;
	  
			resViolations.put(measure, new HashMap<>());
			resSubVio.put(measure, new HashMap<>());
			alls.put(measure, 0d);
			
			JSONObject ds = new JSONObject();
			JSONArray attributes = new JSONArray();
			JSONArray data = new JSONArray();
			
			for(int i =1; i<rs.getMetaData().getColumnCount()-1; i++)
				attributes.put(rs.getMetaData().getColumnLabel(i));
			attributes.put(measure);
			attributes.put("Prov");    
			ds.put("attrs", attributes);
			
			while(rs.next()) {
				if ((operator.equalsIgnoreCase("all") && j<=limit)||(!operator.equalsIgnoreCase("all"))) {
					//List<String> line = new ArrayList<String>();
					JSONArray line = new JSONArray();
					for(int i =1; i<rs.getMetaData().getColumnCount(); i++)
							line.put(rs.getString(i));
					line.put(getSet(rs.getLong(rs.getMetaData().getColumnCount()), positions));
					data.put(line);
					j++;
				}
  
				int vio = rs.getInt(rs.getMetaData().getColumnCount()-1);
				long sub = rs.getLong(rs.getMetaData().getColumnCount());
				
				if (!resViolations.get(measure).containsKey(vio))
					resViolations.get(measure).put(vio, 0d);
				if (!resSubVio.get(measure).containsKey(sub))
					resSubVio.get(measure).put(sub, 0d);
				
				resViolations.get(measure).put(vio, resViolations.get(measure).get(vio)+1);
				resSubVio.get(measure).put(sub, resSubVio.get(measure).get(sub)+1);
				alls.put(measure, alls.get(measure)+1);
			}
			rs.close();
			ds.put("data", data);
			datas.put(ds);
		}

		JSONObject sub_vio = new JSONObject();
		JSONObject vio_dist = new JSONObject();
		
		JSONObject options = new JSONObject();
		JSONObject scales = new JSONObject();
		JSONArray yaxes = new JSONArray();
		JSONObject ticks = new JSONObject();
		ticks.put("beginAtZero", true);
		ticks.put("fontColor", "black");
		ticks.put("fontStyle", "bold");
		JSONObject ticks__ = new JSONObject();
		ticks__.put("ticks", ticks);
		yaxes.put(ticks__);
		
		JSONArray xaxes = new JSONArray();
		JSONObject ticks_ = new JSONObject();
		ticks_.put("fontColor", "black");
		ticks_.put("fontStyle", "bold");
		JSONObject ticks_1 = new JSONObject();
		ticks_1.put("ticks", ticks_);
		xaxes.put(ticks_1);
		scales.put("yAxes", yaxes);
		scales.put("xAxes", xaxes);
		options.put("scales", scales);
		
		sub_vio.put("type", "bar");
		vio_dist.put("type", "bar");
		sub_vio.put("options", options);
		vio_dist.put("options", options);
		
		/*
		 data: {
		labels: ['a', 'b', 'c', 'd', 'd'],
		datasets: [{
			label: 'D2',
			data: [1,4,3, 0],
			backgroundColor: 'red'
		}, 
		{
			label: 'D1',
			data: [1,4,3, 4, 5],
			backgroundColor: 'blue'
		}]
		}
		 */
		
		Set<Long> subX = new HashSet<Long>();
		Set<Integer> vioX = new HashSet<Integer>();
		
		for(String key:resSubVio.keySet()) { 
			subX.addAll(resSubVio.get(key).keySet());
			vioX.addAll(resViolations.get(key).keySet());
		}
		
		JSONObject vioData = new JSONObject();
		JSONObject subData = new JSONObject();
		
		JSONArray vioDatasets = new JSONArray();
		JSONArray subDatasets = new JSONArray();
		int color = 0;
		for(String key:resViolations.keySet()) {
			
			double all = alls.get(key);
			
			JSONObject item = new JSONObject();
			item.put("label", key);
			item.put("backgroundColor", colorsQuery[color]);
			item.put("data", getData_vio(resViolations.get(key), vioX, all));
			vioDatasets.put(item);
			
			JSONObject item1 = new JSONObject();
			item1.put("label", key);
			item1.put("backgroundColor", colorsQuery[color]);
			item1.put("data", getData_sub(resSubVio.get(key), subX, all));
			subDatasets.put(item1);
			color = (color+1)%4;
		}
		
		vioData.put("datasets", vioDatasets);
		subData.put("datasets", subDatasets);

		JSONArray vioLabels = new JSONArray();
		JSONArray subLabels = new JSONArray();
		
		for(Integer x:vioX)
			vioLabels.put(x);
		
		for(Long x:subX) 
			subLabels.put(getSet(x, positions));		
		vioData.put("labels", vioLabels);
		subData.put("labels", subLabels);
		
		JSONObject result = new JSONObject();
		result.put("data", datas);
		sub_vio.put("data", subData);
		vio_dist.put("data", vioData);
		result.put("sub_vio", sub_vio);
		result.put("vio_dist", vio_dist);
		
		return result.toString();
	}
	
	private JSONArray getData_sub(Map<Long, Double> map, Set<Long> subX, double all) {
		JSONArray getData_vio = new JSONArray();
		for(Long i:subX) {
			if (map.containsKey(i))
				getData_vio.put((map.get(i)/all)*100);
			else
				getData_vio.put(0);
		}
		return getData_vio;
	}

	private JSONArray getData_vio(Map<Integer, Double> map, Set<Integer> vioX, double all) {
		JSONArray getData_vio = new JSONArray();
		for(Integer i:vioX) {
			if (map.containsKey(i))
				getData_vio.put((map.get(i)/all)*100);
			else
				getData_vio.put(0);
		}
		return getData_vio;
	}

	@Override
	public String getQueryExecution(String param) throws Exception {

		String res = helpQueryExecution(param);
		//System.out.println(res);
		return res;
		     
		/*
		JSONObject jo =  new JSONObject(param);
		String query = jo.getString("query");
		String operator = jo.getString("operator");
		String measure = jo.getString("measure");
		
		int filter = 0;
		try {
			filter = jo.getInt("filterValue");
		}catch(Exception e) {}
		long cstrs = jo.getLong("selectedConstraints");
	
		String newQuery = getQuery(query, operator, measure, filter, cstrs);

		System.out.println(newQuery);
		
		String dataShown = "{\"attrs\": ";//"data"
		          
		ResultSet rs = Config.con.createStatement().executeQuery(newQuery);
		List<String> columns = new ArrayList<String>();
		for(int i =1; i<rs.getMetaData().getColumnCount()-1; i++)
			columns.add("\""+rs.getMetaData().getColumnLabel(i)+"\"");
		columns.add("\""+measure+"\"");
		dataShown+=columns.toString()+", \"data\":";
		int j = 0;
		List<String> data = new ArrayList<String>();
		
		Map<Integer, Integer> vio_dist = new HashMap<Integer, Integer>();
		Map<Long, Integer> vio_sub = new HashMap<Long, Integer>();
		    
		int all = 0;   
		
		while(rs.next()) {
			if ((operator.equalsIgnoreCase("all") && j<=limit)||(!operator.equalsIgnoreCase("all"))) {
				List<String> line = new ArrayList<String>();
				for(int i =1; i<rs.getMetaData().getColumnCount(); i++)
						line.add("\""+rs.getString(i)+"\"");
				data.add(line.toString());
				j++;
			}
				
			int vio = rs.getInt(rs.getMetaData().getColumnCount()-1);
			long sub = rs.getLong(rs.getMetaData().getColumnCount());
			
			if (!vio_dist.containsKey(vio))
				vio_dist.put(vio, 0);
			if (!vio_sub.containsKey(sub))
				vio_sub.put(sub, 0);
			
			vio_dist.put(vio, vio_dist.get(vio)+1);
			vio_sub.put(sub, vio_sub.get(sub)+1);
			
			all++;
		}
		rs.close();
		
		
		
		Map<Integer, String> constraints = getConstraintPos();
		
		List<String> XvioDist = new ArrayList<String>();
		List<String> XvioSub = new ArrayList<String>();
		
		List<Double> YvioDist = new ArrayList<Double>();
		List<Double> YvioSub = new ArrayList<Double>();
		
		for(Integer key:vio_dist.keySet()) {
			XvioDist.add("\""+key+"\"");
			double e = ((vio_dist.get(key)*1d)/(all*1d))*100;
			YvioDist.add(e);
		}
		
		for(Long key:vio_sub.keySet()) {
			XvioSub.add("\""+getSet(key, constraints)+"\"");
			double e = ((vio_sub.get(key)*1d)/(all*1d))*100;
			YvioSub.add(e);
		}
		
		String sub_vio = "{\"X\":"+XvioSub.toString()+", \"Y\":"+YvioSub.toString()+"}";
		
		String vio_dist_ = "{\"X\":"+XvioDist.toString()+", \"Y\":"+YvioDist.toString()+"}";
		dataShown += data.toString()+"}";
		
		return "{\"data\":"+dataShown+", \"sub_vio\":"+sub_vio+", \"vio_dist\":"+vio_dist_+"}"; 
		*/
		
	}
	

	private String getSet(Long key, Map<Integer, String> constraints) {
		Set<String> set = new HashSet<String>();
		for(Integer p:constraints.keySet()) {
			if (((key>>p)&1)!=0)
				set.add(constraints.get(p));
		}
		return set.toString().replace("[", "{").replace("]", "}");
	}

	private Map<Integer, String> getConstraintPos() throws Exception{
		String query = "SELECT * FROM c.c;";
		ResultSet r = Config.con.createStatement().executeQuery(query);
		Map<Integer, String> cst = new HashMap<Integer, String>();
		while(r.next())
			cst.put(r.getInt("position"), r.getString("id"));
		r.close();
		return cst;
	}

	/*
	 CREATE OR REPLACE FUNCTION bit_count(value bigint) 
	RETURNS numeric AS $$
	DECLARE res  integer := 0;
	i  integer := 0;
	BEGIN 
		i:= 0;
		res = 0;
		While (power(2, i)<=value) loop
			res := res + ((value>>i)&1);
			i := i + 1 ;
		end loop;
		return res; 
	END;
	$$
	LANGUAGE plpgsql IMMUTABLE STRICT;
	 */
	

	
	//for CBM and CBS
	private Map<String, String> getQueries(String query, String op, JSONArray measures, int filter, long cstrs) {
		Map<String, String> results = new HashMap<>();
		for(int i = 0; i<measures.length(); i++) 
			results.put(measures.getString(i), getQuery(query, op, measures.getString(i), filter, cstrs));
		return results;
	}
	
	//for CBM and CBS
	private String getQuery(String query, String op, String measure, int filter, long cstrs) {
		
		String q[] = query.split("( )+");
		query = "";
		for(String s:q) {
			if (s.equalsIgnoreCase("from")) 
				query += " from"; 
			else {
				if (s.equalsIgnoreCase("select"))
					query += " select";
				else {
					if (s.equalsIgnoreCase("where"))
						query += " where";
					else
						query += " "+s;
				}
			}
		}
		
		String tempQuery1[] = query.replace("select", "").split("from");
		
		String select = "SELECT "+tempQuery1[0];
		String from = " FROM "+tempQuery1[1].replace("where", " WHERE ");
		
		//Map<String, String> tabs = new HashMap<String, String>();
		String tab = tempQuery1[1];
		if (tempQuery1[1].contains("where")) {
			tab = tempQuery1[1].split("where")[0];
		}
		
		//select a, min(v1 & v2), v1 & v2 from R1
		
		String tempQuery2[] = tab.split("( )*,( )*");
		
		String adSelect="";
		String adSelect1="";
		   
		for(String relation:tempQuery2) {
			String t1 [] = relation.split("( )+");
			String rel = t1[0];
			if (rel.isEmpty())
				rel = t1[1];
			try {
				rel = t1[2];
			}catch(Exception e) {}
			adSelect1 += rel+".vioset & ";
			if (measure.equalsIgnoreCase("CBS"))
				adSelect += rel+".vioset & ";
			if (measure.equalsIgnoreCase("CBM"))
				adSelect += "bit_count("+rel+".vioset & " + cstrs + ") + ";
		}
		
		adSelect1 = adSelect1+cstrs;//.substring(0, adSelect1.length()-2);
		adSelect  = adSelect.substring(0, adSelect.length()-2);
		
		if (measure.equalsIgnoreCase("CBS"))
			adSelect = "bit_count("+adSelect+" & "+ cstrs +")";
		
		adSelect = "("+adSelect + ") AS vio";
		adSelect1 = "("+adSelect1 + ") AS vioset";
		
		String res = select + ", "+adSelect+", "+adSelect1+" "+from;
		
		if (op.equalsIgnoreCase("top-k")) {
			if (filter>0)
				res += " ORDER BY vio LIMIT "+(filter);
			else
				res += " ORDER BY vio DESC LIMIT "+(filter*(-1));  
		}
		if (!op.equalsIgnoreCase("all") && !op.equalsIgnoreCase("top-k")) {
			if (tempQuery1[1].contains("where"))
				res += " AND vio "+ op +" "+filter;
			else
				res += " WHERE vio "+ op +" "+filter;
		}		
		return res;
	}
	
	private String withA1(String rel, String a1) throws Exception{
		Map<String, Double> res_1 = new HashMap<String, Double>();
		Map<String, Double> res_2 = new HashMap<String, Double>();
		Map<String, Double> res = new HashMap<String, Double>();
		String req1 = "SELECT "+a1+", count(*) FROM "+rel+" WHERE vioset <> 0 GROUP BY "+a1;
		String req2 = "SELECT "+a1+", count(*) FROM "+rel+" WHERE vioset = 0 GROUP BY "+a1;
		ResultSet res1 = Config.con.createStatement().executeQuery(req1);
		while(res1.next())
			res_1.put(res1.getString(1), res1.getDouble(2));
		res1.close();
		ResultSet res2 = Config.con.createStatement().executeQuery(req2);
		while(res2.next()) 
			res_2.put(res2.getString(1), res2.getDouble(2));
		res2.close();
		Set<String> keys = new HashSet<String>();
		keys.addAll(res_1.keySet());
		keys.addAll(res_2.keySet());
		for(String key:keys) {
			if (!res_1.containsKey(key)) {
				res.put(key, 0d);
				continue;
			}
			if (!res_2.containsKey(key)) {
				res.put(key, 100d);
				continue;
			}
			res.put(key, (res_1.get(key)/(res_2.get(key)+res_1.get(key)))*100d);
		}
		List<String> X = new ArrayList<String>();
		List<Double> Y = new ArrayList<Double>();
		
		for(String key:res.keySet()) {
			X.add(key);
			Y.add(res.get(key));
		}
		JSONArray data = new JSONArray();
		
		JSONArray borderColor = new JSONArray();
		for(int i=0; i<Y.size(); i++) {
			for(int j=i; j<Y.size(); j++) {
				if (Y.get(i)<Y.get(j)) {
					String temp1 = X.get(i);
					double temp2 = Y.get(i);
					X.set(i, X.get(j));
					Y.set(i, Y.get(j));
					X.set(j, temp1);
					Y.set(j, temp2);
				}
			}
		}
		JSONArray datasets = new JSONArray();
		JSONObject e = new JSONObject();
		
		e.put("backgroundColor", "blue");
		e.put("pointRadius", 5);
		e.put("pointStyle", "circle");
		
		for(int i=0; i<Y.size(); i++) {
			JSONObject te = new JSONObject();
			te.put("x", X.get(i));
			te.put("y", Y.get(i));
			data.put(te);
			borderColor.put(getNextRed());
		}

		e.put("data", data);
		e.put("borderColor", borderColor);
		datasets.put(e);
		
		JSONObject legend = new JSONObject();
		legend.put("display", false);
		JSONObject options = new JSONObject();
		options.put("legend", legend);
		
		JSONObject datas = new JSONObject();
		datas.put("datasets", datasets);

		JSONObject r = new JSONObject();
		r.put("type", "scatter");
		r.put("data", datas);
		r.put("options", options);
		
		return r.toString();
	}

	Color es = new Color(0, 255, 0);
	
	private String getNextRed(){
		String res = es.toString();
		es.red = (es.red<255)?es.red+10:255;
		es.green = (es.green>=10)?es.green-10:0;
		return res;
	}
	
	private String withA1AndA2(String rel, String a1, String a2) throws Exception{
		
		String req1 = "SELECT "+a1+","+a2+", count(*) FROM "+rel+" WHERE vioset <> 0 GROUP BY "+a1+","+a2;
		String req2 = "SELECT "+a1+","+a2+", count(*) FROM "+rel+" WHERE vioset = 0 GROUP BY "+a1+","+a2;

		Map<String, JSONArray> results = new HashMap<String, JSONArray>();
		
		Map<String, Integer> currentsSize1 = new HashMap<String, Integer>();
		Map<String, Integer> currentsSize2 = new HashMap<String, Integer>();
		Map<String, String> temps = new HashMap<String, String>();
		
		ResultSet res1 = Config.con.createStatement().executeQuery(req1);
		
		int pos = 1;
		Map<String, Integer> poss = new HashMap<String, Integer>();
		
		Set<String> xl = new  HashSet<String>();
		
		while(res1.next()) {
			String v = (res1.getString(2)!=null)?res1.getString(2):"NaN";
			String key = "{\"x\":\""+res1.getString(1)+"\", \"y\":\""+v+"\"}";
			temps.put(key, a1+":"+res1.getString(1)+",\n"+a2+":"+v);
			currentsSize1.put(key, res1.getInt(3));
			
			if (!poss.containsKey(v)) {
				poss.put(v,pos);
				pos++;
			}
			xl.add(res1.getString(1));
		}
		res1.close();
		
		ResultSet res2 = Config.con.createStatement().executeQuery(req2);
		while(res2.next()) { 
			String v = (res2.getString(2)!=null)?res2.getString(2):"NaN";
			String key = "{\"x\":\""+res2.getString(1)+"\", \"y\":\""+v+"\"}";
			temps.put(key, a1+":"+res2.getString(1)+",\n"+a2+":"+v);
			currentsSize2.put(key, res2.getInt(3));		
			if (!poss.containsKey(v)) {
				poss.put(v,pos);
				pos++;
			}
			xl.add(res2.getString(1));
		} 
		res2.close();
		
		Set<String> keys = new HashSet<String>();
		keys.addAll(currentsSize1.keySet());
		keys.addAll(currentsSize2.keySet());
		
		Map<String, String> resTemp = new HashMap<String, String>(); 
		
		for(String key:keys) {
			if (!currentsSize1.containsKey(key)) {
				resTemp.put(key, "0");
				temps.put(key, temps.get(key)+" (I=0, C="+currentsSize2.get(key)+")");
				continue;
			}
			if (!currentsSize2.containsKey(key)) {
				resTemp.put(key, "1");
				temps.put(key, temps.get(key)+" (I="+currentsSize1.get(key)+", C=0)");
				continue;
			}
			resTemp.put(key, reduceFraction(currentsSize1.get(key), (currentsSize2.get(key)+currentsSize1.get(key))));
			temps.put(key, temps.get(key)+" (I="+currentsSize1.get(key)+", C="+currentsSize2.get(key)+")");
		}
		
		Map<String, JSONObject> labels = new HashMap<String, JSONObject>();
		
		for(String k:resTemp.keySet()) {
			String val = resTemp.get(k);
			if (!results.containsKey(val)) {
				results.put(val, new JSONArray());
				labels.put(val, new JSONObject());
			}
			JSONObject obj = new JSONObject(k);
			String resT = obj.getString("y");
			
			obj.put("y", poss.get(resT));
			results.get(val).put(obj);
			 
			String cle = obj.getString("x")+"_"+obj.getString("y");			
			labels.get(val).put(cle, temps.get(k));
		}
		
		Map<Integer, String> poss2 = new HashMap<Integer, String>();
		for(String k:poss.keySet()) 
			poss2.put(poss.get(k), k);
		
		JSONArray js = new JSONArray();
		for(int i=1; i<= poss.size(); i++)
			js.put(poss2.get(i));
		
		JSONObject r = new JSONObject();
		r.put("js", js);
		r.put("xlength", xl.size());
		JSONObject options = new JSONObject();
		JSONObject legend = new JSONObject();
		legend.put("display", true);
		legend.put("position", "right");
		options.put("legend", legend);
		options.put("tooltips", "opts2");
		      
		r.put("type", "scatter");
		r.put("options", options);
		JSONObject data = new JSONObject();
		JSONArray datasets = new JSONArray();
		Color xc = new Color(50, 0, 0);
		xc.n = 0;		 
		start(); 
		for(String key:labels.keySet()) {
			JSONObject rr = new JSONObject();
			rr.put("label", key); 
			rr.put("pointRadius", getRaduis(key));
			rr.put("pointStyle", "circle");
			rr.put("borderColor", getNextColor());
			rr.put("data", results.get(key));
			rr.put("l", labels.get(key));
			datasets.put(rr);
		}		 
		data.put("datasets", datasets); 
		r.put("data", data);		 
		return r.toString();
	}

	
	String tab[] = {"aqua", "black", "blue", "fuchsia", "gray", "green", "lime", "maroon", "navy", "olive", "purple", "red", "silver", "teal", "white", "yellow"};
	int index = 0;
	private void start() {
		index = 0;
	}
	private String getNextColor() {
		String val = tab[index];
		index = (index+1)%tab.length; 
		return val;
	}
	
	private String getRaduis(String key) {
		long res = 0;
		int portion = 0;
		int total = 0;
		
		key = key.replace("(", "").replace(")", "").replace(" ", "");
		
		if (key.contains("/")) {
			portion = Integer.parseInt(key.substring(0, key.indexOf('/')));
			total = Integer.parseInt(key.substring(key.indexOf('/')+1));
		}else {
			if (key.replace(" ", "").equals("1"))
				portion = 1;
			total = 1;
		}
		if (portion == 0)
			res = 5;
		else 
			if (portion == total)
				res = 20;
		else
			res = Math.round(1d+((portion*1d)/(total*1d)))*5;
		return res+"";
	}

	private String reduceFraction(int first, int total) {
		if ((total%first)==0)
			return "(1/"+(total/first)+")";
		int f = first, t = total;
		for(int i=2; i<=f; i++) {
			while((f%i==0)&&(t%i==0)) {
				f = f/i;
				t = t/i;
			}
		}
		return "("+f+"/"+t+")";
	}
 
	@Override
	public String byTable(String param) throws Exception {
		JSONObject obj = new JSONObject(param);
		if (obj.has("a1") && obj.has("a2")) {
			System.out.println("ici 1");
			return withA1AndA2(obj.getString("relation"), obj.getString("a1"), obj.getString("a2"));
		} 
		  
		System.out.println("ici 2");
		
		if (obj.has("a1")) {
			return withA1(obj.getString("relation"), obj.getString("a1"));
		}

		if (obj.has("a2")) {
			return withA1(obj.getString("relation"), obj.getString("a2"));
		}
		return "{}"; 
	}    
              
	@Override 
	public String profilingAttributs(String param) throws Exception {
		JSONObject obj = new JSONObject(param);
		String req = "SELECT * FROM "+obj.getString("relation")+" LIMIT 0";

		ResultSet res = Config.con.createStatement().executeQuery(req);
		Set<String> attrs = new HashSet<String>();
		for(int i =1; i<=res.getMetaData().getColumnCount(); i++)
			if (!res.getMetaData().getColumnName(i).equalsIgnoreCase("vioset"))
				attrs.add("\""+res.getMetaData().getColumnName(i)+"\"");
		return attrs.toString();
	}    
               
	@Override
	public String getConstraintDetail(String param) throws Exception {
		JSONObject jo = new JSONObject(param);
		String req = "SELECT f, w FROM c.c WHERE id='"+jo.getString("id")+"'";
		ResultSet res = Config.con.createStatement().executeQuery(req);
		res.next();
		
		JSONObject jso = new JSONObject();
		jso.put("f", res.getString("f"));
		jso.put("w", res.getString("w"));
		res.close();

		return jso.toString();
	}

	
	
	
	
	/*
	 //		List<String> counts = new ArrayList<String>();
		
		for(int i=0; i<group.length(); i++)
			groups.add(group.getString(i));
		for(int i=0; i<select.length(); i++)
			selects.add(select.getString(i));
//		for(int i=0; i<count.length(); i++)
//			counts.add(count.getString(i));
		
		String query = "SELECT DISTINCT "+selects.toString().replace("[", "").replace("]", "").replace("\"", "")+" FROM "+from+" WHERE "+where.replace(",", " AND ");
		try {
			int sparse = Integer.parseInt(limit.replace(" ", ""));
			if (sparse>0)
				query += " LIMIT "+limit;
		}catch(Exception e) {
			//e.printStackTrace();
		}

//		System.out.println(query);
		
		ResultSet rs = Config.con.createStatement().executeQuery(query);
		       
		//List<String> labels = new ArrayList<String>();
		//List<String> datasets = new ArrayList<String>();
		
		Color col = new Color(40, 100, 0);
		
		String wherez[] = where.split("( )*,( )*");
		Set<String> sets = new HashSet<String>();
		for(String str:wherez)
			if (!(str.contains("<=")||str.contains(">=")||str.contains("<>")))
				sets.add(str);
		
		System.out.println(":: "+where);
		
		where = sets.toString();
		where = where.substring(1);
		where = where.substring(0, where.length()-1);
		
		System.out.println("::- "+where);
		
		while(rs.next()) {
			String tup = "";
			String reqwhere = "";
			for(String s:selects) {
				String tempS = s;
				if (s.split("\\.").length>=2)
					tempS = s.split("\\.")[1];
				
				tup += tup+s+"="+rs.getString(tempS)+", ";
				reqwhere += s+"='"+rs.getString(tempS)+"' AND ";
			}
			tup = "<"+tup.substring(0, tup.length()-2)+">";
			labels.add("\""+tup+"\"");
			reqwhere = reqwhere.substring(0, reqwhere.length()-5);
			String gs = groups.toString().replace("[", "").toString().replace("]", "").toString().replace("\"", "");
			//String cs = counts.toString().replace("[", "").toString().replace("]", "").toString().replace("\"", "");
//			String req = "SELECT "+gs+", COUNT(DISTINCT("+cs+")) as count__ FROM "+from+" WHERE "+ where.replace(",", " AND ")+" AND " + reqwhere+ " GROUP BY "+gs;
			String req = "SELECT "+gs+", COUNT(*) as count__ FROM "+from+" WHERE "+ where.replace(",", " AND ")+" AND " + reqwhere+ " GROUP BY "+gs;
			
			ResultSet rse = Config.con.createStatement().executeQuery(req);
			
//			for(int ii =1 ; ii <= rse.getMetaData().getColumnCount(); ii++) {
//				System.out.print(rse.getMetaData().getColumnLabel(ii)+", ");
//			}   
//			    
//			System.out.println(groups); 
			  
			while(rse.next()) {
				String label = "";
				for(int ii =1 ; ii < rse.getMetaData().getColumnCount(); ii++)
					label += label+groups.get(ii-1)+":"+rse.getString(ii)+", ";
				label = "\""+label.substring(0, label.length()-1)+"\"";
				List<Integer> data = getZero(labels.size());
				data.set(data.size()-1, relSize*rse.getInt("count__"));
				datasets.add("{\"label\":"+label+", \"data\":"+data.toString()+", \"backgroundColor\":\""+getNextColor(col)+"\"}");
			}			
			rse.close(); 
		}   
	 	     
//		System.out.println("end ...");
		 
		rs.close();
		 
		res = "{\"labels\":"+labels.toString()+", \"datasets\":"+datasets.toString()+"}";
		
		return res; 
	 
	 */
	
	
	
	
}   
