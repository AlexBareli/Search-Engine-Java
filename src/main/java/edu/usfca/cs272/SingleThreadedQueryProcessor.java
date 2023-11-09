package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import edu.usfca.cs272.InvertedIndex.QueryMetaData;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Class responsible for storing, building, and printing the Query Results.
 *
 * @author Alexander Bareli
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class SingleThreadedQueryProcessor implements QueryProcessor {
	
	/** 
	 * results Data Structure which is a TreeMap containing a query as a key,
	 * and a Collection of QueryMetaData objects as the value.
	 */
	private final TreeMap<String, List<InvertedIndex.QueryMetaData>> results;
	
	/** index InvertedIndex to be used with single threaded searching */
	private final InvertedIndex index;
		
	/** Stemmer to use when stemming each query line */
	private final Stemmer stemmer;
	
	/**
	 * Constructor for the QueryProcessor class. 
	 * @param index reference to the InvertedIndex which will be searched
	 */
	public SingleThreadedQueryProcessor(InvertedIndex index) {
		results = new TreeMap<>();
		this.index = index;
		this.stemmer = new SnowballStemmer(ENGLISH);
	}
			
	@Override
	public void processQueries(String line, boolean partial) {
		TreeSet<String> stemmedQuery = FileStemmer.uniqueStems(line, stemmer);
		String query = String.join(" ", stemmedQuery);
		if(!query.isEmpty() && !results.containsKey(query)) {
			results.put(query, index.search(stemmedQuery, partial));
		}	
	}
		
	@Override
	public void writeResults(Path path) throws IOException {
		JsonWriter.writeObjectArraysFiles(results, path);
	}
	
	@Override
	public String toString() {
		return results.toString();
	}
		
	@Override
	public Set<String> getQueryLines() {
		return Collections.unmodifiableSet(results.keySet());
	}
	
	@Override
	public List<QueryMetaData> getQueryResults(String line) {
		TreeSet<String> stemmedQuery = FileStemmer.uniqueStems(line, stemmer);
		String query = String.join(" ", stemmedQuery);
		return results.containsKey(query) ? Collections.unmodifiableList(results.get(query)) : Collections.emptyList();
	}
}
