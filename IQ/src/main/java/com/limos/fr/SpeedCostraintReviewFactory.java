package com.limos.fr;

public class SpeedCostraintReviewFactory {

    private String tableName;
    private String valueAttName;
    private String groupAttName;
    private String tsAttName;
    private double maxCoeff, minCoeff;

    public SpeedCostraintReviewFactory(String tableName, String valueAttName, String tsAttName, String groupAttName, double maxCoeff, double minCoeff) {
        this.tableName = tableName;
        this.valueAttName = valueAttName;
        this.tsAttName = tsAttName;
        this.groupAttName = groupAttName;
        this.maxCoeff = maxCoeff;
        this.minCoeff = minCoeff;
    }

    public String getSpeedConstraintReview(Review review){
        return tableName+ " r2 : r2."+tsAttName+">"+ review.ts+" AND ((r2."+valueAttName+"-"+ review.value+")/(r2."+tsAttName+"-"+ review.ts+") > "+maxCoeff +" OR "
                +"(r2."+valueAttName+"-"+ review.value+")/(r2."+tsAttName+"-"+ review.ts+") < "+minCoeff+") AND r2."+groupAttName+"='"+ review.title+"';\n";
    }

    public static void main(String[] args){
        System.out.println(new SpeedCostraintReviewFactory("tableName", "value", "ts", "name",0.1, -0.1).getSpeedConstraintReview(new Review(123L, "finalfantasy", 12L, 1233)));
    }
}
