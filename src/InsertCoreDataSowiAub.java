import java.io.IOException;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.sql.*; // for standard JDBC programs
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.net.URL;

/**
 * 
 * <h1> Extracts data of professors from the given Urls <h1>
 * The InsertCoreDataSowiAub program implements an application that simply 
 * extracts the different professors of the given Urls into a database.
 * 
 * @author Eric
 * @version 1.0
 * @since 11.08.18
 *
 */

public class InsertCoreDataSowiAub {
	private static int count;
	// Person Archive
	static final String[] PERSON_ARCHIVES = { "https://www.htwsaar.de/sowi/fakultaet/personen/index_2014.html",
			"https://www.htwsaar.de/sowi/fakultaet/personen/professoren",
			"https://www.htwsaar.de/sowi/fakultaet/personen/Wissenschaftliche-Mitarbeiterinnen-und-Mitarbeiter",
			"https://www.htwsaar.de/aub/fakultaet/dekanat",
			"https://www.htwsaar.de/aub/fakultaet/personen/schule-fuer-architektur-saar/mitarbeiter/index_2014.html",
			"https://www.htwsaar.de/aub/fakultaet/personen/bauingenieurwesen%20Saar/mitarbeiter-innen-1",
			"https://www.htwsaar.de/aub/fakultaet/personen/schule-fuer-architektur-saar/dozenten/index_2014.html",
			"https://www.htwsaar.de/aub/fakultaet/personen/bauingenieurwesen%20Saar/dozenten-1",
			"https://www.htwsaar.de/aub/fakultaet/personen/schule-fuer-architektur-saar/professoren",
			"https://www.htwsaar.de/aub/fakultaet/personen/bauingenieurwesen%20Saar/professorinnen-und-professoren",
			"https://www.htwsaar.de/aub/fakultaet/personen/schule-fuer-architektur-saar/akademische-mitarbeiter/copy_of_akademische_mitarbeiter",
			"https://www.htwsaar.de/aub/fakultaet/personen/bauingenieurwesen%20Saar/akademische-mitarbeiter-innen", };

	/*
	 *Main Method
	 */
	public static void main(String[] args) {
		
		Connection conn = null;
		Statement stmt = null;

		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			conn = DriverManager.getConnection(args[0], args[1], args[2]);

			// STEP 4: Execute a query
			stmt = conn.createStatement();
		
		for (String url : PERSON_ARCHIVES) {
			crawl(url, args);
		}
		String dropTable = "DROP TABLE person_data;";
		stmt.executeUpdate(dropTable);
		
		String alterTable = "ALTER TABLE temp_person_data RENAME TO person_data;";
		stmt.executeUpdate(alterTable);
		System.out.println("Connection closed.\n" + count + " entries have been inserted. (Sowi + Aub)");
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

	/**
	 * starts at a h2 or an strong tag(name of professor) and collects the specific information
	 * of the professor adds this to an HashMap and continous till there is no data left,
	 * selects the data which is now saved in the HashMap and writes it into database
	 * @param url contains url
	 * @param args contains db credentials
	 */
	private static void crawl(String url, String[] args) {
		Document doc;
		try {
			doc = Jsoup.parse(new URL(url), 1000 * 60 * 2);
		} catch (IOException e) {
			System.err.println("Timeout of: " + url);
			return;
		}

		ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

		Element content = doc.selectFirst("div#content");
		NodeVisitor visitior = new NodeVisitor() {

		 	private String content = "";
			private boolean captureContent = false;
			HashMap<String, String> current = new HashMap<String, String>();

			@Override
			public void head(Node node, int depth) {
				if (node instanceof Element) {
					Element el = (Element) node;
					if (el.tagName() == "h2" || el.tagName() == "strong") {
						captureContent = true;

						if (!current.isEmpty()) {
							data.add(current);
							current = new HashMap<String, String>();
						}

						if (!el.text().trim().isEmpty()) {
							current.put("name", el.text().trim());
						}
					} else if (el.tagName() == "a" && captureContent) {
						if (el.absUrl("href").contains("mailto") && el.absUrl("href").contains("@")) {
							String mail = el.absUrl("href");
							String mails[] = mail.split(":");
							current.put("mail", mails[1]);
						} else if (el.absUrl("href").contains("personen") || el.absUrl("href").contains("author")) {
							String url = el.absUrl("href");
							current.put("url", url);
						}
					} else if(el.tagName() == "p" && captureContent){
						if(el.text().contains("Email") || el.text().contains("Mail")) {
							String[] email = el.text().split(":");
							current.put("mail", email[email.length-1].trim());
						}
					}
					else if (el.tagName() == "div" && captureContent) {
						if (el.text().contains("+49") && el.text().startsWith("t")) {
							current.put("tel", el.text());
						}
						if (el.text().startsWith("Raum")) {
							current.put("room", el.text());
						}
					} else if (captureContent) {
						// System.out.println(el.getElementsByTag("h2").text().trim());
					} else if (el.tagName() == "div" && el.id() == "content") {
						// System.out.println("content");
					}
				} else if (node instanceof TextNode) {
					data((TextNode) node);
				}
			}

			private void data(TextNode node) {
				if (captureContent) {
					if (!node.text().trim().isEmpty())
						content += node.text().trim() + "\n";
				}
			}

			@Override
			public void tail(Node node, int depth) {
				if (depth == 1 && node.nextSibling() == null) {
					if (!current.isEmpty()) {
						data.add(current);
						current = new HashMap<String, String>();
					}
				}
			}
		};

		content.traverse(visitior);
		Connection conn = null;
		Statement stmt = null;

		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			conn = DriverManager.getConnection(args[0], args[1], args[2]);

			// STEP 4: Execute a query
			stmt = conn.createStatement();

			for (HashMap<String, String> dataSet : data) {
				String mail = "";
				String name = "";
				String tel = "";
				String room = "";
				String urls = "";

				// Insert PERSONS FROM WEBSITE
				for (Entry<String, String> entry : dataSet.entrySet()) {
					if (entry.getKey().equals("mail")) {
						mail = entry.getValue().toLowerCase();
					}
					if (entry.getKey().equals("name")) {
						name = entry.getValue().toLowerCase();
					}
					if (entry.getKey().equals("tel")) {
						tel = entry.getValue().toLowerCase();
					}
					if (entry.getKey().equals("room")) {
						room = entry.getValue().toLowerCase();
					}
					if (entry.getKey().equals("url")) {
						urls = entry.getValue().toLowerCase();
					}

					// System.out.println(entry.getKey() + ": " + entry.getValue());
				}
				if (mail.trim().isEmpty() && urls.trim().isEmpty() && room.trim().isEmpty() && tel.trim().isEmpty()
						|| name.trim().isEmpty()) {
					// System.out.println("Nicht genuegend Informationen zum Anlegen einer
					// relevanten Person");
				} else {
					String sql = "INSERT INTO temp_person_data VALUES (NULL,'" + name + "', '" + mail + "', '" + urls
							+ "', '" + room + "', '" + tel + "')";
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
	}
} // class