import java.io.IOException;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class emailAdresse {

	public static String[] parse(String input) {

		String[] parts = input.split("@");
		String name = parts[0].replace('.', ' ').toLowerCase();
		String[] part2 = name.split(" ");

		return part2;
	}

	public static void main(String[] args) {

		try {
			Document doc = Jsoup.connect("https://www.htwsaar.de/ingwi/fakultaet/personen/personen-a-g").get();
			Elements newsHeadlines = doc.select("div.kurzprofil div a");
			for (Element headline : newsHeadlines) {
				if (headline.text().contains("@htwsaar")) {
					System.out.println(headline.text());
					
					String[] arr = parse(headline.text());
					
					System.out.println(arr[0] + " " + arr[1]);
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		}

	}
}