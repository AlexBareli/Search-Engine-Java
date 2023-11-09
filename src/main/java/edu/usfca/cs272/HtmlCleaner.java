package edu.usfca.cs272;

import org.apache.commons.text.StringEscapeUtils;

/**
 * Cleans simple, validating HTML 4/5 into plain text. For simplicity, this
 * class cleans already validating HTML, it does not validate the HTML itself.
 * For example, the {@link #stripEntities(String)} method removes HTML entities
 * but does not check that the removed entity was valid.
 *
 * @author Alexander Bareli
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class HtmlCleaner {
	
	/**
	 * Replaces all HTML tags with an empty string. For example, the html
	 * {@code A<b>B</b>C} will become {@code ABC}.
	 *
	 * @param html text including HTML tags to remove
	 * @return text without any HTML tags
	 */
	public static String stripTags(String html) {
		String regex = "<[^>|<]*>";
		return html.replaceAll(regex, "");
	}

	/**
	 * Replaces all HTML 4 entities with their Unicode character equivalent or, if
	 * unrecognized, replaces the entity code with an empty string. For example:,
	 * {@code 2010&ndash;2012} will become {@code 2010–2012} and {@code &gt;&dash;x}
	 * will become {@code >x} with the unrecognized {@code &dash;} entity getting
	 * removed. (The {@code &dash;} entity is valid HTML 5, but not HTML 4 which
	 * this code uses.) Should also work for entities that use decimal syntax like
	 * {@code &#8211;} or {@code &#x2013}.
	 *
	 * @param html text including HTML entities to remove
	 * @return text with all HTML entities converted or removed
	 */
	public static String stripEntities(String html) {
		html = StringEscapeUtils.unescapeHtml4(html);
		String regex = "&\\w*;";
		return html.replaceAll(regex, "");
	}

	/**
	 * Replaces all HTML comments with an empty string. 
	 * 
	 * @param html text including HTML comments to remove
	 * @return text without any HTML comments
	 */
	public static String stripComments(String html) {
		String regex = "<!--[\\s\\S]*?-->";
		return html.replaceAll(regex, "");
	}

	/**
	 * Replaces everything between the element tags and the element tags themselves
	 * with an empty string.
	 *
	 * @param html text including HTML elements to remove
	 * @param name name of the HTML element (like "style" or "script")
	 * @return text without that HTML element
	 */
	public static String stripElement(String html, String name) {	    
		String regex = "(?i)<(%s\\b)[^>]*>([^<]*|.?)*</%s\\b[^>]*>";
		regex = regex.replaceAll("%s", name);
		return html.replaceAll(regex, "");
	}

	/**
	 * Removes comments and certain block elements from the provided html. The block
	 * elements removed include: head, style, script, noscript, iframe, and svg.
	 *
	 * @param html the HTML to strip comments and block elements from
	 * @return text clean of any comments and certain HTML block elements
	 */
	public static String stripBlockElements(String html) {
		html = stripComments(html);
		html = stripElement(html, "head");
		html = stripElement(html, "style");
		html = stripElement(html, "script");
		html = stripElement(html, "noscript");
		html = stripElement(html, "iframe");
		html = stripElement(html, "svg");
		return html;
	}

	/**
	 * Removes all HTML tags and certain block elements from the provided text.
	 *
	 * @param html the HTML to strip tags and elements from
	 * @return text clean of any HTML tags and certain block elements
	 */
	public static String stripHtml(String html) {
		html = stripBlockElements(html);
		html = stripTags(html);
		html = stripEntities(html);
		return html;
	}
}
