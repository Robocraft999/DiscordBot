package com.github.robocraft999.lib;

import java.util.Arrays;
import java.util.List;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public abstract class BasicCommand{
	
	private String command;
	private String description;
	
	public BasicCommand(String command, String description) {
		this.command = command;
		this.description = description;
	}
	
	/**
	 * 
	 * @param args list of arguments (first arg is command itself)
	 * @param author user who sent the message
	 * @param channel where the message was sent
	 * @return return message
	 */
	public abstract Message perform(String[] args, User author, TextChannel channel);
	
	protected Message msg(String msg) {
		if(!msg.isBlank())
			return new MessageBuilder(msg).build();
		else
			return null;
	}
	
	public String[] processDelayArg(String m) {
		if(m.startsWith("%d")) {
			String[] msgParts = m.split(" ", 2);
			String[] res = {msgParts[1], msgParts[0].substring(2)};
			return res;
		}
		String[] res = {m};
		return res;
	}
	
	public String getCommand() {
		return command;
	}

	public String getDescription() {
		List<String> descAddition = Arrays.asList(getOptions()).stream().map(od -> od.getName()).toList();
		return descAddition.isEmpty() ? description : description + " [" + String.join("] [", descAddition) + "]";
	}
	
	public OptionData[] getOptions() {
		return new OptionData[0];
	}
}
