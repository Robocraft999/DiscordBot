package com.github.robocraft999.lib;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.github.robocraft999.modules.tiers.TierManager.Level;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class GuildCommand extends BasicCommand{
	
	private Level level = Level.LOW;
	private boolean handlesAccessItself = false;
	
	public GuildCommand(String command, String description, Level level) {
		super(command, description);
		this.level = level;
	}
	
	public GuildCommand(String command, String description, boolean handlesAccessItself) {
		super(command, description);
		this.handlesAccessItself = handlesAccessItself;
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
	
	public void performSlash(SlashCommandInteractionEvent event) {
		ArrayList<String> argsList = new ArrayList<>(event.getOptions().stream().map(om -> om.getAsString()).toList());
		argsList.add(0, event.getName());
		String[] args = argsList.toArray(new String[0]);
		Message msg = perform(args, event.getUser(), event.getChannel().asTextChannel());
		if(msg != null) {
			String[] m = processDelayArg(msg.getContentRaw());//TODO maybe change to display
			if(m.length == 2) {
				int delay = Integer.parseInt(m[1]);
				event.reply(m[0]).complete().deleteOriginal().queueAfter(delay, TimeUnit.SECONDS);
			}else {
				event.reply(m[0]).queue();
			}
		}else {
			event.reply("No Answer").complete().deleteOriginal().queueAfter(5, TimeUnit.SECONDS);
		}
	}

	public Level getLevel() {
		return level;
	}
	
	public boolean handlesAccessItself() {
		return handlesAccessItself;
	}
}
