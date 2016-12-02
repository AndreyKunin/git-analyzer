package org.ak.gitanalyzer;

import org.ak.gitanalyzer.util.Configuration;
import org.junit.Before;
import org.junit.Ignore;

/**
 * Created by Andrew on 26.11.2016.
 */
@Ignore
public abstract class AnalyserTestBase {

    @Before
    public void init() throws Exception {
        Configuration.INSTANCE.clean();
        Configuration.INSTANCE.initConfiguration();
    }
}
