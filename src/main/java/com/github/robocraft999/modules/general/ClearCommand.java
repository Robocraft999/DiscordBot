package com.github.robocraft999.modules.general;

import java.util.ArrayList;

import com.github.robocraft999.lib.GuildCommand;
import com.github.robocraft999.modules.tiers.TierManager.Level;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class ClearCommand extends GuildCommand {

	public ClearCommand() {
		super("clear", "Clears specified chat messages (if using native way add one)", Level.HIGHEST);
	}

	@Override
	public Message perform(String[] args, Member author, TextChannel channel) {
		if(args.length > 1) {
			int amount = Integer.parseInt(args[1]);
			ArrayList<Message> msgsToDelete = get(channel, amount);
			channel.purgeMessages(msgsToDelete);
			return msg("%d5 " + msgsToDelete.size() + " Nachrichten wurden gelöscht.");
		}
		return msg("Amount must be specified");
	}
	
	@Override
	public OptionData[] getOptions() {
		OptionData[] data = { new OptionData(OptionType.INTEGER, "amount", "Amount of messages to delete", true) };
		return data;
	}
	
	private ArrayList<Message> get(TextChannel channel, int amount){
		ArrayList<Message> messages = new ArrayList<>();
		int i = amount;// + 1
		
		for(Message message : channel.getIterableHistory().cache(false)) {
			if(!message.isPinned()) {
				messages.add(message);
			}
			if(--i <= 0)break;
		}
		
		return messages;
	}

}
