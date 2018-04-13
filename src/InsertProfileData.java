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
		static final String PASS = "";
		
		static int count_select = 0;
		static int count_insert = 0;


		/*
		 * MAIN METHOD
		 */
		public static void main(String[] args) {

			Connection conn = null;
			Statement stmt = null;
			
			int count_insert_office = 0;
			int count_insert_phone = 0;
			int count_httperr = 0;
			int id = 1;

			
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
					
					count_select++;
				}
				rs.close();
				

				for (int i = 0; i < personUrls.size() ; i++) {		
		            try {
		            	// Persons
						Document persProfile = Jsoup.connect(personUrls.get(i)).get();
						Elements profile = persProfile.select("div.kontakt-table div");
	
			            for (Element data : profile) {
			            	String office = "";
			            	String phone = "";
			            	
			            	//einfuegen Raumnummer
			                if (data.text().contains("Raum")) {
			                	office = data.text();
			                	System.out.println(office);
			                	
			                	String sql_insert = "UPDATE pers_core_data SET office='" + office + "' WHERE id ='" + id + "'";
								stmt.executeUpdate(sql_insert);
								count_insert_office++;
			                }
			                //einfuegen Telefonnummer
			                else if (data.text().contains("t +")) {
			                	phone = data.text();
			                	System.out.println(phone);
			                	
			                	String sql_insert = "UPDATE pers_core_data SET phone='" + phone + "' WHERE id ='" + id + "'";
								stmt.executeUpdate(sql_insert);
								count_insert_phone++;
			                }
			            }
	            	}
	            	catch (HttpStatusException ht) {
	            		id++;
	            		count_httperr++;
	            		System.out.print("HTTP ERROR 404: " + personUrls.get(i) + "\n");
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
			
			System.out.println("\n\nConnection closed.\n" 
			+ count_insert_office + " office numbers have been selected and inserted\n" 
			+ count_insert_phone  + " phone numbers have been selected and inserted\n" 
			+ "-------------------------------\n"
			+ (id - 1) + " core data entries\n"
			+ count_httperr + " urls not found (404)\n"
			+  (id -1 - count_insert_office-count_httperr)+ " room numbers not available\n"
			+  (id -1 - count_insert_phone-count_httperr)+ " phone numbers not available");
		} // main

} // class
