package org.riverframework.wrapper.lotus.domino;

import java.util.logging.Logger;

import lotus.domino.NotesException;

import org.riverframework.River;
import org.riverframework.RiverException;
import org.riverframework.wrapper.Document;
import org.riverframework.wrapper.DocumentIterator;

class DefaultDocumentIterator extends DefaultBase<lotus.domino.Base> implements DocumentIterator<lotus.domino.Base, lotus.domino.Document> {
	private static final Logger log = River.LOG_WRAPPER_LOTUS_DOMINO;
	private enum Type { COLLECTION, VIEW_ENTRY_COLLECTION } 

	private org.riverframework.wrapper.Session<lotus.domino.Base> _session = null;
	private org.riverframework.wrapper.Factory<lotus.domino.Base> _factory = null;

	private volatile lotus.domino.DocumentCollection __documentCollection = null;
	private volatile lotus.domino.ViewEntryCollection __viewEntryCollection = null;

	private volatile Document<lotus.domino.Document> _doc = null;
	private volatile lotus.domino.Document __document = null;
	private volatile lotus.domino.ViewEntry __viewEntry = null;

	private Type type = null;	
	private String objectId = null;

	@SuppressWarnings("unchecked")
	protected DefaultDocumentIterator(org.riverframework.wrapper.Session<lotus.domino.Base> _s, lotus.domino.DocumentCollection __obj) {
		type = Type.COLLECTION;
		_session = _s;
		_factory = _s.getFactory();
		__documentCollection = __obj;

		synchronized (_session){
			try {
				__document = __documentCollection.getFirstDocument();

			} catch (NotesException e) {
				throw new RiverException(e);
			}
			objectId = calcObjectId(__documentCollection);
		}

	}

	@SuppressWarnings("unchecked")
	protected DefaultDocumentIterator(org.riverframework.wrapper.Session<lotus.domino.Base> _s, lotus.domino.View __obj) {
		type = Type.VIEW_ENTRY_COLLECTION;
		_session = _s;		
		_factory = _s.getFactory();

		synchronized (_session){
			try {
				__viewEntryCollection = __obj.getAllEntries();
				__viewEntry = __viewEntryCollection.getFirstEntry();

				updateCurrentDocumentFromViewEntry();
			} catch (NotesException e) {
				throw new RiverException(e);
			}

			objectId = calcObjectId(__viewEntryCollection);
		}

	}

	@SuppressWarnings("unchecked")
	protected DefaultDocumentIterator(org.riverframework.wrapper.Session<lotus.domino.Base> _s, lotus.domino.ViewEntryCollection __obj) {
		type = Type.VIEW_ENTRY_COLLECTION;		
		_session = _s;		
		_factory = _s.getFactory();
		__viewEntryCollection = __obj;

		synchronized (_session){
			try {
				__viewEntry = __viewEntryCollection.getFirstEntry();

				updateCurrentDocumentFromViewEntry();
			} catch (NotesException e) {
				throw new RiverException(e);
			}

			objectId = calcObjectId(__viewEntryCollection);
		}
	}

	private boolean isViewEntryValid(lotus.domino.ViewEntry __ve){
		if(__ve == null) return true;

		try {
			lotus.domino.Document __doc = __ve.getDocument();

			if (__doc == null) return false;

			if (isRecycled(__ve.getDocument())) return false;

			if (__doc.isDeleted()) return false;

		} catch (NotesException e) {
			throw new RiverException(e);
		}

		return true;
	}

	private void updateCurrentDocumentFromViewEntry() {
		synchronized (_session){
			try {
				while (!isViewEntryValid(__viewEntry)) 
				{
					// lotus.domino.ViewEntry __old = __ve;
					__viewEntry = __viewEntryCollection.getNextEntry(__viewEntry);
					// CHECKING __old.recycle();  // To recycle or not to recycle... That is the question.
				} 

				log.finest("Current view entry=" + (__viewEntry == null ? "<null>" :__viewEntry.getDocument().hashCode()));

				__document = __viewEntry == null ? null : __viewEntry.getDocument();				
			} catch (NotesException e) {
				throw new RiverException(e);
			}
		}
	}

	private static String internalCalcObjectId(lotus.domino.Base __object) {
		String objectId = "";
		if (__object != null) { // && !isRecycled(__object)) {

			StringBuilder sb = new StringBuilder();
			sb.append(__object.getClass().getName());
			sb.append(River.ID_SEPARATOR);
			sb.append(__object.hashCode());

			objectId = sb.toString();
		}
		return objectId;
	}

	public static String calcObjectId(lotus.domino.DocumentCollection __object) {
		return internalCalcObjectId(__object);
	}

	public static String calcObjectId(lotus.domino.ViewEntryCollection __object) {
		return internalCalcObjectId(__object);
	}

	public static String calcObjectId(lotus.domino.View __object) {
		lotus.domino.ViewEntryCollection __vec = null;
		try {
			__vec = __object == null ? null : __object.getAllEntries();
		} catch (NotesException e) {
			throw new RiverException(e);
		}

		return internalCalcObjectId(__vec);
	}

	@Override
	public String getObjectId() {
		return objectId;
	}

	@Override
	public boolean hasNext() {
		return __document != null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Document<lotus.domino.Document> next() {
		lotus.domino.Document __current = null;

		__current = __document;
		
		if (isRecycled(__current)) 
			throw new RiverException("Current object was recycled!");

		try {
			switch(type) {
			case COLLECTION:
				__document = __documentCollection.getNextDocument(__document);
				break;

			case VIEW_ENTRY_COLLECTION:
				__viewEntry = __viewEntryCollection.getNextEntry(__viewEntry);
				updateCurrentDocumentFromViewEntry();
				break;
			}
		} catch (NotesException e) {
			throw new RiverException(e);
		}


		_doc = (Document<lotus.domino.Document>) _factory.getDocument(__current); //Document<lotus.domino.Base>
		return _doc;
	}

	@Override
	public DocumentIterator<lotus.domino.Base, lotus.domino.Document> iterator() {
		return this;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();

	}

	@Override
	public DocumentIterator<lotus.domino.Base, lotus.domino.Document> deleteAll() {
		for (Document<lotus.domino.Document> doc: this) {
			doc.delete();
		}

		return this;
	}

	@Override
	public lotus.domino.Base getNativeObject() {
		switch(type) {
		case COLLECTION:
			return __documentCollection;
		case VIEW_ENTRY_COLLECTION:
			return __viewEntryCollection;
		}

		return null;
	}

	@Override
	public boolean isOpen() {
		switch(type) {
		case COLLECTION:
			return __documentCollection != null; // && !isRecycled(__documentCollection);
		case VIEW_ENTRY_COLLECTION:
			return __viewEntryCollection != null; // && !isRecycled(__viewEntryCollection);
		}

		return false;
	}

	@Override
	@Deprecated
	public void close() {

	}

	@Override
	public String toString() {
		return getClass().getName() + "(" + objectId + ")";
	}
}
