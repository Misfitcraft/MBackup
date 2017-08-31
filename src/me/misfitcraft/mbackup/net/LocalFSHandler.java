package me.misfitcraft.mbackup.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalFSHandler extends NetHandler {
	
	File localFile;
	
	public LocalFSHandler(String URI) {
		super(URI);
		localFile = new File(URI.split("://")[1]);
		localFile.mkdirs();
	}

	@Override
	public boolean uploadFile(String folder, String path) {
		File srcFile = new File(path);
		
		File destFile = new File(localFile.getPath() + File.separatorChar + srcFile.getName());
		
		try {
			if(localFile.isDirectory()) {
				BufferedInputStream br = new BufferedInputStream(new FileInputStream(srcFile), 4096);
				BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(destFile), 4096);
				
				byte[] nextBytes = new byte[512];
				int len;
				
				while((len = br.read(nextBytes, 0, 512)) != -1) {
					bo.write(nextBytes, 0, len);
				}
				
				bo.close();
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	public List<String> listFiles(String folder) {
		File[] files = localFile.listFiles();
		List<String> ret = new ArrayList<String>();
		
		for (File f : files) {
			ret.add(f.getName());
		}
		
		return ret;
	}

	@Override
	public boolean deleteFile(String path) {
		String[] components = path.split("/");
		String fileName = components[components.length -1];
		File f = new File(localFile.getPath() + fileName);
		f.delete();
		return true;
	}

	@Override
	public void close() {
		
	}

	@Override
	public long getFileAge(String path) {
		String[] components = path.split("/");
		String fileName = components[components.length -1];
		File f = new File(localFile.getPath() + fileName);
		
		return f.lastModified();
	}
}
