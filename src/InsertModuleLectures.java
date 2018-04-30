import java.io.IOException;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.sql.*;
import java.sql.Connection;




public class InsertModuleLectures {

	
	public static void main(String[] args) {
		
		long timeBefore = System.currentTimeMillis();
			
		// Database credentials
		String DB_URL = "jdbc:mysql://localhost/alexa"; // args[0]
		String USER = "root"; // args[1]
		String PASS = "root"; // args[2]

		Connection conn = null;
		Statement stmt = null;
		int count = 0;
		

		
		try {

			// JDBC Connection
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();	
			
			// DELETE TABLE CONTENT
			String sql = "DELETE FROM module_lectures WHERE id > 0;";
			stmt.executeUpdate(sql);
			
			// SET AI TO 1
			sql = "ALTER TABLE module_lectures AUTO_INCREMENT = 1";
			stmt.executeUpdate(sql);
			
			
			try {
				
				// select 
				stmt = conn.createStatement();
				String sql_select = "SELECT id, url FROM module_courses";
				ResultSet rs = stmt.executeQuery(sql_select);
				
				
				// loop over resultset urls
				while(rs.next()) {
					
					// table attributes
					int id = rs.getInt("id");
					String url = rs.getString("url");
							
					// url as document
					Document moduleArchive = Jsoup.connect(url).get();

					// Get all lectures with urls
					Elements module = moduleArchive.select("h2 + table tbody tr td:first-child > a");

					// insert all lectures
					for (Element lecture : module) {					
						if (lecture.absUrl("href").contains("moduldb")) {
							stmt = conn.createStatement();
							sql = "INSERT INTO module_lectures VALUES (NULL, '" + id + "', '" + lecture.text().replace("'", "") + "', '"+ lecture.absUrl("href").toLowerCase() + "')";
							stmt.executeUpdate(sql);
							
							count++;
						}	
						
						System.out.println(moduleArchive.title() + " - " + lecture.text());
					}
				}
				rs.close();
				
	
				

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
		System.out.println(count + " entries have been inserted.");

	} // main

} // class
