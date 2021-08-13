package autonomicAML.bussinesLayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import autonomicAML.*;
import autonomicAML.dataLayer.DAO_KB_CERPRO;

public class Monitor {

	// Attributes
		private static Logger logger = Logger.getLogger(AutonomicAML.class.getName());
		private DAO_KB_CERPRO daoKB;
		private SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		// Methods
		public Monitor(DAO_KB_CERPRO daoKB) {
			this.daoKB = daoKB;
		}// end constructor Monitor

		/**
		 * Obtiene todas las cuentas existentes en la base de conocimiento
		 * @param patientList lista donde se insertaran las cuentas recuperadas de la
		 *                    base de conocimiento
		 */
		public void getAccounts(List<String> accountList) {
			daoKB.retrieveAccount(accountList);
			logger.info("Retrieve: " + accountList.size() + " Accounts");
		}
		
		/**
	     * Marca lascuentas dormidas o inactivas
	     * Una cuenta inactiva no ha tenido transacciones en 3 meses
	     * Una cuenta inactiva no ha tenido transacciones en 6 meses
	     */
	    public void setInactiveAndDormantAccount() {
	    	//guardamos los tipos de hibernacion par ala cuenta
	    	HashMap<String, Integer> account_type = new HashMap<String, Integer>();
	    	account_type.put("Inactive", 3);
	    	account_type.put("Dormant", 6);
	    	
	    	for(String typ_acct : account_type.keySet()) {
		    	//obtenemos la fecha segun el tipo de cuenta que queramos detectar
		    	Calendar cal = Calendar.getInstance();
		    	cal.add(Calendar.MONTH, -account_type.get(typ_acct));
		    	
		    	String date = fmt.format(cal.getTime());
		    	
		    	//obtenemos las cuetas que no has tenido actividad 
		    	List<String> inct_accnt = daoKB.getNoTransactionAccount(date);
		    	
		    	//mandamos las cuentas que seran cuentas inactivas
		    	daoKB.setInactvieAndDormantAccount(inct_accnt, typ_acct);
	    	}
	    }

		/**
		 * Consolida las transacciones en la base de conocimiento desde el inicio del
		 * periodo de corte hasta el momento en que se mando a llamar al metodo. Es
		 * decir, suma todas las transacciones de retiro desde el inicio de periodo de
		 * corte hasta el dia que se manda a ejecutar este metodo. Lo mismo hace para
		 * las transacciones de deposito.
		 */
		public void consolidateCutOffPeriodTransaction() {
			// obtenemos la fecha de inicio de corte, en este caso será el primer de cada
			// mes
			String dateCO = fmt.format(new Date());
			// cambiamos la fecha para que sea el primer del mes
			dateCO = dateCO.substring(0, 8) + "01T00:00:00";

			dateCO = "2020-03-01T00:00:00";// valor de prueba
			// iteramos entre los tipos de transaccion
			String transactionType[] = { "Credit", "Debit" };
			int trans_t = 0;

			while (trans_t < transactionType.length) {
				
				// obtiene las cuentas con la suma de transacciones en el periodo de corte
				HashMap<String, ArrayList<ArrayList<String>>> account_sum = daoKB.getAccountsWithSumOfTransactionsInCutOffPeriod(transactionType[trans_t], dateCO);

				// obtenemos las cuentas con su respectivo grafo para guardar la suma acumulada
				Map<String, String> period_sum_class = daoKB.getCutOffPeriodSumClass();

				// eliminamos las tripletas existentes
				daoKB.deleteSumOfTransactionsInCutOffPeriod(transactionType[trans_t]);

				//agregamos las nuevas sumas calculadas del periodo de corte
				daoKB.setAccountsWithSumOfTransactionsInCutOffPeriod(account_sum, period_sum_class, transactionType[trans_t]);
				trans_t++;
			}
			//logger.info("Consolido las transacciones");
		}
		
		/**
		 * Verifica si existen procesos de consolidacion y ejecuta la regla de negocio que verifica si hubo un retiro mayor
		 * a 15000 y tiene un proceso de consolidacion.
		 */
		public void consolidationProcessIdentification() {
			// obtenemos la fecha de inicio de corte, en este caso será el primer de cada
			// mes
			String dateCO = fmt.format(new Date());
			// cambiamos la fecha para que sea el primer del mes
			dateCO = dateCO.substring(0, 8) + "01T00:00:00";

			dateCO = "2020-03-01T00:00:00";// valor de prueba
				
			//marca todas las cuetas que tienen un proceso de consolidacion
			consolidationProcess(dateCO);
		}
		
		/**
		 * Verifica que existan depositos de cuentas exteriores pero del mismo cliente por mas de 15000
		 * 
		 * @param start_date la fecha donde inicia el periodo de verificacion
		 */
		private void consolidationProcess(String start_date) {
			// obtiene las cuentas con proceso de consolidacion
			HashMap<String, java.lang.Double> accounts_with_ConsolidationProcess = daoKB.getAccountWithConsolidationProcess(start_date);

			//se guardan las cuentas con sus respectiva tripleta de proceso de consolidación
			daoKB.setAccountWhitConsolidationProcess(accounts_with_ConsolidationProcess, start_date);
			
			//logger.info("Consolidation Process");
			
		}
		
		/**
		 * Metodo para insertar todas las instancias necesarias para ejecutar el protocolo de prueba
		 */
		public void insertInstance() {
			//leemos de la carpeta donde se encuentran los queries para obtener los nombres de los archivos
			//ArrayList<String> nomArch = daoKB.getFileNames("../Insert_queries");
			ArrayList<String> nomArch = daoKB.getFileNames();
			
			for(int i=0;i<nomArch.size(); i++) {
				System.out.println(nomArch.get(i));
				
			}
			//iteramos cada nombre de archivo para insertarlo
			if (nomArch == null || nomArch.size() == 0) {
			    System.out.println("No exist file for the insertion");
			    return;
			}
			else {
			    for (int i=0; i< nomArch.size(); i++) {
			        //System.out.println("/src/Insert_queries/"+nomArch.get(i));
			        daoKB.insertInstance(nomArch.get(i));
			    }
			}
		}
		/**
		 * Consolida las transacciones en cheque en la base de conocimiento desde el inicio del
		 * periodo de corte hasta el momento en que se manda a llamar al metodo. Es
		 * decir, suma todas las transacciones de deposito en cheque desde el inicio de periodo de
		 * corte hasta el dia que se manda a ejecutar este metodo.
		 */
		/*public void consolidateChequeTransaction() {
			// obtenemos la fecha de inicio de corte, en este caso será el primer de cada
			// mes
			String dateCO = fmt.format(new Date());
			// cambiamos la fecha para que sea el primer del mes
			dateCO = dateCO.substring(0, 8) + "01T00:00:00";

			dateCO = "2020-02-01T00:00:00";// valor de prueba
				
			// obtiene las cuentas con la suma de transacciones en el periodo de corte
			Map<String, java.lang.Double> account_sum = daoKB.getAccountsWithSumOfChequeTransactions(dateCO);

			// obtenemos las cuentas con su respectivo grafo para guardar la suma acumulada
			Map<String, String> period_sum_class = daoKB.getCutOffPeriodSumClass();

			// eliminamos las tripletas existentes
			daoKB.deleteSumOfTransactionsInCutOffPeriod("ChequeCredit");

			//agregamos las nuevas sumas calculadas del periodo de corte
			//daoKB.setAccountsWithSumOfTransactionsInCutOffPeriod(account_sum, period_sum_class, "ChequeCredit");
			
			logger.info("Consolido las transacciones en cheque");
		}*/
		
}
