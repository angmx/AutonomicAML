package autonomicAML;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import autonomicAML.bussinesLayer.Analyzer;
import autonomicAML.bussinesLayer.Monitor;
import autonomicAML.dataLayer.DAO_AutonomicLog;
import autonomicAML.dataLayer.DAO_AutonomicAML;
import autonomicAML.dataLayer.DAO_KB_CERPRO;

public class AutonomicAML {

	private static final Logger logger = Logger.getLogger(AutonomicAML.class);
	private static DAO_AutonomicAML daoAutonomicSystem;
	private static DAO_AutonomicLog daoAutonomicLog;
    private static DAO_KB_CERPRO daoKB;
    private static Monitor monitor;
    private static Analyzer analyzer;
    private static List<String> accountList;
    
  //Methods
    /**
     * This is the main method and executes the components
     * "Monitor,Analyser,Planner, and Executor"
     * @param args
     */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		createFolders();
        daoAutonomicLog = new DAO_AutonomicLog();
        daoAutonomicSystem = new DAO_AutonomicAML();
        daoKB = new DAO_KB_CERPRO(daoAutonomicSystem.getENDPOINT(),daoAutonomicSystem.getLastDateOfAnalyis());
        monitor = new Monitor(daoKB);
        analyzer = new Analyzer(daoKB);
        accountList = new ArrayList<>();
        start();
	}
	
	/**
     * This method start the sequence
     */
    public static void start() {
    	
    	monitor.insertInstance();
        //monitor.getAccounts(accountList);//obtienen las cuentas
    	System.out.println("AutonomicAML Begins");
        monitor.setInactiveAndDormantAccount();
        monitor.consolidateCutOffPeriodTransaction();
        monitor.consolidationProcessIdentification();
    	//monitor.consolidateChequeTransaction();
        
        System.out.println("Inference rules execution begins");
        analyzer.startInference();//infiere las reglas de negocio
        
    }//end method: start
    
    public static void createFolders(){
        String folders[] = {"PDF","log"};
        for(int i = 1 ; i < folders.length ; i++){
            try {
                Files.createDirectories(Paths.get(System.getProperty("user.dir") + "/AutonomicAML/" + folders[i]));
            } catch (IOException ex) {
                logger.warn("It was not possible to create folders");
            }
        }
    }

}
