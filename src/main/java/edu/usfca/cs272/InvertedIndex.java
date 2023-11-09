package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class responsible for storing and maintaining an InvertedIndex data structure
 * and a Counts data structure.
 *
 * @author Alexander Bareli
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class InvertedIndex {
	
	/** InvertedIndex Data Structure which is a Map with the key being a word and the value being
	 * another Map. The nested map has a file paths as a key and a list of numbers as value, with 
	 * each number being a position in the file where the word occurs. 
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;
	
	/** Counts Data Structure which is a simple TreeMap containing file paths as a key
	 * and the value being the number of stems the file contains
	 */
	private final TreeMap<String, Integer> counts;
		
	/** Constructor for InvertedIndex class */
	public InvertedIndex() {
		index = new TreeMap<>();
		counts = new TreeMap<>();
	}
	
	/**
	 * Search method that takes a given stemmed Set of queries and a boolean whether or not to perform a partial search.
	 * Calls the appropriate search method specified on the given Set of queries
	 * @param queries Set of queries to perform a partial or exact Search
	 * @param partial Boolean if to perform a partial search
	 * @return List of QueryMetaData objects which is the result of the given query line
	 */
	public List<QueryMetaData> search(Set<String> queries, boolean partial) {
		return partial ? partialSearch(queries) : exactSearch(queries);
	}
		
	/**
	 * Given a Set of query strings, searches for each word within the InvertedIndex index. Generates a QueryMetaData object based on 
	 * the number of words in a given file, the file it was found in, as well as its score which is the number of word occurrences dived 
	 * by the number of words. Builds and returns a TreeSet results which contains the QueryMetaData objects in sorted order 
	 * based on a custom comparator. 
	 * 
	 * @param queries Set which contains unique stems from a given query line
	 * @return List of QueryMetaData which is the generated results from one query line.
	 */
	public List<QueryMetaData> exactSearch(Set<String> queries){
		List<QueryMetaData> results = new ArrayList<>();
		HashMap<String, QueryMetaData> lookup = new HashMap<>();

		for (String query: queries) {
			if (index.containsKey(query)) {
				buildResults(query, lookup, results);
			}
		}
		Collections.sort(results);
		return results;
	}
	
	/**
	 * Same logic as exactSearch, but instead of just searching for each individual split word from a query line within the index, 
	 * it also loops over all the words within the InvertedIndex data structure, and checks to see if any of the words in the index
	 * begin with any of the split words from a given query line.
	 * 
	 * @param queries Set which contains unique stems from a given query line
	 * @return TreeSet of QueryMetaData which is the generated results from one query line.
	 */
	public List<QueryMetaData> partialSearch(Set<String> queries) {
		List<QueryMetaData> results = new ArrayList<>();
		HashMap<String, QueryMetaData> lookup = new HashMap<>();

		for (String query: queries) {
			for (String word: index.tailMap(query).keySet()) {
				if (word.startsWith(query)) {
					buildResults(word, lookup, results);
				} else {
					break;
				}
			}
		}
		Collections.sort(results);
		return results;
	}
	
	/**
	 * Builds a given results data structure by searching a given query in the InvertedIndex, and iterating over all the paths
	 * that index returns. Also maintains a lookup table for easier access to already seen QueryMetaData objects.
	 * 
	 * @param word String word to search for in the InvertedIndex
	 * @param lookup HashMap which is the lookup map to store already seen QueryMetaData objects
	 * @param results List containing all the generated QueryMetaData objects.
	 */
	private void buildResults(String word, HashMap<String, QueryMetaData> lookup, List<QueryMetaData> results) {
		for (String path: index.get(word).keySet()) {
			if (!lookup.containsKey(path)) {
				QueryMetaData queryData = new QueryMetaData(path);
				results.add(queryData);
				lookup.put(path, queryData);
			}
			lookup.get(path).update(word);
		}
	}
	
	/**
	 * Adds a word stem to the Inverted Index Map, a the given file path
	 * where the word is located, and an index where the word can be found
	 * in the file. Also updates the counts data structure
	 *
	 * @param stem the Word stem to be added to the InvertedIndex
	 * @param locations the Path where the stem is located that will be
	 * added to the inverted index
	 * @param positions the Index of where the word can be found in the file
	 */
	public void add(String stem, String locations, int positions) {
		index.putIfAbsent(stem, new TreeMap<>());
		index.get(stem).putIfAbsent(locations, new TreeSet<>());
		boolean modified = index.get(stem).get(locations).add(positions);
		if (modified) {
			counts.put(locations, counts.getOrDefault(locations, 0) + 1);
		}
	}
	
	/**
	 * Adds all the data from a given second InvertedIndex to this InvertedIndex
	 * 
	 * @param indexSecond the InvertedIndex data structure to add to this one
	 */
	public void addAll(InvertedIndex indexSecond) { 
		for (var entry: indexSecond.index.entrySet()) {
			String word = entry.getKey();
			var otherLocations = entry.getValue();
			var thisLocations = this.index.get(word);
			
			if (thisLocations == null) {
				this.index.put(word, otherLocations);
			} else {
				for (var positions: otherLocations.entrySet()) {
					if (!thisLocations.containsKey(positions.getKey())) {
						this.index.get(word).put(positions.getKey(), positions.getValue());
					} else {
						thisLocations.get(positions.getKey()).addAll(positions.getValue());
					}
				}
			}
		}
		
		for (var entry : indexSecond.counts.entrySet()) {
			var path = entry.getKey();
			var size = entry.getValue();
			if (!this.counts.containsKey(path)) {
				this.counts.put(path, size);
			} else {
				this.counts.put(path, this.counts.get(path) + size);
			}
		}
	}
		
	/**
	 * Gets the Counts Data Structure. 
	 * 
	 * @return Unmodifiable Collection of Counts Map.
	 */
	public Map<String, Number> getCounts(){
		return Collections.unmodifiableMap(counts);
	}
	
	/**
	 * Takes a given path and returns the word count
	 * 
	 * @param locations the given key to search in the counts map
	 * @return Integer the word count in given file
	 */
	public Integer getCount(String locations) {
		return counts.get(locations);
	}
			
	/**
	 * Gets the Keys of the InvertedIndex Map
	 * 
	 * @return a Unmodifiable Set which is the InvertedIndex's keySet
	 */
	public Set<String> getWords(){ 
		return Collections.unmodifiableSet(index.keySet());
	}
	
	/**
	 * Takes a word stem found in the InvertedIndex data structure and returns the associated file paths.
	 * 
	 * @param word a Key to be search in the InvertedIndex Structure
	 * @return a Unmodifiable Set containing the file paths where the word stem is found
	 */
	public Set<String> getPaths(String word) {
		return hasWord(word) ? Collections.unmodifiableSet(index.get(word).keySet()) : Collections.emptySet();
	}
	
	/**
	 * Takes a word stem and a file path, and returns a collection of positions of where the word is
	 * located within the file
	 * 
	 * @param word the Word to search within the InvertedIndex
	 * @param locations the Path where a word is found
	 * @return a Unmodifiable Set containing the word positions in the file path
	 */
	public Collection<Integer> getPositions(String word, String locations) {
		return hasStemFile(word, locations) ? Collections.unmodifiableCollection(index.get(word).get(locations)) : Collections.emptyList();
	}
		
	/**
	 * Returns the size of the Counts map
	 * 
	 * @return int which is the size of the counts map
	 */
	public int countsSize() {
		return counts.size();
	}
	
	/**
	 * Returns the size of the index data structure
	 * 
	 * @return int which is the size of the index data structure.
	 */
	public int indexSize() {
		return index.size();
	}
	
	/**
	 * Returns of the size of the nested data structure associated with a word stem
	 * found in the index data structure, which is the number of files the word is found in
	 * 
	 * @param word the Word to search for in the index data structure
	 * @return int which is the number of files the word is contained in
	 */
	public int indexWordSize(String word) {
		return getPaths(word).size();
	}
	
	/**
	 * Returns the numbers of occurrences of a given word within a given file path
	 * 
	 * @param word the Word to search for in the index data structure
	 * @param locations the Path where the word is found which returns a Set of positions
	 * @return the Size of the position Set associated with the given word and path
	 */
	public int indexPositionsSize(String word, String locations) {
		return getPositions(word, locations).size();
	}
			
	/**
	 * Checks to see if a given path is contained in the counts TreeMap
	 * 
	 * @param locations the given key to check if contained in counts 
	 * @return boolean if path is contained in counts
	 */
	public boolean containsCount(String locations) {
		return counts.containsKey(locations);
	}
	
	/**
	 * Checks to see if a given stem is contained in the index TreeMap
	 * 
	 * @param word the given word to check if contained in the index data structure
	 * @return boolean if stem is contained in index
	 */
	public boolean hasWord(String word) {
		return index.containsKey(word);
	}
		
	/**
	 * Checks to see if a given word that is contained in the index data structure
	 * is contained within a given file path
	 * 
	 * @param word the Word to search for in the index data structure
	 * @param locations the Path to check if contained in the nested data structure
	 * associated with word
	 * @return boolean if word's data structure contains path
	 */
	public boolean hasStemFile(String word, String locations) {
		return getPaths(word).contains(locations);
	}
	
	/**
	 * Checks to see if a word position exists within a given file path where the word
	 * is found that is contained in the index data structure
	 * 
	 * @param word the Word to search for in the index data structure
	 * @param locations the Path to check if contained in the nested data structure
	 * @param position the Position of the word to check if contained in the list
	 * of the nested data structure
	 * @return boolean if word has position within a given file
	 */
	public boolean hasStemFilePosition(String word, String locations, int position) {
		return getPositions(word, locations).contains(position);
	}
	
	/**
	 * Write the index data structure to a given file in a JSON format
	 * 
	 * @param path the File path to write the contents to
	 * @throws IOException if unable to write to file
	 */
	public void writeIndex(Path path) throws IOException {
		JsonWriter.writeInvertedIndex(index, path);
	}
	
	/**
	 * Writes the counts map to a given file in a JSON format
	 * 
	 * @param path the File path to write the contents to
	 * @throws IOException if unable to write to file
	 */
	public void writeCounts(Path path) throws IOException {
		JsonWriter.writeObject(counts, path);
	}
	
	/**
	 * Returns the index data structure as a string in a JSON format
	 * 
	 * @return a String containing the index data structure in JSON format
	 */
	@Override
	public String toString() {
		return JsonWriter.writeInvertedIndex(index); 
	}
	
	/**
	 * Returns the counts map as a string in a JSON format
	 * 
	 * @return a String containing the counts map in JSON format
	 */
	public String countsToString() {
		return JsonWriter.writeObject(counts); 
	}
	
	/**
	 * Nested Class responsible for storing the MetaData from a specified query. Also implements 
	 * a Comparable interface so this object could be stored in a sorted data structure.
	 */
	public class QueryMetaData implements Comparable<QueryMetaData>{
		
		/** Count which contains the total number of occurrences of a given query in a file */
		private int count;
		
		/** Score the score for a given search query which is count / total number of words in the path */
		private double score;
		
		/** Path location where the query words can be found */
		private final String path;
		
		/**
		 * Constructor for the QueryMetaData object. Initializes variables with given arguments
		 * 
		 * @param path the location where the query can be located
		 */
		public QueryMetaData(String path) {
			this.count = 0;
			this.score = 0;
			this.path = path;
		}

		/**
		 * Updates this QueryMetaData object's count and score given a query string.
		 * 
		 * @param query String query to search for in the index
		 */
		private void update(String query) {	
			this.count += index.get(query).get(path).size();
			this.score = (double) this.count / counts.get(path);
		}
		
		/**
		 * compareTo method which compares this QueryMetaData object to another object of the same type.
		 * First compares the objects by their scores, the higher score goes first. If the scores are the same,
		 * than compare the counts where the most counts gets ranked first. Finally if both the scores and counts are
		 * the same, the two objects are compared by their file paths. 
		 * 
		 * @param o the QueryMetaData object to compare to this object.
		 */
		@Override
		public int compareTo(QueryMetaData o) {
			int dscore = Double.compare(o.getScore(), this.getScore());
			int iscore = Integer.compare(o.getCount(), this.getCount());
			
			if (dscore == 0) {
				if (iscore == 0) {
					return this.getPath().compareToIgnoreCase(o.getPath());
				} 
				return iscore;
			} 
			return dscore;
		}
		
		/**
		 * Returns String representation of the QueryMetaData's results.
		 * 
		 * @return a String containing the QueryMetaData's results
		 */
		@Override
		public String toString() {
			return "\n \"count:\" " + count + "\n \"score:\" " + String.format("%.8f", score) + "\n \"where:\" " + path + "\n";
		}
		
		/**
		 * Gets this object's counts result
		 * 
		 * @return Integer which is the counts for this object
		 */
		public Integer getCount() {
			return count;
		}
		
		/**
		 * Gets this object's score result
		 * 
		 * @return Double which is the result for this object
		 */
		public Double getScore() {
			return score;
		}
		
		/**
		 * Gets this object's location
		 * 
		 * @return String is the location of the file
		 */
		public String getPath() {
			return path;
		}
	}
}
