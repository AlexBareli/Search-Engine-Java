package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import edu.usfca.cs272.InvertedIndex.QueryMetaData;

/**
 * Interface for constructing the SingleThreadedQueryProcessor and MultiThreadedQueryProcessor classes
 *
 * @author Alexander Bareli
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public interface QueryProcessor {
	
	/**
	 * From a given path to a query file, reads the file and passes each line to
	 * processQueries(String, boolean) for stemming and searching.
	 * 
	 * @param path the Path to a given query file
	 * @param partial Boolean whether or not to perform a partial search
	 * @throws IOException if unable to open the query File or other exception
	 */
	default void processQueries(Path path, boolean partial) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path, UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				processQueries(line, partial);
			}
		}
	}
	
	/**
	 * Given a line of queries, builds the results data structure with the line as the key, and the resultant
	 * TreeSet of QueryMetaData objects that is returned by partialSearch/exactSeach as the value. 
	 * 
	 * @param line String of queries to search for in the InvertedIndex data structure
	 * @param partial Boolean whether or not to perform a partial search
	 */
	public void processQueries(String line, boolean partial);
	
	/**
	 * Writes the results map to a given file in a JSON format
	 * 
	 * @param path the File path to write the contents to
	 * @throws IOException if unable to write to file
	 */
	public void writeResults(Path path) throws IOException;
	
	/**
	 * Returns String representation of the results data structure.
	 * 
	 * @return a String of the results data structure. 
	 */
	public String toString();
	
	/**
	 * Returns all the stemmed and processed queries in the results data structure
	 * 
	 * @return TreeSet of Strings which is the keySet of the results data structure
	 */
	public Set<String> getQueryLines();
	
	/**
	 * Given a String query, returns its results from the results data structure
	 * 
	 * @param line the String to search for in the results data structure
	 * @return List of QueryMetaData objects which are the results for the given query
	 */
	public List<QueryMetaData> getQueryResults(String line);
}
