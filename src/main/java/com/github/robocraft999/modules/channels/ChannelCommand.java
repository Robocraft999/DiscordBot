package com.github.robocraft999.modules.channels;

import java.util.Arrays;

import com.github.robocraft999.DiscordBot;
import com.github.robocraft999.lib.GuildCommand;
import com.github.robocraft999.modules.tiers.TierManager.Level;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class ChannelCommand extends GuildCommand {

	public ChannelCommand() {
		super("channel", "Sets the special type of a channel", Level.HIGHEST);
	}

	@Override
	public Message perform(String[] args, Member author, TextChannel channel) {
		if(args.length > 2) {
			String channel_id = args[1];
			try {
				ChannelType type = ChannelType.valueOf(args[2]);
				if(type == ChannelType.NONE) {
					DiscordBot.INSTANCE.getChannelManager().removeChannelType(channel_id);
				}else {
					DiscordBot.INSTANCE.getChannelManager().setChannelType(channel_id, type);
				}
				return msg("changed type of channel " + channel.getGuild().getGuildChannelById(channel_id).getAsMention() + " to '" + type + "'");
			}catch(IllegalArgumentException e) {}
			return msg("type not valid");
		}
		return msg("");
	}
	
	@Override
	public OptionData[] getOptions() {
		OptionData[] data = { 
			new OptionData(OptionType.CHANNEL, "channel", "Channel to set type", true),
			new OptionData(OptionType.STRING, "type", "one of the following: " + String.join(", ", Arrays.asList(ChannelType.values()).stream().map(l -> l.toString()).toList()), true)
		};
		return data;
	}

}
