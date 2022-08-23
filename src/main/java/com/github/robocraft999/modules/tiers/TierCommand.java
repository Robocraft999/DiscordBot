package com.github.robocraft999.modules.tiers;

import java.util.Arrays;

import com.github.robocraft999.DiscordBot;
import com.github.robocraft999.lib.GuildCommand;
import com.github.robocraft999.modules.tiers.TierManager.Level;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class TierCommand extends GuildCommand {

	public TierCommand() {
		super("tier", "Sets the level for a role", Level.HIGHEST);
	}

	@Override
	public Message perform(String[] args, Member author, TextChannel channel) {
		if(args.length > 2) {
			String roleId = args[1];
			try {
				Level newLevel = Level.valueOf(args[2]);
				DiscordBot.INSTANCE.getSqlite().onUpdate(
						"UPDATE tiers " + 
						"SET level = '" + newLevel + "'" +
						"WHERE role_id = " + roleId);
				return msg("Level for " + channel.getGuild().getRoleById(roleId).getAsMention() + " set to: '" + newLevel + "'");
			}catch(IllegalArgumentException e) {}
			return msg("level not valid");
		}
		return msg("");
	}
	
	@Override
	public OptionData[] getOptions() {
		OptionData[] data = { 
			new OptionData(OptionType.ROLE, "role", "Role to set tier for", true),
			new OptionData(OptionType.STRING, "level", "one of the following: " + String.join(", ", Arrays.asList(Level.values()).stream().map(l -> l.toString()).toList()), true)
		};
		return data;
	}

}
