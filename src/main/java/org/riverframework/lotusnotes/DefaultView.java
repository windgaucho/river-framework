package org.riverframework.lotusnotes;

import java.lang.reflect.Constructor;

import org.riverframework.RiverException;

/*
 * Should I change this class' name to "Index"?
 * This must be in its own package "org.riverframework.lotusnotes"
 */
public class DefaultView implements org.riverframework.lotusnotes.View {
	protected Database rDatabase = null;
	protected lotus.domino.View view = null;
	lotus.domino.Document iteratorDoc = null;

	protected DefaultView(Database d, lotus.domino.View obj) {
		rDatabase = d;
		view = obj;

		initIterator();
	}

	@Override
	public <U extends org.riverframework.Document> U getDocumentByKey(Class<U> clazz, Object key) {
		U rDoc = null;
		lotus.domino.Document doc = null;

		if (clazz == null)
			throw new RiverException("The clazz parameter can not be null.");

		try {
			if (DefaultDocument.class.isAssignableFrom(clazz)) {

				doc = view.getDocumentByKey(key, true);

				Constructor<?> constructor = clazz.getDeclaredConstructor(Database.class, lotus.domino.Document.class);
				rDoc = clazz.cast(constructor.newInstance(rDatabase, doc));
			}
		} catch (Exception e) {
			throw new RiverException(e);
		}

		if (rDoc == null) {
			rDoc = clazz.cast(new DefaultDocument(rDatabase, null));
		} else {
			((DefaultDocument) rDoc).afterCreate().setModified(false);
		}

		return rDoc;
	}

	@Override
	public boolean isOpen() {
		return view != null;
	}

	@Override
	public DocumentCollection getAllDocumentsByKey(Object key) {
		DocumentCollection rDocumentIterator = null;

		try {
			lotus.domino.DocumentCollection col = view.getAllDocumentsByKey(key, true); // Always exact match
			rDocumentIterator = new DefaultDocumentCollection(rDatabase, col);
		} catch (Exception e) {
			throw new RiverException(e);
		}

		return rDocumentIterator;
	}

	@Override
	public View refresh() {
		try {
			view.refresh();
		} catch (Exception e) {
			throw new RiverException(e);
		}
		return this;
	}

	protected void close() {
		try {
			if (view != null) {
				view.recycle();
				view = null;
			}
		} catch (Exception e) {
			throw new RiverException(e);
		}
	}

	/*
	 * Implementing Iterator
	 */
	protected void initIterator() {
		try {
			if (view != null) {
				iteratorDoc = view.getFirstDocument();
			}
		} catch (Exception e) {
			throw new RiverException(e);
		}
	}

	@Override
	public boolean hasNext() {
		return iteratorDoc != null;
	}

	@Override
	public Document next() {
		lotus.domino.Document current = iteratorDoc;
		try {
			iteratorDoc = view.getNextDocument(iteratorDoc);
		} catch (Exception e) {
			throw new RiverException(e);
		}
		Document rDoc = new DefaultDocument(rDatabase, current);
		return rDoc;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void finalize() throws Throwable {
		close();
	}

}