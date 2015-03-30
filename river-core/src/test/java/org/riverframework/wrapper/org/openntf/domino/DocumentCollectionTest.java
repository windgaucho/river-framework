package org.riverframework.wrapper.org.openntf.domino;

import lotus.domino.NotesThread;

import org.junit.After;
import org.junit.Before;

public class DocumentCollectionTest extends org.riverframework.wrapper.AbstractDocumentCollectionTest {
	@Before
	public void open() {
		NotesThread.sinitThread();
		super.open();
	}

	@After
	public void close() {
		super.close();
		NotesThread.stermThread();
	}
}