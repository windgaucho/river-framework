package org.riverframework.wrapper.domino;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import lotus.domino.NotesThread;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.riverframework.RandomString;
import org.riverframework.core.Credentials;
import org.riverframework.wrapper.Database;
import org.riverframework.wrapper.Document;
import org.riverframework.wrapper.Session;

public class DocumentTest {
	private Session session = null;
	private Database database = null;

	final String TEST_FORM = "TestForm";

	@Before
	public void init() {
		NotesThread.sinitThread();

		String password = Credentials.getPassword();
		assertFalse("Password can be an empty string", password.equals(""));

		session = new DefaultSession((String) null, (String) null, password);

		database = session.getDatabase("", Context.getDatabase());
		database.getAllDocuments().deleteAll();
	}

	@After
	public void close() {
		session.close();
		NotesThread.stermThread();

	}

	@Test
	public void testSetAndGetFieldAsString() {
		RandomString rs = new RandomString(10);

		assertTrue("The test database could not be opened.", database.isOpen());

		Document doc = database.createDocument();

		assertTrue("The document could not be created", doc.isOpen());

		doc.setField("Form", TEST_FORM);

		String testField = "TestSetField";
		String testValue = rs.nextString();
		doc.setField(testField, testValue);
		String newValue = doc.getFieldAsString(testField);

		assertTrue("The value retrieved was different to the saved", newValue.equals(testValue));

		doc.setField(testField, 20);
		String strValue = doc.getFieldAsString(testField);
		int intValue = doc.getFieldAsInteger(testField);

		assertFalse("The integer value can not be retrieved as string. ", "20".equals(strValue));
		assertTrue("The integer value can be retrieved as integer. ", 20 == intValue);
	}

	@Test
	public void testSetAndGetFieldAsInteger() {
		assertTrue("The test database could not be opened.", database.isOpen());

		Document doc = database.createDocument();

		assertTrue("The document could not be created", doc.isOpen());

		doc.setField("Form", TEST_FORM);

		String testField = "TestSetField";
		int testValue = 1000;
		doc.setField(testField, testValue);
		int newValue = doc.getFieldAsInteger(testField);

		assertTrue("The value retrieved was different to the saved", newValue == testValue);
	}

	@Test
	public void testSetAndGetFieldAsDouble() {
		assertTrue("The test database could not be opened.", database.isOpen());

		Document doc = database.createDocument();

		assertTrue("The document could not be created", doc.isOpen());

		doc.setField("Form", TEST_FORM);

		String testField = "TestSetField";
		Double testValue = 100.45;
		doc.setField(testField, testValue);
		Double newValue = doc.getFieldAsDouble(testField);

		assertTrue("The value retrieved was different to the saved",
				Double.compare(newValue, testValue) == 0);
	}

	@Test
	public void testSetAndGetFieldAsDate() {
		assertTrue("The test database could not be opened.", database.isOpen());

		Document doc = database.createDocument();

		assertTrue("The document could not be created", doc.isOpen());

		doc.setField("Form", TEST_FORM);

		String testField = "TestSetField";
		Date testValue = new Date();
		doc.setField(testField, testValue);
		Date newValue = doc.getFieldAsDate(testField);

		// Lotus Notes does not save the milliseconds
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(testValue);
		calendar.set(Calendar.MILLISECOND, 0);
		testValue = calendar.getTime();

		assertTrue("The value retrieved was different to the saved", newValue.compareTo(testValue) == 0);
	}

	@Test
	public void testSetAndGetFieldAsArrayString() {
		assertTrue("The test database could not be opened.", database.isOpen());

		Document doc = database.createDocument();

		assertTrue("The document could not be created", doc.isOpen());

		doc.setField("Form", TEST_FORM);

		String testField = "TestSetField";
		doc.setField(testField, new String[] { "VAL1", "VAL2" });
		Vector<Object> newValue = doc.getField(testField);

		assertTrue("The Array saved is different to the Array retrieved", newValue.size() == 2
				&& newValue.get(0).equals("VAL1") && newValue.get(1).equals("VAL2"));
	}

