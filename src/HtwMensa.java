/**
 * <h1> Extracts the diet of the "Mensa" of CAS  <h1>
 * The HtwMensa program implements an application that simply 
 * extracts the different meal types into a database.
 * 
 * @author Eric
 * @version 1.0
 * @since 11.08.18
 *
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.*;
import org.json.JSONException;
import org.json.JSONObject;

public class HtwMensa {

	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://134.96.217.36/alexa";

    // Database credentials
    static final String USER = "insert_alexa";
    static final String PASS = "!HTW_alexa18#Xgh98";

    /**
     * @param rd contains the information from the website
     * @return sb.toString() contains the information from the website in a string(JSON Format)  
     * @throws IOException
     */
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param url url of the "Mensa Plan"
	 * @return json returns an JSONObject 
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	/**
	 * <h1> summary of MAIN <1>:
	 * connect to database; creates a table (temp_mensa_plan); insert data today, tommorrow, yesterday,
	 * with no parameters; extracts the data from the URL; checks with current date which data equals today,
	 * tommorow and yesterday and saves it; insert the information in the temp_mensa_plan;
	 *  delete table mensa_plan(which is already available);
	 * alter table temp_mensa_plan to mensa_plan. 
	 * @param args unused
	 * 
	 * @throws IOException
	 * @throws JSONException
	 */
	public static void main(String[] args) throws IOException, JSONException {

		Connection conn = null;
		Statement stmt = null;
		int count = 0;

		try {
			// STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);

			// STEP 3: Open a connection
			System.out.println("Connecting to a selected database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Connected database successfully...");

			// STEP 4: Execute a query
			System.out.println("Inserting Mensa Data into the table");
			stmt = conn.createStatement();

			String dropIfExists = "DROP TABLE IF EXISTS temp_mesa_plan;";
			String createTable = "CREATE TABLE temp_mensa_plan (\r\n" + "day varchar(50) ,\r\n"
					+ "complete_meal varchar(50) ,\r\n" + "vegetarian_meal varchar(100) ,\r\n"
					+ "optional_meal varchar(100), \r\n" + "PRIMARY KEY (day)\r\n" + ");";

			String insertToday = "INSERT INTO temp_mensa_plan VALUES ('" + "today" + "', NULL, NULL, NULL)";
			String insertTomorr = "INSERT INTO temp_mensa_plan VALUES ('" + "tomorrow" + "', NULL, NULL, NULL)";
			String insertYester = "INSERT INTO temp_mensa_plan VALUES ('" + "yesterday" + "', NULL, NULL, NULL)";
			stmt.executeUpdate(dropIfExists);
			stmt.executeUpdate(createTable);
			stmt.executeUpdate(insertToday);
			stmt.executeUpdate(insertTomorr);
			stmt.executeUpdate(insertYester);
			count++;

			//read from URL
			JSONObject json = readJsonFromUrl("https://mensaar.de/api/1/ZkgY9m3Dfj9vvMp7X5Dt/1/de/getMenu/htwcas");
			
			//get current date
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();

			String[] currentDate = dateFormat.format(cal.getTime()).split("/");
			String[] days = currentDate[2].split(" ");
			
			String[] dateExtracted;
			String menu = "";
			String food = "";

			/** <h1> Important for-loop <h1>
			 * goes through the entrys which are in a JSON format,
			 * starts at "days", extracts the current date of the entry,
			 * then extracts all entries in "meals" and inserts them in 
			 * database with the current date 
			 */
			
			JSONArray entry = json.getJSONArray("days");// 1
			for (int i = 0; i < entry.length(); i++) {
				String day = "";
				JSONObject jsonObject1 = (JSONObject) entry.get(i);// 2
				JSONArray jsonarray1 = (JSONArray) jsonObject1.get("counters");// 3
				dateExtracted = ((JSONObject) entry.get(i)).get("date").toString().split("-");
				String[] date2 = dateExtracted[2].split("T");
				if (dateExtracted[0].equals(currentDate[0]) && dateExtracted[1].equals(currentDate[1]) && date2[0].equals(days[0])) {
					day = "today";
				} else if (Integer.parseInt(days[0]) < Integer.parseInt(date2[0])) {
					day = "tommorrow";
				} else {
					day = "yesterday";
				}
				for (int j = 0; j < jsonarray1.length(); j++) {
					System.out.println("\n");
					menu = "";
					food = "";
					menu = ((JSONObject) jsonarray1.get(j)).get("displayName").toString();
					JSONObject jsonObject2 = (JSONObject) jsonarray1.get(j);
					JSONArray jsonarray2 = (JSONArray) jsonObject2.get("meals");
					for (int k = 0; k < jsonarray2.length(); k++) {
						food += ((JSONObject) jsonarray2.get(k)).get("name").toString() + ", ";
					}
					String sql4 = "";
					if (menu.equals("Komplett Menü")) {
						sql4 = "UPDATE temp_mensa_plan SET complete_meal ='" + food.toLowerCase().trim()
								+ "'WHERE day ='" + day + "'";
					}
					if (menu.equals("Vegetarisches Menü")) {
						sql4 = "UPDATE temp_mensa_plan SET vegetarian_meal ='" + food.toLowerCase().trim()
								+ "'WHERE day ='" + day + "'";
					}
					if (menu.equals("Wahlessen")) {
						sql4 = "UPDATE temp_mensa_plan SET optional_meal ='" + food.toLowerCase().trim()
								+ "'WHERE day ='" + day + "'";
					}
					stmt.executeUpdate(sql4);
					count++;
					System.out.println(day + " " + menu + ": " + food);
				}
			}
			String dropTable = "DROP TABLE mensa_plan;";
			stmt.executeUpdate(dropTable);

			String alterTable = "ALTER TABLE temp_mensa_plan RENAME TO mensa_plan;";
			stmt.executeUpdate(alterTable);

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