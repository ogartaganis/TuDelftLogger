package com.orestis.tudelftlogger.database;

public class MBrowser {
	private long bid;
	private long blid;
	private long btimestamp;
	private String search;
	private String url;
	private float light;
	private int last_call;
	private int uploaded;
	private String incentive;
	
	
	public long getBid(){
		return bid;
	}
	
	public void setBid(long bid){
		this.bid = bid;
	}
	
	
	public long getBlid(){
		return blid;
	}
	
	public void setBlid(long blid){
		this.blid = blid;
	}
	
	
	public long getBTimestamp(){
		return btimestamp;
	}
	
	public void setBTimestamp(long btimestamp){
		this.btimestamp = btimestamp;
	}
	
	
	public String getSearch(){
		return search;
	}
	
	public void setSearch(String search){
		this.search = search;
	}
	
	
	public String getUrl(){
		return url;
	}
	
	public void setUrl(String url){
		this.url = url;
	}
	
	
	public float getLight(){
		return light;
	}
	
	public void setLight(float light){
		this.light = light;
	}
	
	
	public int getLastCall(){
		return last_call;
	}
	
	public void setLastCall(int last_call){
		this.last_call = last_call;
	}
	
	
	public int getUploaded(){
		return this.uploaded;
	}
	
	public void setUploaded(int uploaded){
		this.uploaded = uploaded;
	}
	
	
	public String getIncentive(){
		return this.incentive;
	}
	
	public void setIncentive(String incentive){
		this.incentive = incentive;
	}
	
}
