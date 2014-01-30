package com.orestis.tudelftlogger.database;

/**
 * This class defines the object MLocation
 * 
 * entry_type distinguishes between 0 (location entry) and 1 (browser entry)
 * 
 * @author Orestis
 *
 */
public class MLocation {
	private long lid;
	private int entry_type;
	private long ltimestamp;
	private double location_lat;
	private double location_long;
	private String provider;
	private String closeTo;
	private float meters;
	private int luploaded;
	
	
	public long getLid(){
		return lid;
	}
	
	public void setLid(long lid){
		this.lid = lid;
	}
	
	
	public long getEntryType(){
		return entry_type;
	}
	
	public void setEntryType(int entry_type){
		this.entry_type = entry_type;
	}
	
	
	public long getLTimestamp(){
		return ltimestamp;
	}
	
	public void setLTimestamp(long ltimestamp){
		this.ltimestamp = ltimestamp;
	}
	
	
	public double getLocationLat(){
		return location_lat;
	}
	
	public void setLocationLat(double location_lat){
		this.location_lat = location_lat;
	}
	
	
	public double getLocationLong(){
		return location_long;
	}
	
	public void setLocationLong(double location_long){
		this.location_long = location_long;
	}
	
	
	public String getProvider(){
		return provider;
	}
	
	public void setProvider(String provider){
		this.provider = provider;
	}

	
	public String getCloseTo(){
		return closeTo;
	}
	
	public void setCloseTo(String closeTo){
		this.closeTo = closeTo;
	}
		

	public float getMeters(){
		return meters;
	}
	
	public void setMeters(float meters){
		this.meters = meters;
	}
	
	
	public int getLUploaded(){
		return luploaded;
	}
	
	public void setLUploaded(int luploaded){
		this.luploaded = luploaded;
	}
}
