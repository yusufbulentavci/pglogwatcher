package pglogwatcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Hello world!
 *
 */
public class App {
	static final Logger logger = LogManager.getLogger(App.class.getName());

	public static void main(String[] args) {
//		args=new String[] {"--dir=ali", "--dir=veli"};
		try {
			logger.info("Application start:" + new Date().toString());
			List<String> dirs=new ArrayList<>();
			for (String string : args) {
				if(string.startsWith("--dir=")) {
					dirs.add(string.substring("--dir=".length()));
				}
			}
			logger.info("Directory destinations:"+dirs.toString());
			
			for (String string : dirs) {
				LogDir logDir=new LogDir(string, -1, -1);
				logDir.run();
			}
			
//			System.out.println(dirs);
			logger.info("Application ended successfully:" + new Date().toString());
		} catch (Exception e) {
			logger.error("Application ended with error:" + new Date().toString(), e);
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
