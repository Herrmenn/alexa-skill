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
public class InsertDatesSemester {

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
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			System.out.println("Connecting to a selected database...");
			conn = DriverManager.getConnection(args[0], args[1], args[2]);
			System.out.println("Connected database successfully...");

			// STEP 4: Execute a query
			System.out.println("Inserting Semester Dates into the table");
			stmt = conn.createStatement();

			// GET PERSONS FROM WEBSITE
			try {


					// Persons
					Document semesterArchive = Jsoup.connect(SEMESTER_DATES_ARCHIVE).get();
					
					Elements infoBlocks = semesterArchive.select("table.listing");
				
					
					for (Element block: infoBlocks) {
											
						//Elements tableElements = block.children();
						
						String semester          = block.previousElementSibling().text();
						String semester_start    = block.getElementsContainingOwnText("Beginn des").next().text();
						String lectures_start    = block.getElementsContainingOwnText("Beginn der").next().text();
						String lectures_end      = block.getElementsContainingOwnText("Ende der").next().text();
						String semester_end      = block.getElementsContainingOwnText("Ende des").next().text();
						String lecture_free_time = block.getElementsContainingOwnText("Vorlesungsfreie").next().text();
						String closing  		 = block.select("td.linksbundig[align=center]").html();
						
						String sql = "INSERT INTO semester_dates VALUES (NULL, '"	+ semester + "', '"+ semester_start + "', '" + lectures_start + "', '" + lectures_end + "', '" + semester_end 
																					+ "', '" + lecture_free_time + "', '"+ closing +"')";
						
					stmt.executeUpdate(sql);
					count++;
					}

			} catch (IOException e) {
				System.out.println(e);
			}
					
			// print needed time
			long timeNeeded = (System.currentTimeMillis() - timeBefore);
			System.out.println("\nTotal time needed: " + timeNeeded + " ms.");
			

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
		System.out.println("Connection closed.\n" + count + " semester dates have been inserted.\n\n");

	} // main

} // class