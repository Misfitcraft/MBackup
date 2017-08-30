package me.misfitcraft.mbackup;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.misfitcraft.mbackup.net.HandlerFactory;
import me.misfitcraft.mbackup.net.NetHandler;

public class BackupScheduler {
	
	public double backupPeriod;
	
	public BackupScheduler(MBackup instance, BackupHandler handler) {
		this.backupPeriod = instance.config.getDouble("backuptime");
		instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, new Runnable() {

			@Override
			public void run() {
				String folder = instance.config.getString("ftpfolder");
				List<String> dests = instance.config.getStringList("dest");
				
				for (String s : dests) {
					NetHandler nhandler = HandlerFactory.getHandler(s);
					
					for (String file : nhandler.listFiles(folder)) {
						Date fileDate = new Date(nhandler.getFileAge(folder + "/" + file));
						Date now = new Date();
						Date cuttoff = new Date(now.getTime() - (int)(86400000 * instance.config.getDouble("deleteafter")));
						
						Pattern p = Pattern.compile("^" + instance.config.getString("backupname").replaceAll("%DATE%", "[0-9]*\\.[0-9]*\\.[0-9]*")
								.replaceAll("%TIME%", "[0-9]*h[0-9]*mUTC") + "\\....$");
						Matcher m = p.matcher(file);
						
						if(fileDate.before(cuttoff) && m.matches()) {
							nhandler.deleteFile(file);
						}
					}
				}
				
				handler.startBackup();
			}
			
		}, (int)(backupPeriod * 3600), (int)(backupPeriod * 3600));
	}
}
