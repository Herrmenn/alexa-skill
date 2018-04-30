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
 * ausfuehren aller Files
 *
 */
public class ExecuteInsertion {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/alexa";

	// Database credentials
	static final String USER = "root";
	static final String PASS = "";

	
	/*
	 * MAIN METHOD
	 */
	public static void main(String[] args) {
		
		String dbInfo[] = {DB_URL, USER, PASS};
			InsertDatesSemester.main(dbInfo);
			InsertCoreData.main(dbInfo);
			InsertProfileData.main(dbInfo);
					
	} //main
} // class