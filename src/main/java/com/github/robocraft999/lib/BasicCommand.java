package com.github.robocraft999.lib;

import java.util.ArrayList;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
	
	public void performSlash(SlashCommandInteractionEvent event) {
		event.deferReply();
		ArrayList<String> argsList = new ArrayList<>(event.getOptions().stream().map(om -> om.getAsString()).toList());
		argsList.add(0, event.getName());
		String[] args = argsList.toArray(new String[0]);
		Message m = perform(args, event.getUser(), event.getChannel().asTextChannel());
		if(!m.getContentRaw().isEmpty()) {
			event.reply(m).queue();
		}
	}
	
	protected Message msg(String msg) {
		return new MessageBuilder(msg).build();
	}
	
	public String getCommand() {
		return command;
	}

	public String getDescription() {
		return description;
	}
	
	public OptionData[] getOptions() {
		return new OptionData[0];
	}
}
