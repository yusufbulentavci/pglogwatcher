package pglogwatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Ini {
	static final Logger logger = LogManager.getLogger(Ini.class.getName());

	private IniFile file;
	private List<ConfDir> dirs = new ArrayList<>();

	public Ini(String fn) throws IOException {
		this.file=new IniFile(fn);
		
		for(int i=0; i<100; i++) {
			String section="dir-"+i;
			if(!file.containsSection(section))
				continue;
			String path=file.getString(section, "path", null);
			if(path==null) {
				logger.error("Configuration path is missing in section:"+section);
				continue;
			}
			ConfDir cd=new ConfDir(path);
			getDirs().add(cd);
		}
		
	}

	public List<ConfDir> getDirs() {
		return dirs;
	}

}
