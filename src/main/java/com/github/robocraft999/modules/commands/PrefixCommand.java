package com.github.robocraft999.modules.commands;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.github.robocraft999.DiscordBot;
import com.github.robocraft999.lib.GuildCommand;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class PrefixCommand extends GuildCommand {

	public PrefixCommand() {
		super("prefix", "shows or sets the prefix");
	}

	@Override
	public Message perform(String[] args, Member author, TextChannel channel) {
		if(args.length > 1) {
			String newPref = args[1];
			DiscordBot.INSTANCE.getSqlite().onUpdate(
					"UPDATE guilds " + 
					"SET prefix = '" + newPref + "'" +
					"WHERE guild_id = " + channel.getGuild().getIdLong());
			return msg("");
		}else {
			ResultSet set = DiscordBot.INSTANCE.getSqlite().onQuery(
					"SELECT prefix " + 
					"FROM guilds " + 
					"WHERE guild_id = " + channel.getGuild().getIdLong());
			if(set != null) {
				try {
				    //set.first();
				    return msg(set.getString("prefix"));
				} catch (SQLException e) {}
			}
			return msg("");
		}
	}

}