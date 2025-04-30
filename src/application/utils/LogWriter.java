package application.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import application.Config;

public class LogWriter {
	public static void writeLog(String logMessage) {
		String logFilePath = Config.APP_PATH + File.separator + "DigiSignLog.log";
		File logFile = new File(logFilePath);

        try {
            // Create the log file if it doesn't exist
            if (logFile.createNewFile()) {
            	
                //System.out.println("Log file created at: " + logFilePath);
            	
                
            } else {
                //System.out.println("Log file already exists at: " + logFilePath);
            }

            // Write to the log file
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write("[" + java.time.LocalDateTime.now() + "] "+logMessage+".\n");
                // Add more log entries as needed
            }

        } catch (IOException e) {
            System.err.println("An error occurred while writing to the log file."+e.getMessage());
            e.printStackTrace();
        }
	}
}
