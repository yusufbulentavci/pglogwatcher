package pglogwatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class LogWriter {
	static final Logger logger = LogManager.getLogger(LogWriter.class.getName());

	final File file;

	private FileWriter fileWriter;

//	private File fileTmp;

	private String jsonFileName;

	private File csvFile;

	private File csvFileDone;

	public LogWriter(File dir, String jsonFileName, String csvFileName) {
		this.jsonFileName = jsonFileName;
		this.file = new File(dir, jsonFileName);
		this.csvFile = new File(dir, csvFileName);
		this.csvFileDone = new File(dir, csvFileName + "-done");

		if (this.file.exists()) {
			Integer resumeInd = resumeIndex();
			if (resumeInd == null || resumeInd == 0) {
				this.file.delete();
			}
		}
//		
//		this.fileTmp = new File(dir,fnTmp);
		try {
			this.fileWriter = new FileWriter(file, true);
		} catch (IOException e) {
			logger.error("Unexpected log writer error:" + file.getPath(), e);
			throw new RuntimeException(e);
		}

	}

	private Integer resumeIndex() {
		try (BufferedReader br = new BufferedReader(new FileReader(file));) {

			String lastLine = br.readLine();
			String prevLine = null;
			while (lastLine != null) {
				prevLine = lastLine.length()==0?prevLine:lastLine;
				lastLine = br.readLine();
			}
			if (prevLine == null) {
				return 0;
			}
			
			JSONObject jo = new JSONObject(prevLine);

			return jo.optInt("log-ind");
		} catch (IOException e) {
			logger.error("Failed resume index", e);
			return null;
		}
	}

	public void write(LogLine ll) {
//		logger.info(ll.toJson(fn).toString());
		try {
			this.fileWriter.write(ll.toJson(jsonFileName).toString());
			this.fileWriter.write(System.lineSeparator());

			if (logger.isDebugEnabled()) {
				logger.debug(ll.toString());
				logger.debug(ll.toJson(jsonFileName));
			}
		} catch (IOException e) {
			logger.error("Error in line:" + ll.toString(), e);
		}
	}

	public void close() {
		logger.info("LogFile close:" + csvFile);

		try {
			this.fileWriter.close();
		} catch (IOException e) {
			logger.error("Failed logfile done; file:" + csvFile.getPath(), e);
		}

	}

	public void done() {
		this.csvFile.renameTo(csvFileDone);
		logger.info("LogFile done/renamed:" + csvFile);
	}

}