	@Test
	public void testSetAndGetFieldAsVector() {
		assertTrue("The test database could not be opened.", database.isOpen());

		Document doc = database.createDocument();

		assertTrue("The document could not be created", doc.isOpen());

		doc.setField("Form", TEST_FORM);

		String testField = "TestSetField";
		doc.setField(testField, new String[] { "VAL1", "VAL2" });
		Vector<Object> newValue = doc.getField(testField);

		assertTrue("The Vector saved is different to the Vector retrieved", newValue.size() == 2
				&& newValue.get(0).equals("VAL1") && newValue.get(1).equals("VAL2"));
	}

	@Test
	public void testGetIsFieldEmpty() {
		assertTrue("The test database could not be opened.", database.isOpen());

		Document doc = database.createDocument();

		assertTrue("The document could not be created", doc.isOpen());

		doc.setField("Form", TEST_FORM);
		doc.setField("THIS_FIELD_EXISTS", "SOME_VALUE");

		assertTrue("An inexistent field was not detected.",
				doc.isFieldEmpty("THIS_FIELD_DOES_NOT_EXIST"));
		assertFalse("An existent field was not detected.", doc.isFieldEmpty("THIS_FIELD_EXISTS"));
	}

	@Test
	public void testIsOpen() {
		assertTrue("The test database could not be opened.", database.isOpen());

		Document doc = database.getDocument();

		assertFalse("The document is not being detected as NOT open.", doc.isOpen());

		doc = null;
		doc = database.createDocument();

		assertTrue("The document could not be created", doc.isOpen());
	}

	@Test
	public void testIsNew() {
		assertTrue("The test database could not be opened.", database.isOpen());

		Document doc = database.createDocument();

		assertTrue("The document is new and is not detected like that.", doc.isNew());
	}

	@Test
	public void testGetUniversalId() {
		assertTrue("The test database could not be opened.", database.isOpen());

		Document doc = database.createDocument();
		String uniqueId = doc.getObjectId();

		assertFalse("It could not be retrieved de document's unique id.", uniqueId.equals(""));
	}

	public void testSave() {
		assertTrue("The test database could not be opened.", database.isOpen());

		RandomString rs = new RandomString(10);
		String uniqueId = "";
		Document doc = database.createDocument();

		doc.setField("Form", TEST_FORM);
		uniqueId = doc.getObjectId();

		doc = null;
		doc = database.getDocument(uniqueId);

		assertFalse("A document that was not saved was found in the database.", doc.isOpen());

		doc = null;
		doc = database.createDocument();
		uniqueId = doc.getObjectId();
		doc.save();
		doc = null;
		doc = database.getDocument(uniqueId);

		assertTrue(
				"A document was created, NOT MODIFIED and saved was not found in the database.",
				doc.isOpen());

		doc = null;
		doc = database.createDocument();
		uniqueId = doc.getObjectId();
		doc.setField("SOME_TEST_FIELD", rs.nextString());
		doc.save();
		doc = null;
		doc = database.getDocument(uniqueId);

		assertTrue(
				"A document was created, MODIFIED and saved was not found in the database.",
				doc.isOpen());
	}

	@Test
	public void testRecalc() {
		assertTrue("The test database could not be opened.", database.isOpen());

		Document doc = database.createDocument().setField("Form", TEST_FORM);
		doc.setField("CALCULATED_FROM_FORM", "TEMPORAL_DATA");

		String calculated = doc.getFieldAsString("CALCULATED_FROM_FORM");
		assertTrue("There is a problem setting the value for the field CALCULATED_FROM_FORM.", calculated.equals("TEMPORAL_DATA"));

		doc.recalc();
		calculated = doc.getFieldAsString("CALCULATED_FROM_FORM");
		assertTrue("There is a problem with the recalc() method.", calculated.equals("ROGER"));

		String non_existent = doc.getFieldAsString("NON_EXISTENT");
		assertTrue("A non-existent field returned the value '" + non_existent + "'.",
				non_existent.equals(""));
	}

}