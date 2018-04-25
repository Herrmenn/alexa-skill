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
 * @author Herrmann
 * uebernimmt semestertermine und schlieﬂtage von der htw webseite
 *
 */
public class InsertModulDB {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/alexa";

	// Database credentials
	static final String USER = "root";
	static final String PASS = "";

	// Person Archive
	static final String MODULDB_DATES_ARCHIVE =  "https://moduldb.htwsaar.de/";

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
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
		//	System.out.println("Connected database successfully...");

			// STEP 4: Execute a query
		//	System.out.println("Inserting records into the table...\n");
		//	stmt = conn.createStatement();

			// GET PERSONS FROM WEBSITE
			try {


					// Persons
					Document semesterArchive = Jsoup.connect(MODULDB_DATES_ARCHIVE).get();
					
					Elements infoBlocks = semesterArchive.select("table.pretty-table");
				
					
					for (Element block: infoBlocks) {
											
						//Elements tableElements = block.children();
					
						Elements linksDB = block.select("tr").next();
						Elements linkser = linksDB.select("[href$=lang=de]");
			
						String courseOfStudiesName = linksDB.select("[href$=lang=de]").html();
						String courseOfStudiesUrl ="" ;
						System.out.println(courseOfStudiesName);
						for (Element url : linkser) {
							//System.out.println(url);
							if (url.absUrl("href").contains("lang=de")) {
								System.out.println(url.absUrl("href"));
							}
							if (url.toString() == "") {
								System.out.println("KEINE URL");
							}
						}
						
					//	System.out.println(linksDB.select("[href$=lang=de]"));
					//	String sql = "INSERT INTO semester_dates VALUES (NULL, '"	+ semester + "', '"+ semester_start + "', '" + lectures_start + "', '" + lectures_end + "', '" + semester_end 
					//																+ "', '" + lecture_free_time + "', '"+ closing +"')";
						
				//stmt.executeUpdate(sql);
					count++;
					}

			} catch (IOException e) {
				System.out.println(e);
			}
					
			// print needed time
			long timeNeeded = (System.currentTimeMillis() - timeBefore);
			System.out.println("\n\nTotal time needed: " + timeNeeded + " ms.");
			

		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
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
		System.out.println("Connection closed.\n" + count + " modules have been inserted.");

	} // main

} // class