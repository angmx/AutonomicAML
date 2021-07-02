package autonomicAML.dataLayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.log4j.Logger;

import autonomicAML.*;

public class DAO_KB_CERPRO {

	private String ENDPOINT;
	private String lastDateOfAnalysis;
	private String today;
	private SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private SimpleDateFormat st = new SimpleDateFormat("yyyy-MM-dd 'at' HH-mm-ss");
	private RDFConnection cnx;
	private static Logger logger = Logger.getLogger(AutonomicAML.class.getName());
	private Date dateToday;
	InputStream is;
	InputStreamReader ir;
	
	public DAO_KB_CERPRO(String ENDPOINT, String lastDateOfAnalysis) {
		this.ENDPOINT = ENDPOINT;
		this.lastDateOfAnalysis = lastDateOfAnalysis;
		this.dateToday = new Date();
		this.today = fmt.format(dateToday);
		cnx = RDFConnectionFactory.connect(this.ENDPOINT + "query", this.ENDPOINT + "update", this.ENDPOINT + "data");
		//cnx = RDFConnectionFactory.connect(this.ENDPOINT + "query", this.ENDPOINT + "update", this.ENDPOINT + "data");
	}

	/**
	 * Recupera todas la cuentas de la base de conocimiento, la consulta la obtiene
	 * de qRetrieveAccounts.txt
	 * 
	 * @param accountList lista de cadena de caracteres donde se insertaran todas
	 *                    las cuentas existentes en la base d econocimiento
	 */
	public void retrieveAccount(List<String> accountList) {
		// lee el archivo de donde se obtiene la consulta para la ontologia
		is = getClass().getResourceAsStream("/files/qRetrieveAccounts.txt");
		ir = new InputStreamReader(is);// lee el archivo como un flujo de datos
		String queryString = readFile(ir);// convertimos el flujo de datos a una cadena

		// ejecuta el query e inserta en la lista accountList las instancias obtenidas
		List<QuerySolution> list = ResultSetFormatter.toList(cnx.query(queryString).execSelect());// ejecutamos el query
		for (int i = 0; i < list.size(); i++) {
			accountList.add(list.get(i).getResource("account").getLocalName());// obtenemos el recurso, pues es una
																				// instancia
		}
	}
	
	/**
	 * Regresa las cuenta squ eno han tenido transacciones desde la fecha dada hasta la actual
	 * @param date La fecha desde la que se buscara las falta de transacciones
	 * @return Una lista con las cuentas que no han tenido transacciones 
	 */
	public List<String> getNoTransactionAccount(String date){
		is = getClass().getResourceAsStream("/files/qRetrieveNoTransactionAccount.txt");
		ir = new InputStreamReader(is);// lee el archivo como un flujo de datos
		// insertamos la fecha a partir de la cual se van a recuperar las transacciones
		// y
		// el tipo de transaccion que se quiere recuperar
		String queryString = String.format(readFile(ir), date);
		// ejecutamos el query SPARQL
		List<QuerySolution> list = ResultSetFormatter.toList(cnx.query(queryString).execSelect());
		// guardamos las cuentas y el monto sumado en una estructura de datos
		List<String> inactive_acnt = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			inactive_acnt.add(list.get(i).getResource("account").getLocalName());
		}
		
