package org.ak.step1.git.builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 30.09.2016.
 */
public class GitLsTreeBuilder extends CommandBuilder {

    private String branchName;

    public GitLsTreeBuilder setBranchName(String branchName) {
        this.branchName = branchName;
        return this;
    }

    @Override
    public String[] buildCommand() {
        List<String> parameters = new ArrayList<>();
        parameters.add("git");
        parameters.add("ls-tree");
        parameters.add("-r");
        parameters.add("--name-only");
        parameters.add(branchName);
        return parameters.toArray(new String[parameters.size()]);
    }
}
