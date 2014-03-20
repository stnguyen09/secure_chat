package com.securechat.message;

import com.google.gson.Gson;

public class Message {

	private byte[] payload;
	private byte[] randomKey;
	
	public Message(byte[] payload, byte[] randomKey){
		this.payload = payload;
		this.randomKey = randomKey;
	}
	
	public String toJson(){
		return new Gson().toJson(this);
	}
	
	public byte[] getPayload(){
		return this.payload;
	}
	
	public void setMessageString(byte[] payload){
		this.payload = payload;
	}
	
	public byte[] getRandomKey(){
		return this.randomKey;
	}
	
	public void setRandomKey(byte[] randomKey){
		this.randomKey = randomKey;
	}
}
