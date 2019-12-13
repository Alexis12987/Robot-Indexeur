import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UrlReader {

	/** liste des url en attente de lecture **/
	public ConcurrentSkipListSet<String> urlWaintingList;

	/** liste des url deja lu **/
	public ConcurrentSkipListSet<String> urlAlreadyRead;

	/** liste des url qui contiennent l'expression **/
	public ConcurrentSkipListSet<String> urlMatched;

	/** expression rechercher **/
	public static String expression;

	/** constructeur **/
	public UrlReader(String startUrl, String expression) {
		urlWaintingList = new ConcurrentSkipListSet<String>();
		urlWaintingList.add(startUrl);
		urlAlreadyRead = new ConcurrentSkipListSet<String>();
		urlMatched = new ConcurrentSkipListSet<String>();
		this.expression = expression;

	}

	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();
		

		
		/** creation d'un url reader **/
		UrlReader reader = new UrlReader("https://fr.wikipedia.org/wiki/Nantes", "Nantes");

		/** creation des 10 threads **/
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (int i = 0; i < 10; i++) {
			executor.execute(new ThreadReader(reader.expression, reader));

		}

		/** tant que moins de 200 url n'ont pas été explorées **/
		while (reader.urlAlreadyRead.size() < 200) {
		}

		/** on termine les threads **/
		executor.shutdown();

		/** affichage des url qui ont matchées **/
		if (executor.isShutdown()) {
			long endTime = System.currentTimeMillis();
			long duration = (endTime - startTime);
			System.out.println("========================================================================");
			reader.urlMatched.forEach(url -> {
				System.out.println("| URL contenant le mot " + expression + " ==> " + url);

			});
			System.out.println("|Temps d'execution : " + duration /1000 +"s");

		}

	}
}
