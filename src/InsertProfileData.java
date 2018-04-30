import java.io.IOException;

import org.jsoup.*;
import org.jsoup.helper.*;
import org.jsoup.internal.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.sql.*; // for standard JDBC programs
import java.sql.Connection;
import java.util.ArrayList;
import java.math.*; // for BigDecimal and BigInteger support



public class InsertProfileData {
		
		static int count_select = 0;
		static int count_insert = 0;

		/*
		 * MAIN METHOD
		 */
		public static void main(String[] args) {

			Connection conn = null;
			Statement stmt = null;
			
			long timeBefore = System.currentTimeMillis();
			
			int count_insert_office = 0;
			int count_insert_phone = 0;
			int count_httperr = 0;
			int id = 1;

			
			try {

				// STEP 2: Register JDBC driver
				Class.forName("com.mysql.jdbc.Driver");

				// STEP 3: Open a connection
				System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(args[0], args[1], args[2]);
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
				
				System.out.println("Inserting Profile Data of selected persons...");
				

				for (int i = 0; i < personUrls.size() ; i++) {		
		            try {
		            	// Persons
						Document persProfile = Jsoup.connect(personUrls.get(i)).get();
						Elements persOffice = persProfile.select("div.kontakt-table div");
						Elements persPhone = persProfile.select("div.kontakt-table div span");
						
	
			            for (Element data : persOffice) {
			            	String office = "";
			            	
			            	//einfuegen Raumnummer
			                if (data.text().contains("Raum")) {
			                	office = data.text();
			                	
			                	String sql_insert = "UPDATE pers_core_data SET office='" + office + "' WHERE id ='" + id + "'";
								stmt.executeUpdate(sql_insert);
								count_insert_office++;
			                }
			                
			            }
			            
			            for (Element data : persPhone) {
			            	String phone = "";
			            	
			            	//einfuegen Telefonnummer
			                if (data.text().contains("+")) {
			                	phone = data.text();
			                	
			                	String sql_insert = "UPDATE pers_core_data SET phone='" + phone + "' WHERE id ='" + id + "'";
								stmt.executeUpdate(sql_insert);
								count_insert_phone++;
			                }
			                
			            }
			            
			            System.out.println("loading...");
			            
	            	}
	            	catch (HttpStatusException ht) {
	            		id++;
	            		count_httperr++;
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
			
			
			
			
			long timeNeeded = (System.currentTimeMillis() - timeBefore);
			System.out.println("\n\n\n\nTotal time needed: " + timeNeeded + " ms");
			System.out.println("Connection closed.\n" 
			+ count_insert_office + " office numbers have been selected and inserted\n" 
			+ count_insert_phone  + " phone numbers have been selected and inserted\n" 
			+ "-------------------------------\n"
			+ (id - 1) + " core data entries\n"
			+ count_httperr + " urls not found (404)\n"
			+  (id -1 - count_insert_office-count_httperr)+ " room numbers not available\n"
			+  (id -1 - count_insert_phone-count_httperr)+ " phone numbers not available");
		} // main

} // class
