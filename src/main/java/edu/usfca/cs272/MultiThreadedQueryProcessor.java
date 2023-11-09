package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.usfca.cs272.InvertedIndex.QueryMetaData;

/**
 * MutliThreaded Class responsible for storing, building, and printing the Query Results.
 *
 * @author Alexander Bareli
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class MultiThreadedQueryProcessor implements QueryProcessor {

	/** Logger to use for debugging */
	public static final Logger log = LogManager.getLogger();

	/** 
	 * results Data Structure which is a TreeMap containing a query as a key,
	 * and a Collection of QueryMetaData objects as the value.
	 */
	private final TreeMap<String, List<InvertedIndex.QueryMetaData>> results;
				
	/** safeIndex ThreadSafeInvertedIndex to be used with multi threaded searching */
	private final ThreadSafeInvertedIndex safeIndex;
		
	/** WorkQueue to use to manage the Task objects */
	private final WorkQueue queue;

	/**
	 * Constructor for the MultiThreadedQueryProcessor class with a thread safe InvertedIndex. 
	 * @param safeIndex reference to the ThreadSafeInvertedIndex which will be searched
	 * @param queue reference to the WorkQueue to manage the Task objects
	 */
	public MultiThreadedQueryProcessor(ThreadSafeInvertedIndex safeIndex, WorkQueue queue) {
		results = new TreeMap<>();
		this.safeIndex = safeIndex;
		this.queue = queue;
	}
	
	@Override
	public void processQueries(Path path, boolean partial) throws IOException {
		QueryProcessor.super.processQueries(path, partial);
		queue.finish();
	}
	
	@Override
	public void processQueries(String line, boolean partial) {
		Runnable task = new Task(line, partial);
		queue.execute(task);
	}
		
	@Override
	public void writeResults(Path path) throws IOException {
		queue.finish();
		synchronized (results) {
			JsonWriter.writeObjectArraysFiles(results, path);
		}
	}
	
	@Override
	public String toString() {
		queue.finish();
		synchronized (results) {
			return results.toString();
		}
	}
		
	@Override
	public Set<String> getQueryLines() {
		queue.finish();
		synchronized (results) {
			return Collections.unmodifiableSet(results.keySet());
		}
	}
	
	@Override
	public List<QueryMetaData> getQueryResults(String line) {
		queue.finish();
		TreeSet<String> stemmedQuery = FileStemmer.uniqueStems(line);
		String query = String.join(" ", stemmedQuery);
		synchronized (results) {
			return results.containsKey(query) ? Collections.unmodifiableList(results.get(query)) : Collections.emptyList();
		}
	}
		
	/**
	 * Private Task class that implements Runnable which will populate the WorkQueue, that searches the ThreadSafeInvertedIndex
	 * from a given query line, and builds the shared results data structure.
	 */
	private class Task implements Runnable {

		/** line String which is the query line to search for in the safeIndex */
		private final String line;
		
		/** partial Boolean whether or not to perform a partial search */
		private final boolean partial;
			
		/**
		 * Constructor for this Task object which will search the ThreadSafeInvertedIndex
		 * and also build the shared results data structure.
		 * 
		 * @param line String query to search for in the ThreadSafeInvertedIndex
		 * @param partial Boolean whether or not the perform a partial search
		 */
		public Task(String line, boolean partial) {
			this.line = line;
			this.partial = partial;
			log.debug("Created Task with line: ", line);
		}

		@Override
		public void run() {
			log.debug("Searching index with line: ", line);
			TreeSet<String> stemmedQuery = FileStemmer.uniqueStems(line);
			String query = String.join(" ", stemmedQuery);
			synchronized (results) {
				if (query.isEmpty() || results.containsKey(query)) {
					return;
				}
				results.put(query, null);
			}
			var local = safeIndex.search(stemmedQuery, partial);
			synchronized (results) {
				results.put(query, local);
			}
			log.debug("Searched index with line: ", line);
		}
	}
}
