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
 * @author Herrmann, Morlo Uebernimmt Vorname, Nachname, E-Mail, URL aller
 *         Angestellten im Studienbereich Ingenieurwissenschaften, Wirtschaftswissenschaften in die
 *         Datenbank
 *
 */
public class InsertCoreDataSowiAub {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/alexa";

	// Database credentials
	static final String USER = "root";
	static final String PASS = "";

	// Person Archive
	static final String[] PERSON_ARCHIVES =   { "https://www.htwsaar.de/sowi/fakultaet/personen/index_2014.html",
												"https://www.htwsaar.de/sowi/fakultaet/personen/professoren",
												"https://www.htwsaar.de/sowi/fakultaet/personen/Wissenschaftliche-Mitarbeiterinnen-und-Mitarbeiter",
												};

	/*
	 * PARSE METHOD
	 */
	private static String[] parse(String input) {

		String[] parts = input.split("@");
		String name = parts[0].replace(".", " ").toLowerCase();
		String[] nameParts = name.split(" ");

		if (nameParts.length == 1) {
			String[] namePartsTemp = new String[2];
			namePartsTemp[0] = " ";
			namePartsTemp[1] = nameParts[0];

			return namePartsTemp;
		}

		return nameParts;
	}

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

				for (int i = 0; i < PERSON_ARCHIVES.length; i++) {
					
					long timeBeforePersons = System.currentTimeMillis();

					// sql variables
					ArrayList<String> urls = new ArrayList<>();
					ArrayList<String> emails = new ArrayList<>();
					ArrayList<String> firstnames = new ArrayList<>();
					ArrayList<String> lastnames = new ArrayList<>();
					ArrayList<String> roomnumbers = new ArrayList<>();
					ArrayList<String> phonenumbers = new ArrayList<>();

					// Persons
					Document persArchive = Jsoup.connect(PERSON_ARCHIVES[i]).get();

					Elements persEmail = persArchive.select("div.kontakt-table div a:not(.marron_u)");
					Elements persUrl = persArchive.select("div.linkToPerson a, a.external-link, a.internal-link, span.internal-link");
					Elements persRoom = persArchive.select("div.kontakt-table div");
					// get all urls
					for (Element url : persUrl) {
						System.out.println(url);
						if (url.absUrl("href").contains("http")){
							//System.out.println(url.absUrl("href").toLowerCase());
							urls.add(url.absUrl("href").toLowerCase());
						}
						else if(!url.absUrl("href").contains("http") && url.outerHtml().contains("<span class=\"internal-link\">Weitere Informationen</span>")){
							//System.out.println(url.text());
							urls.add("Keine URL");
							//System.out.println("Keine URL");
						}
					}

					// get all emails + names
					for (Element email : persEmail) {
						if (email.text().contains("@") || email.text().contains(".de")) {
							String split[] = email.text().split("@");
							if(split[0].contains(".")) {
							emails.add(email.text().toLowerCase());
							//System.out.println(email.text().toLowerCase());
							String[] name = parse(email.text());

							firstnames.add(name[0]);
							lastnames.add(name[1]);
							}
						}
					}
					int counter = 0;
					for (Element room : persRoom) {
						if (counter < 9 && room.text().contains("Raum")) {
							roomnumbers.add(room.text());
						//	System.out.println(room.text());	
							counter = 0;
						}
						else if(counter >= 9){
							roomnumbers.add("Kein Raum");
						//	System.out.println("ADD");
							counter = 0;
						}
						else {
							counter++;
							//System.out.println("Kein Raum");
						}
					}
					
					// print needed time
					long timeNeededPersons = (System.currentTimeMillis() - timeBeforePersons);
					System.out.println("Time needed: " + timeNeededPersons + " ms - " + emails.size() + " Emails - " + urls.size() + " Urls - " + roomnumbers.size()+ " Raumnummern - ("+ persArchive.title() + ")");

					for (int j = 0; j < urls.size(); j++) {
						String sql = "INSERT INTO pers_core_data VALUES (NULL, '" + firstnames.get(j) + "', '"+ lastnames.get(j) + "', '" + emails.get(j) + "', '" + urls.get(j) + "', 'NULL', 'NULL')";
						stmt.executeUpdate(sql);
						count++;
					}
					
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