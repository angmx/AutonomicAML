# AutonomicAML
Regulatory Compliance Financial Fraud Detection

# Description:
  This software was created to support the business rules of the thesis project "Intelligent model for the detection of money laundering".

* The software inserts instances to test the responses of the SPARQL queries to the competency questions.

* The software activates the business rule inferences defined by the development team with the general purpose inference language JENA.

* The software executes SPARQL queries and inserts instances to be able to execute the business rules defined by the work team.

# Organization:
**The software is divided into 3 layers:**

***autonomicAML:*** Layer where the main method is located, it consists of a single class "AutonomicAML.java" where all the necessary variables are initialized and the necessary methods are executed both for the insertion of instances and the execution of business rules.

***autonomicAML.bussinesLayer:*** This is the business layer, this layer defines all the business rules that the software has to execute both to insert the instances and to execute the business rules, it consists of two classes:

> Analyzer: This class executes the business rules or the inferences about the ontological model and the model instances.

> Monitor: This class inserts the instances to test the SPARQL queries that answer the competency questions, it also executes SPARQL queries that support the inference of the ontological model

***autonomicAML.dataLayer:*** This is the data layer, it is used to insert data to the ontology in the ENDPOINT and obtain data from the ontology in the ENDPOINT, it consists of 3 classes:

> DAO_AutonomicAML: In this class the ENDPOINT data is initialized on the last date and time of analysis.

> DAO_AutonomicLog: Class used to write a record of everything that happens in the software.

> DAO_KB_CERPRO: Class used to query the ENPOINT and to insert data from either SPARQL queries or triples obtained from the reasoning of the general purpose JENA rules.
----
  The software also has a folder called “Insert_queries” where all the insert triples used to test the SPARQL tests that answer the competency questions are located.

  In the "files" folder are the SPARQL queries that are used to obtain triples and process this data that is later used to execute the business rules.

  In the folder "AutonomicAML / Rules" are the business rules defined in the general purpose rules language JENA, the rules defined in this folder are the ones used to make the inference.

  We can also find in the root the file "AutonomicAML.txt" in this file the ENPOINT used to obtain the model triplets is defined, as well as the insertions of new triples, the definition of the triples is in:

SPARQL end point:          |
---------------------------|
<< here goes the ENPOINT >>|

CQs to be populated (Format: CQ<<ID>>, where id is the CQ number, eg. CQ8_2. The query's file name MUST be the name indicated here):
