package com.orestis.tudelftlogger.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Results {
	List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
    List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
	
    public List<Map<String, String>> getGroup(){
    	return this.groupData;
    }
    
    public void setGroup(List<Map<String, String>> groupData){
    	this.groupData = groupData;
    }
    
    public List<List<Map<String, String>>> getChildren(){
    	return this.childData;
    }
    
    public void setChildren(List<List<Map<String, String>>> childData){
    	this.childData = childData;
    }
    
    public void clearAll(){
    	this.childData.clear();
    	this.groupData.clear();
    }
}
