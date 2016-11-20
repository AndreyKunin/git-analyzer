package org.ak.gitanalyzer.http;

import org.ak.gitanalyzer.step2.data.DataRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andrew on 26.10.2016.
 */
public enum Application {
    INSTANCE;

    /**
     * @GuardedBy(this)
     */
    private Map<String, Session> sessions = new HashMap<>();

    /**
     * @GuardedBy(this)
     */
    private DataRepository dataRepository;

    public synchronized Session getSession(String sessionId) {
        if (!sessions.containsKey(sessionId)) {
            Session session = new Session(sessionId);
            sessions.put(sessionId, session);
        }
        return sessions.get(sessionId);
    }

    public synchronized DataRepository getDataRepository() {
        return dataRepository;
    }

    public synchronized void setDataRepository(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
        sessions.clear();
    }
}
