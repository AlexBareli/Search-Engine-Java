package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Class responsible for building the data structures contained within the InvertedIndex class
 *
 * @author Alexander Bareli
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class InvertedIndexBuilder {
	
	/** Logger to use for debugging */
	public static final Logger log = LogManager.getLogger();
	
	/**
	 * Given a path to a valid file, builds the counts TreeMap
	 * and the index TreeMap found within the InvertedIndex class
	 * 
	 * @param path the given Path to a readable file.
	 * @param index the InvertedIndex class the will contain the data structures 
	 * 			to populate
	 * @throws IOException if unable to listStems
	 */
	public static void parseFile(Path path, InvertedIndex index) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path, UTF_8)) {
			Stemmer stemmer = new SnowballStemmer(ENGLISH);
			String line;
			int i = 1;
			while ((line = reader.readLine()) != null) {
				for (String word: FileStemmer.parse(line)) {
					index.add(stemmer.stem(word).toString(), path.toString(), i);
					i++;
				}
			}
		}
	}
		
	/** 
	 * Given a path, checks to see if the path is to a valid file and calls 
	 * the parseFile method on this path. If the path is a directory then 
	 * loops over all the readable files within the directory and calls
	 * parseFile on them. 
	 * 
	 * @param path the given Path to either a directory or a file
	 * @param index the InvertedIndex class that will contain the data structures
	 * 			to populate.
	 * @throws IOException if unable to process text file at the given path
	 */
	public static void build(Path path, InvertedIndex index) throws IOException {
		if (Files.isDirectory(path)) {
			for (Path paths : DirectoryTraverser.getTextFiles(path)) {
				parseFile(paths, index);
			}
		} else {
			parseFile(path, index);
		}
	}		
	
	/**
	 * Extends the implementation for the single threaded build(path, index) method. Creates a Task for each text file, 
	 * which will build the InvertedIndex from the text file at the path, using a thread safe implementation with a
	 * custom lock object of InvertedIndex. Uses a WorkQueue class to manage and allocate threads for each task.
	 * 
	 * @param path Path to either a directory or a file
	 * @param index ThreadSafeInvertedIndex thread safe implementation of InvertedIndex
	 * @param queue WorkQueue to use for maintaining the Task objects
	 * @throws IOException if unable to process text file at the given path
	 */
	public static void multiThreadedBuild(Path path, ThreadSafeInvertedIndex index, WorkQueue queue) throws IOException {
		log.debug("Starting MultiThreaded Build");
		if (Files.isDirectory(path)) {
			for (Path paths: DirectoryTraverser.getTextFiles(path)) {
				Runnable task = new Task(paths, index);
				queue.execute(task);
			}
		} else {
			Runnable task = new Task(path, index);
			queue.execute(task);
		}
		queue.finish();
		log.debug("Finished MultiThreaded Build");
	}
	
	/**
	 * Private Static Task class that implements Runnable which will populate the WorkQueue, that builds the ThreadSafeInvertedIndex
	 * from a given file path.
	 */
	private static class Task implements Runnable {
		
		/** Path to a Text file to process and build the index from*/
		private final Path path;
		
		/** Shared ThreadSafeInvertedIndex data structure that will be accessed by multiple Task classes*/
		private final ThreadSafeInvertedIndex index;
		
		/**
		 * Constructor for this Task class which will build ThreadSafeInvertedIndex index
		 * @param path Path to a text file to process
		 * @param index ThreadSafeInvertedIndex data structure to build
		 */
		public Task(Path path, ThreadSafeInvertedIndex index) {
			this.path = path;
			this.index = index;
			log.debug("Created Task with path: ", path);
		}

		@Override
		public void run() {
			log.debug("Building index from path: ", path);
			try {
				InvertedIndex temp = new InvertedIndex();
				parseFile(path, temp);
				index.addAll(temp);
			} catch (IOException e) {
				log.debug("IOException at path:", path);
				throw new UncheckedIOException(e);
			}
			log.debug("Built index from path: ", path);
		}
	}
}
