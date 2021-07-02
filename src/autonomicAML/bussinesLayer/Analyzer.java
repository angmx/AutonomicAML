package autonomicAML.bussinesLayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.log4j.Logger;

import autonomicAML.*;
import autonomicAML.dataLayer.DAO_KB_CERPRO;

public class Analyzer {

	//Attributes
    private static Logger logger = Logger.getLogger(AutonomicAML.class.getName());
    private DAO_KB_CERPRO daoKB;
    private Model model,modelInf;
    private Reasoner reasoner;
    private InfModel inf;
    
 
    //Methods
    public Analyzer(DAO_KB_CERPRO daoKB) {
        this.daoKB = daoKB;
    }//end constructor Analyzer

    /**
     * Manda a llamar la funcion que hace inferencias sobre el modelo OAML
     */
    public void startInference() {
    	//System.out.println("manda a llamar el metodo que hace las inferencias");
    	//inferenceOfModel();
        inferenceOfSuspiciousAccount();
        //logger.info("Reasoning OAML Rules");
        //inferenceOfCandidacy();
        //logger.info("Reasoning candidacy patient");
    }

    /**
     * Hace inferencias en las instancias del modelo ontologico OAML utilizando las reglas SWRL escritas en OAMLRules.txt
     *  
     */
    private void inferenceOfSuspiciousAccount() {
    	//se crea un modelo de ModelFactory para almacenar el modelo ontologico del endpoint
        model = ModelFactory.createDefaultModel();
        model.read(daoKB.getEndPoint() + "data");
        System.out.println("Creating the ontology and instance model");
        
        //obtenemos el archivo de las reglas
        reasoner = new GenericRuleReasoner(Rule.rulesFromURL(System.getProperty("user.dir")+"/AutonomicAML/Rules/OAMLRules.txt"));
        System.out.println("Reading the Inference Rules file");
        
        //se activa en el registro del las inferencias para poder acceder a ellas
        reasoner.setDerivationLogging(true);
        
        //se construye un nuevo modelo inferido que se guardara en la variable inf
        inf = ModelFactory.createInfModel(reasoner, model);
        //obtiene el nuevo modelo inferido, es decir las tripletas resultantes de la inferencia
        modelInf = inf.getDeductionsModel();
        System.out.println("The inference execution is done");

        //se le da formato a las tripletas resultantes de la inferencia, en este caso el formato es turtle
        //y se guardan en out que es un StringWriter un flujo de salida
        String syntax = "Turtle";
        StringWriter out = new StringWriter();
        modelInf.write(out, syntax);
        
        //pasamos las tripletas con formato turtle a una cadena de caracteres
        String result = out.toString();
        System.out.println("Printing out the inferred triplets:\n"+result);
        
        //se reemplazan los true y false con formato RDF
        result = result.replaceAll("true", "\"true\"^^xsd:boolean");
        result = result.replaceAll("false", "\"false\"^^xsd:boolean");
        
        //guardar en un archivo de texto las inferencias del razonador
        
        insertInferedData("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\nINSERT DATA{"+result+"}\n");
        System.out.println("Inserting the inferred triplets into the KB");
        System.out.println("Inferences rules execution successfully done");
    }

    /**
     * Hace inferencias del modelo
     *  
     */
    private void inferenceOfModel() {
    	//se crea un modelo de ModelFactory para almacenar el modelo ontologico del endpoint
        Model model = ModelFactory.createDefaultModel();
        model.read(daoKB.getEndPoint() + "data");
        System.out.println("**OWL inference, create model");
        
        //obtenemos un razonador owl
        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
        
        //se activa en el registro del las inferencias para poder acceder a ellas
        reasoner.setDerivationLogging(true);
        System.out.println("**OWL tenemos el razonador ");
        
        reasoner = reasoner.bindSchema(model);
        //se construye un nuevo modelo inferido que se guardara en la variable inf
        InfModel inf = ModelFactory.createInfModel(reasoner, model);
        //obtiene el nuevo modelo inferido, es decir las tripletas resultantes de la inferencia
        modelInf = inf.getDeductionsModel();
        System.out.println("hizo la inferencia");

        //se le da formato a las tripletas resultantes de la inferencia, en este caso el formato es turtle
        //y se guardan en out que es un StringWriter un flujo de salida
        String syntax = "Turtle";
        StringWriter out = new StringWriter();
        modelInf.write(out, syntax);
        System.out.println("cambio formato de las tripletas a turtle");
        
        //pasamos las tripletas con formato turtle a una cadena de caracteres
        String result = out.toString();
        System.out.println("\n\nINFERENCIA Antes de tratar ****:"+result);
        
        //se reemplazan los true y false con formato RDF
        result = result.replaceAll("true", "\"true\"^^xsd:boolean");
        result = result.replaceAll("false", "\"false\"^^xsd:boolean");
        //insertInferedData("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\nINSERT DATA{"+result+"}\n");
        //System.out.println("\n\nINFERENCIA DESPUES de tratar ****:"+result);
    }
    
    private void insertInferedData(String data) {
        daoKB.update(data);
        //logger.info("Insert criteria infered");
    }
    
    public String readFile(InputStreamReader fileName) {

        try {
            BufferedReader br = new BufferedReader(fileName);
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }//end: while
            return sb.toString();
        } catch (IOException e) {
            logger.warn("It was impossible read file...");
            return "";
        }//end: try-catch

    }
}
