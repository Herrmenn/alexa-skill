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

/**
 * 
 * @author Herrmann, Morlo Uebernimmt Vorname, Nachname, E-Mail, URL aller
 *         Angestellten im Studienbereich Ingenieurwissenschaften, Wirtschaftswissenschaften in die
 *         Datenbank
 *
 */
public class InsertCoreData {

	// Person Archive
	static final String[] PERSON_ARCHIVES =   { "https://www.htwsaar.de/ingwi/fakultaet/personen/personen-a-g",
												"https://www.htwsaar.de/ingwi/fakultaet/personen/personen-h-n",
												"https://www.htwsaar.de/ingwi/fakultaet/personen/personen-o-z",
												"https://www.htwsaar.de/wiwi/fakultaet/personen/dozenten-a-g",
												"https://www.htwsaar.de/wiwi/fakultaet/personen/dozenten-h-o",
												"https://www.htwsaar.de/wiwi/fakultaet/personen/dozenten-p-z",
												"https://www.htwsaar.de/wiwi/fakultaet/personen/mitarbeiter" };

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
			conn = DriverManager.getConnection(args[0], args[1], args[2]);
			System.out.println("Connected database successfully...");

			// STEP 4: Execute a query
			System.out.println("Inserting Core Data of persons into the table...\n");
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

					// Persons
					Document persArchive = Jsoup.connect(PERSON_ARCHIVES[i]).get();

					Elements persUrl = persArchive.select("div.kurzprofil div a.marron_k");
					Elements persEmail = persArchive.select("div.kurzprofil div a:not(.marron_k)");

					// get all urls
					for (Element url : persUrl) {
						if (url.absUrl("href").contains("http")) {
							urls.add(url.absUrl("href").toLowerCase());
						}
						if (url.toString() == "") {
							System.out.println("KEINE URL");
						}
					}

					// get all emails + names
					for (Element email : persEmail) {
						if (email.text().contains("@")) {
							emails.add(email.text().toLowerCase());

							String[] name = parse(email.text());

							firstnames.add(name[0]);
							lastnames.add(name[1]);

						}
						if (email.toString() == "") {
							System.out.println("KEINE EMAIL");
						}
					}
					
					// print needed time
					long timeNeededPersons = (System.currentTimeMillis() - timeBeforePersons);
					System.out.println("Time needed: " + timeNeededPersons + " ms - " + emails.size() + " Emails - " + urls.size() + " Urls (" + persArchive.title() + ")");

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