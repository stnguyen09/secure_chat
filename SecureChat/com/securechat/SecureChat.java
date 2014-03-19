package com.securechat;

import com.securechat.client.ClientNode;
import com.securechat.server.Server;

public class SecureChat {

	/**
	 * Takes 2 args: arg[0] = client || server, arg[1] = socket number
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 2){
			System.err.println("Argument error. Usage is arg[0] = 'server' || 'client', arg[1] = socketNumber");
			System.exit(0);
		}
	
		int socketNumber = -1;
		try{
			socketNumber = Integer.parseInt(args[1]);
		} catch(NumberFormatException e){
			System.err.println("Argument error. arg[1] is not a valid number.");
			System.exit(0);
		}
		
		Thread runner = null;
		
		switch(args[0].toLowerCase()){
			case "client":
				runner = new Thread(new ClientNode(socketNumber));
				break;
			case "server":
				runner = new Thread(new Server(socketNumber));
				break;
			default:
				System.err.println("Unknown first argument. Must either be 'client' or 'server'.");
		}
		
		if(runner != null){
			runner.start();
			
			try {
				runner.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Exiting...");
	}

}
