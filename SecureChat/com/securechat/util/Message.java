package com.securechat.util;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.google.gson.Gson;

public class Message {

	// TODO: These properties will change depending on the encrypted data and how we want to send stuff
	private String messageString;
	private Key randomKey;
	
	public Message(){
		
	}
	
	public String createEncryptedMessageJSON(PublicKey pub, String message){
		
		// TODO: Implement this
		
		// Must use the Gson library to return a JSON string, we'll send that on the wire Gson.toJSON(this)
		
		return null;
	}
	
	public String getMessageString(){
		return this.messageString;
	}
	
	public void setMessageString(String message){
		this.messageString = message;
	}
	
	public Key getRandomKey(){
		return this.randomKey;
	}
}
