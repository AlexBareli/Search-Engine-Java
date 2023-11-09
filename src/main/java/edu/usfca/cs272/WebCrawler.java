package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Class responsible for building the data structures contained within the InvertedIndex class from a given URL link
 *
 * @author Alexander Bareli
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class WebCrawler {
	
	/** HashSet of already parsed URLs */
	public final HashSet<URL> seen = new HashSet<>();
	
	/**
	 * Given a link and a number of redirects, if the link is HTTP, calls parseLink to parse the HTML content,
	 * and build the inverted index.
	 * 
	 * @param link in String form to be converted to URL
	 * @param crawl int number of links to crawl and parse
	 * @param index InvertedIndex to add the stemmed HTML
	 * @throws IOException throws exception if unable to parse link
	 */
	public void crawlWeb(String link, int crawl, InvertedIndex index) throws IOException {
		URL base = null;
		try {
			base = new URL(link);
			base = LinkFinder.normalize(base);
		} catch (Exception e) {
			System.err.print("Invalid base link");
		}
		if (base != null && LinkFinder.isHttp(base)) {
			parseLink(base, index);
			seen.add(base);
			String html = HtmlFetcher.fetch(base);
			if (html != null && crawl > 1) {
				html = HtmlCleaner.stripBlockElements(html);
				ArrayList<URL> urls = new ArrayList<>();
				urls.add(base);
				LinkFinder.findUrls(base, html, urls);
				int y = 0;
				for (int i = 0; i < urls.size(); i++) {
					base = urls.get(i);
					if (y < crawl && !seen.contains(base)) {
						String currHtml = HtmlFetcher.fetch(base);
						if (currHtml != null) {
							currHtml = HtmlCleaner.stripBlockElements(currHtml);
							LinkFinder.findUrls(base, currHtml, urls);
						}
						parseLink(base, index);
						seen.add(base);
						y++;
					}
				}
			} 
		}
	}
	
	/**
	 * Given a link and a number of links to crawl, if the link if HTTP, creates a new Worker which will parse the HTML content,
	 * and build the inverted index.
	 * 
	 * @param link in String form to be converted to URL
	 * @param crawl int number of redirects to follow when fetching the HTML
	 * @param safeIndex Inverted Index to add the stemmed HTML
	 * @param queue WorkQueue to use to manage worker objects
	 * @throws IOException throws exception if unable to parse link
	 */
	public void multiThreadedCrawl(String link, int crawl, ThreadSafeInvertedIndex safeIndex, WorkQueue queue) throws IOException {
		URL base = null;
		try {
			base = new URL(link);
			base = LinkFinder.normalize(base);
		} catch (Exception e) {
			System.err.print("Invalid base link");
		}
		if (base != null && LinkFinder.isHttp(base)) {
			String html = HtmlFetcher.fetch(base);
			if (html != null) {
				html = HtmlCleaner.stripBlockElements(html);
				ArrayList<URL> urls = new ArrayList<>();
				urls.add(base);
				LinkFinder.findUrls(base, html, urls);
				int y = 0;
				for (int i = 0; i < urls.size(); i++) {
					base = urls.get(i);
					if (!seen.contains(base) && y < crawl) {
						String currHtml = HtmlFetcher.fetch(base);
						if(currHtml != null && urls.size() < crawl) {
							currHtml = HtmlCleaner.stripBlockElements(currHtml);
							LinkFinder.findUrls(base, currHtml, urls);
						}
						Runnable task = new Task(base, safeIndex);
						queue.execute(task);
						seen.add(base);
						y++;
					}
					if (y == crawl) {
						break;
					}
				}
			}
		}
		queue.finish();
	}

	/**
	 * Given a URL link and a number of redirects, follows the URL and if given a 200 OK status message, gets the HTML content
	 * which will then be parsed, stemmed and added to the given InvertedIndex. If given 300 status message, follows the number of
	 * given redirects, and returns the HTML content. Otherwise, returns null.
	 * @param link URL link to fetch the HTML content
	 * @param index InvertedIndex data structure to add parsed and stemmed html.
	 * @throws IOException throws exception if unable to fetch the HTML or if content if not HTML.
	 */
	public void parseLink(URL link, InvertedIndex index) throws IOException {
		String linkRead = HtmlFetcher.fetch(link, 3);
		if (linkRead != null) {
			String cleanHtml = HtmlCleaner.stripHtml(linkRead);
			try (BufferedReader reader = new BufferedReader(new StringReader(cleanHtml))) {
				Stemmer stemmer = new SnowballStemmer(ENGLISH);
				String line;
				int i = 1;
				while ((line = reader.readLine()) != null) {
					for (String word: FileStemmer.parse(line)) {
						if (word != "") {
							index.add(stemmer.stem(word).toString(), link.toString(), i);
							i++;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Private Static Task class that implements Runnable which will populate the WorkQueue, that builds the ThreadSafeInvertedIndex
	 * from a given URL link.
	 */
	private class Task implements Runnable {

		/** URL link to a website containing html to parse */
		private final URL link;
		
		/** ThreadSafeInvertedIndex to add the parsed HTML content to */
		private final ThreadSafeInvertedIndex safeIndex;
		
		/**
		 * Constructor for this task class which sets the URL link to parse and the ThreadSafeInvertedIndex to populate.
		 * @param link URL link to parse
		 * @param safeIndex ThreadSafeInvertedIndex to populate
		 */
		public Task(URL link, ThreadSafeInvertedIndex safeIndex) {
			this.link = link;
			this.safeIndex = safeIndex;
		}
		
		@Override
		public void run() {
			try {
				InvertedIndex temp = new InvertedIndex();
				parseLink(link, temp);
				safeIndex.addAll(temp);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}