package autonomicAML.dataLayer;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

public class DAO_AutonomicLog {

	final String logName = "autonomicLog.log";
    final String propertiesName = System.getProperty("user.dir")+"/Autonomic System/Properties/logProperties.properties";
    
    public DAO_AutonomicLog(){
        //createLogProperties();
        //configLog();
    }

    private void createLogProperties() {
        String content = "# Root logger option\n"
                + "log4j.rootLogger=INFO, file, stdout\n"
                + "\n"
                + "# Direct log messages to a log file\n"
                + "log4j.appender.file=org.apache.log4j.RollingFileAppender\n"
                + "log4j.appender.file.File=" +  logName + "\n"
                + "log4j.appender.file.MaxFileSize=10MB\n"
                + "log4j.appender.file.MaxBackupIndex=1\n"
                + "log4j.appender.file.layout=org.apache.log4j.PatternLayout\n"
                + "log4j.appender.file.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n\n"
                + "\n"
                + "# Direct log messages to stdout\n"
                + "log4j.appender.stdout=org.apache.log4j.ConsoleAppender\n"
                + "log4j.appender.stdout.Target=System.out\n"
                + "log4j.appender.stdout.layout=org.apache.log4j.PatternLayout\n"
                + "log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n";
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(propertiesName));
            writer.write(content);
            writer.close();
        }catch(IOException ex){
            System.err.println("Error, it was not possible to write file");
        }
    }
    
    private void configLog(){
        Properties p = new Properties();
        try {
            p.load(new FileInputStream(propertiesName));
            PropertyConfigurator.configure(p);
        } catch (FileNotFoundException ex ) {
            System.err.println("Error, it was not possible to read properties");
        } catch ( IOException ex){
            System.err.println("Error, it was not possible to read file");
        }
           
    }
    
}
