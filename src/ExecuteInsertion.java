/**
 * <h1> Execute Classes at once <h1>
 * Executes selected classes by arguments. If there is no argument 
 * all classes will be executed.
 * 
 * @author Eric, Alex
 * @version 1.0
 * @since 11.08.18
 *
 */
public class ExecuteInsertion {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	
//	static final String DB_URL = "jdbc:mysql://localhost/alexa";
	// Database credentials for local sql 
//	static final String USER = "root";
//	static final String PASS = "";
	
	static final String USER = "insert_alexa";
	static final String PASS = "!HTW_alexa18#Xgh98";
	
	static final String DB_URL = "jdbc:mysql://134.96.217.36/alexa";

	
	/**
	 * MAIN METHOD
	 * @param args contains classes which will be executed, if empty execute all
	 * @param dbInfo contains db credentials and it will be passed to the main methods of the classes
	 */
	public static void main(String[] args) {
		String dbInfo[] = {DB_URL, USER, PASS};
		
		if(args.length == 0) {
			InsertDatesSemester.main(dbInfo);
			InsertPersons.main(dbInfo);
			InsertModule.main(dbInfo);	
		}
		
		for (int i = 0; i < args.length; i++) {
			
			if(args[i].equals("Semester")){
				InsertDatesSemester.main(dbInfo);
			}
			if(args[i].equals("Module")){
				InsertModule.main(dbInfo);
			}
			if(args[i].equals("Personen")){
				InsertPersons.main(dbInfo);
			}
		}		
	} //main
} // class