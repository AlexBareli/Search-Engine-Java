package edu.usfca.cs272;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Java Servlet class responsible for handling doPost and doGet requests with the SearchEngineServer.
 *
 * @author Alexander Bareli
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class SearchEngineServlet extends HttpServlet {

	/** long UID for this Servlet */
	private static final long serialVersionUID = 7770202038145075202L;
	
	/** String title for this Servlet */
	private static final String TITLE = "Boogle";
	
	/** QueryProcessor to process user queries and output results from its InvertedIndex reference */
	private final QueryProcessor query;
	
	/** ThreadSafeInvertedIndex which will be used for stemmed words found in valid html */
	private ThreadSafeInvertedIndex safeIndex;
	
	/** WebCrawler to use to crawl new links and add to the safeIndex */
	WebCrawler crawler = new WebCrawler();
	
	/** String currentQuery that the user has searched */
	private String currentQuery = "";
	
	/** Generated results from searched query of links */
	private Set<String> links;
	
	/** htmlTemplate that will be outputed by the servlet */
	private final String htmlTemplate;
	
	/** String which will be outputed if failed to add to the inverted index */
	private String failed = "";
	
	/** String day which is todays day **/
	public String DAY = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
	
	/**
	 * Constructor for this servlet. Takes in a QueryProcessor which will process the user queries based on a already build InvertedIndex
	 * @param safeIndex ThreadSafeInvertedIndex to use for this servlet
	 * @param query QueryProcessor to process user queries
	 * @throws IOException if Unable to initialize servlet
	 */
	public SearchEngineServlet(ThreadSafeInvertedIndex safeIndex, QueryProcessor query) throws IOException {
		super();
		this.query = query;
		this.links = new HashSet<>();
		this.safeIndex = safeIndex;
		htmlTemplate = Files.readString(Path.of("src", "main", "resources", "SearchEngine.html"), StandardCharsets.UTF_8);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("text/html");		
		HttpSession session = request.getSession(true);
		try {
			session.getAttribute("currentQuery");
		} catch (Exception e) {
			
		}
		session.setAttribute("currentQuery", currentQuery);
		Map<String, String> values = new HashMap<>();
		values.put("title", TITLE);
		values.put("thread", Thread.currentThread().getName());
		values.put("method", "POST");
		values.put("action", request.getServletPath());
		values.put("session", session.getId());
		values.put("day", DAY);
		StringSubstitutor replacer = new StringSubstitutor(values);
		String html = replacer.replace(htmlTemplate);
		PrintWriter out = response.getWriter();
		out.println(html);
		out.println(failed);
		out.println("<em> Search Results for: " + currentQuery + "</em>");
		synchronized (links) {
			for (String link: links) {
				out.println(link);
			}
		}
		failed = "";
		out.flush();
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("text/html");
		String newLink = request.getParameter("link");
		if (newLink != "") {
			if (newLink == null || safeIndex.containsCount(newLink)) {
				failed = "<p>Failed to crawl new link or link has already been crawled </p>";
			} else {
				crawler.crawlWeb(newLink, 20, safeIndex);
			}
		}
		String usrQuery = request.getParameter("query");
		usrQuery = usrQuery == null || usrQuery.isBlank() ? "" : usrQuery;
		usrQuery = StringEscapeUtils.escapeHtml4(usrQuery);
		query.processQueries(usrQuery, true);
		links = new HashSet<>();
		List<InvertedIndex.QueryMetaData> results = query.getQueryResults(usrQuery);
		for (InvertedIndex.QueryMetaData result: results) {
			synchronized (links) {
				links.add("<li><a href=\"" + result.getPath() + "\">" + result.getPath() + "</a></li>");
			}
		}
		HttpSession session = request.getSession();
		session.setAttribute("currentQuery", usrQuery);
		currentQuery = usrQuery;
		response.setStatus(HttpServletResponse.SC_OK);
		response.sendRedirect(request.getServletPath());
	}
}
