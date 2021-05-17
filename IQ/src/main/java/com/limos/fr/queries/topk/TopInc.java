package com.limos.fr.queries.topk;

import java.util.List;

public class TopInc {
	// relations.size = selections.size
	private List<Relation> relations;
	private List<String> selections;
	private List<Join> joins;

	
	
	public List<Relation> getRelations() {
		return relations;
	}
	public List<String> getSelections() {
		return selections;
	}
	public List<Join> getJoins() {
		return joins;
	}
	public void setRelations(List<Relation> relations) {
		this.relations = relations;
	}
	public void setSelections(List<String> selections) {
		this.selections = selections;
	}
	public void setJoins(List<Join> joins) {
		this.joins = joins;
	}
}
