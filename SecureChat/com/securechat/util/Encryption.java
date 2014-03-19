package com.securechat.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Encryption {

	/**
	 * Returns an array of keys. [0] = private key, [1] = public key
	 * @return
	 */
	public static KeyPair generateKeyPair(){
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(512);
			
			return keyGen.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Key generateRandomKey() {
		KeyGenerator keyGen;
		try {
			keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(512);
			
			return keyGen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}	
		
		return null;
	}
	
	public static byte[] encryptMessage(String message, Key key, String algorithm){
		return doEncryptDecrypt(message.getBytes(), key, Cipher.ENCRYPT_MODE, algorithm);
	}
	
	public static byte[] decryptMessage(String message, Key key, String algorithm){	
		return doEncryptDecrypt(message.getBytes(), key, Cipher.DECRYPT_MODE, algorithm);
	}
	
	/**
	 * This returns a String of the key that was to be encrypted. This Key object must be reconstructed from the string.
	 * @param keyToBeEncrypted
	 * @param key
	 * @return
	 */
	public static byte[] encryptKey(Key keyToBeEncrypted, Key key){
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.WRAP_MODE, key);
			
			return cipher.wrap(keyToBeEncrypted);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Key decryptKey(String keyToBeDecrypted, PrivateKey key){
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.UNWRAP_MODE, key);
			
			return (SecretKey) cipher.unwrap(keyToBeDecrypted.getBytes(), "AES", Cipher.SECRET_KEY);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static byte[] doEncryptDecrypt(byte[] data, Key key, int mode, String algorithm){
		if(mode == Cipher.ENCRYPT_MODE || mode == Cipher.DECRYPT_MODE){
			try {
				Cipher cipher = Cipher.getInstance(algorithm);
				cipher.init(mode, key);
				
				byte[] cipheredBytes = cipher.doFinal(data);
				
				return cipheredBytes;
			} catch (NoSuchPaddingException | NoSuchAlgorithmException 
					| InvalidKeyException | IllegalBlockSizeException 
					| BadPaddingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
