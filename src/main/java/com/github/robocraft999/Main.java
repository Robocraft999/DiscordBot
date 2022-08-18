package com.github.robocraft999;

public class Main {

	public static void main(String[] args) {
		if(args[0].equals("--dev")) {
			new DiscordBot(true);
		}else {
			new DiscordBot(false);
		}
	}

}
