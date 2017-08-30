package me.misfitcraft.mbackup.net;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.List;
import java.util.ArrayList;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

public class WebDAVHandler extends NetHandler {
	
	private Sardine sardine;
	private String URL;
	
	public WebDAVHandler(String URI) {
		super(URI);
		String[] URIComponents = URI.split("\\|");
		URL = URIComponents[0];
		sardine = SardineFactory.begin(URIComponents[1], URIComponents[2]);
	}

	@Override
	public boolean uploadFile(String folder, String path) {
		File f = new File(path);
		
		try {
			sardine.createDirectory(URL + "/" + folder);
			
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
			sardine.put(URL + "/" + folder + "/" + f.getName(), bis);
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	public List<String> listFiles(String folder) {
		try {
			List<DavResource> resources = sardine.list(URL + "/" + folder, 1);
			ArrayList<String> ret = new ArrayList<String>();
			
			for(DavResource r : resources)
			{
				ret.add(r.getName());
			}
			
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean deleteFile(String path) {
		try {
			sardine.delete(URL + path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public long getFileAge(String path) {
		try {
			if(sardine.exists(URL + path)) {
				List<DavResource> fileInfo = sardine.list(URL + path, 0);
				if(!fileInfo.isEmpty()) {
					return fileInfo.get(0).getCreation().getTime();
				}
				else
				{
					return 0;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void close() {
		sardine.shutdown();
	}
}
