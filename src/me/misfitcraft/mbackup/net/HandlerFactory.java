package me.misfitcraft.mbackup.net;

public class HandlerFactory {
	/*Lazy implementation but fine for internal use when the config is properly configured*/
	public static NetHandler getHandler(String connectionString) {
		if(connectionString.split("\\|")[0].equalsIgnoreCase("ftp")) {
			return new FTPHandler(connectionString);
		} else {
			switch (connectionString.split("://")[0]) {
			case "http":
				return new WebDAVHandler(connectionString);
			case "https":
				return new WebDAVHandler(connectionString);
			case "file":
				return new LocalFSHandler(connectionString);
			default:
				return null;
			}
		}
	}
}
