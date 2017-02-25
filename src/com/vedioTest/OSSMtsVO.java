package com.vedioTest;

import com.alibaba.fastjson.JSONObject;

public class OSSMtsVO {

	private String bucket;
	private String location;
	private String object;
	public String getBucket() {
		return bucket;
	}
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getObject() {
		return object;
	}
	public void setObject(String object) {
		this.object = object;
	}
	public OSSMtsVO(String bucket, String location, String object) {
		this.bucket = bucket;
		this.location = location;
		this.object = object;
	}
	public OSSMtsVO() {
	}
	public JSONObject toJson(){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("Bucket", bucket);
		jsonObject.put("Location", location);
		jsonObject.put("Object", object);
		return jsonObject;
		
	}
	
}
