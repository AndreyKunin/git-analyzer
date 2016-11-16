package org.ak.step3;

import org.ak.step2.data.Link;

import java.util.Date;

/**
 * Created by Andrew on 05.10.2016.
 */
public abstract class Analyzer {

    protected Date dateFrom;
    protected Date dateTo;

    public <T extends Analyzer> T cast() {
        return (T) this;
    }

    public Analyzer setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
        return this;
    }

    public Analyzer setDateTo(Date dateTo) {
        this.dateTo = dateTo;
        return this;
    }

    //returns false if filtering out
    protected boolean filter(Link link) {
        Date commitTime = link.getCommit().getDateTime();
        return !(dateFrom != null && dateFrom.compareTo(commitTime) > 0) &&
                !(dateTo != null && dateTo.compareTo(commitTime) <= 0);
    }
}
