package pglogwatcher;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class LogFile {
	static final Logger logger = LogManager.getLogger(LogFile.class.getName());

	private File f;
	private Map<String, PgConnection> connections = new HashMap<>();
	private LogWriter logWriter;

	int csvLine = 0;

	String csvFile;
	private String jsonFile;

	private File logDir;

	private int sleepForTailCount;

	protected boolean error=false;

	public LogFile(File logDir, String csvFile, int sleepForTailCount) {
		this.logDir = logDir;
		this.csvFile = csvFile;
		this.jsonFile = csvFile + ".json";
		this.sleepForTailCount = sleepForTailCount;
	}

	void process(DirState dirState) {

		this.f = new File(logDir, csvFile);
		this.logWriter = new LogWriter(logDir, jsonFile, csvFile);

		LogCsvReader reader = new LogCsvReader(f, new File(jsonFile), new LogTailListener() {
			PgConnection using = null;

			@Override
			public void beforeSleeping(LogCsvReader logCsvReader) {
				if (sleepForTailCount == 0) {
					logCsvReader.terminate();
					return;
				}

				if (dirState.checkRollover()) {
					logCsvReader.terminate();
					return;
				}
				sleepForTailCount--;
			}

			@Override
			public void fileErrorModified(LogCsvReader logCsvReader) {
				logger.error("RR");
				LogFile.this.error = true;
				LogLine ll=new LogLine(logCsvReader.csvInd, "ERROR", "LOG_CSV_PARSE", "csv file modified");
				processNoSession(ll);
			}

			@Override
			public void error(LogCsvReader logCsvReader, String string, Exception e) {
				logger.error("RR:" + string, e);
				LogFile.this.error = true;
				LogLine ll=new LogLine(logCsvReader.csvInd, "ERROR", "LOG_CSV_PARSE", string);
				processNoSession(ll);
			}

			@Override
			public void appendCsv(LogCsvReader logCsvReader, String[] dd) {
				LogLine ll = new LogLine(logCsvReader.csvInd, dd);
				if (ll.session_id != null) {
					ll.session_id = ll.session_id.trim();
					if (ll.session_id.length() > 0) {
						if (using == null || !using.sessionId.equals(ll.session_id)) {
							using = connections.get(ll.session_id);
						}
						if (using == null) {
							using = new PgConnection(logWriter, ll);
							connections.put(ll.session_id, using);
						}
						using.process(ll);
						return;
					}
				}
				processNoSession(ll);
			}
		});
		reader.run();
		processEndOfFile();

//		File jf = new File(logDir, jsonFile);

//		try (CSVReader reader = new CSVReader(new FileReader(f))) {
//			while(true) {
//				try {
//					csvLine++;
//					String[] fields=reader.readNext();
//					if(fields==null) {
//						processEndOfFile();
//						break;
//					}
////					System.out.println(Arrays.toString(fields));
//					LogLine ll=new LogLine(fields);
//					if(ll.session_id!=null) {
//						ll.session_id=ll.session_id.trim();
//						if(ll.session_id.length()>0) {
//							if(using==null || !using.sessionId.equals(ll.session_id)) {
//								using=connections.get(ll.session_id);
//							}
//							if(using==null) {
//								using=new PgConnection(logWriter, ll);
//								connections.put(ll.session_id, using);
//							}
//							using.process(ll);
//							continue;
//						}
//					}
//					processNoSession(ll);
//					
//				} catch (Exception e) {
//					logger.error("Error while processing file:"+f.toString(), e);
//				}
//				
//			}
//			
//		}
	}

	private void processEndOfFile() {
		for (PgConnection c : connections.values()) {
			c.resetPbcc(false);
		}
		logWriter.close();
	}

	private void processNoSession(LogLine ll) {
		logWriter.write(ll);
	}

	public void done() {
		logWriter.done();
	}

}
