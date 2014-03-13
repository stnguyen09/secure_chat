package com.securechat.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MessageReceiver implements Runnable{

	private Socket socket;
	
	public MessageReceiver(Socket socket){
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			while(true){
	
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
