package com.battybovine.twistbot;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;

public class TwistBotMain {

	public static void main(String[] args) {
		
		bot = new TwistBot();
		if(bot == null)
			System.exit(ERROR_CANNOT_INSTANTIATE_BOT);
		
		for(String arg : args) {
			if(arg.startsWith("-")) {
				if(arg.contains("v"))	bot.setVerbose(true);
				if(arg.contains("s"))	bot.setSecure(true);
			} else {
				if(server.isEmpty()) {
					String[] sp = arg.split(":");
					server = sp[0];
					if(sp.length>1)	port = Integer.parseInt(sp[1]);
					continue;
				}
				if(channels!=null && arg.matches("^[#&+!](.*)")) {
					channels.add(arg);
					continue;
				}
				if(nspass.isEmpty() && !arg.matches("n(ull|o(ne)?)?")) {
					nspass = arg;
					continue;
				}
			}
		}
		
		if(!server.isEmpty()) {
			try {
				bot.connect(server, port);
			} catch (NickAlreadyInUseException e) {
				bot.printMessage("Error: " + e.getLocalizedMessage());
			} catch (IOException e) {
				bot.printMessage("I/O Exception: " + e.getLocalizedMessage());
				System.exit(ERROR_CONNECT_IO_EXCEPTION);
			} catch (IrcException e) {
				bot.printMessage("IRC Exception: " + e.getLocalizedMessage());
				System.exit(ERROR_CONNECT_IRC_EXCEPTION);
			}
		} else {
			bot.printMessage("Please enter a server to connect to.");
			System.exit(ERROR_NO_SERVER_DEFINED);
		}
		
		if(!channels.isEmpty()) {
			for(String chan : channels)
				bot.joinChannel(chan);
		} else {
			bot.printMessage("Sorry, but you'll need to join a channel.");
			System.exit(ERROR_NO_CHANNELS_DEFINED);
		}
		
		if(!nspass.isEmpty()) {
			bot.identify(nspass);
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(bot.isConnected()) {
			String in = null;
			try {
				System.out.print(bot.getNick()+"> ");
				in = br.readLine();
			} catch (IOException e) {
				bot.printMessage("Cannot capture input: " + e.getLocalizedMessage());
			}
			if(in.isEmpty()) continue;
			Matcher m = Pattern.compile("^[/!](.*)").matcher(in);
			if(m.matches()) {
				if(in.startsWith("/"))
					bot.handleServerCommand(m.group(1).trim());
//				else if(in.startsWith("!"))
//					bot.handleClientCommand(channel, bot.getNick(), m.group(1).trim());
//			} else {
//				for(String channel : channels)
//					bot.sendMessage(channel, bot.twistify(in));
			}
		}
		
		System.exit(ERROR_FINISHED);
	}
	
	
	
	private static TwistBot bot = null;
	private static String server = "", nspass = "";
	private static int port = 6667;
	private static List<String> channels = new ArrayList<String>();
	
	private static final int ERROR_FINISHED					= 0x00;
	private static final int ERROR_CANNOT_INSTANTIATE_BOT	= 0x01;
	private static final int ERROR_NO_SERVER_DEFINED		= 0x02;
	private static final int ERROR_CONNECT_IO_EXCEPTION		= 0x04;
	private static final int ERROR_CONNECT_IRC_EXCEPTION	= 0x08;
	private static final int ERROR_NO_CHANNELS_DEFINED		= 0x10;
	
}
