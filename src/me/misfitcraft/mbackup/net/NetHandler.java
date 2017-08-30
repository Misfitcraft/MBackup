package me.misfitcraft.mbackup.net;

import java.util.List;

public abstract class NetHandler {
	
	protected String URI;
	
	public abstract boolean uploadFile(String folder, String path);
	public abstract List<String> listFiles(String folder);
	public abstract boolean deleteFile(String path);
	public abstract void close();
	public abstract long getFileAge(String path);
	
	public NetHandler(String URI)
	{
		this.URI = URI;
	}
}
