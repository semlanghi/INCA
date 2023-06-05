package com.limos.fr;

public class SpeedCostraintGPSFactory {

    private String tableName;
    private String valueAttXName;
    private String valueAttYName;
    private String tsAttName;
    private double maxCoeff, minCoeff;

    public SpeedCostraintGPSFactory(String tableName, String valueAttXName, String tsAttName, String valueAttYName, double maxCoeff, double minCoeff) {
        this.tableName = tableName;
        this.valueAttXName = valueAttXName;
        this.valueAttYName = valueAttYName;
        this.tsAttName = tsAttName;
        this.maxCoeff = maxCoeff;
        this.minCoeff = minCoeff;
    }

    public String getSpeedConstraintGPS(GPS gps){
        return tableName+ " r2 : r2."+tsAttName+">"+ gps.ts+" AND ((r2."+ valueAttXName +"-"+ gps.x+")/(r2."+tsAttName+"-"+ gps.ts+") > "+maxCoeff +" OR "
                +"(r2."+ valueAttXName +"-"+ gps.x+")/(r2."+tsAttName+"-"+ gps.ts+") < "+minCoeff +" OR "
                +"(r2."+ valueAttYName +"-"+ gps.y+")/(r2."+tsAttName+"-"+ gps.ts+") < "+minCoeff +" OR "
                +"(r2."+ valueAttYName +"-"+ gps.y+")/(r2."+tsAttName+"-"+ gps.ts+") > "+maxCoeff+") ;\n";
    }

    public static void main(String[] args){
        System.out.println(new SpeedCostraintGPSFactory("tableName", "value", "ts", "name",0.1, -0.1).getSpeedConstraintGPS(new GPS(123L, 123, 12L)));
    }
}