		return inactive_acnt;
	}
	
	/**
	 * 
	 * Crea la tripleta para marcar que cuentas son inactivas
	 * @param inactive_account Una List con todas las cuentas inactivas
	 * @param account_type si se marcara como una cuenta inactiva o dormida
	 */
	public void setInactvieAndDormantAccount(List<String> inactive_account, String account_type) {
		// creamos el query de insercion de las sumas
		String bodyQuery = "";
		for(int i=0; i<inactive_account.size(); i++) {
			bodyQuery = bodyQuery+"  AMIF:"+inactive_account.get(i)+" AMIF:is"+account_type+"Account \"true\"^^xsd:boolean.\n";
		}

		is = getClass().getResourceAsStream("/files/qInsertInactiveAndDormantAccount.txt");
		ir = new InputStreamReader(is);
		String queryString = String.format(readFile(ir), bodyQuery);
		
		// guardamos las cuentas inactivas
		update(queryString);
	}

	/**
	 * Regresa las cuentas que han tenido transacciones en el ultimo periodo de cortes.
	 * @param transaction_type El tipo de transacción que se quiere recuperar (Debit o Credit)
	 * @param cutoff_period el periodo de corte donde se quieren recuperar
	 * @return Una Hashmap que tiene clave: cuenta, valor: la suma de las transacciones desde el 
	 * 		inicio del periodo de corte hasta el dia que se manda a llamar el metodo
	 */
	public HashMap<String, ArrayList<ArrayList<String>>> getAccountsWithSumOfTransactionsInCutOffPeriod(String transaction_type, String cutoff_period){
		is = getClass().getResourceAsStream("/files/qRetrieveAccountsWithSumOfTransactionsInCutOffPeriod.txt");
		ir = new InputStreamReader(is);// lee el archivo como un flujo de datos
		// insertamos la fecha a partir de la cual se van a recuperar las transacciones
		// y
		// el tipo de transaccion que se quiere recuperar
		String queryString = String.format(readFile(ir), transaction_type, cutoff_period);
		//System.out.println("query1:\n"+queryString);
		// ejecutamos el query SPARQL
		List<QuerySolution> list = ResultSetFormatter.toList(cnx.query(queryString).execSelect());
		// guardamos las cuentas y el monto sumado en una estructura de datos
		Map<String, ArrayList<ArrayList<String>>> account_sum = new HashMap<String, ArrayList<ArrayList<String>>>();
		String accnt;
		for (int i = 0; i < list.size(); i++) {
			accnt = list.get(i).getResource("account").getLocalName();
			
			ArrayList<String> graph_result = new ArrayList<String>();
			graph_result.add(list.get(i).getResource("pay_meth").getLocalName());
			graph_result.add(String.valueOf(list.get(i).getLiteral("sum_mont_money").getDouble()));
			
			if(account_sum.containsKey(accnt)) {
				ArrayList<ArrayList<String>> graph_accnt = account_sum.get(accnt);
				graph_accnt.add(graph_result);
				account_sum.put(accnt,graph_accnt);
			}else {
				ArrayList<ArrayList<String>> graph_accnt = new ArrayList<ArrayList<String>>();
				graph_accnt.add(graph_result);
				account_sum.put(accnt, graph_accnt);
			}
		}
		return (HashMap<String, ArrayList<ArrayList<String>>>) account_sum;
	}

	/**
	 * Regrera la cuenta con la suma del periodo de corte correspondiente "CutOffPeriodSum".
	 * @return Un HashMap que costa de clave:cuenta, valor: clase CutOffPeriodSum correspondiente a la cuenta 
	 */
	public HashMap<String, String> getCutOffPeriodSumClass(){
		is = getClass().getResourceAsStream("/files/qRetrieveCutOffPeriodSumClass.txt");
		ir = new InputStreamReader(is);
		String queryString = readFile(ir);
		List<QuerySolution> listR = ResultSetFormatter.toList(cnx.query(queryString).execSelect());
		// guardamos la cuenta y el cutoff sum
		Map<String, String> period_sum_class = new HashMap<String, String>();
		for (int i = 0; i < listR.size(); i++) {
			period_sum_class.put(listR.get(i).getResource("account").getLocalName(),
					listR.get(i).getResource("cutoff_period_sum").getLocalName());
		}
		
		return (HashMap<String, String>)period_sum_class;
	}
	
	/**
	 * Elmina todas todas la sumas de transacciones existentes.
	 */
	public void deleteSumOfTransactionsInCutOffPeriod(String transaction_type) {
		is = getClass().getResourceAsStream("/files/qDeleteSumOfTransactionsInCutOffPeriod.txt");
		ir = new InputStreamReader(is);
		String queryString = String.format(readFile(ir), transaction_type, transaction_type);
		
		update(queryString);
	}
	
	/**
	 * Inserta las sumas de las transacciones de la clase CutOffPeriodSum.
	 * @param account_sum HashMap donde clave: cuenta, valor: la suma de las transacciones.
	 * @param period_sum_class HashMap donde clave:cuenta, valor: clase CutOffPeriodSum correspondiente a la cuenta.
	 * @param transaction_type El tipo de transaccion que se va a insertar
	 */
	public void setAccountsWithSumOfTransactionsInCutOffPeriod(HashMap<String, ArrayList<ArrayList<String>>>  account_sum, Map<String, String> period_sum_class, String transaction_type) {
		// creamos el query de insercion de las sumas
		String bodyQuery = "";
		
		Iterator<String> it_acnt_sum = account_sum.keySet().iterator();
		while (it_acnt_sum.hasNext()) {
			String accnt = it_acnt_sum.next();
			ArrayList<ArrayList<String>> graph = account_sum.get(accnt);
			
			bodyQuery = bodyQuery+"\n"+createTriples(period_sum_class.get(accnt), graph,transaction_type);
		}

		is = getClass().getResourceAsStream("/files/qUpdateAccountsWithSumOfTransactionsInCutOffPeriod.txt");
		ir = new InputStreamReader(is);
		String queryString = String.format(readFile(ir), bodyQuery);
		//System.out.println("QUERY:  \n"+queryString);
		// guardamos las sumas acumuladas de los usuarios
		update(queryString);
	}
	
	private String createTriples(String period_sum_class, ArrayList<ArrayList<String>> graph, String trans_type) {
		String pay_met[] = { "Cash", "Cheque", "Electronic"};
		String bodyQuery = "";
				
		//agregamos por cada tipo de pago  (electornico, cash, cheque)
		for(int i=0; i< graph.size(); i++) {
			
			int i_pm = 0;
			while(i_pm < pay_met.length) {
				ArrayList<String> i_graph = graph.get(i);
				//checamos si existe la palabra dentro del metodo de pago y si es el tipo de transaccion
				if(i_graph.get(0).contains(pay_met[i_pm])) {
					bodyQuery = bodyQuery + "AMIF:"+period_sum_class+" AMIF:has"+pay_met[i_pm]+trans_type+"Sum \""+i_graph.get(1)+"\"^^xsd:decimal.\n  ";
				}
				i_pm++;
			}
		}
		
		return bodyQuery;
	}
	
	
	public void setAccountWhitConsolidationProcess(HashMap<String, java.lang.Double>  accounts, String start_date) {
		// creamos el query de insercion de las sumas
		String bodyQuery = "";
		int cont = 0;
		Iterator<String> it_acnt_sum = accounts.keySet().iterator();
		while (it_acnt_sum.hasNext()) {
			String accnt = it_acnt_sum.next();
			double credit_sum = accounts.get(accnt);
			
			String num_accnt= accnt.substring(19,21);//esto es hardCode hay que sustituir la extracion de la cadena de manera dinamica
			System.out.println("SUBCADENA: "+num_accnt);
			String client = accnt.substring(22);
			System.out.println("cliente: "+client);
			String consProc_inst = "AMIF-ConsolidationProcess-A"+num_accnt+"-"+client+"-"+cont++;
			
			bodyQuery = bodyQuery+"\t"+"AMIF:"+accnt+" AMIF:hasConsolidationProcess "+"AMIF:"+consProc_inst+".\n"+
								"\t"+"AMIF:"+consProc_inst+" AMIF:hasDateOfDetection \""+start_date+"\"^^xsd:dateTime.\n"+
								"\t"+"AMIF:"+consProc_inst+" AMIF:hasCreditSum \""+credit_sum+"\"^^xsd:decimal.\n";
		}

		is = getClass().getResourceAsStream("/files/qInsert.txt");
		ir = new InputStreamReader(is);
		String queryString = String.format(readFile(ir), bodyQuery);
		//System.out.println("QUERY:  \n"+queryString);
		// guardamos las sumas acumuladas de los usuarios
		update(queryString);
	}
	
	/**
	 * Regresa las cuentas que han tenido transacciones en el ultimo periodo de cortes.
	 * @param transaction_type El tipo de transacción que se quiere recuperar (Debit o Credit)
	 * @param cutoff_period el periodo de corte donde se quieren recuperar
	 * @return Una Hashmap que tiene clave: cuenta, valor: un grafo con los siguientes valores: pay_met | trans_cat | sum_mont_money
	 */
	public HashMap<String, java.lang.Double> getAccountsWithSumOfChequeTransactions(String cutoff_period){
		is = getClass().getResourceAsStream("/files/qRetrieveChequeTransaction.txt");
		ir = new InputStreamReader(is);// lee el archivo como un flujo de datos
		// insertamos la fecha a partir de la cual se van a recuperar las transacciones
		// y
		// el tipo de transaccion que se quiere recuperar
		String queryString = String.format(readFile(ir), cutoff_period);
		// ejecutamos el query SPARQL
		List<QuerySolution> list = ResultSetFormatter.toList(cnx.query(queryString).execSelect());
		// guardamos las cuentas y el monto sumado en una estructura de datos
		Map<String, java.lang.Double> account_sum = new HashMap<String, java.lang.Double>();
		for (int i = 0; i < list.size(); i++) {
			account_sum.put(list.get(i).getResource("account").getLocalName(),
					list.get(i).getLiteral("sum_mont_money").getDouble());
		}
		return (HashMap<String, Double>) account_sum;
	}
	
	/**
	 * Ejecuta un query SPARQL para obtener las cuetas que tiene un proceso de consolidacion
	 * @param start_date fecha inicio del periodo de corte
	 * @return un diccionario echo de <Cuenta, Suma de depositos>
	 */
	public HashMap<String, java.lang.Double> getAccountWithConsolidationProcess(String start_date){
		//obtiene el queri de proceso de consolidacion
		InputStream is = getClass().getResourceAsStream("/files/qConsolidationProcess.txt");
		InputStreamReader ir = new InputStreamReader(is);// lee el archivo como un flujo de datos
		// insertamos la fecha a partir de la cual se van a recuperar las transacciones
		// y
		// el tipo de transaccion que se quiere recuperar
		String queryString = String.format(readFile(ir), start_date);
		// ejecutamos el query SPARQL
		List<QuerySolution> query_result_list = ResultSetFormatter.toList(cnx.query(queryString).execSelect());
		// guardamos las cuentas y el monto sumado en una estructura de datos
		Map<String, java.lang.Double> account_sum = new HashMap<String, java.lang.Double>();
		for (int i = 0; i < query_result_list.size(); i++) {
			//del elemento i-esimo de la lista devuelve solo el objeto Account, se utiliza getResource por que Account es una instancia de clase
			//del elemento i-esimo de la lista devuelve solo la literal Credits_sum, se utiliza getLiteral por que Credits_sum NO es una instancia de clase
			account_sum.put(query_result_list.get(i).getResource("Account").getLocalName(),
					query_result_list.get(i).getLiteral("Credits_sum").getDouble());
		}
		return (HashMap<String, Double>) account_sum;
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
			} // end: while
			return sb.toString();
		} catch (IOException e) {
			logger.warn("It was impossible read file...");
			return "";
		} // end: try-catch

	}// fin del mÃ©todo: readFile

	public String getEndPoint() {
		return ENDPOINT;
	}

	public void update(String query) {
		cnx.update(query);
		cnx.commit();
	}

	public void close() {
		cnx.close();
	}

	public String getDate() {
		return st.format(dateToday);
	}
}
