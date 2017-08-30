package me.misfitcraft.mbackup;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import me.misfitcraft.mbackup.commands.BackupExecutor;

public class MBackup extends JavaPlugin {
	
	public FileConfiguration config;
	public BackupHandler backupHandler;
	public BackupScheduler backupScheduler;
	
	public boolean isBackupRunning = false; //We NEVER want to run more than one backup at once for obvious reasons
	
	@Override
	public void onLoad() {
		saveDefaultConfig();
		config = getConfig();
		backupHandler = new BackupHandler(this);
	}
	
	@Override
	public void onEnable() {
		BackupExecutor be = new BackupExecutor(this);
		backupScheduler = new BackupScheduler(this, backupHandler);
		getCommand("backup").setExecutor(be);
		getCommand("mbackup").setExecutor(be);
	}
}
