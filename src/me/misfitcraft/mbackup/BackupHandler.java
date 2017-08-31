package me.misfitcraft.mbackup;

import java.util.List;
import java.util.TimeZone;
import java.util.Calendar;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.Base64;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

import me.misfitcraft.mbackup.net.HandlerFactory;
import me.misfitcraft.mbackup.net.NetHandler;

public class BackupHandler {
	public boolean isBackupRunning = false;
	public MBackup pluginInstance;
	private BackupThread backupThread;
	
	public BackupHandler(MBackup instance) {
		pluginInstance = instance;
	}

	public void startBackup() {
		
		if(!isBackupRunning) {
			isBackupRunning = true;
		}
		else
		{
			return;
		}
		
		pluginInstance.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "BACKUP STARTING! Expect " + ChatColor.RED + "lag.");
		
		pluginInstance.getServer().broadcastMessage(ChatColor.GREEN + "Saving worlds!");
		
		for (World w : pluginInstance.getServer().getWorlds()) {
			w.save();
		}
		
		pluginInstance.getServer().broadcastMessage(ChatColor.GREEN + "Generating backup file, you will not be able to modify the world.");
		
		//We NEED to do the reading on the main thread to avoid race conditions like the world being written to while we are reading
		
		List<String> files = pluginInstance.config.getStringList("files");
		List<String> folders = pluginInstance.config.getStringList("folders");
		List<String> flist = pluginInstance.config.getStringList("flist");
		
		String backupFormat = pluginInstance.config.getString("backupname");
		
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		
		backupFormat = backupFormat.replaceAll("%TIME%", c.get(Calendar.HOUR_OF_DAY) + "h" + c.get(Calendar.MINUTE) + "m" + "UTC")
		.replaceAll("%DATE%", c.get(Calendar.DAY_OF_MONTH) + "." + c.get(Calendar.MONTH) + "." + c.get(Calendar.YEAR));
		
		backupFormat = backupFormat + ".zip";
		
		File f = new File(backupFormat);
		
		try {
			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
			
			for(String list : flist) {
				File folder = new File(list);
				if(folder.isDirectory()) {
					ZipEntry ze = new ZipEntry(list + ".lst");
					String[] lFiles = folder.list();
					zos.putNextEntry(ze);
					StringBuilder sb = new StringBuilder();
					
					for(String s : lFiles) {
						sb.append(s + "\r\n");
					}
					
					zos.write(sb.toString().getBytes());
				}
			}
			
			for(String folder : folders) {
				File backupFolder = new File(folder);
				if(backupFolder.isDirectory()) {
					Files.walkFileTree(backupFolder.toPath(), new FileVisitor<Path>() {

						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
								throws IOException {
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							files.add(file.toString());
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
							return FileVisitResult.CONTINUE;
						}
						
					});
				}
			}
			
			byte[] nextBytes = new byte[512];
			int len = 0;
			
			for(String file : files){
				File backupFile = new File(file);
				if(backupFile.isFile()) {
					for (int i = 0; i < nextBytes.length; i++) {
						nextBytes[i] = 0;
					}
					ZipEntry ze = new ZipEntry(file);
					zos.putNextEntry(ze);
					
					BufferedInputStream br = new BufferedInputStream(new FileInputStream(backupFile), 4096);
					
					while((len = br.read(nextBytes, 0, 512)) != -1) {
						zos.write(nextBytes, 0, len);
					}
					
					zos.flush();
					br.close();
				}
			}
			
			zos.close();
			
			String checksum = getFileChecksum(f);
			File checkfile = new File(f.getName() + ".md5");
			
			FileWriter fw = new FileWriter(checkfile);
			fw.write(checksum);
			fw.close();
			
			pluginInstance.getServer().broadcastMessage(ChatColor.GREEN + "Backup file generated, starting file transfer! You can now play as normal, network lag possible.");
			
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		backupThread = new BackupThread(pluginInstance, backupFormat, pluginInstance.config.getStringList("dest"), pluginInstance.config.getString("ftpfolder"));
		
		pluginInstance.getServer().getScheduler().runTaskAsynchronously(pluginInstance, backupThread);
	}
	
	private static String getFileChecksum(File file) throws IOException, NoSuchAlgorithmException
	{
	    
	    DigestInputStream dis = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance("MD5"));
	    
	    byte[] byteArray = new byte[8192];
	    
	      
	    while(dis.read(byteArray) != -1) {
	    	continue;
	    }
	    
	    dis.close();
	    
	    return Base64.getEncoder().encodeToString(dis.getMessageDigest().digest());
	}
	
	private class BackupThread implements Runnable {
		
		MBackup inst;
		String backupFile;
		List<String> destinations;
		String backupFolder;
		
		public BackupThread(MBackup inst, String backupFile, List<String> destinations, String backupFolder) {
			this.inst = inst;
			this.backupFile = backupFile;
			this.destinations = destinations;
			this.backupFolder = backupFolder;
		}
		
		@Override
		public void run() {
			for(String dest : destinations) {
				NetHandler nh = HandlerFactory.getHandler(dest);
				
				nh.uploadFile(backupFolder, backupFile);
				nh.uploadFile(backupFolder, backupFile + ".md5");
				
				nh.close();
			}
			
			new File(backupFile + ".md5").delete();
			new File(backupFile).delete();
			
			inst.getServer().getScheduler().scheduleSyncDelayedTask(inst, new MessageCallable());
		}
		
	}
	
	private class MessageCallable implements Runnable {

		public void run() {
			isBackupRunning = false;
			Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "Backup finished!");
		}
	}
}
