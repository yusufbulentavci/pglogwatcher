package pglogwatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import net.nbug.hexprobe.server.telnet.EasyTerminal;

public class LogCsvReader {

	static final Logger logger = LogManager.getLogger(LogCsvReader.class.getName());

	private File csvFile;
	private boolean keepRunning = true;
	private long _updateInterval = 1000;
	private long _filePointer;
	private LogTailListener tailer;

	protected int csvInd = -1;

	private File jsonFile;

	private int targetCsvInd;
	private String status;

	public LogCsvReader(File csvFile, File jsonFile, LogTailListener tailer) {
		this.csvFile = csvFile;
		this.jsonFile = jsonFile;
		this.tailer = tailer;
		status = "init";
	}

	public void run() {
		try {
			status = "run";

			if (jsonFile.exists()) {
				status = "resume";
				resumeToInd();
			}

			while (keepRunning) {
				try {
					status = "wait tail";
					Thread.sleep(_updateInterval);
				} catch (Exception e) {
					continue;
				}
				status = "run";
				long len = csvFile.length();

				if (len < _filePointer) {
					// Log must have been jibbled or deleted.
					this.tailer.fileErrorModified(this);
					_filePointer = len;
				} else if (len > _filePointer) {
					// File must have had something added to it!
					try (RandomAccessFile raf = new RandomAccessFile(csvFile, "r");) {
						raf.seek(_filePointer);

						InputStream is = Channels.newInputStream(raf.getChannel());
						CSVReader reader = new CSVReader(new InputStreamReader(is));

						String[] dd = readCsvLine(raf, reader);
						while (dd != null) {
							if (csvInd >= targetCsvInd) {
								this.tailer.appendCsv(this, dd);
								this.targetCsvInd = csvInd;
							}
							dd = readCsvLine(raf, reader);
						}

					}
				}
				this.tailer.beforeSleeping(this);
			}
		} catch (Exception e) {
			tailer.error(this, "Fatal error reading log file, log tailing has stopped.", e);
		}
		// dispose();
	}

	protected String[] readCsvLine(RandomAccessFile raf, CSVReader reader) {

		String[] ret = null;
		for (int i = 0; i < 1000 && keepRunning; i++) {
			try {
				ret = reader.readNext();
				csvInd++;
				_filePointer = raf.getFilePointer();
				return ret;
			} catch (CsvValidationException | IOException e) {
				try {
					raf.seek(_filePointer);
				} catch (IOException e1) {
				}
			}
		}
		return ret;

	}

	private void resumeToInd() {
		try (BufferedReader bf = new BufferedReader(new FileReader(jsonFile))) {
			int jind = 1;
			String line = bf.readLine();
			while (line != null) {
				try {
					JSONObject jo = new JSONObject(line);
					targetCsvInd = jo.getInt("csv_ind") + 1;
					line = bf.readLine();
					jind++;
				} catch (JSONException je) {
					logger.error("Error json parsing at file:" + jsonFile + " line:" + jind + " txt:" + line, je);
					throw je;
				}
			}
		} catch (IOException e) {
			targetCsvInd = 0;
			logger.error("Resuming failed after csv_ind:" + targetCsvInd, e);
		}
	}

	public void terminate() {
		this.keepRunning = false;
		status = "terminate";
	}

	public void status(EasyTerminal terminal) throws IOException {
		terminal.writeLine("CsvReader, ind:" + csvInd);
		terminal.writeLine("targetInd:" + targetCsvInd);
		terminal.writeLine("status:" + status);
	}

}
