package org.ak.step1.git.parser;

import org.ak.step1.data.RawFile;

import java.util.List;

/**
 * Created by Andrew on 02.10.2016.
 */
public class LsTreeParser {

    public void parseLsTree(List<RawFile> rawFiles, String output) {
        if (output == null || output.isEmpty()) {
            return;
        }
        rawFiles.add(new RawFile(output));
    }
}
