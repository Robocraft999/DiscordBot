package com.github.robocraft999.modules.tiers;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.github.robocraft999.DiscordBot;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class TierManager {

	public static boolean isRequiredLevel(Level own, Level required) {
		return own.compareTo(required) >= 0;
	}
	
	public static Level getLevelForMember(Member member) {
		Level highest = Level.LOWEST;
		for(Role role : member.getRoles()) {
			ResultSet set = DiscordBot.INSTANCE.getSqlite().onQuery(
				"SELECT level " +
				"FROM tiers " +
				"WHERE role_id = " + role.getIdLong()
			);
			//System.out.println(role.getName());
			try {
				Level level = Level.valueOf(set.getString("level"));
				if(isRequiredLevel(level, highest))highest = level;
			} catch (SQLException e) {e.printStackTrace();}
		}
		return highest;
	}
	
	public enum Level{
		LOWEST,
		LOW,
		MEDIUM,
		HIGH,
		HIGHEST,
		OWNER
	}
}
