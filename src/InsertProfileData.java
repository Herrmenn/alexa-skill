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
import java.math.*; // for BigDecimal and BigInteger support



public class InsertProfileData {

		// JDBC driver name and database URL
		static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		static final String DB_URL = "jdbc:mysql://localhost/alexa";

		// Database credentials
		static final String USER = "root";
		static final String PASS = "root";
		
		static int count_select = 0;
		static int count_insert = 0;



		/*
		 * MAIN METHOD
		 */
		public static void main(String[] args) {

			Connection conn = null;
			Statement stmt = null;
			
			int count_select = 0;
			int count_insert = 0;

			
			try {

				// STEP 2: Register JDBC driver
				Class.forName("com.mysql.jdbc.Driver");

				// STEP 3: Open a connection
				System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				System.out.println("Connected database successfully...");

				
				
				// Person Profile Urls
				ArrayList<String> personUrls = new ArrayList<>();

				//STEP 4: Execute a query
				System.out.println("Creating statement...");
				stmt = conn.createStatement();

				String sql_select = "SELECT url FROM pers_core_data";
				ResultSet rs = stmt.executeQuery(sql_select);
				
				//STEP 5: Extract data from result set
				while(rs.next()){
					String url = rs.getString("url");
					personUrls.add(url);
					System.out.println("\nUrl: " + url);
					
					count_select++;
				}
				rs.close();
				
				
				
				
				int id = 1;
				for (int i = 0; i < personUrls.size() ; i++) {
					

		            try {
		            	// Persons
						Document persProfile = Jsoup.connect(personUrls.get(i)).get();
						Elements profile = persProfile.select("div.kontakt-table div");

						
			            for (Element data : profile) {
			            	
			            	
			                if (data.text().contains("Raum")) {
			                	String office = data.text();
			                    System.out.println(office);
			                    
			                    String sql_insert = "UPDATE pers_core_data SET office='" + office + "' WHERE id ='" + id + "'";
								stmt.executeUpdate(sql_insert);
								count_insert++;
								
			                }
			                ;
			            }
	            	}
	            	catch (HttpStatusException ht) {
	            		id++;
	            		System.out.println("404");
	            		continue;
	            	}
					
		            id++;
				}
				
				
			      
			      
				
				
			
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
			System.out.println("\n\nConnection closed.\n" + count_insert + " entries have been selected\n" + count_insert + " entries have been inserted");

		} // main

} // class
