package me.misfitcraft.mbackup.net;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandlerFactory {
	/*Lazy implementation but fine for internal use when the config is properly configured*/
	
	static final String[] regexes = {"^ftp\\|.*\\|.*\\|.+\\|[0-9]+$", "file:\\/\\/.+", "^https?\\:\\/\\/.+\\|.*\\|.*$"};
	
	public static NetHandler getHandler(String connectionString) {
		
		for (int i = 0; i < regexes.length; i++) {
			Pattern p = Pattern.compile(regexes[i]);
			Matcher m = p.matcher(connectionString);
			
			if (m.matches()) {
				switch(i) {
					case 0:
						return new FTPHandler(connectionString);
					case 1:
						return new LocalFSHandler(connectionString);
					case 2:
						return new WebDAVHandler(connectionString);
				}
			}
		}
		
		return null;
	}
}
