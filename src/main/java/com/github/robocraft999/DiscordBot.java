package com.github.robocraft999;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.robocraft999.modules.commands.CommandManager;
import com.github.robocraft999.util.SQLite;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class DiscordBot {

	private final String CONFIG_PATH = "src/main/resources/bot.properties";

	private final Logger logger = LoggerFactory.getLogger("Bot-Main");

	private boolean isDev;
	private ShardManager shardManager;
	private CommandManager cmdManager;
	private SQLite sqlite;
	public Thread activityloop;
	private final String[] STATI = { "%members Mitglieder" };

	public static DiscordBot INSTANCE;

	public DiscordBot(boolean isDev) {
		INSTANCE = this;
		this.isDev = isDev;

		Properties prop = new Properties();
		FileInputStream in;

		try {
			in = new FileInputStream(CONFIG_PATH);
			prop.load(in);
			in.close();
		} catch (IOException e) {
			logger.error("No config file found. Generating new one.");
			File f = new File(CONFIG_PATH);
			if (!f.exists())
				generateConfigFile(f);
		}

		initialize(prop);
		this.shardManager.getGuilds().forEach(gu -> {//TODO move to own function

			Long id = gu.getIdLong();
			try {
				if(!this.getSqlite().onQuery("SELECT * FROM guilds WHERE guild_id = " + id).next())
					this.getSqlite().onUpdate("INSERT INTO guilds(guild_id) VALUES(" + id + ")");
			} catch (SQLException e) {
				e.printStackTrace();
			}

		});
		awaitReady();
		
		startShutdownThread();
		startActivityThread();
	}

	private void initialize(Properties prop) {
		String token = prop.getProperty("token");
		int shardCount = !prop.getProperty("shardCount").isEmpty() ? Integer.parseInt(prop.getProperty("shardCount")) : -1;

		try {
			buildBot(token, shardCount);
		} catch (LoginException | IllegalArgumentException e) {
			logger.error("Couldn't start Bot! - Please check the config -> Message='" + e.getMessage() + "'");
		}
		
		contructManagers();
	}

	private void buildBot(String token, int shardCount) throws LoginException, IllegalArgumentException {
		DefaultShardManagerBuilder builder;

		builder = DefaultShardManagerBuilder.createDefault(token, EnumSet.allOf(GatewayIntent.class));
		if (shardCount != -1)
			builder.setShardsTotal(shardCount);

		builder.setMemberCachePolicy(MemberCachePolicy.ALL);
		
		builder.setStatus(OnlineStatus.ONLINE);
		
		this.cmdManager = new CommandManager();
		builder.addEventListeners(cmdManager);

		this.shardManager = builder.build();
	}
	
	private void contructManagers() {
		this.sqlite = new SQLite();
		sqlite.connect();
		
		this.cmdManager.contruct();
	}

	private void generateConfigFile(File f) {

		try {
			f.createNewFile();

			BufferedWriter stream = Files.newBufferedWriter(Path.of(CONFIG_PATH), Charset.forName("UTF-8"),
					StandardOpenOption.TRUNCATE_EXISTING);

			Properties prop = new Properties();

			prop.setProperty("token", "");
			prop.setProperty("ownerId", "");
			prop.setProperty("shardCount", "");
			prop.setProperty("vplanpw", "");
			prop.setProperty("schoolID", "");

			prop.store(stream, "Bot-Configfile\n 'token' is required!");
			stream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void awaitReady() {

		logger.info("Awaiting jda ready");
		this.shardManager.getShards().forEach(jda -> {

			try {
				logger.debug("Awaiting jda ready for shard: " + jda.getShardInfo());
				jda.awaitReady();
			} catch (InterruptedException e) {
				logger.info("could not start shardInfo: " + jda.getShardInfo() + " and Self-Username :"
						+ jda.getSelfUser().getName());
				e.printStackTrace();
			}

		});
		logger.info("Bot was started");
	}

	public void shutdown() {
		if (this.shardManager != null) {
			this.shardManager.setStatus(OnlineStatus.OFFLINE);
			this.shardManager.shutdown();
			getSqlite().disconnect();
		}

		if (activityloop != null) {
			activityloop.interrupt();
		} else
			logger.info("acitvityLoop = null");
		System.exit(0); //TODO maybe remove for restarting?
	}

	// ---------------------------Threads---------------------------
	private void startShutdownThread() {
		Thread t = new Thread(() -> {

			String line = "";
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			try {
				while ((line = reader.readLine()) != null) {
					if (line.equalsIgnoreCase("exit")) {
						shutdown();
						reader.close();
					}
				}
			} catch (IOException e) {
			}
		});
		t.setName("Shutdown Thread");
		t.start();
	}

	private void startActivityThread() {
		this.activityloop = new Thread(() -> {
			long time = System.currentTimeMillis();

			int index = 0;

			while (true) {
				if (System.currentTimeMillis() >= time + 1000) {
					time = System.currentTimeMillis();

					shardManager.getShards().forEach(jda -> {
						String text = STATI[index].replaceAll("%members", "" + jda.getUsers().size());

						//jda.getPresence().setActivity(Activity.of(ActivityType.CUSTOM_STATUS, text));
						jda.getPresence().setActivity(Activity.playing(text));
					});
				}
			}

		});
		this.activityloop.setName("ActivityLoop");
		this.activityloop.start();
	}
	
	// ---------------------------Getters---------------------------
	public ShardManager getShardManager() {
		return shardManager;
	}

	public CommandManager getCmdManager() {
		return cmdManager;
	}
	
	public SQLite getSqlite() {
		return sqlite;
	}
}
