package org.ak.gitanalyzer.http;

import org.ak.gitanalyzer.step2.data.Author;
import org.ak.gitanalyzer.step3.data.*;

import java.util.Date;
import java.util.List;

/**
 * Created by Andrew on 14.10.2016.
 */
public class Session {

    public static final long MS_IN_DAY = 24 * 60 * 60 * 1000L;

    private final String id;

    private FileStatistics fileStatistics;
    private FileStatistics moduleStatistics;
    private List<FileAuthors> fileAuthorsStatistics;
    private List<FileAuthors> moduleAuthorsStatistics;
    private AuthorStatistics authorsStatistics;
    private AuthorGroupStatistics teamsStatistics;
    private AuthorGroupStatistics locationsStatistics;
    private List<AgeStatistics> filesAgeStatistics;
    private List<AgeStatistics> modulesAgeStatistics;
    private List<ActivitySummary<Author>> authorActivity;
    private List<ActivitySummary<String>> teamActivity;
    private ActivitySummary<String> projectActivity;
    private Forest dependencies;
    private List<Author> filteredAuthors;

    private String fileMask;
    private String fileMaskQuoted;
    private DateFilter currentDateFilter = DateFilter.ALL;
    private Date dateFrom;
    private Date nextTimeToRenew = new Date();

    private void invalidateData() {
        fileStatistics = null;
        moduleStatistics = null;
        fileAuthorsStatistics = null;
        moduleAuthorsStatistics = null;
        authorsStatistics = null;
        teamsStatistics = null;
        locationsStatistics = null;
        filesAgeStatistics = null;
        modulesAgeStatistics = null;
        authorActivity = null;
        teamActivity = null;
        projectActivity = null;
        dependencies = null;
        filteredAuthors = null;
    }

    public Session(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public FileStatistics getFileStatistics() {
        return fileStatistics;
    }

    public void setFileStatistics(FileStatistics fileStatistics) {
        this.fileStatistics = fileStatistics;
    }

    public FileStatistics getModuleStatistics() {
        return moduleStatistics;
    }

    public void setModuleStatistics(FileStatistics moduleStatistics) {
        this.moduleStatistics = moduleStatistics;
    }

    public List<FileAuthors> getFileAuthorsStatistics() {
        return fileAuthorsStatistics;
    }

    public void setFileAuthorsStatistics(List<FileAuthors> fileAuthorsStatistics) {
        this.fileAuthorsStatistics = fileAuthorsStatistics;
    }

    public List<FileAuthors> getModuleAuthorsStatistics() {
        return moduleAuthorsStatistics;
    }

    public void setModuleAuthorsStatistics(List<FileAuthors> moduleAuthorsStatistics) {
        this.moduleAuthorsStatistics = moduleAuthorsStatistics;
    }

    public AuthorStatistics getAuthorsStatistics() {
        return authorsStatistics;
    }

    public void setAuthorsStatistics(AuthorStatistics authorsStatistics) {
        this.authorsStatistics = authorsStatistics;
    }

    public AuthorGroupStatistics getTeamsStatistics() {
        return teamsStatistics;
    }

    public void setTeamsStatistics(AuthorGroupStatistics teamsStatistics) {
        this.teamsStatistics = teamsStatistics;
    }

    public AuthorGroupStatistics getLocationsStatistics() {
        return locationsStatistics;
    }

    public void setLocationsStatistics(AuthorGroupStatistics locationsStatistics) {
        this.locationsStatistics = locationsStatistics;
    }

    public List<AgeStatistics> getFilesAgeStatistics() {
        return filesAgeStatistics;
    }

    public void setFilesAgeStatistics(List<AgeStatistics> filesAgeStatistics) {
        this.filesAgeStatistics = filesAgeStatistics;
    }

    public List<AgeStatistics> getModulesAgeStatistics() {
        return modulesAgeStatistics;
    }

    public void setModulesAgeStatistics(List<AgeStatistics> modulesAgeStatistics) {
        this.modulesAgeStatistics = modulesAgeStatistics;
    }

    public List<ActivitySummary<Author>> getAuthorActivity() {
        return authorActivity;
    }

    public void setAuthorActivity(List<ActivitySummary<Author>> authorActivity) {
        this.authorActivity = authorActivity;
    }

    public List<ActivitySummary<String>> getTeamActivity() {
        return teamActivity;
    }

    public void setTeamActivity(List<ActivitySummary<String>> teamActivity) {
        this.teamActivity = teamActivity;
    }

    public ActivitySummary<String> getProjectActivity() {
        return projectActivity;
    }

    public void setProjectActivity(ActivitySummary<String> projectActivity) {
        this.projectActivity = projectActivity;
    }

    public Forest getDependencies() {
        return dependencies;
    }

    public void setDependencies(Forest dependencies) {
        this.dependencies = dependencies;
    }

    public List<Author> getFilteredAuthors() {
        return filteredAuthors;
    }

    public void setFilteredAuthors(List<Author> filteredAuthors) {
        this.filteredAuthors = filteredAuthors;
    }

    public DateFilter getCurrentDateFilter() {
        return currentDateFilter;
    }

    public void setCurrentDateFilter(DateFilter currentFilter) {
        if (this.currentDateFilter != currentFilter) {
            invalidateData();
        }
        this.currentDateFilter = currentFilter;
        switch (currentFilter) {
            case ALL:
                dateFrom = null;
                break;
            case LAST_12:
                dateFrom = new Date(System.currentTimeMillis() - 365 * MS_IN_DAY);
                break;
            case LAST_6:
                dateFrom = new Date(System.currentTimeMillis() - 365 * MS_IN_DAY / 2);
                break;
            case LAST_3:
                dateFrom = new Date(System.currentTimeMillis() - 91 * MS_IN_DAY);
                break;
            case LAST_1:
                dateFrom = new Date(System.currentTimeMillis() - 30 * MS_IN_DAY);
                break;
        }
        nextTimeToRenew = new Date(System.currentTimeMillis() + MS_IN_DAY / 24);
    }

    public Date getDateFrom() {
        if (nextTimeToRenew.getTime() <= System.currentTimeMillis()) {
            setCurrentDateFilter(currentDateFilter);
        }
        return dateFrom;
    }

    public void setFileMask(String fileMaskOriginal, String fileMaskQuoted) {
        if ((this.fileMaskQuoted == null) != (fileMaskQuoted == null) || this.fileMaskQuoted != null && !this.fileMaskQuoted.equals(fileMaskQuoted)) {
            invalidateData();
        }
        this.fileMask = fileMaskOriginal;
        this.fileMaskQuoted = fileMaskQuoted;
    }

    public String getFileMask() {
        return fileMask;
    }

    public String getFileMaskQuoted() {
        return fileMaskQuoted;
    }

    public enum DateFilter {
        ALL, LAST_12, LAST_6, LAST_3, LAST_1
    }
}
