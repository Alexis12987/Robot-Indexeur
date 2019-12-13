import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.util.concurrent.*;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.List;

public class ThreadReader implements Runnable {

	/** expresion **/
	public String expression;

	/** url reader pour recuperer la liste des url a examin�e **/
	public UrlReader urlReader;

	/** list temporaire des url trouv�es dans la page **/
	public List<String> tmpListUrl;

	/** si l'expression est trouv�e **/
	boolean expressionfind = false;

	/** constructeur **/
	ThreadReader(String expression, UrlReader urlreader) {
		this.expression = expression;
		this.urlReader = urlreader;
	}

	public void run() {

		URL oracle;
		BufferedReader in;
		String inputLine;
		Pattern pName;
		Pattern pUrl;
		String url;

		/** check 200 url max **/
		while (urlReader.urlAlreadyRead.size() < 200) {

			/** on prends le premier url de la liste et on le retire **/
			url = urlReader.urlWaintingList.pollFirst();

			/** si la liste est vide on recommence **/
			if (url == null)
				continue;

			try {
				/** l'expression n'est pas trouv�e de base **/
				expressionfind = false;

				/** on ajoute l'url aux url lu **/
				urlReader.urlAlreadyRead.add(url);

				/** on dans la console le thread qui lit et quelle url **/
				System.out.println(Thread.currentThread().getName() + " Lecture de " + url + " "
						+ urlReader.urlAlreadyRead.size());

				oracle = new URL(url);

				/** si on arrive � se connecter **/
				if (oracle.openConnection() != null) {
					HttpURLConnection httpConn = (HttpURLConnection) oracle.openConnection();

					/** si il n'y a pas d'erreur http **/
					if (httpConn.getResponseCode() == 200) {
						in = new BufferedReader(new InputStreamReader(oracle.openStream()));

						/** construit la list d'url temporaire **/
						tmpListUrl = new ArrayList<String>();

						/** regex de l'expresion **/
						pName = Pattern.compile("(" + expression + ")", Pattern.DOTALL);

						/** regex d'un url **/
						pUrl = Pattern.compile(
								"https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:_\\+.~#//=]*)",
								Pattern.DOTALL);

						/** lecture de l'url **/
						while ((inputLine = in.readLine()) != null) {

							Matcher m = pUrl.matcher(inputLine);
							Matcher m1 = pName.matcher(inputLine);

							/** si on trouve un url on l'ajoute � la liste temporaire **/
							if (m.find()) {
								tmpListUrl.add(m.group());
							}

							/** si on trouve l'expression expressionfind devient true **/
							if (m1.find()) {
								expressionfind = true;
							}
						}

						/** si on a trouv� l'expression **/
						if (expressionfind == true) {

							/** on ajoute l'url � la liste des url qui contient l'expression **/
							urlReader.urlMatched.add(url);

							/**
							 * compare si tout les url pr�sent dans cette page sont deja dans la liste des
							 * url explor�s si non on l'ajoute � la liste des url en attente d'�tre lu
							 **/
							tmpListUrl.forEach(tmpUrl -> {
								if (!urlReader.urlAlreadyRead.contains(tmpUrl)) {
									urlReader.urlWaintingList.add(tmpUrl);
								}
							});
						}
						in.close();
					}
				}

			} catch (IOException e) {

				System.out.println(Thread.currentThread().getName() + "  ERREUR sur = " + url);
			}
		}
	}
}
