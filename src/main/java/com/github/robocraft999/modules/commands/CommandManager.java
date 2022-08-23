package com.github.robocraft999.modules.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.github.robocraft999.DiscordBot;
import com.github.robocraft999.lib.BasicCommand;
import com.github.robocraft999.lib.GuildCommand;
import com.github.robocraft999.modules.channels.ChannelCommand;
import com.github.robocraft999.modules.general.ClearCommand;
import com.github.robocraft999.modules.general.HelpCommand;
import com.github.robocraft999.modules.general.PrefixCommand;
import com.github.robocraft999.modules.tiers.TierCommand;
import com.github.robocraft999.modules.tiers.TierManager;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class CommandManager extends ListenerAdapter {

	public static ArrayList<BasicCommand> commands = new ArrayList<>();
	
	public void contruct() {
		commands.add(new HelpCommand());
		commands.add(new PrefixCommand());
		commands.add(new ClearCommand());
		commands.add(new TierCommand());
		commands.add(new ChannelCommand());

		DiscordBot.INSTANCE.getShardManager().getShards().forEach(shard -> {
			CommandListUpdateAction commup = shard.updateCommands();
			for (BasicCommand cmd : commands) {
				commup.addCommands(Commands.slash(cmd.getCommand(), cmd.getDescription()).addOptions(cmd.getOptions()));
			}
			commup.complete();
		});
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String message = event.getMessage().getContentStripped();

		switch (event.getChannelType()) {
		case TEXT: {
			try {
				String prefix = ".";
				ResultSet set = DiscordBot.INSTANCE.getSqlite().onQuery(
						"SELECT prefix " + 
						"FROM guilds " + 
						"WHERE guild_id = " + event.getGuild().getIdLong());
				if(set != null) {
					prefix = set.getString("prefix");
				}
				onGuildMessageReceived(event, message, prefix);
			} catch (IllegalStateException | SQLException e) {
				e.printStackTrace();
			}
			break;
		}
		/*
		 * case VOICE: { break; }
		 */
		default:
			break;
		}
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		event.deferReply();
		for (BasicCommand bc : commands) {
			if (bc instanceof GuildCommand cmd && cmd.getCommand().equalsIgnoreCase(event.getName())) {
				if(TierManager.isRequiredLevel(TierManager.getLevelForMember(event.getMember()), cmd.getLevel()) || cmd.handlesAccessItself()){
					cmd.performSlash(event);
					break;
				}else {
					event.reply("Access Denied").queue();
				}
			}
		}
	}

	protected void onGuildMessageReceived(MessageReceivedEvent event, String message, String prefix) {
		TextChannel channel = event.getChannel().asTextChannel();

		if (message.startsWith(prefix) && message.length() != 0) {
			String[] args = message.substring(prefix.length()).split(" ");
			if (args.length > 0) {
				for (BasicCommand bc : commands) {
					if (bc instanceof GuildCommand cmd && cmd.getCommand().equalsIgnoreCase(args[0])) {
						if(TierManager.isRequiredLevel(TierManager.getLevelForMember(event.getMember()), cmd.getLevel()) || cmd.handlesAccessItself()){
							Message msg = cmd.perform(args, event.getAuthor(), channel);
							if(msg != null) {
								String[] m = cmd.processDelayArg(msg.getContentRaw());//TODO maybe change to display
								if(m.length == 2) {
									int delay = Integer.parseInt(m[1]);
									channel.sendMessage(m[0]).complete().delete().queueAfter(delay, TimeUnit.SECONDS);
								}else
									channel.sendMessage(m[0]).queue();
							}
							break;
						}else {
							channel.sendMessage("Access denied").queue();
						}
					}
				}
			}
		}
	}

}
