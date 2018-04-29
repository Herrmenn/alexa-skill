import java.io.IOException;

import org.jsoup.*;
import org.jsoup.helper.*;
import org.jsoup.internal.*;
import org.jsoup.nodes.*;
import org.jsoup.parser.*;
import org.jsoup.safety.*;
import org.jsoup.select.*;

import java.sql.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.math.*;




public class InsertModuleCourses {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/alexa";

	// Database credentials
	static final String USER = "root";
	static final String PASS = "root";

	// Module Database
	static final String MODULE_DB = "https://moduldb.htwsaar.de/cgi-bin/moduldb-index";



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
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Connected database successfully...");

			// STEP 4: Execute a query
			System.out.println("Inserting records into the table...\n");
			stmt = conn.createStatement();

			// GET PERSONS FROM WEBSITE
			try {

				long timeBeforePersons = System.currentTimeMillis();

				// SQL variables
				ArrayList<String> courses = new ArrayList<>();
				ArrayList<String> urls = new ArrayList<>();
				
				// Courses
				Document moduleArchive = Jsoup.connect(MODULE_DB).get();

				// Get all courses with urls
				Elements module = moduleArchive.select("tbody tr td:first-child > a");

				int i = 0;
				for (Element course : module) {
					if (i == 0) {
						if (course.absUrl("href").contains("moduldb")) {
							courses.add(course.text());
							urls.add(course.absUrl("href").toLowerCase());
						}
					}
					else {					
						if (course.absUrl("href").contains("moduldb") && !(course.text().equals(courses.get(courses.size() - 1).toString()))) {
							courses.add(course.text());
							urls.add(course.absUrl("href").toLowerCase());
						}	
					}
					
					i++;
				}

				
				// print needed time
				long timeNeededPersons = (System.currentTimeMillis() - timeBeforePersons);
				System.out.println("Time needed: " + timeNeededPersons + " ms - " + courses.size() + " Courses - " + urls.size() + " Urls (" + moduleArchive.title() + ")");

				for (int j = 0; j < urls.size(); j++) {
					String sql = "INSERT INTO module_courses VALUES (NULL, '" + courses.get(j) + "', '"+ urls.get(j) + "')";
					stmt.executeUpdate(sql);
					count++;
				}
					
				

			} catch (IOException e) {
				System.out.println(e);
			}
			
			
			// print needed time
			long timeNeeded = (System.currentTimeMillis() - timeBefore);
			System.out.println("\n\nTotal time needed: " + timeNeeded + " ms");
			

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
		System.out.println("Connection closed.\n" + count + " entries have been inserted.");

	} // main

} // class