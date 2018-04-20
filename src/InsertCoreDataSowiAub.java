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

					// Persons
					Document persArchive = Jsoup.connect(PERSON_ARCHIVES[i]).get();
					
					Elements infoBlocks = persArchive.select("div.kontaktdaten");
				
					for (Element block: infoBlocks) {
						String email ="";
						String room = "";
						String phone = "";
						String url="";
						Elements mailElements = block.select("[href^=mailto]");
						if (mailElements.isEmpty()) {
							email = null;
						} else {
							email = mailElements.first().attr("href").substring(7);
						}

						Elements rooms = block.select("div:containsOwn(Raum)");
						//Elements phone = block.select(":contains(+)");
						for (Element roomT: rooms)
							room = roomT.text();
						
						Elements phones = block.select("span:containsOwn(+)");
						for (Element phoneT: phones)
							phone = phoneT.text();
						
						// Weitere Infos suchen
						List<Element> nextSiblings = block.siblingElements().subList(
							block.elementSiblingIndex(),
							block.siblingElements().size()
						);
						
						Element moreInfo = null;
						for (Element el: nextSiblings) {
							if (el.is("div.kontaktdaten")) {
								// Nächhster Kontaktdatenblock erreicht
								break;
							} else if (!el.select("a").isEmpty()) {
								// Mehr Informationen Block gefunden
								moreInfo = el;
								break;
							}
						}
						
						if (moreInfo == null) {
							url = "No URL available";
						} else {
							url= moreInfo.select("a").first().attr("href");
							if(PERSON_ARCHIVES[i].contains("professoren") || PERSON_ARCHIVES[i].contains("Mitarbeiter"))
									url = PERSON_ARCHIVES[i] +"/"+ url;
						}
					Person p = new Person(email, url, room, phone);
					stmt.executeUpdate(p.toSql());
					count++;
					}
				}

			} catch (IOException e) {
				System.out.println(e);
			}
			
			
			// print needed time
			long timeNeeded = (System.currentTimeMillis() - timeBefore);
			System.out.println("\n\nTotal time needed: " + timeNeeded + " ms - "+ count + " additional people added(Sowi)");
			

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