package com.orestis.tudelftlogger.database;

public class MPlace {
	private long pid;
	private String tag;
	private String address;
	private double places_lat;
	private double places_long;
	
	
	public long getPid(){
		return pid;
	}
	
	public void setPid(long pid){
		this.pid = pid;
	}
	
	
	public String getTag(){
		return tag;
	}
	
	public void setTag(String tag){
		this.tag = tag;
	}
	
	
	public String getAddress(){
		return address;
	}
	
	public void setAddress(String address){
		this.address = address;
	}
	
	
	public double getLat(){
		return places_lat;
	}
	
	public void setLat(double places_lat){
		this.places_lat = places_lat;
	}
	
	
	public double getLong(){
		return places_long;
	}
	
	public void setLong(double places_long){
		this.places_long = places_long;
	}
}