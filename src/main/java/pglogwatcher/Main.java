package pglogwatcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.nbug.hexprobe.server.telnet.EasyTerminal;

/**
 * Hello world!
 *
 */
public class Main {
	static final Logger logger = LogManager.getLogger(Main.class.getName());
	private List<LogDir> runningDirs=new ArrayList<>();
	private List<Thread> runningThreads=new ArrayList<>();

	private static Main one;
	public static void main(String[] args) {
		one = new Main();
		one.mainIn(args);
	}
	
	public static Main one() {
		return one;
	}
	public void status(EasyTerminal terminal) throws IOException {
		for (LogDir it : runningDirs) {
			it.status(terminal);
		}
	}
	
	protected void shutdownHook() {
		logger.info("Shutdown signal detected");
        for (LogDir logDir : runningDirs) {
			logDir.terminate();
		}
        for(Thread t: runningThreads) {
        	t.interrupt();
        }
        Telnet.terminate();
	}
	
	public void mainIn(String[] args)  {
		one=this;
		try {
			Telnet.start();
		} catch (IOException e1) {
			logger.error("Failed to start telnet server", e1);
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                shutdownHook();
            }
			
        });
		
//		args=new String[] {"--dir=ali", "--dir=veli"};
		try {
			logger.info("Application start:" + new Date().toString());
			List<ConfDir> dirs=new ArrayList<>();
			File iniFile=new File("/etc/pglogwatcher.ini");
			if(iniFile.exists()) {
				try {
					Ini ini=new Ini("/etc/pglogwatcher.ini");
					dirs.addAll(ini.getDirs());
				}catch(Exception e) {
					logger.error("Failed to obtaion configuration from file:/etc/pglogwatcher.ini", e);
				}
			}
			
			for (String string : args) {
				if(string.startsWith("--dir=")) {
					dirs.add(new ConfDir(string.substring("--dir=".length())));
				}
			}
			logger.info("Directory destinations:"+dirs.toString());
			
			for (ConfDir conf : dirs) {
				LogDir logDir=new LogDir(conf, -1, -1);
				runningDirs.add(logDir);
				Thread t=new Thread(logDir);
				runningThreads.add(t);
				t.start();
			}
			
//			System.out.println(dirs);
			logger.info("Application start completed successfully:" + new Date().toString());
		} catch (Exception e) {
			logger.error("Application start ended with error:" + new Date().toString(), e);
			shutdownHook();
		}

//    	String smp = "duration: 0.25 ms dsfafd";
//    	String a = smp.substring(10);
//    	int firstSpace = a.indexOf(' ');
//    	String time = a.substring(0, firstSpace);
//    	Double msec=Double.parseDouble(time);
//    	String aa=a.substring(firstSpace+1);
//    	int nextSpace=aa.indexOf(' ');
//    	String unit=aa.substring(0, nextSpace);
//    	System.out.println(time);
//    	System.out.println(aa);
//    	System.out.println(unit);
//    	System.out.println(msec);
//        String fileName = "c:\\test\\csv\\country.csv";
//        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
//            List<String[]> r = reader.readAll();
//            r.forEach(x -> System.out.println(Arrays.toString(x)));
//        }
	}
}
