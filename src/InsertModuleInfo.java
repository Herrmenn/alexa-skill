import java.io.IOException;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.sql.*;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class InsertModuleInfo {

	
	public static void main(String[] args) {
		
		long timeBefore = System.currentTimeMillis();
			
		// Database credentials
		String DB_URL = "jdbc:mysql://localhost/alexa"; // args[0]
		String USER = "root"; // args[1]
		String PASS = ""; // args[2]

		Connection conn = null;
		Statement stmt = null;
		int count = 0;
		

		
		try {

			// JDBC Connection
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();	
			
			// DELETE TABLE CONTENT
			String sql = "DELETE FROM module_info WHERE id > 0;";
			stmt.executeUpdate(sql);
			
			// SET AI TO 1
			sql = "ALTER TABLE module_info AUTO_INCREMENT = 1";
			stmt.executeUpdate(sql);
			
			
			try {
				
				// select 
				stmt = conn.createStatement();
				String sql_select = "SELECT id, url FROM module_lectures";
				ResultSet rs = stmt.executeQuery(sql_select);
				
				
				// loop over resultset urls
				while(rs.next()) {
					
					// table attributes
					int id = rs.getInt("id");
					String url = rs.getString("url");
							
					// url as document
					Document lectureArchive = Jsoup.connect(url).get();
					
					// get all table rows
					Elements trElements = lectureArchive.select("table tbody tr td");
					
					// input vars
					String ects = "";
					String semester = "";
					String examtype = "";
					String lecturer = "";
					String learninggoals = "";
					String content = "";
					
					count++;
					
					
					System.out.println("\n\n" + lectureArchive.title() + " - " + url);
					System.out.println("id: " + count);
					
					
					for (Element row : trElements) {

						if (row.text().contains("ECTS-Punkte:")) {

							// <b>ECTS-Punkte: </b>3</td>
							String pattern = "</b>[0-9]*</td>";
					        Pattern r = Pattern.compile(pattern);
					        Matcher m = r.matcher(row.toString());
											
					        if (m.find( )) {	
					        	String result = m.group(0).replaceAll("</b>", "");
					        	result = result.replaceAll("</td>", "");
					        	ects = result.replaceAll(" ", "");
					        	System.out.println("ECTS: " + ects);
					        }
							
						}
						else if (row.text().contains("Studiensemester:")) {
							
							String pattern = "</b>[a-zA-z0-9]*";
					        Pattern r = Pattern.compile(pattern);
					        Matcher m = r.matcher(row.toString());
											
					        if (m.find( )) {	
					        	String result = m.group(0).replaceAll("</b>", "");
					        	semester = result.replaceAll(" ", "");
					        	System.out.println("Semester: " + semester);
					        }
					        
						}
						else if (row.text().contains("Prüfungsart:")) {
							
							String pattern = "<br> [a-zA-z0-9]*";
					        Pattern r = Pattern.compile(pattern);
					        Matcher m = r.matcher(row.toString());
											
					        if (m.find( )) {	
					        	String result = m.group(0).replaceAll("<br>", "");
					        	examtype = result.replaceAll(" ", "");
					        	System.out.println("Prüfungsart: " + examtype);
					        }
							
						}
						else if (row.text().contains("Dozent:")) {
							String pattern = "<br>.*\\[";
					        Pattern r = Pattern.compile(pattern);
					        Matcher m = r.matcher(row.toString());
											
					        if (m.find( )) {	
					        	String result = m.group(0).replaceAll("<br>", "");
					        	lecturer = result.replaceAll("\\[", "");
					        	lecturer = lecturer.replaceAll("(<)[^&]*(>)", "");
					        	System.out.println("Dozent: " + lecturer);
					        }
						}
						else if (row.text().contains("Lernziele:")) {
							String pattern = "<br>.*\\[";
					        Pattern r = Pattern.compile(pattern);
					        Matcher m = r.matcher(row.toString());
											
					        if (m.find( )) {	
					        	String result = m.group(0).replaceAll("<br>", "");
					        	result = result.replaceAll("&nbsp;", " ");
					        	learninggoals = result.replaceAll("\\[", "");
					        	System.out.println("Lernziele: " + learninggoals);
					        }
						}
						else if (row.text().contains("Inhalt:")) {
							String pattern = "<br>.*\\[";
					        Pattern r = Pattern.compile(pattern);
					        Matcher m = r.matcher(row.toString());
											
					        if (m.find( )) {	
					        	String result = m.group(0).replaceAll("<br>", "");
					        	result = result.replaceAll("&nbsp;", " ");
					        	content = result.replaceAll("\\[", "");
					        	System.out.println("Inhalt: " + content);
					        }
						}
						
					}
					

					stmt = conn.createStatement();
					sql = "INSERT INTO module_info VALUES (NULL, '" + id + "', '" + ects + "', '"+ semester + "', '"+ examtype + "', '"+ lecturer + "', '"+ learninggoals + "', '"+ content + "')";
					stmt.executeUpdate(sql);
					
					
					
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
