package com.example.monitor;

public class MessageObject {
	private String property;
	private String phoneNumber;
	private String type;
	private String message;
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String toString()
	{
		String returnStr = "";
		returnStr +="property:[" + this.property + "] number:[" + this.phoneNumber + "] type:[" + this.type + "] message:[" + this.message+"]";
		return returnStr;
	}
}
