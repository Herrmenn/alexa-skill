/**
 * <h1> Execute all Person Classes at once <h1>
 * 
 * @author Eric
 * @version 1.0
 * @since 11.08.18
 *
 */
public class InsertPersons {	
	/**
	 * @param args contains db credentials
	 */
	public static void main(String[] args) {
		
		String dbInfo[] = {args[0], args[1], args[2]};
			InsertCoreDataIngwiWiwi.main(dbInfo);
			InsertCoreDataSowiAub.main(dbInfo);
					
	} //main
} // class