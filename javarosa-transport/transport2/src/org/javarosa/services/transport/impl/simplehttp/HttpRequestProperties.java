package org.javarosa.services.transport.impl.simplehttp;

import java.util.Hashtable;

import javax.microedition.io.HttpConnection;

import de.enough.polish.io.Serializable;

public class HttpRequestProperties implements Serializable {
 
	
	private String requestMethod = HttpConnection.POST;
	private String userAgent = "Profile/MIDP-2.0 Configuration/CLDC-1.1";
	private String contentLanguage = "en-US";
	private String mimeVersion="1.0";
	private String contentType =  "text/xml";
	
	private Hashtable otherProperties=new Hashtable();
	
	 
	public void setRequestProperty(String property,String value){
		this.otherProperties.put(property, value);
	}
	public Hashtable getOtherProperties() {
		return otherProperties;
	}
	public String getRequestMethod() {
		return requestMethod;
	}
	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}
	public String getUserAgent() {
		return userAgent;
	}
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	public String getContentLanguage() {
		return contentLanguage;
	}
	public void setContentLanguage(String contentLanguage) {
		this.contentLanguage = contentLanguage;
	}
	public String getMimeVersion() {
		return mimeVersion;
	}
	public void setMimeVersion(String mimeVersion) {
		this.mimeVersion = mimeVersion;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	

}