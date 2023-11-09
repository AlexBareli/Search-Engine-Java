package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author Alexander Bareli
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class JsonWriter {
	
	/**
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Writer writer, int indent) throws IOException {
		while (indent-- > 0) {
			writer.write("  ");
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuote(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeArray(Collection<? extends Number> elements, Writer writer, int indent) throws IOException {		
		writeIndent("[", writer, 0);
		var iterate = elements.iterator();
		if (iterate.hasNext()) {
			writer.write("\n");
			writeIndent(String.valueOf(iterate.next()), writer, indent + 1);
		}
		while (iterate.hasNext()) {
			writer.write(",\n");
			writeIndent(String.valueOf(iterate.next()), writer, indent + 1);
		}
		writer.write("\n");
		writeIndent("]", writer, indent);
	}
	
	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeArray(Collection<? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String writeArray(Collection<? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArray(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Writer writer, int indent) throws IOException {		
		writeIndent("{", writer, 0);
		var iterate = elements.entrySet().iterator();
		if (iterate.hasNext()) {
			var entry = iterate.next();
			writer.write("\n");
			writeQuote(entry.getKey(), writer, indent + 1);
			writer.write(": " + String.valueOf(entry.getValue()));
		}
		while (iterate.hasNext()) {
			var entry = iterate.next();
			writer.write(",\n");
			writeQuote(entry.getKey(), writer, indent + 1);
			writer.write(": " + String.valueOf(entry.getValue()));
		}
		writer.write("\n");
		writeIndent("}", writer, indent);	
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String writeObject(Map<String, ? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObject(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of number objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Writer writer,
			int indent) throws IOException {		
		writeIndent("{", writer, 0);
		var iterate = elements.entrySet().iterator();
		if (iterate.hasNext()) {
			var entry = iterate.next();
			writer.write("\n");
			writeQuote(entry.getKey(), writer, indent + 1);
			writer.write(": ");
			writeArray(entry.getValue(), writer, indent + 1);
		}
		while (iterate.hasNext()) {
			var entry = iterate.next();
			writer.write(",\n");
			writeQuote(entry.getKey(), writer, indent + 1);
			writer.write(": ");
			writeArray(entry.getValue(), writer, indent + 1);
		}
		writer.write("\n");
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectArrays(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObjectArrays(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects. The generic
	 * notation used allows this method to be used for any type of collection with
	 * any type of nested map of String keys to number objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		writeIndent("[", writer, 0);
		var iterate = elements.iterator();
		if (iterate.hasNext()) {
			var entry = iterate.next();
			writer.write("\n");
			writeIndent(writer, indent + 1);
			writeObject(entry, writer, indent + 1);
		}
		while (iterate.hasNext()) {
			var entry = iterate.next();
			writer.write(",\n");
			writeIndent(writer, indent + 1);
			writeObject(entry, writer, indent + 1);
		}
		writer.write("\n");
		writeIndent("]", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArrayObjects(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArrayObjects(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Writes a given Inverted Index data structure as a pretty JSON format
	 * 
	 * @param iterate a Iterator that contains the entrySet of the Inverted Index data structure
	 * @param writer the BufferedWriter to use to output the contents to a
	 * 		  specified output, or index.json as default
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IOException occurs
	 */
	public static void writeIndexArray(Iterator<? extends Map.Entry<String, ? extends Map<String, ? extends Collection<Integer>>>> iterate, 
			Writer writer, int indent) throws IOException {
		var entry = iterate.next();
		writer.write("\n");
		writeQuote(entry.getKey(), writer, indent + 1);
		writer.write(": ");
		writeObjectArrays(entry.getValue(), writer, indent + 1);
	}

	/**
	 * Traverses a Inverted Index data structure and prints the contents
	 * as a pretty JSON format
	 *
	 * @param index the InvertedIndex data structure to write
	 * @param writer the BufferedWriter to use to output the contents to a
	 * 		  specified output, or index.json as default
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IOException occurs
	 */
	public static void writeInvertedIndex(Map<String, ? extends Map<String, ? extends Collection<Integer>>> index, Writer writer, int indent) throws IOException {
		writeIndent("{", writer, 0);
		var iterate = index.entrySet().iterator();
		if (iterate.hasNext()) {
			writeIndexArray(iterate, writer, indent);
		}
		while (iterate.hasNext()) {
			writer.write(",");
			writeIndexArray(iterate, writer, indent);
		}
		writer.write("\n");
		writeIndent("}", writer, indent);
	}
	
	/**
	 * Writes the InvertedIndex as a pretty JSON array to file.
	 *
	 * @param index the InvertedIndex data structure to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeInvertedIndex(Map<String, ? extends Map<String, ? extends Collection<Integer>>> index, Path path) 
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeInvertedIndex(index, writer, 0);
		}
	}
	
	/**
	 * Returns the InvertedIndex as a pretty JSON array.
	 *
	 * @param index the InvertedIndex data structure to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String writeInvertedIndex(Map<String, ? extends Map<String, ? extends Collection<Integer>>> index) {
		try {
			StringWriter writer = new StringWriter();
			writeInvertedIndex(index, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Writes a Collection of QueryMetaData objects as a pretty JSON object.
	 * 
	 * @param query the Collection to write
	 * @param writer the Writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQueryObject(Collection<InvertedIndex.QueryMetaData> query, Writer writer, int indent) throws IOException {
		var iterate = query.iterator();
		if (iterate.hasNext()) {
			var entry = iterate.next();
			if(entry.getPath() != null) {
				writer.write("\n");
				writeIndent("{\n", writer, indent + 1);
				writeIndent("  \"count\": ", writer, indent + 1);
				writer.write(entry.getCount() + ",\n");
				writeIndent("  \"score\": ", writer, indent + 1);
				writer.write(String.format("%.8f", entry.getScore()) + ",\n");
				writeIndent("  \"where\": ", writer, indent + 1);
				writer.write("\"" + entry.getPath() + "\"\n");
				writeIndent("}", writer, indent + 1);
			}
		}
		while (iterate.hasNext()) {
			var entry = iterate.next();
			writer.write(",\n");
			writeIndent("{\n", writer, indent + 1);
			writeIndent("  \"count\": ", writer, indent + 1);
			writer.write(entry.getCount() + ",\n");
			writeIndent("  \"score\": ", writer, indent + 1);
			writer.write(String.format("%.8f", entry.getScore()) + ",\n");
			writeIndent("  \"where\": ", writer, indent + 1);
			writer.write("\"" + entry.getPath() + "\"\n");
			writeIndent("}", writer, indent + 1);
		}
	}
	
	/**
	 * Writes the elements as a pretty JSON object with nested arrays. This method
	 * is to be used with a Map that contains a nested Collection of QueryMetaData objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObjectArraysFiles(Map<String, ? extends Collection<InvertedIndex.QueryMetaData>> elements, Writer writer,
			int indent) throws IOException {	
		
		writeIndent("{", writer, indent);
		var iterate = elements.entrySet().iterator();
		if (iterate.hasNext()) {
			var entry = iterate.next();
			writer.write("\n");
			writeQuote(entry.getKey(), writer, indent + 1);
			writer.write(": [");
			writeQueryObject(entry.getValue(), writer, indent + 1);
			writer.write("\n");
			writeIndent("]", writer, indent + 1);
		}
		while (iterate.hasNext()) {
			var entry = iterate.next();
			writer.write(",\n");
			writeQuote(entry.getKey(), writer, indent + 1);
			writer.write(": [");
			writeQueryObject(entry.getValue(), writer, indent + 1);
			writer.write("\n");
			writeIndent("]", writer, indent + 1);
		}
		writer.write("\n");
		writeIndent("}", writer, indent);
	}
	
	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 * This method is to be used a nested Collection of QueryMetaData objects.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObjectArraysFiles(Map<String, ? extends Collection<InvertedIndex.QueryMetaData>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectArraysFiles(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 * This method is to be used a nested Collection of QueryMetaData objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String writeObjectArraysFiles(Map<String, ? extends Collection<InvertedIndex.QueryMetaData>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObjectArraysFiles(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}
}
