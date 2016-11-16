package org.ak.step1.data;

import java.util.Comparator;

/**
 * Created by Andrew on 10.10.2016.
 */
public class RawCommitComparator implements Comparator<RawCommit> {

    @Override
    public int compare(RawCommit o1, RawCommit o2) {
        return o1.getHash().compareTo(o2.getHash());
    }
}
