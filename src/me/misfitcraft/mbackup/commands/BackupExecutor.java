package me.misfitcraft.mbackup.commands;

import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.misfitcraft.mbackup.MBackup;

public class BackupExecutor implements CommandExecutor {
	
	MBackup pluginInstance;
	
	public BackupExecutor(MBackup mBackup) {
		this.pluginInstance = mBackup;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if(alias.equalsIgnoreCase("backup") || alias.equalsIgnoreCase("mbackup"))
		{
			if(!sender.hasPermission("mbackup.backup"))
			{
				sender.sendMessage(Color.RED + "I'm sorry but you don't have permission to back up this server!");
				return true;
			}
			
			if(pluginInstance.backupHandler.isBackupRunning)
			{
				sender.sendMessage("Backup is already running!");
			}
			else
			{
				pluginInstance.backupHandler.startBackup();
				sender.sendMessage("Backup started!");
			}
		}
		return true;
	}

}
