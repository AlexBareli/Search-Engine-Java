package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Alexander Bareli
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class Driver {

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {		
		SearchEngineServer server;
		ArgumentParser parser = new ArgumentParser(args);
		ThreadSafeInvertedIndex safeIndex = null;
		WebCrawler crawler = new WebCrawler();
		WorkQueue workers = null;
		InvertedIndex index;
		QueryProcessor query;
		
		if (parser.hasFlag("-threads") || parser.hasFlag("-server")) {
			int threads = parser.getInteger("-threads");
			if (threads < 1) {
				workers = new WorkQueue(5);
			} else {
				workers = new WorkQueue(threads);
			}
			safeIndex = new ThreadSafeInvertedIndex();
			query = new MultiThreadedQueryProcessor(safeIndex, workers);
			index = safeIndex;
		} else {
			index = new InvertedIndex();
			query = new SingleThreadedQueryProcessor(index);
		}
		
		if (parser.hasFlag("-text")) {
			Path path = parser.getPath("-text");
			if(safeIndex != null && workers != null) {
				try {
					InvertedIndexBuilder.multiThreadedBuild(path, safeIndex, workers);
				} catch (Exception e) {
					System.err.println("Unable to process input file at: " + path);
				}
			} else {
				try {
					InvertedIndexBuilder.build(path, index);
				} catch (Exception e) {
					System.err.println("Unable to process input file at: " + path);
				}
			}
		}
		
		if (parser.hasFlag("-html")) {
			String url = parser.getString("-html");
			int crawl = parser.getInteger("-crawl");
			if (crawl < 1) {
				crawl = 1;
			}
			if(safeIndex != null && workers != null) {
				try {
					if (url != null) {
						crawler.multiThreadedCrawl(url, crawl, safeIndex, workers);
					}
				} catch (IOException e) {
					System.err.println("Unable to process link at:" + url);
				}
			} else {
				try {
					if (url != null) {
						crawler.crawlWeb(url, crawl, index);
					} 
				} catch (IOException e) {
					System.err.println("Unable to process link at: " + url);
				}
			}
		}
		if (parser.hasFlag("-query")) {
			Path path = parser.getPath("-query");
			boolean partial = parser.hasFlag("-partial");
			try {
				query.processQueries(path, partial);
			} 
			catch (Exception e) {
				System.err.println("Unable to process query file at: " + path + e);
			}
		}
				
		if (parser.hasFlag("-server")) {
			int port = parser.getInteger("-server");
			if (port < 0) {
				port = 8080;
			}
			try {
				SearchEngineServer.startServer(safeIndex, query, port);
			} catch (Exception E) {
				System.err.println("Unable to Start Server at PORT:" + port);
			}
		}
		
		if (workers != null) {
			workers.shutdown();
		}
			
		if (parser.hasFlag("-counts")) {
			try {
				index.writeCounts(parser.getPath("-counts", Path.of("counts.json")));
			} catch (Exception e) {
				System.err.println("Could not write counts in JSON format");
			}
		}
			
		if (parser.hasFlag("-index")) {
			try {
				index.writeIndex(parser.getPath("-index", Path.of("index.json")));
			} catch (Exception e) {
				System.err.println("Could not write index in JSON format");
			}
		}
					
		if (parser.hasFlag("-results")) {
			try {
				query.writeResults(parser.getPath("-results", Path.of("results.json")));
			} catch (Exception e) {
				System.err.println("Could not write results in JSON format");
			}
		}
	}
}
