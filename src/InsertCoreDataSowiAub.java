import java.io.IOException;
import java.io.*;

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
import java.net.URL;

/**
 * 
 * @author
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
												"https://www.htwsaar.de/aub/fakultaet/dekanat",
												"https://www.htwsaar.de/aub/fakultaet/personen/schule-fuer-architektur-saar/mitarbeiter/index_2014.html",
												"https://www.htwsaar.de/aub/fakultaet/personen/bauingenieurwesen%20Saar/mitarbeiter-innen-1", 
												"https://www.htwsaar.de/aub/fakultaet/personen/schule-fuer-architektur-saar/dozenten/index_2014.html",
												"https://www.htwsaar.de/aub/fakultaet/personen/bauingenieurwesen%20Saar/dozenten-1",
												"https://www.htwsaar.de/aub/fakultaet/personen/schule-fuer-architektur-saar/professoren",
												"https://www.htwsaar.de/aub/fakultaet/personen/bauingenieurwesen%20Saar/professorinnen-und-professoren",
												"https://www.htwsaar.de/aub/fakultaet/personen/schule-fuer-architektur-saar/akademische-mitarbeiter/copy_of_akademische_mitarbeiter", 
												"https://www.htwsaar.de/aub/fakultaet/personen/bauingenieurwesen%20Saar/akademische-mitarbeiter-innen", 
												};

	/*
	 * MAIN METHOD
	 */
	public static void main(String[] args) {
		for (String url: PERSON_ARCHIVES)
			crawl(url);
		return;
	} 

	private static void crawl(String url) {
		Document doc;
		try {
			doc = Jsoup.parse(new URL(url), 1000 * 60 * 2);
		} catch (IOException e) {
			System.err.println("Timeout of: " + url);
			return;
		}
		
		Element content = doc.selectFirst("div#content");
		content.traverse(new NodeVisitor() {
			
			private String content = "";
			private boolean captureContent = false;
			
			@Override
			public void head(Node node, int depth) {
				if (node instanceof Element) {
					Element el = (Element) node;
					if (el.tagName() == "h2" || el.tagName() == "strong") {
						captureContent = true;
						
						if (content.trim() != "")
							System.out.println(content + "\n");
						content = "";
					} else if (el.tagName() == "a" && captureContent) {
						//System.out.println(el.absUrl("href") + " ");		
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
				
					}
		});
	}
} // class