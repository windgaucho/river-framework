package org.riverframework.wrapper.lotus.domino;

// import java.util.logging.Level;
// import java.util.logging.Logger;

import lotus.domino.NotesException;

import org.riverframework.River;
import org.riverframework.RiverException;
import org.riverframework.wrapper.Document;
import org.riverframework.wrapper.DocumentIterator;
import org.riverframework.wrapper.View;

class DefaultView extends DefaultBase<lotus.domino.View> implements org.riverframework.wrapper.View<lotus.domino.View> {
	// private static final Logger log = River.LOG_WRAPPER_LOTUS_DOMINO;
	protected org.riverframework.wrapper.Session<lotus.domino.Session> _session = null;
	protected volatile lotus.domino.View __view = null;
	private String objectId = null;

	protected DefaultView(org.riverframework.wrapper.Session<lotus.domino.Session> s, lotus.domino.View v) {
		__view = v;
		_session = s;
		// synchronized (_session){
		objectId = calcObjectId(__view);
		// }		
	}

	@Override
	public String getObjectId() {
		return objectId;
	}

	public static String calcObjectId(lotus.domino.View __view) {
		String objectId = "";

		if (__view != null) { // && !isRecycled(__view)) {
			try {
				lotus.domino.Database __database = __view.getParent();

				StringBuilder sb = new StringBuilder(1024);
				sb.append(__database.getServer());
				sb.append(River.ID_SEPARATOR);
				sb.append(__database.getFilePath());
				sb.append(River.ID_SEPARATOR);
				sb.append(__view.getName());

				objectId = sb.toString();
			} catch (NotesException e) {
				throw new RiverException(e);
			}	
		}

		return objectId;
	}

	@Override
	public lotus.domino.View getNativeObject() {
		return __view;
	}

	@Override
	public Document<lotus.domino.Base> getDocumentByKey(String key) {
		// synchronized (_session){
		lotus.domino.Document __doc = null;

		try {
			__doc = __view.getDocumentByKey(key, true);
		} catch (NotesException e) {
			throw new RiverException(e);
		}

		@SuppressWarnings("unchecked")
		Document<lotus.domino.Base> doc = _session.getFactory().getDocument(__doc);

		return doc;
		// }
	}

	@Override
	public boolean isOpen() {
		return __view != null && !isRecycled(__view);
	}

	@Override
	public DocumentIterator<lotus.domino.Base, lotus.domino.Document> getAllDocuments() {
		// synchronized (_session){
		lotus.domino.ViewEntryCollection _col;
		try {
			_col = __view.getAllEntries();
		} catch (NotesException e) {
			throw new RiverException(e);
		}

		@SuppressWarnings("unchecked")
		DocumentIterator<lotus.domino.Base, lotus.domino.Document> result = _session.getFactory().getDocumentIterator(_col);
		return result;
		// }
	}

	@Override
	public DocumentIterator<lotus.domino.Base, lotus.domino.Document> getAllDocumentsByKey(Object key) {
		// synchronized (_session){
		lotus.domino.DocumentCollection _col;
		try {
			_col = __view.getAllDocumentsByKey(key, true);
		} catch (NotesException e) {
			throw new RiverException(e);
		}

		@SuppressWarnings("unchecked")
		DocumentIterator<lotus.domino.Base, lotus.domino.Document> result = _session.getFactory().getDocumentIterator(_col);
		return result;
		// }
	}

	@Override
	public View<lotus.domino.View> refresh() {
		// synchronized (_session){
		try {
			__view.refresh();
		} catch (NotesException e) {
			throw new RiverException(e);
		}
		// }
		return this;
	}

	@Override
	public void delete() {
		// synchronized (_session){
		if (__view != null) {
			try {
				__view.remove();
				// __view.recycle();  <== Let the server to recycle
			} catch (NotesException e) {
				throw new RiverException(e);
			} finally {
				__view = null;
			}
		}
		// }
	}

	@Override
	public DocumentIterator<lotus.domino.Base, lotus.domino.Document> search(String query) {
		// synchronized (_session){
		lotus.domino.View __temp = null;

		try {
			__temp = __view.getParent().getView(__view.getName());			
			__temp.FTSearch(query);
		} catch (NotesException e) {
			throw new RiverException(e);
		}

		@SuppressWarnings("unchecked")
		DocumentIterator<lotus.domino.Base, lotus.domino.Document> _iterator = _session.getFactory().getDocumentIterator(__temp);

		return _iterator;
		// }
	}

	@Override
	@Deprecated
	public void close() {
		// Don't recycle or close it. Let the server do that.
	}

	@Override
	public String toString() {
		return getClass().getName() + "(" + objectId + ")";
	}
}
