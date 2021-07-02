package autonomicAML.dataLayer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import autonomicAML.AutonomicAML;

public class DAO_AutonomicAML {

	private static Logger logger = Logger.getLogger(AutonomicAML.class.getName());
    private SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private String today;
    private final String file = System.getProperty("user.dir") + "/../AutonomicAML.txt";
    private String data[];
    
    public DAO_AutonomicAML() {
        this.today = fmt.format(new Date());
        retrieveData();
    }
    
    private void retrieveData() {
        String file = readFile();//lee el archivo autonomicSystem.txt donde se encuentra los datos del ultimo analisis y el ENDPOINT
        data = file.split("\n");//separa la cadena por el caracter \n y se mete en el arreglo data
    }
    
    private String readFile() {
        try {
            FileReader fr = new FileReader(file);//abre el archivo
            BufferedReader br = new BufferedReader(fr);//lo mete en un buffer
            StringBuilder sb = new StringBuilder();//construye una cadena que posteriormente se formateara
            String line = null;
            try {
                line = br.readLine();//lee el archivo autonomicSystem.txt
            
	            while (line != null) {
	                sb.append(line);//agrega el texto de autonomicSystem.txt
	                sb.append("\n");//agrega un salto de linea al final del documento
	                    line = br.readLine();
	                
	            }//end: while
	            br.close();
            } catch (IOException ex) {
                logger.warn("It was no possible to read the line...");
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            File archivo = new File(file);
            BufferedWriter bw;
            if (archivo.exists()) {
                logger.warn("The dateFile exists, check out!...");
            } else {
                try {
                    bw = new BufferedWriter(new FileWriter(archivo));
                    bw.write(String.format("Last date of Analysis:%n%s%nSPARQL EndPoint:%n%s%n", fmt.format(new Date()), "http://liim.izt.uam.mx:8080/fuseki/dsAMIF-AML_SuspiciousDetection_v2_3/"));
                    logger.warn("The dataFile doesn't exist, and it was created!...");
                    bw.close();
                    readFile();
                } catch (IOException ex) {
                    logger.warn("It was no possible to write the line...");
                }
            }
            return "";
        }//end: try-catch//end: try-catch

    }
    
    private void writeFile() {
        File file = new File(this.file);
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.write(String.format("Last date of Analysis:%n%s%nSPARQL EndPoint:%n%s%n", data[1], data[3]));
            logger.info("Autonomic AML file was update...");
        } catch (IOException e) {
            logger.error("It was impossible open the sensor's file...");
        }
        
    }
    
    public String getLastDateOfAnalyis() {
        return data[1];
    }
    
    public String getENDPOINT() {
        return data[3];
    }
    
    public void setLastDateOfAnalysis() {
        data[1] = today;
        writeFile();
    }
}
