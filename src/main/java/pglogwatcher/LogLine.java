package pglogwatcher;

import java.math.BigDecimal;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class LogLine {
	static final Logger logger = LogManager.getLogger(LogLine.class.getName());

	String log_time;
	String user_name;
	String database_name;
	Integer process_id;
	String connection_from;
	String session_id;
	Long session_line_num;
	String command_tag;
	String session_start_time;
	String virtual_transaction_id;
	Long transaction_id;
	String error_severity;
	String sql_state_code;
	String message;
	String detail;
	String hint;
	String internal_query;
	Integer internal_query_pos;
	String context;
	String query;
	Integer query_pos;
	String location;
	String application_name;
	private BigDecimal duration;
	private BigDecimal bindDur;
	private BigDecimal parseDur;
	Integer virtual_session_id;

	Integer csvInd;

	String tz;
	Boolean unix_socket;

	private String connection_from_port;

	private String bindDetail;

	public LogLine(Integer csvInd, String error_severity, String command_tag, String message) {
		this.csvInd = csvInd;
		this.error_severity = error_severity;
		this.command_tag = command_tag;
		this.message = message;
	}

	// 2021-02-08 10:31:38.693
	// yyyy-MM-dd HH:mm:ss.SSS
//	static SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	// Feb 10, 2021 @ 14:43:57.058
	// MMM dd, YYYY @ HH:mm:ss.SSS
	// YYYY-MM-ddTHH:mm:ss.SSSZ
//	static SimpleDateFormat gener =
//			new SimpleDateFormat("YYYY-MM-ddTHH:mm:ss.SSSZ", Locale.US);
	public LogLine(int csvInd, String[] record) {
		this.csvInd = csvInd;
		{
			String[] dd = record[0].split(" ");
			this.log_time = nul(dd[0] + "T" + dd[1] + "Z");
			this.tz = dd[2];
		}
//		String llmen = nul(record[0]+" "+record[1));
//		try {
////			Date parsedDate = parser.parse(llmen);
////			log_time=gener.format(parsedDate);
//		} catch (ParseException e) {
//			logger.error("Failed to parse, regen date:"+llmen, e);
//		}

		user_name = nul(record[1]);
		database_name = nul(record[2]);
		process_id = parseInt(record[3]);
		connection_from = nul(record[4]);
		if (connection_from != null) {
			if (connection_from.equals("[local]")) {
				unix_socket = true;
			} else {
				String[] dd = connection_from.split(":");
				connection_from = dd[0];
				if (dd.length > 1) {
					connection_from_port = dd[1];
				} else {
					connection_from_port = "0";
				}
			}
		}
		session_id = nul(record[5]);
		session_line_num = parseLong(record[6]);
		command_tag = nul(record[7]);
		session_start_time = nul(record[8]);
		virtual_transaction_id = nul(record[9]);
		transaction_id = parseLong(record[10]);
		error_severity = nul(record[11]);
		sql_state_code = nul(record[12]);
		message = nul(record[13]);
		detail = nul(record[14]);
		hint = nul(record[15]);
		internal_query = nul(record[16]);
		internal_query_pos = parseInt(record[17]);
		context = nul(record[18]);
		query = nul(record[19]);
		query_pos = parseInt(record[20]);
		location = nul(record[21]);
		application_name = nul(record[22]);

		if (message != null && message.startsWith("duration:")) {
//			message.indexOf(" )
			this.duration = parseDuration(message);
			// System.out.println(message+"===>"+duration);

		}
	}

	private BigDecimal parseDuration(String smp) {
//		String smp = "\"message\":\"duration: 0.25 ms dsfafd";
		try {
			String a = smp.substring(10);
			int firstSpace = a.indexOf(' ');
			if (firstSpace < 0) {
				System.err.println("-1");
				return null;
			}
			String time = a.substring(0, firstSpace);
			BigDecimal bt = new BigDecimal(time);
			bt.setScale(4);
			BigDecimal sec = durInSec(bt);

			String aa = a.substring(firstSpace + 1);
			if (!aa.startsWith("ms")) {
				System.err.println("-2");
				return null;
			}
//			int nextSpace = aa.indexOf(' ');
//			if (nextSpace < 0) {
//				System.err.println("-2"+aa+"-");
//				return null;
//			}
//			String unit = aa.substring(0, nextSpace);
//			if (!unit.equals("ms")) {
//				System.err.println("-3");
//				return null;
//			}
//    	System.out.println(time);
//    	System.out.println(aa);
//    	System.out.println(unit);
//    	System.out.println(msec);
			return sec;
		} catch (Exception e) {
			System.err.println(smp);
			e.printStackTrace();

			return null;
		}
	}

	private String nul(String string) {
		if (string == null || string.trim().length() == 0)
			return null;
		return string;
	}

	private Integer parseInt(String string) {
		if (string == null || string.trim().length() == 0)
			return null;
		try {
			return Integer.parseInt(string);
		} catch (Exception e) {
			System.err.println(string);
//			return null;
			throw new RuntimeException(string);
		}
	}

	private Long parseLong(String string) {
		if (string == null)
			return null;
		return Long.parseLong(string);
	}

	JSONObject toJson(String fileName) {
		JSONObject ret = new JSONObject();
		if (log_time != null)
			ret.put("@timestamp", log_time);
		if (this.user_name != null)
			ret.put("postgresql.log.user", this.user_name);
		if (this.database_name != null)
			ret.put("postgresql.log.database", this.database_name);
		if (this.process_id != null)
			ret.put("process.pid", this.process_id);
		if (this.connection_from != null) {
			ret.put("client.ip", this.connection_from);
			ret.put("client.port", this.connection_from_port);
		}
		if (this.session_id != null)
			ret.put("session_id", this.session_id);
		if (this.session_line_num != null)
			ret.put("session_line_num", this.session_line_num);
		if (this.command_tag != null)
			ret.put("command_tag", this.command_tag);
		if (this.session_start_time != null)
			ret.put("session_start_time", this.session_start_time);
		if (this.transaction_id != null)
			ret.put("transaction_id", this.transaction_id);
		if (this.error_severity != null)
			ret.put("postgresql.log.level", this.error_severity);
		if (this.sql_state_code != null)
			ret.put("sql_state_code", this.sql_state_code);
		if (this.message != null)
			ret.put("postgresql.log.message", this.message);
		if (this.detail != null)
			ret.put("detail", this.detail);
		if (this.hint != null)
			ret.put("hint", this.hint);
		if (this.internal_query != null)
			ret.put("internal_query", this.internal_query);
		if (this.internal_query_pos != null)
			ret.put("internal_query_pos", this.internal_query_pos);
		if (this.context != null)
			ret.put("context", this.context);
		if (this.query != null)
			ret.put("query", this.query);
		if (this.query_pos != null)
			ret.put("query_pos", this.query_pos);
		if (this.location != null)
			ret.put("location", this.location);
		if (this.application_name != null)
			ret.put("application_name", this.application_name);

		if (this.duration != null) {
//			System.out.println("->>>>"+duration.doubleValue());
			ret.put("duration", duration.doubleValue());
		}

		if (this.bindDur != null) {
			ret.put("bind_duration", bindDur.doubleValue());
		}
		if(this.bindDetail!=null) {
			ret.put("parameters", this.bindDetail);
		}
		if (this.parseDur != null) {
			ret.put("parse_duration", parseDur.doubleValue());
		}
		if (this.virtual_session_id != null) {
			ret.put("virtual_session_id", virtual_session_id);
		}
		if (this.unix_socket != null) {
			ret.put("unix_socket", unix_socket);
		}

		if (tz != null)
			ret.put("postgresql.log.timezone", tz);

		if (csvInd != null)
			ret.put("csv_ind", csvInd);

		ret.put("csv", fileName);

		return ret;
	}

	public void updateDur(BigDecimal bind, BigDecimal parse, String message, String bindDetail) {
		this.bindDur = bind;
		this.parseDur = parse;
		if (duration == null) {
			duration = new BigDecimal(0);
			duration.setScale(4);
		}
		if (bindDur != null)
			duration.add(bindDur);
		if (parseDur != null)
			duration.add(parseDur);

//		duration = durInSec(duration);

		this.message = message;
		this.bindDetail = bindDetail;
	}

	static BigDecimal bin = new BigDecimal(1000);
	static {
		bin.setScale(4);
	}

	protected BigDecimal durInSec(BigDecimal dur) {
		return dur.divide(bin);
	}

	public BigDecimal getDuration() {
		return duration;
	}

	@Override
	public String toString() {
		return "LogLine [log_time=" + log_time + ", user_name=" + user_name + ", database_name=" + database_name
				+ ", process_id=" + process_id + ", connection_from=" + connection_from + ", session_id=" + session_id
				+ ", session_line_num=" + session_line_num + ", command_tag=" + command_tag + ", session_start_time="
				+ session_start_time + ", virtual_transaction_id=" + virtual_transaction_id + ", transaction_id="
				+ transaction_id + ", error_severity=" + error_severity + ", sql_state_code=" + sql_state_code
				+ ", message=" + message + ", detail=" + detail + ", hint=" + hint + ", internal_query="
				+ internal_query + ", internal_query_pos=" + internal_query_pos + ", context=" + context + ", query="
				+ query + ", query_pos=" + query_pos + ", location=" + location + ", application_name="
				+ application_name + ", duration=" + duration + ", bindDur=" + bindDur + ", parseDur=" + parseDur
				+ ", virtual_session_id=" + virtual_session_id + ", csvInd=" + csvInd + ", tz=" + tz + ", unix_socket="
				+ unix_socket + ", connection_from_port=" + connection_from_port + "]";
	}

}
