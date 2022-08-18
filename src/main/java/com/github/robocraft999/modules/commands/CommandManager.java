package com.github.robocraft999.modules.commands;

import java.util.ArrayList;

import com.github.robocraft999.DiscordBot;
import com.github.robocraft999.lib.BasicCommand;
import com.github.robocraft999.lib.GuildCommand;

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
				String prefix = "."; // change to database
				onGuildMessageReceived(event, message, prefix);
			} catch (IllegalStateException e) {
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
		for (BasicCommand cmd : commands) {
			if (cmd.getCommand().equalsIgnoreCase(event.getName())) {
				cmd.performSlash(event);
			}
		}
	}

	protected void onGuildMessageReceived(MessageReceivedEvent event, String message, String prefix) {
		TextChannel channel = event.getChannel().asTextChannel();

		if (message.startsWith(prefix) && message.length() != 0) {
			String[] args = message.substring(prefix.length()).split(" ");
			if (args.length > 0) {
				for (BasicCommand cmd : commands) {
					if (cmd.getCommand().equalsIgnoreCase(args[0])) {
						channel.sendMessage(cmd.perform(args, event.getAuthor(), channel)).queue();
					}
				}
			}
		}
	}

}
