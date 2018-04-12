import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
	
public class emailAdresse {
	
	
	public static void parse(String input) {
		
		String [] parts = input.split("@");
		String part1 = parts[0];
	   System.out.println(part1.replace('.', ' ').toLowerCase());
	}
	
	     public static void main(String[] args) {        
	            
	            try {
	                Document doc = Jsoup.connect("https://www.htwsaar.de/ingwi/fakultaet/personen/personen-a-g").get();                
	                Elements newsHeadlines = doc.select("div.kurzprofil div a");
	                for (Element headline : newsHeadlines) {
	                      if (headline.text().contains("@htwsaar")) {
	                    	  System.out.println(headline.text());
	     	                       parse(headline.text());
	                    }
	                }
	            } catch (IOException e) {
	                System.out.println(e);
	            }
	            
	            
	        }
	} 