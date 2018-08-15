import java.io.IOException;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.sql.*; // for standard JDBC programs
import java.sql.Connection;

/**
 * <h1> Extracts specific semester Dates <h1>
 * The InsertDatesSemester program implements an application that simply 
 * extracts all semester dates from the given URL and insert these into a database.  
 * @author Eric
 * @version 1.0
 * @since 11.08.18
 *
 */
public class InsertDatesSemester {

	// Person Archive
	static final String SEMESTER_DATES_ARCHIVE =  "https://www.htwsaar.de/studium/organisation/semestertermine";

	/**
	 * Similar to the other classes: 
	 * connects to DB; creates Table temp_semester_dates, extracts data from URL;
	 * inserts dates into temp_semester_dates; drops table semester_dates;
	 * alter table temp... to semester_dates
	 *  @param semester current semester       
	 *  @param semester_start start of semester
	 *  @param lectures_start start of lectures
	 *  @param lectures_end end of lectures     
	 *  @param semester_end end of semester 
	 *  @param lecture_free_time free time 
	 *  @param closing closing days
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
			String dropIfExists = "DROP TABLE IF EXISTS temp_semester_dates;";
			String createSemesterTable = "CREATE TABLE temp_semester_dates (\r\n" + 
					"semester varchar(100),\r\n" + 
					"semester_start varchar(50), \r\n" + 
					"lectures_start varchar(50),\r\n" + 
					"lectures_end varchar(50) ,\r\n" + 
					"semester_end varchar(50) ,\r\n" + 
					"lecture_free_time varchar(255),\r\n" + 
					"closure_days varchar(255),\r\n" + 
					"PRIMARY KEY (semester)\r\n" + 
					");";
			stmt.executeUpdate(dropIfExists);
			stmt.executeUpdate(createSemesterTable);
			
			try {


					// People
					Document semesterArchive = Jsoup.connect(SEMESTER_DATES_ARCHIVE).get();
					
					Elements infoBlocks = semesterArchive.select("table.listing");
				
					
					for (Element block: infoBlocks) {						
						
						String semester          = block.previousElementSibling().text();
						String semester_start    = block.getElementsContainingOwnText("Beginn des").next().text();
						String lectures_start    = block.getElementsContainingOwnText("Beginn der").next().text();
						String lectures_end      = block.getElementsContainingOwnText("Ende der").next().text();
						String semester_end      = block.getElementsContainingOwnText("Ende des").next().text();
						String lecture_free_time = block.getElementsContainingOwnText("Vorlesungsfreie").next().text();
						String closing  		 = block.select("td.linksbundig[align=center]").html();
						
						String [] temp = semester.split("/");
						semester = temp[0];
						
						// Insert entries into database
						String insertTableSQL = "INSERT INTO temp_semester_dates (semester, semester_start, lectures_start, lectures_end, semester_end, lecture_free_time, closure_days)" 
						+ "VALUES (?,?,?,?,?,?,?)";
						
						PreparedStatement ps = conn.prepareStatement(insertTableSQL);

						ps.setString(1, semester.toLowerCase());
						ps.setString(2, semester_start.toLowerCase());
						ps.setString(3, lectures_start.toLowerCase());
						ps.setString(4, lectures_end.toLowerCase());
						ps.setString(5, semester_end.toLowerCase());
						ps.setString(6, lecture_free_time.toLowerCase());
						ps.setString(7, closing.toLowerCase());
						ps.executeUpdate();
						
//						String sql = "INSERT INTO temp_semester_dates VALUES ('"+ semester.toLowerCase() + "', '"+ semester_start.toLowerCase() + "', '" + lectures_start.toLowerCase() + "', '" + 
//																			lectures_end.toLowerCase() + "', '" + semester_end.toLowerCase() + "', '" + lecture_free_time.toLowerCase() 
//																			+ "', '"+ closing.toLowerCase() +"')";
						
//					stmt.executeUpdate(sql);
					count++;
					}

			} catch (IOException e) {
				System.out.println(e);
			}
					
			String dropTable = "DROP TABLE semester_dates;";
			stmt.executeUpdate(dropTable);
			
			String alterTable = "ALTER TABLE temp_semester_dates RENAME TO semester_dates;";
			stmt.executeUpdate(alterTable);
			// print time needed
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
