package com.limos.fr.queries.topk;

public class Join {
	
	private String leftRelation;
	private String rightRelation;
	private String leftAttribute;
	private String rightAttribute;
	private Theta theta;

	public String getLeftRelation() {
		return leftRelation;
	}
	public void setLeftRelation(String leftRelation) {
		this.leftRelation = leftRelation;
	}
	public String getRightRelation() {
		return rightRelation;
	}
	public void setRightRelation(String rightRelation) {
		this.rightRelation = rightRelation;
	}
	public String getLeftAttribute() {
		return leftAttribute;
	}
	public void setLeftAttribute(String leftAttribute) {
		this.leftAttribute = leftAttribute;
	}
	public String getRightAttribute() {
		return rightAttribute;
	}
	public void setRightAttribute(String rightAttribute) {
		this.rightAttribute = rightAttribute;
	}
	public Theta getTheta() {
		return theta;
	}
	public void setTheta(Theta theta) {
		this.theta = theta;
	}
	
}
