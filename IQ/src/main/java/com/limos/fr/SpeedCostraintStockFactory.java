package com.limos.fr;

public class SpeedCostraintStockFactory {

    private String tableName;
    private String valueAttName;
    private String groupAttName;
    private String tsAttName;
    private double maxCoeff, minCoeff;

    public SpeedCostraintStockFactory(String tableName, String valueAttName, String tsAttName, String groupAttName, double maxCoeff, double minCoeff) {
        this.tableName = tableName;
        this.valueAttName = valueAttName;
        this.tsAttName = tsAttName;
        this.groupAttName = groupAttName;
        this.maxCoeff = maxCoeff;
        this.minCoeff = minCoeff;
    }

    public String getSpeedConstraintStock(Stock stock){
        return tableName+ " s2 : s2."+tsAttName+">"+stock.ts+" AND ((s2."+valueAttName+"-"+stock.value+")/(s2."+tsAttName+"-"+stock.ts+") > "+maxCoeff +" OR "
                +"(s2."+valueAttName+"-"+stock.value+")/(s2."+tsAttName+"-"+stock.ts+") < "+minCoeff+") AND s2."+groupAttName+"='"+stock.name+"';\n";
    }

    public static void main(String[] args){
        System.out.println(new SpeedCostraintStockFactory("tableName", "value", "ts", "name",0.1, -0.1).getSpeedConstraintStock(new Stock('A', 12.3, 12334L)));
    }
}
