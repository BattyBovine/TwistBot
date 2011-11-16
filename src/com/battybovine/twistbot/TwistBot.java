package com.battybovine.twistbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jibble.pircbot.Colors;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

public class TwistBot extends PircBot {

	public TwistBot() {
		this.setName(this.twistify("Twist"));
		this.setMessageDelay(100);
		this.setFloodDelay(5);
	}
	
	public void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		Pattern p = Pattern.compile("^!(.*)$");
		Matcher m = p.matcher(message);
		if(m.matches())
			this.handleClientCommand(channel, sender, login, hostname, m.group(1).trim());
	}
	
	public void onPrivateMessage(String sender,
			String login, String hostname, String message) {
		Pattern p = Pattern.compile("^!(.*)");
		Matcher m = p.matcher(message);
		if(m.matches()) {
			String[] cmdargs = m.group(1).split("\\s+");
			for(String cmdarg : cmdargs) {
				System.out.print(cmdarg + " ");
			}
		} else {
			if(sender==this.getNick()) return;
			if(message.trim().isEmpty()) {
				this.sendMessage(sender, this.twistify(
					"Please provide the text you want to convert to Twist text."));
				return;
			}
			this.sendMessage(sender, this.twistify(message));
		}
	}
	
	public void onInvite(String targetnick, String sourcenick, String login,
			String hostname, String channel) {
		this.joinChannel(channel);
	}
	
	public void onJoin(String channel, String sender, String login, String hostname) {
		if(sender.equals(this.getNick())) {
			channels.add(channel);
			this.sendRawLine("WHO "+channel);
		}
	}
	
	public void onPart(String channel, String sender, String login, String hostname) {
		if(sender.equals(this.getNick())) {
			channels.remove(channel);
		}
	}
	
	public void onKick(String channel, String kickernick, String kickerlogin,
			String kickerHostname, String recipient, String reason) {
		if(recipient==this.getNick())	this.joinChannel(channel);
	}
	
	public void onSetModerated(String channel, String sourcenick,
			String sourcelogin, String sourcehostname) {
		this.setMode(channel, "+N");
	}
	
	public void onRemoveModerated(String channel, String sourcenick,
			String sourcelogin, String sourcehostname) {
		this.setMode(channel, "-N");
	}
	
	
	
	private String getStatus(String channel, String nick) {
		List<User> users = new ArrayList<User>(Arrays.asList(getUsers(channel)));
		User cmp = new User("", nick, "", "");
		if(users.contains(cmp))
			return users.get(users.indexOf(cmp)).getPrefix();
		return "";
	}
	
	
	
	public void printMessage(String message) {
		System.out.println(message);
	}
	
	public void handleClientCommand(String channel, String sender,
			String login, String hostname, String cmdin) {
		String[] cmdsplit = cmdin.split("\\s+", 2);
		String cmd = cmdsplit[0].toLowerCase();
		String[] args = null;
		String senderstatus = this.getStatus(channel, sender);
		if(cmdsplit.length>1) { args = cmdsplit[1].split("\\s+"); };
		
		// !flood : Only OPs and above should be able to do this
		if(cmd.matches("flood")) {
			if(senderstatus.matches("[&@~]")) {
				if(args!=null && args.length>=1) {
					int d = Integer.parseInt(args[0]);
					if(d<=0) {
						this.sendMessage(channel, this.twistify("Flood delay must be greater than zero"));
						return;
					}
					this.setFloodDelay(d);
					this.sendMessage(channel, this.twistify("Flood delay set to "+d+" seconds."));
				}
			} else {
				this.kickNoOps(channel, sender);
			}
			return;
		}
		
		// !mod : Only AOPs and above should be able to do this
		if(cmd.matches("mod")) {
			if(senderstatus.matches("[@~]")) {
				if(args!=null && args.length>=1) {
					if(args[0].trim().toLowerCase().equals("on"))
						this.setMode(channel, "+mN");
					else if(args[0].trim().toLowerCase().equals("off"))
						this.setMode(channel, "-mN");
				}
			} else {
				this.kickNoOps(channel, sender);
			}
			return;
		}
		
		// !mute : Only OPs and above should be able to do this
		if(cmd.matches("mute")) {
			if(senderstatus.matches("[&@~]")) {
				if(args!=null && args.length>=1) {
					this.setMode(channel, "+b ~q*"+args[0]+"*!*@*");
				}
			} else {
				this.kickNoOps(channel, sender);
			}
			return;
		}
		
		// !unmute : Only OPs and above should be able to do this
		if(cmd.matches("unmute")) {
			if(senderstatus.matches("[&@~]")) {
				if(args!=null && args.length>=1) {
					this.setMode(channel, "-b ~q*"+args[0]+"*!*@*");
				}
			} else {
				this.kickNoOps(channel, sender);
			}
			return;
		}
		
		// !kick : Only OPs and above should be able to do this
		if(cmd.matches("kick")) {
			if(senderstatus.matches("[&@~]")) {
				if(args!=null && args.length>=1) {
					String reason = "";
					for(int i=1; i<args.length; i++) reason += args[i] + " ";
					if(reason.isEmpty())
						this.kick(channel, args[0]);
					else
						this.kick(channel, args[0], this.twistify(reason.trim()));
				}
			} else {
				this.kickNoOps(channel, sender);
			}
			return;
		}
		
		// !ban : Only OPs and above should be able to do this
		if(cmd.matches("ban")) {
			if(senderstatus.matches("[&@~]")) {
				if(args!=null && args.length>=1) {
//					String[] hostsplit = hostname.split("\\.");
//					if(hostsplit.length>=2) {
//						String hostban = "";
//						for(int i=1; i<hostsplit.length; i++)
//							hostban += "."+hostsplit[i];
//						hostban = "*"+hostban;
//						this.ban(channel, "*!*@"+hostban);
//					} else {
//						this.ban(channel, "*!*@"+hostname);
//					}
					this.ban(channel, "*"+args[0]+"*!*@*");
					String reason = "";
					for(int i=1; i<args.length; i++) reason += args[i] + " ";
					if(reason.isEmpty())
						this.kick(channel, args[0]);
					else
						this.kick(channel, args[0], this.twistify(reason.trim()));
				}
			} else {
				this.kickNoOps(channel, sender);
			}
			return;
		}
		
		// !unban : Only OPs and above should be able to do this
		if(cmd.matches("unban")) {
			if(senderstatus.matches("[&@~]")) {
				if(args!=null && args.length>=1) {
					this.unBan(channel, "*"+args[0]+"*!*@*");
				}
			} else {
				this.kickNoOps(channel, sender);
			}
			return;
		}
		
		// Flood-controlled functions go inside the "else" clause below
		if(this.isFlood()) {
			this.sendMessage(sender, this.twistify("Please wait another " + this.getFloodRemaining() +
					" seconds before trying that again."));
		} else {
		
			if(cmd.matches("pon(n?y|i)show?")) {
				this.sendMessage(channel, this.getPoniShow(0));
				this.startFloodTimer();
				return;
			}
			if(cmd.matches("n(ew|u)pon(n?y|i)")) {
				this.sendMessage(channel, this.getPoniShow(NUM_PONI_SHOWS));
				this.startFloodTimer();
				return;
			}
			
			// !aop : Only channel owners should be able to do this
			if(cmd.matches("aop")) {
				if(senderstatus.matches("[~]")) {
					if(args!=null && args.length>=1) {
						this.aop(channel, args[0]);
					}
				} else {
					this.kickNoOps(channel, sender);
				}
				return;
			}
			
			// !deaop : Only channel owners should be able to do this
			if(cmd.matches("deaop")) {
				if(senderstatus.matches("[~]")) {
					if(args!=null && args.length>=1) {
						this.deAop(channel, args[0]);
					}
				} else {
					this.kickNoOps(channel, sender);
				}
				return;
			}
			
			// !op : Only AOPs and above should be able to do this
			if(cmd.matches("op")) {
				if(senderstatus.matches("[@~]")) {
					if(args!=null && args.length>=1) {
						this.op(channel, args[0]);
					}
				} else {
					this.kickNoOps(channel, sender);
				}
				return;
			}
			
			// !deop : Only AOPs and above should be able to do this
			if(cmd.matches("deop")) {
				if(senderstatus.matches("[@~]")) {
					if(args!=null && args.length>=1) {
						this.deOp(channel, args[0]);
					}
				} else {
					this.kickNoOps(channel, sender);
				}
				return;
			}
			
			// !hop : Only OPs and above should be able to do this
			if(cmd.matches("hop")) {
				if(senderstatus.matches("[&@~]")) {
					if(args!=null && args.length>=1) {
						this.hop(channel, args[0]);
					}
				} else {
					this.kickNoOps(channel, sender);
				}
				return;
			}
			
			// !dehop : Only OPs and above should be able to do this
			if(cmd.matches("dehop")) {
				if(senderstatus.matches("[&@~]")) {
					if(args!=null && args.length>=1) {
						this.deHop(channel, args[0]);
					}
				} else {
					this.kickNoOps(channel, sender);
				}
				return;
			}
			
			// !voice : Only HOPs and above should be able to do this
			if(cmd.matches("voice")) {
				if(senderstatus.matches("[%&@~]")) {
					if(args!=null && args.length>=1) {
						this.voice(channel, args[0]);
					}
				} else {
					this.kickNoOps(channel, sender);
				}
				return;
			}
			
			// !devoice : Only HOPs and above should be able to do this
			if(cmd.matches("devoice")) {
				if(senderstatus.matches("[%&@~]")) {
					if(args!=null && args.length>=1) {
						this.deVoice(channel, args[0]);
					}
				} else {
					this.kickNoOps(channel, sender);
				}
				return;
			}
		
		}
	}
	
	public boolean handleServerCommand(String cmdin) {
		String[] cmdsplit = cmdin.split("\\s+", 2);
		String cmd = cmdsplit[0].toLowerCase();
		String[] args = null;
		if(cmdsplit.length>1) { args = cmdsplit[1].split("\\s+"); };
		
		if(!cmd.isEmpty()) {
			/*if(cmd.matches("connect")) {
				if(args.length>=1) {
					this.connect(args[0]);
				}
			} else */if(cmd.matches("join")) {
				if(args!=null && args.length>=1)
					this.joinChannel(args[0]);
			} else if(cmd.matches("kick")) {
				if(args!=null && args.length>=2)
					this.kick(args[0], args[1]);
			} else if(cmd.matches("nick")) {
				if(args!=null && args.length>=1)
					this.changeNick(args[0]);
			} else if(cmd.matches("nickserv|ns")) {
				if(args!=null && args.length>=1)
					if(args[0].matches("identify") && args.length>=2)
						this.identify(args[1]);
					else
						this.sendMessage("NickServ", args[0]);
			} else if(cmd.matches("part")) {
				if(args!=null && args.length>=1)
					this.partChannel(args[0]);
			} else if(cmd.matches("quit")) {
				if(args!=null && args.length>=1) {
					String quitmsg = "";
					for(String arg : args) quitmsg += (arg+" ");
					this.quitServer(this.twistify(quitmsg.trim()));
				} else
					this.quitServer(this.getNick());
				return false;
			}
		}
		return true;
	}
	
	
	
	private void kickNoOps(String channel, String sender) {
		this.kick(channel, sender, this.twistify("Nice try, buster!"));
	}
	
	private String getPoniShow(int poninum) {
		String poniname = "";
		String ponicode = "";
		int num = poninum;
		if(num==0)	num = new Random().nextInt(NUM_PONI_SHOWS)+1;
		else		num = NUM_PONI_SHOWS;
		
		switch(num) {
		case 1:		poniname = "Friendship Is Magic - Part 1";		ponicode = "q6sUJR1GiGI";	break;
		case 2:		poniname = "Friendship Is Magic - Part 2";		ponicode = "SYVA0h5gfV8";	break;
		case 3:		poniname = "The Ticket Master";					ponicode = "TDhJdGwPPx0";	break;
		case 4:		poniname = "Applebuck Season";					ponicode = "BEi_BjDN1Nk";	break;
		case 5:		poniname = "Griffon The Brush-Off";				ponicode = "grq_IW1pdg8";	break;
		case 6:		poniname = "Boast Busters";						ponicode = "JSvjBcDbFHA";	break;
		case 7:		poniname = "Dragonshy";							ponicode = "Qr879wBybpw";	break;
		case 8:		poniname = "Look Before You Sleep";				ponicode = "zyrNM2_ncZg";	break;
		case 9:		poniname = "Bridle Gossip";						ponicode = "kslLINriezQ";	break;
		case 10:	poniname = "Swarm Of The Century";				ponicode = "k0iMgyP1Qhc";	break;
		case 11:	poniname = "Winter Wrap Up";					ponicode = "gwybXq7pdbA";	break;
		case 12:	poniname = "Call Of The Cutie";					ponicode = "W7grh5exALQ";	break;
		case 13:	poniname = "Fall Weather Friends";				ponicode = "xouOH84DM6I";	break;
		case 14:	poniname = "Suited For Success";				ponicode = "X6A5njFwJTI";	break;
		case 15:	poniname = "Feeling Pinkie Keen";				ponicode = "C7wqjGZzoss";	break;
		case 16:	poniname = "Sonic Rainboom";					ponicode = "20qW-w1fJFw";	break;
		case 17:	poniname = "Stare Master";						ponicode = "eqt-M_bmN-8";	break;
		case 18:	poniname = "The Show Stoppers";					ponicode = "1T0TeTCCnzs";	break;
		case 19:	poniname = "A Dog And Pony Show";				ponicode = "UdzMhcT_VeM";	break;
		case 20:	poniname = "Green Isn't Your Color";			ponicode = "A9FJJjd1YF0";	break;
		case 21:	poniname = "Over A Barrel";						ponicode = "Zou9iRGA9iI";	break;
		case 22:	poniname = "A Bird In The Hoof";				ponicode = "NpzYBlt-xOY";	break;
		case 23:	poniname = "The Cutie Mark Chronicles";			ponicode = "0FgL5W9b_Lk";	break;
		case 24:	poniname = "Owl's Well That Ends Well";			ponicode = "L0pYwG_QF1c";	break;
		case 25:	poniname = "Party Of One";						ponicode = "_qDahzMNTJw";	break;
		case 26:	poniname = "The Best Night Ever";				ponicode = "oXJvULB7hKA";	break;
		case 27:	poniname = "The Return of Harmony - Part 1";	ponicode = "RQqvIYyybl8";	break;
		case 28:	poniname = "The Return of Harmony - Part 2";	ponicode = "UxkknJTKue4";	break;
		case 29:	poniname = "Lesson Zero";						ponicode = "Q2SX0BwlETI";	break;
		case 30:	poniname = "Luna Eclipsed";						ponicode = "Pfhd82PFJi8";	break;
		case 31:	poniname = "Sisterhooves Social";				ponicode = "9cDyMjm7uP4";	break;
		case 32:	poniname = "The Cutie Pox";						ponicode = "ijPtYR0IxYY";	break;
		}
		return (poniname.isEmpty() || ponicode.isEmpty())
				? Colors.RED+"UNDEFINED" : (poniname+": " + "http://youtu.be/"+ponicode);
	}
	
	
	
	public String twistify(String input) {
		String output = input.trim();
		return output
				.replaceAll("SH", "TH")
				.replaceAll("([A-Z])SS", "$1TH")
				.replaceAll("SS([A-Z])", "TH$1")
				.replaceAll("([A-Z])S", "$1TH")
				.replaceAll("S([A-Z])", "TH$1")
				.replaceAll("(\\b)x", "$1th")
				.replaceAll("(\\b)X([A-Z])", "$1TH$2")
				.replaceAll("(\\b)X", "$1Th")
				.replaceAll("X([A-Z])", "KTH$1")
				.replaceAll("([A-Z])X", "$1KTH")
				.replaceAll("sh", "th")
				.replaceAll("Sh", "Th")
				.replaceAll("ss", "th")
				.replaceAll("Ss", "Th")
				.replaceAll("s", "th")
				.replaceAll("S", "Th")
				.replaceAll("x", "kth")
				.replaceAll("X", "Kth")
				.replaceAll("ce", "the")
				.replaceAll("ci", "thi")
				.replaceAll("cy", "thy")
				.replaceAll("CE", "THE")
				.replaceAll("CI", "THI")
				.replaceAll("CY", "THY")
				.replaceAll("Ce", "The")
				.replaceAll("Ci", "Thi")
				.replaceAll("Cy", "Thy");
	}
	
	private void startFloodTimer() {
		floodstart = System.currentTimeMillis();
	}
	private void setFloodDelay(int d) {
		flooddelay = d*1000;
	}
	private boolean isFlood() {
		return (System.currentTimeMillis() < (floodstart+flooddelay));
	}
	private int getFloodRemaining() {
		int time = (int)((floodstart+flooddelay)-System.currentTimeMillis());
		return (int)(time>0 ? Math.ceil(time/1000) : 0);
	}
	
	
	
	private List<String> channels = new ArrayList<String>();
	
	private static final int NUM_PONI_SHOWS = 32;
	
	private long floodstart;
	private int flooddelay;
	
}
