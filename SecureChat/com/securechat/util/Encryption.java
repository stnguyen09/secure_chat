package com.securechat.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

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
	public static Key[] generateKeyPair(){
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DiffieHellman");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			keyGen.initialize(1024, random);
			
			KeyPair pair = keyGen.generateKeyPair();
			PrivateKey priv = pair.getPrivate();
			PublicKey pub = pair.getPublic();
			
			Key[] keys = {priv,pub};
			
			return keys;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Key generateRandomKey() {
		KeyGenerator keyGen;
		try {
			keyGen = KeyGenerator.getInstance("HmacSHA256");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			keyGen.init(1024, random);
			
			return keyGen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}	
		
		return null;
	}
	
	public static String encryptMessage(String message, Key key){
		return doEncryptDecrypt(message.getBytes(), key, Cipher.ENCRYPT_MODE).toString();
	}
	
	public static String decryptMessage(String message, Key key){	
		return doEncryptDecrypt(message.getBytes(), key, Cipher.DECRYPT_MODE).toString();
	}
	
	/**
	 * This returns a String of the key that was to be encrypted. This Key object must be reconstructed from the string.
	 * @param keyToBeEncrypted
	 * @param key
	 * @return
	 */
	public static String encryptKey(Key keyToBeEncrypted, Key key){
		try {
			Cipher cipher = Cipher.getInstance("SHA-256");
			cipher.init(Cipher.WRAP_MODE, key);
			
			return cipher.wrap(keyToBeEncrypted).toString();
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Key decryptKey(String keyToBeDecrypted, PrivateKey key){
		try {
			Cipher cipher = Cipher.getInstance("SHA-256");
			cipher.init(Cipher.UNWRAP_MODE, key);
			
			return (SecretKey) cipher.unwrap(keyToBeDecrypted.getBytes(), "SHA-256", Cipher.SECRET_KEY);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static byte[] doEncryptDecrypt(byte[] data, Key key, int mode){
		if(mode == Cipher.ENCRYPT_MODE || mode == Cipher.DECRYPT_MODE){
			try {
				Cipher cipher = Cipher.getInstance("SHA-256");
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
