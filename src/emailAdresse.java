import java.io.IOException;

	import org.jsoup.*;
	import org.jsoup.nodes.*;
	import org.jsoup.select.*;
	
public class emailAdresse {
	     public static void main(String[] args) {        
	            
	            try {
	                Document doc = Jsoup.connect("https://www.htwsaar.de/ingwi/fakultaet/personen/personen-a-g").get();
	                System.out.println(doc.title());
	                Elements newsHeadlines = doc.select("div.kurzprofil div a");
	                for (Element headline : newsHeadlines) {
	                         if (headline.text().contains("@htwsaar")) {
	                        System.out.println(headline.text());
	                    }
	                }
	            } catch (IOException e) {
	                System.out.println(e);
	            }
	            
	            
	        }
	} 