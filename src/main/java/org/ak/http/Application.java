package org.ak.http;

import org.ak.step2.data.DataRepository;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
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

    private final ThreadLocal<NumberFormat> nf = new ThreadLocal<NumberFormat>() {
        @Override
        protected NumberFormat initialValue() {
            NumberFormat nf = DecimalFormat.getInstance(Locale.US);
            nf.setMaximumFractionDigits(2);
            nf.setRoundingMode(RoundingMode.HALF_UP);
            nf.setGroupingUsed(false);
            return nf;
        }
    };

    public NumberFormat getDefaultNumberFormat() {
        return nf.get();
    }

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
