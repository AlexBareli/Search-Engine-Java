package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;

/**
 * Class responsible for traversing a directory and adding readable files to a TreeSet for processing
 * 
 * @author Alexander Bareli
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class DirectoryTraverser {
	
	/**
	 * Traverses a given directory path, and adds all files ending
	 * in .txt or .text to a ordered TreeSet.
	 *
	 * @param directory the path of the given directory to traverse
	 * @param collection the Collection which the found readable files will be added to
	 * @throws IOException if an IOException occurs
	 */
	public static void traverseDirectory(Path directory, Collection<Path> collection) throws IOException {
		try ( DirectoryStream<Path> paths = Files.newDirectoryStream(directory) ;){
			for (Path path : paths) {
				if (Files.isDirectory(path)) {
					traverseDirectory(path, collection);
				} else if (isText(path)) { 
					collection.add(path);
				}
			}
		}
	}
	
	/**
	 * Checks to see if a given path is a .txt or .text file
	 * 
	 * @param path the given path to a file
	 * @return boolean True if file ends with .txt or .text, False if otherwise
	 * or file is not a regular file.
	 */
	public static boolean isText(Path path) {
		if (Files.isRegularFile(path)) {
			String filename = path.getFileName().toString().toLowerCase();
			return (filename.endsWith(".txt") || filename.endsWith(".text") );
		}
		return false;
	}
	
	/**
	 * Creates a new HashSet and calls the traverseDirectory method. 
	 * 
	 * @param path a given path
	 * @return HashSet containing all the found .txt or .text files
	 * @throws IOException if cannot traverseDirectory
	 */
	public static HashSet<Path> getTextFiles(Path path) throws IOException {
		HashSet<Path> paths = new HashSet<>();
		traverseDirectory(path, paths);
		return paths;
	}
}
