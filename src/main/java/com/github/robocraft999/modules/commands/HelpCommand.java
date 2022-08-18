package com.github.robocraft999.modules.commands;

import com.github.robocraft999.lib.BasicCommand;
import com.github.robocraft999.lib.GuildCommand;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class HelpCommand extends GuildCommand {

	public HelpCommand() {
		super("help", "shows help for given command");
	}

	@Override
	public Message perform(String[] args, Member author, TextChannel channel) {
		if (args.length > 1) {
			String command = args[1];
			for (BasicCommand cmd : CommandManager.commands) {
				if (command.equalsIgnoreCase(cmd.getCommand())) {
					return msg("'" + cmd.getCommand() + "': " + cmd.getDescription());
				}
			}
		}
		return msg("");
	}

	@Override
	public OptionData[] getOptions() {
		OptionData[] data = { new OptionData(OptionType.STRING, "cmd", "Command you need help for", true) };
		return data;
	}
}
