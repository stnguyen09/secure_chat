package com.securechat;

import com.securechat.client.ClientNode;
import com.securechat.server.Server;

public class SecureChat {

	/**
	 * Takes 2 args: arg[0] = client || server, arg[1] = socket number
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 2 || args.length > 4){
			System.err.println("Argument error. Usage is args[0] = 'server' || 'client', args[1] = port number");
			System.err.println("If running 'client', args[2] = server hostname, args[3] = your name");
			System.exit(0);
		}
		
		// Parse the port number
		int portNumber = -1;
		try{
			portNumber = Integer.parseInt(args[1]);
		} catch(NumberFormatException e){
			System.err.println("Argument error. args[1] is not a valid number.");
			System.exit(0);
		}
		
		Thread runner = null;
		
		switch(args[0].toLowerCase()){
			case "client":
				if(args.length != 4){
					System.err.println("Invalid number of args. Usages is [server, client], [port number], [server hostname], [your name]");
					System.exit(0);
				}
				
				runner = new Thread(new ClientNode(args[2], portNumber, args[3]));
				break;
			case "server":
				runner = new Thread(new Server(portNumber));
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
