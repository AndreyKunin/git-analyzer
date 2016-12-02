package org.ak.gitanalyzer.mock;

import org.ak.gitanalyzer.step1.git.Subprocess;
import org.ak.gitanalyzer.step1.git.SubprocessException;
import org.junit.Assert;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.hamcrest.core.IsCollectionContaining.hasItem;

/**
 * Created by Andrew on 21.11.2016.
 */
public class SubprocessMock extends Subprocess {

    private Map<String, SubprocessResult> expectedResults = new ConcurrentHashMap<>();
    private Map<String, SubprocessException> expectedExceptions = new ConcurrentHashMap<>();
    private Map<String, String> executedCommands = new ConcurrentHashMap<>();

    public SubprocessMock(Map<String, SubprocessResult> expectedResults, Map<String, SubprocessException> expectedExceptions) {
        this.expectedResults.putAll(expectedResults);
        this.expectedExceptions.putAll(expectedExceptions);
    }

    public void start(Consumer<Integer> exitCallback, String ... command) throws SubprocessException {

    }

    public SubprocessResult execute(String workingDirectory, boolean redirectStderr, String ... command) throws SubprocessException {
        String key = String.join(",", Arrays.asList(command));
        executedCommands.put(key, "");
        if (expectedExceptions.containsKey(key)) {
            throw expectedExceptions.get(key);
        }
        if (expectedResults.containsKey(key)) {
            return expectedResults.get(key);
        }
        return null;//should not be reached. Check the test if it does.
    }

    public void assertCalls() {
        Assert.assertEquals(expectedResults.size() + expectedExceptions.size(), executedCommands.size());
        Set<String> expectedCalls = new HashSet<>(expectedExceptions.keySet());
        expectedCalls.addAll(expectedResults.keySet());
        expectedCalls.forEach(expectedResult -> {
            Set<String> actual = executedCommands.keySet();
            Assert.assertThat(actual, hasItem(expectedResult));
        });
    }
}
