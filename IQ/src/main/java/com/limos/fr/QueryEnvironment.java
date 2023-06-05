package com.limos.fr;

import org.json.JSONObject;

import java.util.Collection;

public class QueryEnvironment {
    public static final String query1 = "select s1.name, s1.value, s1.ts, s2.name, s2.value, s2.ts from Stock s1, Stock s2 where s1.name!=s2.name and s1.ts = s2.ts";
    public static final String query2 = "select r1.title, r1.userId, r1.value, r1.ts from Review r1";
    public static final String query3 = "select g1.x, g1.y, g1.ts from GPS g1";
    public static final String query4 = "select s1.vid, s1.speed, s1.timestamp, s2.vid, s2.speed, s2.timestamp from SpeedEvent s1, SpeedEvent s2 where s1.xWay = s2.xWay and s1.segment = s2.segment";
    private int filterValue;
    private int selectedQuery;
    private QueryEnvironment.Operator operator;
    private Collection<QueryEnvironment.Measure> measures;

    public QueryEnvironment(int filterValue, int selectedQuery, Operator operator, Collection<Measure> measures) {
        this.filterValue = filterValue;
        this.selectedQuery = selectedQuery;
        this.operator = operator;
        this.measures = measures;
    }

    public static String getQuery(int queryIndex){
        switch (queryIndex){
            case 1: return query1;
            case 2: return query2;
            case 3: return query3;
            case 4: return query4;
            default: throw new UnsupportedOperationException("No query available.");
        }
    }

    public void changeQuery(int selectedQuery) {
        this.selectedQuery = selectedQuery;
    }

    public String getQuery() {
        switch (this.selectedQuery){
            case 1: return query1;
            case 2: return query2;
            case 3: return query3;
            case 4: return query4;
            default: throw new UnsupportedOperationException("No query available.");
        }
    }

    public JSONObject getJSONConfig(){
        JSONObject object = new JSONObject();
        object.put("query", getQuery()); // the input SQL query
        object.put("operator", operator); // topk, thershold, or both (all)
        object.put("measure", measures); // cbm, cbs, css, csm
        object.put("filterValue", this.filterValue); // a number for topk, the k
        object.put("selectedConstraints", Long.MAX_VALUE); //constraints considered in a binary format
        return object;
    }

    public enum Operator{
        TOPK,
        THRESHOLD,
        ALL
    }

    public enum Measure {
        CBS("cbs"),CBM("cbm"), CSS("css"), CSM("csm");

        private final String description;

        Measure(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
