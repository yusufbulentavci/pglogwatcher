package pglogwatcher;


public interface LogTailListener {

	void error(LogCsvReader logCsvReader, String string, Exception e);

	void fileErrorModified(LogCsvReader logCsvReader);

	void appendCsv(LogCsvReader logCsvReader, String[] dd);

	void beforeSleeping(LogCsvReader logCsvReader);

}
