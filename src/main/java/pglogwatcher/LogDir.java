package pglogwatcher;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class LogDir implements Runnable {
	static final Logger logger = LogManager.getLogger(LogDir.class.getName());

	File logDir;
	Set<String> commandTags = new HashSet<String>();

	String processCsvFileName = null;
//	boolean isToTail = false;

	private String dir;

	private boolean taken;

	private int switchFileCount;

	private int sleepForTailCount;

	private LogFile logFile;

	private boolean alive=true;

	public LogDir(ConfDir dir, int switchFileCount, int sleepForTailCount) {
		this.dir = dir.getPath();
		this.switchFileCount = switchFileCount;
		this.sleepForTailCount = sleepForTailCount;
	}

	protected LogDir(String string, int switchFileCount2, int sleepForTailCount2) {
		this(new ConfDir(string), switchFileCount2, sleepForTailCount2);
	}

	protected void init(String dir) {
		logDir = new File(dir);

	}

	@Override
	public void run() {
		logger.info("Starting:" + dir);
		init(dir);
		while (this.alive) {
			if (!switchFile(true, false)) {
				if (switchFileCount != -1 && switchFileCount == 0) {
					logger.info("Dont wait, leaving...." + this.switchFileCount);
					break;
				}

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				continue;
			}
			if (processCsvFileName != null) {

				logger.info("Working on:" + processCsvFileName);
				this.logFile = new LogFile(logDir, processCsvFileName, sleepForTailCount);
				this.logFile.process(new DirState() {

					@Override
					public boolean checkRollover() {
						return LogDir.this.switchFile(false, logFile.error);
					}
				});
				logger.info("Ends working on:" + processCsvFileName);
			}
		}
		logger.info("Logdir terminating gracefully:"+this.dir);
	}

	public boolean switchFile(boolean makeChange, boolean error) {
		String[] allCsvs = logDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (!name.startsWith("postgres") || !name.endsWith(".csv")) {
					return false;
				}
				if(logFile!=null && logFile.csvFile.equals(name)) {
					return false;
				}
				return true;
//				File json = new File(dir, name + ".json");
//				return !json.exists();
			}
		});
		List<String> csvs = new ArrayList<String>();
		for (String string : allCsvs) {
			csvs.add(string);
		}
		Collections.sort(csvs);

		if (csvs.size() == 0) {
			return false;
		}

		if (processCsvFileName != null && csvs.get(0).equals(processCsvFileName)) {
			logger.info("Json file is not created yet; continue with same file:" + processCsvFileName);
			return false;
		}
		if (makeChange) {
			processCsvFileName = csvs.get(0);
//			isToTail = (csvs.size() == 1);
			this.taken = false;
			if (logFile != null)
				logFile.done();
		}
		if (switchFileCount > 0)
			switchFileCount--;
		return true;
	}

	public void terminate() {
		this.alive=false;
		if(logFile!=null)
			logFile.terminate();
	}

}
