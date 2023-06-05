package com.limos.fr.queries;

import java.sql.ResultSet;
import java.sql.SQLException;
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
import com.limos.fr.queries.topk.ne.api.topk.Parameters;

@Component
public class QueryImp implements QueryService{

	@Override
	public String runQuery(String jsonParam) throws Exception {
		String result = runQueryHelp(jsonParam);
		return result;
	}

	private String runQueryHelp(String jsonParam) throws Exception {
		JSONObject jo =  new JSONObject(jsonParam);
		String query = jo.getString("query");
		String operator = jo.getString("operator");
		JSONArray measures = jo.getJSONArray("measure");	
		int filter = 0;
		
		try {
			filter = jo.getInt("filterValue");
		}catch(Exception e) {}
		long cstrs = jo.getLong("selectedConstraints");
		return run(query, filter, cstrs, measures, operator);
	}
	
	private String run(String query, int filter, long cstrs, JSONArray measures, String operator) throws Exception {
		
		Map<String, Map<Integer, Double>> resViolations = new HashMap<String, Map<Integer, Double>>();
		Map<String, Map<Long, Double>> resSubVio = new HashMap<String, Map<Long, Double>>();
		Map<String, Double> alls = new HashMap<String, Double>();
		JSONArray datas = new JSONArray();
		Map<Long, String> positions = Config.getConstraintPos();
		
		for(int i = 0; i<measures.length(); i++) {
			String measure = measures.getString(i);

			List<List<String>> iResult = null;
			
			if (operator.equalsIgnoreCase("top-k")) 
				iResult = runTopK(query, filter, cstrs, measure);
			else 
				iResult = runAllAndThreshold(query, operator, filter, cstrs, measure);

			JSONObject ds = generateJson(iResult, positions, measure);
			datas.put(ds);
			Map<Integer, Double> vioAll = new HashMap<Integer, Double>();
			Map<Long, Double> provAll = new HashMap<Long, Double>();
			
			for(int j = 1; j<iResult.size(); j++) {
				List<String> list = iResult.get(j);
				long prov = toLong(list.get(list.size()-2));
				int vio = Integer.parseInt(list.get(list.size()-1));
				if (!provAll.containsKey(prov))
					provAll.put(prov, 0d);
				if (!vioAll.containsKey(vio))
					vioAll.put(vio, 0d);
				provAll.put(prov, provAll.get(prov)+1);
				vioAll.put(vio, vioAll.get(vio)+1);
			}
			resViolations.put(measure, vioAll);
			resSubVio.put(measure, provAll);
			alls.put(measure, (1d)*(iResult.size()-1));
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
		ticks.put("fontSize", ProfilingImp.size__);
		
		JSONObject ticks__ = new JSONObject();
		ticks__.put("ticks", ticks);
		yaxes.put(ticks__);
		
		JSONArray xaxes = new JSONArray();
		JSONObject ticks_ = new JSONObject();
		ticks_.put("fontColor", "black");
		ticks_.put("fontStyle", "bold");
		ticks_.put("fontSize", ProfilingImp.size__);
		JSONObject ticks_1 = new JSONObject();
		ticks_1.put("ticks", ticks_);
		xaxes.put(ticks_1);
		scales.put("yAxes", yaxes);
		scales.put("xAxes", xaxes);
		
		//legend: {display: false,labels: {fontColor: "black", fontStyle: "bold", fontSize:size__}}
		
		JSONObject legend = new JSONObject();
		legend.put("display", true);
		JSONObject labels = new JSONObject();
		labels.put("fontColor", "black");
		labels.put("fontStyle", "bold");
		labels.put("fontSize", ProfilingImp.size__);
		legend.put("labels", labels);
		
		options.put("scales", scales);
		options.put("legend", legend);
		
		sub_vio.put("type", "bar");
		vio_dist.put("type", "bar");
		sub_vio.put("options", options);
		vio_dist.put("options", options);
	
		
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
			item.put("data", Config.getData_vio(resViolations.get(key), vioX, all));
			vioDatasets.put(item);
			 
			JSONObject item1 = new JSONObject();
			item1.put("label", key);
			item1.put("backgroundColor", colorsQuery[color]);
			item1.put("data", Config.getData_sub(resSubVio.get(key), subX, all));
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
			subLabels.put(Config.getSet(x, positions));		
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
	
	private long toLong(String value) {
		String [] vals = value.split("#");
		long res = 0;
		for (String str:vals){
			long l = Long.parseLong(str);
			res = res | l;
		}
		return res;
	}

	private String longToString(String value, Map<Long, String> positions) {
		String [] vals = value.split("#");
		String res = "";
		Map<String, Integer> values = new HashMap<String, Integer>();
		
		for (String str:vals){
			long l = Long.parseLong(str);
			for(long k:positions.keySet()) {
				if (((1<<k)&l)!=0) {
					if (!values.containsKey(positions.get(k)))
						values.put(positions.get(k), 0);
					values.put(positions.get(k), values.get(positions.get(k))+1);
				}
			}
		}
		for(String key:values.keySet()) {
			if (values.get(key)>1)
				res += values.get(key)+"*"+key+"_";
			else
				res += key+"_";
		}
		if (res.endsWith("_"))
			res = res.substring(0, res.length()-1);
		return res;
	} 
	
	private JSONObject generateJson(List<List<String>> iResult, Map<Long, String> positions, String measure) {
		JSONObject ds = new JSONObject();
		JSONArray attrs = new JSONArray();
		JSONArray data = new JSONArray();
		
//		for(String str:iResult.get(0))
//			attrs.put(str);
		for (int i=0; i<iResult.get(0).size()-1;i++)
			attrs.put(iResult.get(0).get(i));
		attrs.put(measure);
		
		for(int i =1; i<iResult.size(); i++) {
			JSONArray line = new JSONArray();
			for(int j = 0; j<iResult.get(i).size()-2; j++) {
				String str = iResult.get(i).get(j);
				line.put(str);
			}
			int a = iResult.get(i).size()-2, b = iResult.get(i).size()-1;
			line.put(longToString(iResult.get(i).get(a), positions));
			line.put(iResult.get(i).get(b));
			data.put(line);	
		}
		
		ds.put("attrs", attrs);
		ds.put("data", data);
		
		return ds;
	}
	

	//top-k algorithm for different measures ----------------------------------- start
	private List<List<String>> runTopK(String query, int filter, long cstrs, String measure) throws Exception{
		List<List<String>> iResult = null;
		if (measure.equalsIgnoreCase("cbs"))
			iResult = runCBS(query, filter, cstrs);
		if (measure.equalsIgnoreCase("cbm"))
			iResult = runCBM(query, filter, cstrs);
		if (measure.equalsIgnoreCase("css"))
			iResult = runCSS(query, filter, cstrs);
		if (measure.equalsIgnoreCase("csm"))
			iResult = runCSM(query, filter, cstrs);
		return iResult;
	}
	
	private List<List<String>> runCSM(String query, int filter, long cstrs) throws Exception{
		
		List<List<String>> runAllAndThreshold = new ArrayList<List<String>>();

		String selects[] = query.split("(?i)from");
		String select = selects[0];
		String from = selects[1];

		String rel = removeSpaceEndAndStart(from.split("(?i)where")[0]);
		
		String orExpression = "";
		String cbmExpression = "";
		String csm_agg = "";
		
		for(String val:rel.split("( )*,( )*")) {
			String label = val.split("( )+")[1];
			orExpression += label+".vioset"+"|";
			cbmExpression += "bit_count("+label+".vioset & "+cstrs+")"+"+";
		}
		
		csm_agg = orExpression.substring(0, orExpression.length()-1);
		orExpression = "("+csm_agg+ ")& "+cstrs;;
		cbmExpression = cbmExpression.substring(0, cbmExpression.length()-1);		
		
		String groupPart = ""; 
		select += ",min_card_set_multi(Concat("+csm_agg.replace("|", " & "+cstrs+",'a',")+" & "+cstrs+")) AS Prov, min("+cbmExpression+") AS \"Nb Vio\"";		
		if (filter<0)
			groupPart = "GROUP BY "+selects[0].replaceAll("(?i)select", "")+" ORDER by \"Nb Vio\" DESC LIMIT "+(-1)*(filter);
		else
			groupPart = "GROUP BY "+selects[0].replaceAll("(?i)select", "")+" ORDER by \"Nb Vio\" LIMIT "+filter;
		
		String sql = select+" FROM "+from+" "+groupPart;
		
		System.out.println(sql);
		
		ResultSet res = Config.getCon().createStatement().executeQuery(sql);
		
		List<String> attrs = new ArrayList<String>();
		for(int i =1; i<=res.getMetaData().getColumnCount(); i++)
			attrs.add(res.getMetaData().getColumnName(i));
		runAllAndThreshold.add(attrs);
		
		while(res.next()) {
			List<String> line = new ArrayList<String>();
			for(int i=1; i<=res.getMetaData().getColumnCount(); i++)
				line.add(res.getString(i));
			runAllAndThreshold.add(line);
		}
		return runAllAndThreshold;

		
//		throw new FunctionNotImplemented();
	}

	private List<List<String>> runCSS(String query, int filter, long cstrs) throws Exception{
		
		List<List<String>> runAllAndThreshold = new ArrayList<List<String>>();

		String selects[] = query.split("(?i)from");
		String select = selects[0];
		String from = selects[1];

		String rel = removeSpaceEndAndStart(from.split("(?i)where")[0]);
		
		String orExpression = "";
		String cbmExpression = "";
		String cbsExpression = "";
		
		String csm_agg = "";
		
		for(String val:rel.split("( )*,( )*")) {
			String label = val.split("( )+")[1];
			orExpression += label+".vioset"+"|";
			cbmExpression += "bit_count("+label+".vioset & "+cstrs+")"+"+";
		}
		
		csm_agg = orExpression.substring(0, orExpression.length()-1);
		orExpression = "("+csm_agg+ ")& "+cstrs;;
		cbmExpression = cbmExpression.substring(0, cbmExpression.length()-1);
		cbsExpression = "bit_count("+orExpression+")";
		
		
		String groupPart = "";
		
		select += ",min_card_set("+orExpression+") AS Prov, min("+cbsExpression+") AS \"Nb Vio\"";
		if (filter<0)
			groupPart = "GROUP BY "+selects[0].replaceAll("(?i)select", "")+" ORDER by \"Nb Vio\" DESC LIMIT "+(-1)*(filter);
		else
			groupPart = "GROUP BY "+selects[0].replaceAll("(?i)select", "")+" ORDER by \"Nb Vio\" LIMIT "+filter;
		
		String sql = select+" FROM "+from;
		sql += " "+groupPart; 
		
		ResultSet res = Config.getCon().createStatement().executeQuery(sql);
		
		List<String> attrs = new ArrayList<String>();
		for(int i =1; i<=res.getMetaData().getColumnCount(); i++)
			attrs.add(res.getMetaData().getColumnName(i));
		runAllAndThreshold.add(attrs);
		
		while(res.next()) {
			List<String> line = new ArrayList<String>();
			for(int i=1; i<=res.getMetaData().getColumnCount(); i++)
				line.add(res.getString(i));
			runAllAndThreshold.add(line);
		}
		return runAllAndThreshold;
		
		//throw new FunctionNotImplemented();
	}

	private List<List<String>> runCBM(String query, int filter, long cstrs) throws Exception {
		return topK(query, filter, cstrs, 1);
	}

	private List<List<String>> runCBS(String query, int filter, long cstrs) throws Exception {
		return topK(query, filter, cstrs, 0);
	}

	private List<List<String>> topK(String query, int filter, long cstrs, int mes) throws Exception{
		Parameters param = Parameters.getInstance();
		String q = "";
		if (filter>0)
			q = query+" +="+filter+"#"+mes;
		else
			q = query+" -="+((-1)*filter)+"#"+mes;
		param.setQuery(q);
		
		param.setValableConstraints(cstrs);
		
		param.run();
		List<List<String>> res = new ArrayList<List<String>>();
		res.add(param.getAttrsRes());
		res.addAll(param.getResults());
		return res;
	}
	
	//top-k algorithm for different measures ----------------------------------- end
		
	String [] colorsQuery = {"blue", "green", "maroon", "black"};
	
	private String removeSpaceEndAndStart(String val) {
		String res = val;
		while (res.startsWith(" ") || res.endsWith(" ")) {
			if (res.startsWith(" "))
				res = res.substring(1);
			if (res.endsWith(" "))
				res = res.substring(0, res.length()-1);
		}
		return res;
	}
	
//	private List<List<String>> runAll(String query, String operator, int filter, long cstrs, String measure) {
//		return null;
//	}
		
	private List<List<String>> runAllAndThreshold(String query, String operator, int filter, long cstrs, String measure) throws SQLException {

		List<List<String>> runAllAndThreshold = new ArrayList<List<String>>();

		String selects[] = query.split("(?i)from");
		String select = selects[0];
		String from = selects[1];

		String rel = removeSpaceEndAndStart(from.split("(?i)where")[0]);
		
		String orExpression = "";
		String cbmExpression = "";
		String cbsExpression = "";
		
		String csm_agg = "";
		
		for(String val:rel.split("( )*,( )*")) {
			String label = val.split("( )+")[1];
			orExpression += label+".vioset"+"|";
			cbmExpression += "bit_count("+label+".vioset & "+cstrs+")"+"+";
		}
		
		csm_agg = orExpression.substring(0, orExpression.length()-1);
		orExpression = "("+csm_agg+ ")& "+cstrs;;
		cbmExpression = cbmExpression.substring(0, cbmExpression.length()-1);
		cbsExpression = "bit_count("+orExpression+")";
		
		
		String sql = "";
		
		String groupPart = "";
		
		
		
		if (measure.equalsIgnoreCase("cbs")) {
			select += ","+orExpression+" AS Prov,"+cbsExpression+" AS \"Nb Vio\"";
//			val = cbsExpression;
			if (!operator.equalsIgnoreCase("all")) {
				if (query.toLowerCase().contains("where"))
					sql = select+" FROM "+from+" AND "+cbsExpression+operator+filter;
//					val = " AND "+val+operator+filter;
				else
					sql = select+" FROM "+from+" WHERE "+cbsExpression+operator+filter;
			}else {
				sql = select+" FROM "+from;
			}
		}
		if (measure.equalsIgnoreCase("cbm")) { 
			select += ","+orExpression+" AS Prov,"+cbmExpression+" AS \"Nb Vio\"";

			if (!operator.equalsIgnoreCase("all")) {
				if (query.toLowerCase().contains("where"))
					sql = select+" FROM "+from+" AND "+cbmExpression+operator+filter;
//					val = " AND "+val+operator+filter;
				else
					sql = select+" FROM "+from+" WHERE "+cbmExpression+operator+filter;
			}else {
				sql = select+" FROM "+from;
			}
		}
		if (measure.equalsIgnoreCase("css")) {
			select += ",min_card_set("+orExpression+") AS Prov, min("+cbsExpression+") AS \"Nb Vio\"";
			groupPart = "GROUP BY "+selects[0].replaceAll("(?i)select", "")+" ";
			String val = "HAVING "+"min("+cbsExpression+")"+operator+filter;

			if (!operator.equalsIgnoreCase("all"))
				sql = select + " FROM " + from + " " + groupPart+ " "+val;
			else 
				sql = select+" FROM "+from +" "+groupPart;
		}

		if (measure.equalsIgnoreCase("csm")) {
			select += ",min_card_set_multi(Concat("+csm_agg.replace("|", " & "+cstrs+",'a',")+" & "+cstrs+")) AS Prov, min("+cbmExpression+") AS \"Nb Vio\"";
			groupPart = "GROUP BY "+selects[0].replaceAll("(?i)select", "")+" ";
			String val = "HAVING "+"min("+cbsExpression+")"+operator+filter;
			if (!operator.equalsIgnoreCase("all"))
				sql = select + " FROM " + from + " " + groupPart+ " "+val;
			else 
				sql = select+" FROM "+from +" "+groupPart;
		}
		System.out.println(sql); 
		
		ResultSet res = Config.getCon().createStatement().executeQuery(sql);
		
		List<String> attrs = new ArrayList<String>();
		for(int i =1; i<=res.getMetaData().getColumnCount(); i++)
			attrs.add(res.getMetaData().getColumnName(i));
		runAllAndThreshold.add(attrs);
		
		while(res.next()) {
			List<String> line = new ArrayList<String>();
			for(int i=1; i<=res.getMetaData().getColumnCount(); i++)
				line.add(res.getString(i));
			runAllAndThreshold.add(line);
		}
		return runAllAndThreshold;
	}
		
}
