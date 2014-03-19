package com.securechat.message;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.google.gson.Gson;

public class Message {

	// TODO: These properties will change depending on the encrypted data and how we want to send stuff
	private byte[] payload;
	private byte[] randomKey;
	
	/*
	 *  TODO: The JSON'd instance of this class isn't the final thing we're sending, we must wrap this 
	 *  in another class that contains source/destination/MessageType. This other class is not encrypted.
	 */
	
	public Message(){}
	
	public String createEncryptedMessageJSON(PublicKey pub, PrivateKey priv, String message){
		
		// TODO: Implement this
		
		/* 
		 * Encrypt the message with the random key
		 * Encrypt the random key with THEIR public key
		 * Encrypt the entire (JSON'd) thing with YOUR private key
		 * 
		 * Only the encryption of the random key needs to be repeated when sending to different recipients
		 */
		
		/*
		 * When the receiver gets the item...
		 * 1. They decrypt the JSON with YOUR public key
		 * 2. They decrypt the random key with THEIR private key
		 * 3. They decrypt the message with the random key
		 */
		
		// Must use the Gson library to return a JSON string, we'll send that on the wire Gson.toJSON(this)
		String messageJSON = new Gson().toJson(this);
		
		return null;
	}
	
	public String getPayload(){
		return this.payload.toString();
	}
	
	public void setMessageString(byte[] payload){
		this.payload = payload;
	}
	
	public byte[] getRandomKey(){
		return this.randomKey;
	}
}
