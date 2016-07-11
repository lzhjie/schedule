package org.lzhjie.schedule;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Schedule {

	public static void main(String[] args) {
		try {
			new XmlConfigLoader();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("main abort", e);
		}
	}
	
	static final Logger logger = Logger.getLogger(Schedule.class);
	static {
		 PropertyConfigurator.configure(Schedule.class.getResourceAsStream("/log4j.properties"));
	}
}
