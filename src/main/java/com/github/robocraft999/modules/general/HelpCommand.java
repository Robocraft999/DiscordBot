package com.github.robocraft999.modules.general;

import com.github.robocraft999.lib.BasicCommand;
import com.github.robocraft999.lib.GuildCommand;
import com.github.robocraft999.modules.commands.CommandManager;
import com.github.robocraft999.modules.tiers.TierManager.Level;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class HelpCommand extends GuildCommand {

	public HelpCommand() {
		super("help", "shows help for all or given command", Level.LOWEST);
	}

	@Override
	public Message perform(String[] args, Member author, TextChannel channel) {
		if (args.length > 1) {
			String command = args[1];
			for (BasicCommand cmd : CommandManager.commands) {
				if (command.equalsIgnoreCase(cmd.getCommand())) {
					return msg(getHelpString(cmd));
				}
			}
		}
		//return msg(String.join("\n", CommandManager.commands.stream().map(cmd -> getHelpString(cmd)).toList().toArray(new String[0])));
		return msg(String.join("\n", CommandManager.commands.stream().map(cmd -> "'" + cmd.getCommand() + "'").toList().toArray(new String[0])));
	}
	
	private String getHelpString(BasicCommand cmd) {
		String result = "'" + cmd.getCommand() + "': " + cmd.getDescription();
		if (cmd instanceof GuildCommand gc && !gc.handlesAccessItself())result += " (Level '" + gc.getLevel() + "')";
		return result;
	}

	@Override
	public OptionData[] getOptions() {
		OptionData[] data = { new OptionData(OptionType.STRING, "cmd", "Command you need help for", false) };
		return data;
	}
}
