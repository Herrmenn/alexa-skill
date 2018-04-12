import java.io.IOException;

import org.jsoup.*;
import org.jsoup.helper.*;
import org.jsoup.internal.*;
import org.jsoup.nodes.*;
import org.jsoup.parser.*;
import org.jsoup.safety.*;
import org.jsoup.select.*;

import java.sql.* ;  // for standard JDBC programs
import java.sql.Connection;
import java.util.ArrayList;
import java.math.* ; // for BigDecimal and BigInteger support




public class InsertCoreData {
	
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
 	static final String DB_URL = "jdbc:mysql://localhost/alexa";

 	// Database credentials
 	static final String USER = "root";
 	static final String PASS = "";
 	
 	
 	
 	// Person Archive
 	static final String[] PERSON_ARCHIVES = {"https://www.htwsaar.de/ingwi/fakultaet/personen/personen-a-g","https://www.htwsaar.de/ingwi/fakultaet/personen/personen-h-n","https://www.htwsaar.de/ingwi/fakultaet/personen/personen-o-z"};
	

 	
 	/*
 	 * PARSE METHOD
 	 */
 	private static String[] parse(String input) {

 		String[] parts = input.split("@");
        String name = parts[0].replace(".", " ").toLowerCase();
        String[] nameParts = name.split(" ");
        
        
        if(nameParts.length == 1) {
        	String[] namePartsTemp = new String[2];
        	namePartsTemp[0] = nameParts[0];
        	namePartsTemp[1] = ""; 
	    
	        return namePartsTemp;      
        }

        return nameParts;
    }
 	
 	
 	
 	
	
	
	
 	/*
 	 * MAIN METHOD
 	 */
	public static void main(String[] args) {

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
	        System.out.println("Inserting records into the table...");
	        stmt = conn.createStatement();
	        

	        
	        
	        // GET PERSONS FROM WEBSITEE
	        try {
	        	
	        	
	        	for (int i = 0; i < PERSON_ARCHIVES.length; i++) {
	        		
	        		// sql variables
		            ArrayList<String> urls = new ArrayList<>();
		            ArrayList<String> emails = new ArrayList<>();
		            ArrayList<String> firstnames = new ArrayList<>();
		            ArrayList<String> lastnames = new ArrayList<>();
	        		
	        		// Persons
		            Document persArchive = Jsoup.connect(PERSON_ARCHIVES[i]).get(); 
		            
		            Elements persUrl = persArchive.select("div.kurzprofil div a.marron_k");
		            Elements persEmail = persArchive.select("div.kurzprofil div a:not(.marron_k)");
		            

		       
		            
		            // get all urls
		            for (Element url : persUrl) {
		                if (url.absUrl("href").contains("https")) { 
		                    urls.add(url.absUrl("href").toLowerCase());
		                }   
		            }
		            
		            
		            // get all emails + names
		            for (Element email : persEmail) {
		                if (email.text().contains("@htwsaar")) { 
		                    emails.add(email.text().toLowerCase());
		                    
		                    String[] name = parse(email.text());
		                    
		                    firstnames.add(name[0]);
		                    lastnames.add(name[1]);
		                    
		                }   
		            }
		            
		            
		            
		            for (int j = 0; j < urls.size(); j++) {
		            	String sql = "INSERT INTO pers_core_data VALUES (NULL, '" + firstnames.get(j) + "', '" + lastnames.get(j) + "', '" + emails.get(j) + "', '" + urls.get(j) + "')";
	        	        stmt.executeUpdate(sql);
	        	        count ++;
		            }
		            
		            
		            
		            
		            
	        		
	        		
	        	}
	            
	            
	        } catch (IOException e) {
	            System.out.println(e);
	        }
	        
	        
	        
	        
	        
	        
	        
	        
	    	
	    } 
	    catch(SQLException se) {
	      //Handle errors for JDBC
	      se.printStackTrace();
	    }
	    catch(Exception e) {
	      //Handle errors for Class.forName
	      e.printStackTrace();
	    }
	    finally {
	    	//finally block used to close resources
	    	try {
	    		if(stmt!=null)
	    			conn.close();
	    	} 
	    	catch(SQLException se) {
	    	} // do nothing
	    	try {
	    		if(conn!=null)
	    			conn.close();
	    	} 
	    	catch(SQLException se) {
	    		se.printStackTrace();
	    	} //end finally try
	    } //end try
	   System.out.println("\n\nConnection closed.\n" + count + " entries have been inserted.");
		
		
		


	} // main

} // class