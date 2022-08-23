package com.github.robocraft999.modules.general;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.github.robocraft999.DiscordBot;
import com.github.robocraft999.lib.GuildCommand;
import com.github.robocraft999.modules.tiers.TierManager;
import com.github.robocraft999.modules.tiers.TierManager.Level;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class PrefixCommand extends GuildCommand {

	public PrefixCommand() {
		super("prefix", "shows or sets the prefix", true);
	}

	@Override
	public Message perform(String[] args, Member author, TextChannel channel) {
		if(args.length > 1) {
			if(TierManager.isRequiredLevel(TierManager.getLevelForMember(author), Level.HIGH)) {
				String newPref = args[1];
				if(newPref.length() > 10)newPref = newPref.substring(0, 10);
				DiscordBot.INSTANCE.getSqlite().onUpdate(
					"UPDATE guilds " + 
					"SET prefix = '" + newPref + "'" +
					"WHERE guild_id = " + channel.getGuild().getIdLong());
				return msg("Changed prefix to '" + newPref + "'");
			}else return msg("Access Denied");
		}else {
			if(TierManager.isRequiredLevel(TierManager.getLevelForMember(author), Level.LOWEST)) {
				ResultSet set = DiscordBot.INSTANCE.getSqlite().onQuery(
					"SELECT prefix " + 
					"FROM guilds " + 
					"WHERE guild_id = " + channel.getGuild().getIdLong());
				if(set != null) {
					try {
						return msg("Prefix is '" + set.getString("prefix") + "'");
					} catch (SQLException e) {}
				}
			}else return msg("Access Denied");
		}
		return msg("");
	}
	
	@Override
	public OptionData[] getOptions() {
		OptionData[] data = { new OptionData(OptionType.STRING, "prefix", "New prefix", false) };
		return data;
	}

}