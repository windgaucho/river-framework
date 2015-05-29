package org.riverframework.wrapper.lotus.domino;

import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;

import lotus.domino.NotesException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.riverframework.River;
import org.riverframework.RiverException;
import org.riverframework.wrapper.Database;
import org.riverframework.wrapper.Session;

public class DefaultSession implements Session {
	private static final Logger log = River.LOG_WRAPPER_LOTUS_DOMINO;
	private final DefaultFactory factory = DefaultFactory.getInstance(this);

	private lotus.domino.Session __session = null;
	private String objectId = null;

	protected DefaultSession(lotus.domino.Session obj) {
		__session = obj;
		objectId = UUID.randomUUID().toString();

		log.fine("ObjectId:" + getObjectId());
	}

	public DefaultFactory getFactory() {
		return factory;
	}

	@Override
	public String getObjectId() {
		return objectId;
	}

	@Override
	public lotus.domino.Session getNativeObject() {
		return __session;
	}

	@Override
	public boolean isOpen() {
		return (__session != null);
	}

	@Override
	public Database getDatabase(String... location) {
		log.fine("location=" + Arrays.deepToString(location));

		lotus.domino.Database __database = null;

		if (location.length != 2)
			throw new RiverException("It is expected two parameters: server and path, or server and replicaID");

		String server = location[0];
		String path = location[1];

		try {
			if (path.length() == 16) {
				log.finer("Trying with a replica ID");
				boolean res = false;
				__database = __session.getDatabase(null, null);
				res = __database.openByReplicaID(server, path);
				if (!res) __database = null;
			}
		} catch (NotesException e) {
//			try {
//				if (__database != null) 
//					__database.recycle();
//			} catch (NotesException e1) {
//				Do nothing
//			} finally {
//				__database = null;
//			}
		}

		try {
			if (__database == null || !__database.isOpen()) {
				log.finer("Trying with a file path");
				__database = __session.getDatabase(server, path, false);
			}
		} catch (NotesException e) {
//			try {
//				if (__database != null) { 
//					__database.recycle();
//				}
//			} catch (NotesException e1) {
//				Do nothing
//			} finally {
//				__database = null;
//			}
		}

		try {
			if (__database != null && !__database.isOpen()) {
				log.finer("The database could not be opened");
				try {
					__database.recycle();
				} catch (NotesException e) {
					throw new RiverException(e);
				} finally {	
					__database = null;
				}
			}
		} catch (NotesException e) {
			throw new RiverException(e);
		}
		
		//Database database = Factory.createDatabase(this, _database);
		//Database database = getPool().create(org.riverframework.wrapper.lotus.domino.DefaultDatabase.class, __database);

		Database database = new DefaultDatabase(this, __database);

		return database;
	}

	@Override
	public String getUserName() {
		String userName = "";

		try {
			userName = __session.getUserName();
		} catch (NotesException e) {
			throw new RiverException(e);
		}

		log.finest("getUserName=" + userName);
		return userName;
	}

	@Override
	public void close() {
		log.fine("Closing factory");
		getFactory().close();

		log.fine("Recycling the session");
		try {
			__session.recycle();
		} catch (NotesException e) {
			throw new RiverException(e);
		} finally {
			__session = null;
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public void finalize() {
		log.finest("Finalized: id=" + objectId + " (" + this.hashCode() + ")");
	}
}
