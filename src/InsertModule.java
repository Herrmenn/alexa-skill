import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * 
 * <h1> Extracts data of modules <h1>
 * The InsertModule program implements an application that simply 
 * extracts the different modules of the given Urls into a database.
 * 
 * @author Alex
 * @version 1.0
 * @since 11.08.18
 *
 */


public class InsertModule {
	
	
	static final String MODULE_DB = "https://moduldb.htwsaar.de/cgi-bin/moduldb-index";
	

	public static void main(String[] args) {
		
		System.out.println("Started...\n\n");
		
		long timeBefore = System.currentTimeMillis();
		
		
		try {

			ArrayList<String> courses = new ArrayList<String>();
			ArrayList<String> coursesURLs = new ArrayList<String>();
			
			ArrayList<String> lectures = new ArrayList<String>();
			ArrayList<String> lecturesURLs = new ArrayList<String>();
			
			// JSOUP: Courses
			Document moduleArchive = Jsoup.connect(MODULE_DB).get();

			// SELECTOR: Courses URLS 
			Elements module = moduleArchive.select("tbody tr td:first-child > a");
			
			
			Elements moduleInstitute = moduleArchive.select("tbody tr td:nth-child(5)");
			moduleInstitute.remove(0); // Institute table-heading

			
			
			// Get all courses
			int i = 0;
			for (Element course : module) {
				
				if (i == 0) {
					if (course.absUrl("href").contains("moduldb")) {
						courses.add(course.text());
						coursesURLs.add(course.absUrl("href").toLowerCase());
					}
				}
				else {	
					// only get newest course (no duplicates)
					if (course.absUrl("href").contains("moduldb") && !(course.text().equals(courses.get(courses.size() - 1).toString())) && (moduleInstitute.get(i).toString().equals("<td>&nbsp;</td>"))) {
						courses.add(course.text());
						coursesURLs.add(course.absUrl("href").toLowerCase());
					}	
				}
				
				i++;
			}
			
			
			System.out.println("     Courses: " + courses.size());
			System.out.println("Courses URLs: " + coursesURLs.size());
			System.out.println("\n");
			
			
			
			
			// Get all lectures from courses
			for (String url : coursesURLs) {
				
				Document courseArchive = Jsoup.connect(url).get();
				
				// Get all lectures with urls
				Elements lecturesList = courseArchive.select("h2 + table tbody tr td:first-child > a");

				// insert all lectures
				for (Element lecture : lecturesList) {					
					if (lecture.absUrl("href").contains("moduldb")) {
						lectures.add(lecture.text());
						lecturesURLs.add(lecture.absUrl("href").toLowerCase());
					}	
				}
				
			}
			
			System.out.println("    Lectures: " + lectures.size());
			System.out.println("Lecture URLs: " + lecturesURLs.size());
			System.out.println("\n\n");
			
			
			
			
			
			Connection conn = null;
			Statement stmt = null;
			
			
			try {
			
				// JDBC Connection
				Class.forName("com.mysql.jdbc.Driver");
				conn = DriverManager.getConnection(args[0], args[1], args[2]);
				stmt = conn.createStatement();
				
				String dropIfExists = "DROP TABLE IF EXISTS temp_moduledb;";
				
				// create temp table for new entries
				String updateRoutine = "CREATE TABLE temp_moduledb ("+
											"id int(11) NOT NULL AUTO_INCREMENT,"+
											"module varchar(255) DEFAULT NULL,"+
											"course varchar(255) DEFAULT NULL,"+
											"ects varchar(5) DEFAULT NULL,"+
											"semester varchar(5) DEFAULT NULL,"+
											"exam_type varchar(255) DEFAULT NULL,"+
											"lecturer varchar(255) DEFAULT NULL,"+
											"learning_goals text,"+
											"content text,"+
											"PRIMARY KEY (id)"+
										");";
				stmt.executeUpdate(dropIfExists);
				stmt.executeUpdate(updateRoutine);
				
				
				
				// Get information from lecture (module) site
				int j = 0;
				for (String url : lecturesURLs) {
							
					// url as document
					Document lectureArchive = Jsoup.connect(url).get();
					
					// get all table rows
					Elements trElements = lectureArchive.select("table tbody tr td");
					
					// input vars
					String moduleName = lectures.get(j);
					String courseName = "";
					String ects = "";
					String semester = "";
					String examtype = "";
					String lecturer = "";
					String learninggoals = "";
					String content = "";
					
					System.out.println("Modulname: " + moduleName);
					
					
					
					/*
					 * Collect information from table elements
					 * and replace HTML tags
					 */
					for (Element row : trElements) {
						
						String row_text = row.text()
										  .replace("\'", "")
										  .replace("\"", "")
										  .replaceAll("&[a-zA-Z]{2,5};", "");
						
		
						if (row_text.contains("Studiengang:")) {
	
							String pattern = "</b>(.*)</td>";
					        Pattern r = Pattern.compile(pattern);
					        Matcher m = r.matcher(row.toString());
											
					        if (m.find( )) {	
					        	String result = m.group(0).replaceAll("</b>", "");
					        	courseName = result.replaceAll("</td>", "");
					        	courseName = courseName.split(",", 2)[0];
					        	courseName = courseName.substring(1);
					        	System.out.println("Studiengang: " + courseName);
					        }
							
						}
						else if (row_text.contains("ECTS-Punkte:")) {
	
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
						else if (row_text.contains("Studiensemester:")) {
							
							String pattern = "</b>[a-zA-z0-9]*";
					        Pattern r = Pattern.compile(pattern);
					        Matcher m = r.matcher(row.toString());
											
					        if (m.find( )) {	
					        	String result = m.group(0).replaceAll("</b>", "");
					        	semester = result.replaceAll(" ", "");
					        	System.out.println("Semester: " + semester);
					        }
					        
						}
						else if (row_text.contains("Prüfungsart:")) {
							
							String pattern = "<br> [a-zA-z0-9]*";
					        Pattern r = Pattern.compile(pattern);
					        Matcher m = r.matcher(row.toString());
											
					        if (m.find( )) {	
					        	String result = m.group(0).replaceAll("<br>", "");
					        	examtype = result.replaceAll(" ", "");
					        	System.out.println("Prüfungsart: " + examtype);
					        }
							
						}
						else if (row_text.contains("Dozent:")) {
							String pattern = "</b>.*\\[";
					        Pattern r = Pattern.compile(pattern);
					        Matcher m = r.matcher(row.toString());
											
					        if (m.find( )) {
					        	String result = m.group(0).replaceAll("<br>", "");
					        	lecturer = Jsoup.parse(result).text();
					        	lecturer = lecturer.replaceAll("\\[", "");
					        	System.out.println("Dozent: " + lecturer);
					        }
						}
						else if (row_text.contains("Lernziele:")) {
							String pattern = "<br>.*\\[";
					        Pattern r = Pattern.compile(pattern);
					        Matcher m = r.matcher(row.toString());
											
					        if (m.find( )) {	
					        	String result = Jsoup.parse(m.group(0)).text();
					        	learninggoals = result.replaceAll("\\[", "");
					        	System.out.println("Lernziele: " + learninggoals);
					        }
						}
						else if (row_text.contains("Inhalt:")) {
							String pattern = "<br>.*\\[";
					        Pattern r = Pattern.compile(pattern);
					        Matcher m = r.matcher(row.toString());
											
					        if (m.find( )) {	
					        	String result = Jsoup.parse(m.group(0)).text();
					        	content = result.replaceAll("\\[", "");
					        	System.out.println("Inhalt: " + content + "\n\n");
					        }
						}
						
					}
					
					
					
					
					moduleName = moduleName.toLowerCase();
				    // Replace I's in moduleName (e.g. Mathematik II)
			        if (moduleName.matches("(.*)i$")) {
			        	String iString = moduleName.substring(moduleName.lastIndexOf(" "));
			        	System.out.println("iString" + iString);
			        	
			            int count = iString.length() - iString.replace("i", "").length();
			            String newStr = moduleName.substring(0, moduleName.length() - count) + "" + count;
			            System.out.println("Old String: " + moduleName + "\nNew String: " + newStr);
			            moduleName = newStr;
			        }
			        
			        
			        
			        moduleName = moduleName.replace("1", "eins");
					moduleName = moduleName.replace("2", "zwei");
					moduleName = moduleName.replace("3", "drei");
					moduleName = moduleName.replace("4", "vier");
					moduleName = moduleName.replace("5", "f�nf");
					moduleName = moduleName.replace("6", "sechs");
			        
					
					
					// Insert entries into database
					String insertTableSQL = "INSERT INTO temp_moduledb (module, course, ects, semester, exam_type, lecturer, learning_goals, content)" 
					+ "VALUES (?,?,?,?,?,?,?,?)";
					
					PreparedStatement ps = conn.prepareStatement(insertTableSQL);

					ps.setString(1, moduleName.toLowerCase());
					ps.setString(2, courseName.toLowerCase());
					ps.setString(3, ects.toLowerCase());
					ps.setString(4, semester.toLowerCase());
					ps.setString(5, examtype.toLowerCase());
					ps.setString(6, lecturer.toLowerCase());
					ps.setString(7, learninggoals.toLowerCase());
					ps.setString(8, content.toLowerCase());
					ps.executeUpdate();
					
					j++;
				}
				
				
				// Update Routine (switch tables)
				String dropTable = "DROP TABLE moduledb;";
				stmt.executeUpdate(dropTable);
				
				String alterTable = "ALTER TABLE temp_moduledb RENAME TO moduledb;";
				stmt.executeUpdate(alterTable);
				
				
				
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
			

			
		}
		catch (Exception e) {
			System.out.println(e);
		}
		

	}

}
