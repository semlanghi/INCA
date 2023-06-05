package com.limos.fr;

public class SpeedCostraintSpeedEventFactory {

    private final String tableName;
    private final String valueAttName;
    private final String groupAttName;
    private final String tsAttName;
    private final double maxCoeff;
    private final double minCoeff;

    public SpeedCostraintSpeedEventFactory(String tableName, String valueAttName, String tsAttName, String groupAttName, double maxCoeff, double minCoeff) {
        this.tableName = tableName;
        this.valueAttName = valueAttName;
        this.tsAttName = tsAttName;

        this.groupAttName = groupAttName;
        this.maxCoeff = maxCoeff;
        this.minCoeff = minCoeff;
    }

    public String getSpeedConstraintSpeedEvent(SpeedEvent speedEvent){
        int speedSignAdjusted = speedEvent.speed < 0 ? -speedEvent.speed : speedEvent.speed;
        return tableName+ " r2 : r2."+tsAttName+">"+ speedEvent.timestamp+" AND ((r2."+valueAttName+"-"+ speedSignAdjusted +")/(r2."+tsAttName+"-"+ speedEvent.timestamp+") > "+maxCoeff +" OR "
                +"(r2."+valueAttName+"-"+ speedSignAdjusted +")/(r2."+tsAttName+"-"+ speedEvent.timestamp+") < "+minCoeff+") AND r2."+ groupAttName +"="+ speedEvent.vid+";\n";
    }

    public static void main(String[] args){
        System.out.println(new SpeedCostraintSpeedEventFactory("tableName", "value", "ts", "name", 0.1, -0.1).getSpeedConstraintSpeedEvent(new SpeedEvent(123L, 12, 12L, 1233, 12)));
    }
}
