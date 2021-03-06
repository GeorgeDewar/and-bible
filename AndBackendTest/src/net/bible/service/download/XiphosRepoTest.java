package net.bible.service.download;

import java.util.List;

import junit.framework.TestCase;

import org.crosswire.jsword.book.Book;

public class XiphosRepoTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testGetXiphosRepoBooks() {
		try {
			List<Book> books = new XiphosRepo().getRepoBooks(true);
			
			assertEquals(18, books.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
