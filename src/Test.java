import java.io.IOException;

import org.jsoup.*;
import org.jsoup.helper.*;
import org.jsoup.internal.*;
import org.jsoup.nodes.*;
import org.jsoup.parser.*;
import org.jsoup.safety.*;
import org.jsoup.select.*;


public class Test {

	public static void main(String[] args) {		
		
		try {
			Document doc = Jsoup.connect("https://www.htwsaar.de/ingwi/fakultaet/personen/profile/helmut-folz").get();
			//System.out.println(doc.title());
			Elements newsHeadlines = doc.select("div.kontakt-table div");
			for (Element headline : newsHeadlines) {
				if (headline.text().contains("Raum")) {
					System.out.println(headline.text());
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		}
		
		
	}

}
