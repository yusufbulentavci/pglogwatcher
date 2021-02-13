package pglogwatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.opencsv.CSVReader;

public class LogCsvReader {

	static final Logger logger = LogManager.getLogger(LogCsvReader.class.getName());

	private File csvFile;
	private boolean keepRunning = true;
	private long _updateInterval = 1000;
	private long _filePointer;
	private LogTailListener tailer;

	protected int csvInd = 0;

	private File jsonFile;

	private int targetCsvInd;

	public LogCsvReader(File csvFile, File jsonFile, LogTailListener tailer) {
		this.csvFile = csvFile;
		this.jsonFile = jsonFile;
		this.tailer = tailer;
	}

//	public CSVRecord next() {
//		
//	}

	public void run() {
		try {

			if (jsonFile.exists()) {
				targetCsvInd = resumeToInd();
			}

			while (keepRunning) {
				Thread.sleep(_updateInterval);
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

						String[] dd = reader.readNext();
						while (dd != null) {
							if (csvInd == targetCsvInd) {
								this.tailer.appendCsv(this, dd);
								this.targetCsvInd++;
							}
							csvInd++;
							dd = reader.readNext();
						}

//						String line = null;
//						while ((line = raf.readLine()) != null) {
//							line=line.replaceAll(System.lineSeparator(), "\t");
//							System.out.println(line);
//							Reader in = new StringReader(line);
//							CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT);
//							List<CSVRecord> list = parser.getRecords();
//							for (CSVRecord csvRecord : list) {
//								this.tailer.appendCsv(this, csvRecord);
//							}
//						}
						_filePointer = raf.getFilePointer();
					}
				}
				this.tailer.beforeSleeping(this);
			}
		} catch (Exception e) {
			tailer.error(this, "Fatal error reading log file, log tailing has stopped.", e);
		}
		// dispose();
	}

	private int resumeToInd() {
		try (BufferedReader bf = new BufferedReader(new FileReader(jsonFile))) {
			String line = bf.readLine();
			while (line != null) {
				JSONObject jo = new JSONObject(line);
				targetCsvInd = jo.getInt("csv_ind");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public void terminate() {
		this.keepRunning = false;
	}

}
