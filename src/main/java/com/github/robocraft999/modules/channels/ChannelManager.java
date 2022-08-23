package com.github.robocraft999.modules.channels;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.robocraft999.DiscordBot;

import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChannelManager extends ListenerAdapter {
	
	private final Logger logger = LoggerFactory.getLogger("Bot-ChannelManager");
	
	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		onGuildVoiceJoinOrMove(event);
	}
	
	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		onGuildVoiceJoinOrMove(event);
	}
	
	public void onGuildVoiceJoinOrMove(GenericGuildVoiceUpdateEvent event){
		AudioChannel joined = event.getChannelJoined();
		if(joined instanceof VoiceChannel joinedVC) {
			ChannelType type = getChannelType(joined.getId());
			Category category = joinedVC.getParentCategory();
			switch(type) {
			case PRIVATE_VOICE_CREATE:{
				VoiceChannel pvc = category.createVoiceChannel("Channel von: " + event.getMember().getEffectiveName()).complete();
				setChannelType(pvc.getId(), ChannelType.PRIVATE_VOICE);
				event.getGuild().moveVoiceMember(event.getMember(),  pvc).queue();
				break;
			}
			case PRIVATE_VOICE:{
				break;
			}
			default:{
				break;
			}
			}
		}
	}
	
	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		AudioChannel joined = event.getChannelLeft();
		if(joined instanceof VoiceChannel joinedVC) {
			ChannelType type = getChannelType(joined.getId());
			//Category category = joinedVC.getParentCategory();
			switch(type) {
			case PRIVATE_VOICE:{
				if(joinedVC.getMembers().size() == 0) {
					removeChannelType(joinedVC.getId());
					joinedVC.delete().queue();
				}
			}
			default:{
				break;
			}
			}
		}
	}
	
	//--------------------------------Channel Database Stuff--------------------------------
	protected void setChannelType(String channelId, ChannelType type) {
		try {
			if(!DiscordBot.INSTANCE.getSqlite().onQuery("SELECT * FROM channels WHERE channel_id = " + channelId).next())
				DiscordBot.INSTANCE.getSqlite().onUpdate("INSERT INTO channels(channel_id, channel_type)" + " VALUES(" + channelId + ", '" + type + "')");
			else
				DiscordBot.INSTANCE.getSqlite().onUpdate("UPDATE channels SET channel_type = '" + type + "' WHERE channel_id = " + channelId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected void removeChannelType(String channelId) {
		DiscordBot.INSTANCE.getSqlite().onUpdate("DELETE FROM channels WHERE channel_id = " + channelId);
	}
	
	private ChannelType getChannelType(String channelId) {
		ChannelType type = ChannelType.NONE;
		ResultSet res = DiscordBot.INSTANCE.getSqlite().onQuery("SELECT channel_type FROM channels WHERE channel_id = " + channelId);
		try {
			if(!res.isClosed())
				type = ChannelType.valueOf(res.getString("channel_type"));
		}catch(SQLException e) {
			e.printStackTrace();
			logger.warn("Channel has no special type");
		}catch(IllegalArgumentException e) {}
		return type;
	}
}
