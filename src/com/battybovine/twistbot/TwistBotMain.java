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
			System.exit(1);
		String server = "", nspass = "";
		List<String> channels = new ArrayList<String>();
		
		for(String arg : args) {
			if(arg.startsWith("-")) {
				if(arg.contains("v"))
					bot.setVerbose(true);
			} else {
				if(server.isEmpty()) {
					server = arg;
					continue;
				}
				if(channels!=null && arg.startsWith("#")) {
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
				bot.connect(server);
			} catch (NickAlreadyInUseException e) {
				bot.printMessage("Error: " + e.getLocalizedMessage());
			} catch (IOException e) {
				bot.printMessage("I/O Exception: " + e.getLocalizedMessage());
			} catch (IrcException e) {
				bot.printMessage("IRC Exception: " + e.getLocalizedMessage());
			}
		} else {
			bot.printMessage("Please enter a server to connect to.");
			System.exit(2);
		}
		
		if(!channels.isEmpty()) {
			for(String chan : channels)
				bot.joinChannel(chan);
		} else {
			bot.printMessage("Sorry, but you'll need to join a channel.");
			System.exit(3);
		}
		
		if(!nspass.isEmpty()) {
			bot.identify(nspass);
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		boolean loop = true;
		while(loop) {
			String in = null;
			try {
				System.out.print(bot.getNick()+"> ");
				in = br.readLine();
			} catch (IOException e) {
				bot.printMessage("Cannot capture input: " + e.getLocalizedMessage());
			}
			if(in.isEmpty()) continue;
			Pattern p = Pattern.compile("^[/!](.*)");
			Matcher m = p.matcher(in);
			if(m.matches()) {
				if(in.startsWith("/"))
					loop = bot.handleServerCommand(m.group(1).trim());
//				else if(in.startsWith("!"))
//					bot.handleClientCommand(channel, bot.getNick(), m.group(1).trim());
//			} else {
//				bot.sendMessage(channel, bot.twistify(in));
			}
		}
		
		System.exit(0);
	}
	
	
	
	private static TwistBot bot = null;
	
}
