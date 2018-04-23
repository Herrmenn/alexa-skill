import java.io.IOException;

import org.jsoup.*;
import org.jsoup.helper.*;
import org.jsoup.internal.*;
import org.jsoup.nodes.*;
import org.jsoup.parser.*;
import org.jsoup.safety.*;
import org.jsoup.select.*;

import java.sql.*; // for standard JDBC programs
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.math.*; // for BigDecimal and BigInteger support

/**
 * 
 * @author Herrmann, Morlo Uebernimmt Vorname, Nachname, E-Mail, URL aller
 *         Angestellten im Studienbereich Ingenieurwissenschaften, Wirtschaftswissenschaften in die
 *         Datenbank
 *
 */
public class InsertDatesSemester {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/alexa";

	// Database credentials
	static final String USER = "root";
	static final String PASS = "";

	// Person Archive
	static final String SEMESTER_DATES_ARCHIVE =  "https://www.htwsaar.de/studium/organisation/semestertermine";

	/*
	 * MAIN METHOD
	 */
	public static void main(String[] args) {
		
		long timeBefore = System.currentTimeMillis();

		Connection conn = null;
		Statement stmt = null;
		int count = 0;

		try {

			// STEP 2: Register JDBC driver
			//Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			//System.out.println("Connecting to a selected database...");
		//	conn = DriverManager.getConnection(DB_URL, USER, PASS);
		//	System.out.println("Connected database successfully...");

			// STEP 4: Execute a query
		//	System.out.println("Inserting records into the table...\n");
		//	stmt = conn.createStatement();

			// GET PERSONS FROM WEBSITE
			try {


					// Persons
					Document persArchive = Jsoup.connect(SEMESTER_DATES_ARCHIVE).get();
					
					Elements infoBlocks = persArchive.select("table.listing");
				
					for (Element block: infoBlocks) {
											
						//Elements tableElements = block.children();
						String semester_start    = block.getElementsContainingOwnText("Beginn des").next().text();
						String lectures_start    = block.getElementsContainingOwnText("Beginn der").next().text();
						String lectures_end      = block.getElementsContainingOwnText("Ende der").next().text();
						String semster_end       = block.getElementsContainingOwnText("Ende des").next().text();
						String lecture_free_time = block.getElementsContainingOwnText("Vorlesungsfreie").next().text();
						String closing = "";
						
						//List<Node> allSiblings = block.childNodes();
						//System.out.println(allSiblings);
						
						
						// Weitere Infos suchen 
					//	List<Element> nextSiblings = block.siblingElements().subList(
					//		block.get,
					//		block.siblingElements().size()
					//	);
					//System.out.println(nextSiblings);
						System.out.println(block.nextElementSibling().getElementsContainingOwnText("Schlieﬂtage"));
						//System.out.println(block.getElementsContainingOwnText("Schlieﬂtage").);
						
					//	System.out.println(block.getElementsContainingOwnText("Vorlesungsfreie Zeit").next().text());
						System.out.println("\n");
				//	stmt.executeUpdate(p.toSql());
					count++;
					}

			} catch (IOException e) {
				System.out.println(e);
			}
			
			
			// print needed time
			long timeNeeded = (System.currentTimeMillis() - timeBefore);
			System.out.println("\n\nTotal time needed: " + timeNeeded + " ms.");
			
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					conn.close();
			} catch (SQLException se) {
			} // do nothing
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		System.out.println("Connection closed.\n" + count + " entries have been inserted.");

	} // main

} // class