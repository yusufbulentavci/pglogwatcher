package pglogwatcher;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class PgConnection {
	static final Logger logger = LogManager.getLogger(PgConnection.class.getName());

	public PgConnection(LogWriter logWriter, LogLine ll) {
		this.logWriter = logWriter;
		sessionId = ll.session_id;
	}

	LogWriter logWriter;
	String sessionId;
	int virtualSessionId = 0;
	int sessionCount;
	long line;
	long parseDur;
	long bindDur;
	String query;
	private LogLine bind;
	private LogLine parse;
	private LogLine command;
	private int pbccPattern = 0;
	private String bindDetail;

	public void process(LogLine ll) {
		ll.virtual_session_id = this.virtualSessionId;
		if (ll.error_severity.equals("LOG") && ll.command_tag != null) {

			if (ll.command_tag.equals("DISCARD ALL")) {
				virtualSessionId++;
				resetPbcc(false);
				return;
			}

			if (pbccPattern == 0 && ll.command_tag.equals("PARSE")) {
				this.parse = ll;
				this.pbccPattern = 1;
				return;
			} else if (pbccPattern == 1 && ll.command_tag.equals("BIND")) {
				this.bind = ll;
				this.bindDetail = ll.detail;
				this.pbccPattern = 2;
				return;
			} else if (pbccPattern == 2) {
//				&& ll.command_tag != null
//						&& (ll.command_tag.equals("SET") || ll.command_tag.equals("SELECT")
//								|| ll.command_tag.equals("BEGIN") || ll.command_tag.equals("UPDATE")
//								|| ll.command_tag.equals("COMMIT") || ll.command_tag.equals("UPDATE")
//								|| ll.command_tag.equals("idle"))
				this.command = ll;
				this.pbccPattern = 3;
				return;
			} else if (pbccPattern == 3 && ll.command_tag.equals(command.command_tag)) {
				ll.updateDur(bind.getDuration(), parse.getDuration(), command.message, bindDetail);
				resetPbcc(true);
			} else {
				resetPbcc(false);
			}
		} else {
			resetPbcc(false);
		}

		logWriter.write(ll);
	}

	public void resetPbcc(boolean suc) {
		if (this.pbccPattern == 0)
			return;
		this.pbccPattern = 0;
		if (!suc) {
			if (bind != null) {
				logWriter.write(bind);
			}
			if (parse != null)
				logWriter.write(parse);
			if (command != null)
				logWriter.write(command);
		}
		this.parseDur = 0;
		this.bindDur = 0;
		this.bindDetail=null;
		this.bind = null;
		this.parse = null;
		this.command = null;
	}

}
