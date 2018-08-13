import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.sql.*; // for standard JDBC programs
import java.sql.Connection;
import java.util.ArrayList;


/**
 * 
 * <h1> Extracts data of professors from the given Urls <h1>
 * The InsertCoreDataSowiAub program implements an application that simply 
 * extracts the different professors of the given Urls into a database.
 * 
 * @author Alex, Eric
 * @version 1.0
 * @since 11.08.18
 *
 */
public class InsertCoreDataIngwiWiwi {

	// Person Archive
	static final String[] PERSON_ARCHIVES = { "https://www.htwsaar.de/ingwi/fakultaet/personen/personen-a-g",
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

	// args[] => db informationen
	public static void main(String[] args) {
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
			System.out.println("Inserting Person Data into the table");
			stmt = conn.createStatement();
			String dropIfExists = "DROP TABLE IF EXISTS temp_mesa_plan;";

			String createTable ="CREATE TABLE temp_person_data (\r\n" + 
								"id int NOT NULL AUTO_INCREMENT,\r\n" + 
								"name varchar(255) ,\r\n" + 
								"email varchar(50),\r\n" + 
								"url varchar(255) ,\r\n" + 
								"office varchar(255) ,\r\n" + 
								"phone varchar(255),\r\n" + 
								"PRIMARY KEY (id)\r\n" + 
								");";
			stmt.executeUpdate(dropIfExists);
			stmt.executeUpdate(createTable);
			
			// GET PERSONS FROM WEBSITE

			for (int i = 0; i < PERSON_ARCHIVES.length; i++) {

				long timeBeforePersons = System.currentTimeMillis();

				// sql variables
				ArrayList<String> urls = new ArrayList<>();
				ArrayList<String> emails = new ArrayList<>();
				ArrayList<String> names = new ArrayList<>();
				ArrayList<String> phones = new ArrayList<>();
				ArrayList<String> offices = new ArrayList<>();

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
						System.out.println("");
					}
				}

				// get all emails + names
				for (Element email : persEmail) {
					if (email.text().contains("@")) {
						emails.add(email.text().toLowerCase());

						String[] name = parse(email.text());

						names.add(name[0] + " " + name[1]);

					}
					if (email.toString() == "") {
						System.out.println("");
					}
				}

				for (int k = 0; k < urls.size(); k++) {
					try {
						// Persons
						Document persProfile = Jsoup.connect(urls.get(k)).get();
						Elements persOffice = persProfile.select("div.kontakt-table div");
						Elements persPhone = persProfile.select("div.kontakt-table div span");

						String office = " ";
						String phone = " ";
						int zahl = 0;
						for (Element data : persOffice) {
							// einfuegen Raumnummer
							if (data.text().contains("Raum") && zahl < 1) {
								office = data.text();
								offices.add(office);
								zahl++;
							}
						}
						if (office == " ") {
							offices.add("");
						}

						for (Element data : persPhone) {

							// einfuegen Telefonnummer
							if (data.text().contains("+") || data.text().contains("0681")
									|| data.text().contains("49")) {
								phone = data.text().trim();
								phones.add(phone);
							}
						}
						if (phone == " ") {
							phones.add("");
						}

					} catch (HttpStatusException ht) {
						offices.add("");
						phones.add("");
						continue;
					}
				}

				// print needed time
				long timeNeededPersons = (System.currentTimeMillis() - timeBeforePersons);
				System.out.println("Time needed: " + timeNeededPersons + " ms - EMails - " + emails.size()
						+ " Phones - " + phones.size() + " Office - " + offices.size() + " URL - " + urls.size() + " ("
						+ persArchive.title() + ")");

				for (int j = 0; j < urls.size(); j++) {
					String sql = "INSERT INTO temp_person_data VALUES (NULL,'" + names.get(j).toLowerCase() + "', '"
							+ emails.get(j).toLowerCase() + "', '" + urls.get(j).toLowerCase() + "', '"
							+ offices.get(j).toLowerCase() + "', '" + phones.get(j).toLowerCase() + "')";
					stmt.executeUpdate(sql);
					count++;
				}
				
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
		System.out.println("Connection closed.\n" + count + " entries have been inserted.");

	} // main

} // class