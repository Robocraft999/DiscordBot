package com.github.robocraft999.lib;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public abstract class GuildCommand extends BasicCommand{

	public GuildCommand(String command, String description) {
		super(command, description);
	}

	/**
	 * 
	 * @param args list of arguments (first arg is command itself)
	 * @param author member who sent the message
	 * @param channel where the message was sent
	 * @return return message
	 */
	public abstract Message perform(String[] args, Member author, TextChannel channel);
	@Override
	public Message perform(String[] args, User author, TextChannel channel) {
		return perform(args, channel.getGuild().getMember(author), channel);
	}
}
