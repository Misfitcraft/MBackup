package me.misfitcraft.mbackup.net;

import java.util.List;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;

public class FTPHandler extends NetHandler {
	
	protected FTPClient ftp;
	private boolean isLoggedIn = true;
	
	public FTPHandler(String URI) {
		super(URI);
		ftp = new FTPClient();
		
		String[] URIComponents = URI.split("\\|");
		
		try {
			InetAddress addr = InetAddress.getByName(URIComponents[3]);
			ftp.enterLocalPassiveMode();
			ftp.connect(addr, Integer.parseInt(URIComponents[4]));
			if(!ftp.login(URIComponents[1], URIComponents[2]))
			{
				isLoggedIn = false;
				System.out.println("FTP Login failed!");
			}
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean uploadFile(String folder, String path) {
		if(!isLoggedIn)
		{
			return false;
		}
		
		String[] dir = folder.split("/");
		
		try {
			for(String d : dir) {
				ftp.makeDirectory(d);
				ftp.changeWorkingDirectory(d);
			}
			
			File f = new File(path);
			
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
			
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			
			boolean ret = ftp.storeFile(f.getName(), bis);
			
			bis.close();
			
			ftp.changeWorkingDirectory("/");
			
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public List<String> listFiles(String folder) {
		try {
			ftp.changeWorkingDirectory(folder);
			ArrayList<String> ret = new ArrayList<String>();
			
			for(FTPFile f : ftp.listFiles()) {
				if(f.isFile())
				{
					ret.add(f.getName());
				}
			}
			
			ftp.changeWorkingDirectory("/");
			
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean deleteFile(String path) {
		try {
			ftp.deleteFile(path);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void close() {
		try {
			ftp.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public long getFileAge(String path) {
		try {
			String t = ftp.getModificationTime(path);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			Date time = sdf.parse(t.substring(t.indexOf(" ") + 1));
			
			return time.getTime();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
