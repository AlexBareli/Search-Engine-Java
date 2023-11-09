package edu.usfca.cs272;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Class responsible for running this project using a Java Server.
 *
 * @author Alexander Bareli
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class SearchEngineServer {
	
	/**
	 * Starts the SearchEngineServer on the specified port, using servlets
	 * 
	 * @param safeIndex ThreadSafeInvertedIndex to use with the SearchEngineServlet
	 * @param query QueryProcess to use with the SearchEngineServlet
	 * @param port number to start the server on
	 * @throws Exception if unable to start the server
	 */
	public static void startServer(ThreadSafeInvertedIndex safeIndex, QueryProcessor query, int port) throws Exception {
		ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		handler.setContextPath("/");
		handler.addServlet(new ServletHolder(new SearchEngineServlet(safeIndex, query)), "/");
		Server server = new Server(port);
		server.setHandler(handler);
		server.start();
		System.out.printf("Started server at PORT: %s :)", port);
		server.join();
	}
}
