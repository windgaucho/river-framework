package local.tests._local;

import org.riverframework.River;
import org.riverframework.core.Session;
import org.riverframework.utils.Credentials;

public final class Context extends org.riverframework.core.AbstractContext {
	@Override
	public String getConfigurationFileName() {
		return "test-configuration-lotus-domino-local";
	}

	@Override
	public Session getSession() {
		Session session = River.getSession(River.MOCK);
		return session;
	}

	@Override
	public void closeSession() {
		River.closeSession(River.MOCK);
	}
}
