package com.citysdk.demo.invoker;

import citysdk.tourism.client.poi.single.POI;
import citysdk.tourism.client.requests.ParameterList;

public abstract class Invoker {
	protected String version = "1.0";
	protected String base;
	protected String id;
	protected String term;
	private int itemId = -1;
	private String link;
	
	public String getVersion() {
		return version;
	}
	
	public Invoker setVersion(String version) {
		this.version = version;
		return this;
	}
	
	public String getBase() {
		return base;
	}
	
	public String getId() {
		return id;
	}
	
	public void setBase(String base) {
		this.base = base;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
	
	public String getLink() {
		return link;
	}
	
	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public abstract POI invoke(ParameterList parameterList, String homeUrl);
}
