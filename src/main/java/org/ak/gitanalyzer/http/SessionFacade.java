package org.ak.gitanalyzer.http;

import org.ak.gitanalyzer.step2.data.Author;
import org.ak.gitanalyzer.step3.*;
import org.ak.gitanalyzer.step3.data.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.ak.gitanalyzer.http.ServiceFacade.SESSION_ID;
import static org.ak.gitanalyzer.util.FileHelper.quoteFileMask;

/**
 * Created by Andrew on 26.10.2016.
 */
public enum SessionFacade {
    INSTANCE;

    public String getDateFilter(Map<String, String> parameters) {
        return getSession(parameters).getCurrentDateFilter().name();
    }

    public void setDateFilter(Map<String, String> parameters, String filter) {
        Session session = getSession(parameters);
        try {
            Session.DateFilter dateFilter = Session.DateFilter.valueOf(filter);
            session.setCurrentDateFilter(dateFilter);
        } catch (Exception e) {
            System.out.println("Unknown date filter: " + filter);
        }
    }

    public String getFileMask(Map<String, String> parameters) {
        Session session = getSession(parameters);
        String fileMask = session.getFileMask();
        return fileMask == null ? "" : fileMask;
    }

    public Pattern getFileMaskPattern(Map<String, String> parameters) {
        Session session = getSession(parameters);
        String fileMaskQuoted = session.getFileMaskQuoted();
        return fileMaskQuoted == null ? null : Pattern.compile(fileMaskQuoted);
    }

    public void setFileMask(Map<String, String> parameters, String fileMask) {
        Session session = getSession(parameters);
        String fileMaskQuoted = quoteFileMask(fileMask);
        if (fileMaskQuoted == null) {
            fileMask = null;
        }
        session.setFileMask(fileMask, fileMaskQuoted);
    }

    public Date getDateFrom(Map<String, String> parameters) {
        Session session = getSession(parameters);
        return session.getDateFrom();
    }

    public List<AgeStatistics> lazyGetFilesAgeStatistics(Map<String, String> parameters, TimelineAnalyzer timelineAnalyzer) {
        Session session = getSession(parameters);
        List<AgeStatistics> result = session.getFilesAgeStatistics();
        if (result == null) {
            result = timelineAnalyzer.analyzeFilesAge(Application.INSTANCE.getDataRepository());
            session.setFilesAgeStatistics(result);
        }
        return result;
    }

    public List<AgeStatistics> lazyGetModulesAgeStatistics(Map<String, String> parameters, TimelineAnalyzer timelineAnalyzer) {
        Session session = getSession(parameters);
        List<AgeStatistics> result = session.getModulesAgeStatistics();
        if (result == null) {
            result = timelineAnalyzer.analyzeModulesAge(Application.INSTANCE.getDataRepository());
            session.setModulesAgeStatistics(result);
        }
        return result;
    }

    public AuthorGroupStatistics lazyGetTeamStatistics(Map<String, String> parameters, AuthorsAnalyzer authorsAnalyzer) {
        Session session = getSession(parameters);
        AuthorGroupStatistics result = session.getTeamsStatistics();
        if (result == null) {
            AuthorStatistics authorStatistics = lazyGetAuthorStatistics(parameters, authorsAnalyzer);
            result = authorsAnalyzer.getContributions(authorStatistics, as -> as.getAuthor().getTeam());
            session.setTeamsStatistics(result);
        }
        return result;
    }

    public AuthorGroupStatistics lazyGetLocationStatistics(Map<String, String> parameters, AuthorsAnalyzer authorsAnalyzer) {
        Session session = getSession(parameters);
        AuthorGroupStatistics result = session.getLocationsStatistics();
        if (result == null) {
            AuthorStatistics authorStatistics = lazyGetAuthorStatistics(parameters, authorsAnalyzer);
            result = authorsAnalyzer.getContributions(authorStatistics, as -> as.getAuthor().getLocation());
            session.setLocationsStatistics(result);
        }
        return result;
    }

    public AuthorStatistics lazyGetAuthorStatistics(Map<String, String> parameters, AuthorsAnalyzer authorsAnalyzer) {
        Session session = getSession(parameters);
        AuthorStatistics result = session.getAuthorsStatistics();
        if (result == null) {
            result = authorsAnalyzer.getAllContributors(Application.INSTANCE.getDataRepository());
            session.setAuthorsStatistics(result);
        }
        return result;
    }

    public FileStatistics lazyGetFileStatistics(Map<String, String> parameters, FileCommitsAnalyzer fileCommitsAnalyzer) {
        Session session = getSession(parameters);
        FileStatistics result = session.getFileStatistics();
        if (result == null) {
            result = fileCommitsAnalyzer.getFileStatistics(Application.INSTANCE.getDataRepository());
            session.setFileStatistics(result);
        }
        return result;
    }

    public FileStatistics lazyGetModuleStatistics(Map<String, String> parameters, FileCommitsAnalyzer fileCommitsAnalyzer) {
        Session session = getSession(parameters);
        FileStatistics result = session.getModuleStatistics();
        if (result == null) {
            FileStatistics fileStatistics = lazyGetFileStatistics(parameters, fileCommitsAnalyzer);
            result = fileCommitsAnalyzer.getModuleStatistics(fileStatistics);
            session.setModuleStatistics(result);
        }
        return result;
    }

    public List<FileAuthors> lazyGetFileAuthorsStatistics(Map<String, String> parameters, FileAuthorsAnalyzer fileAuthorsAnalyzer) {
        Session session = getSession(parameters);
        List<FileAuthors> result = session.getFileAuthorsStatistics();
        if (result == null) {
            result = fileAuthorsAnalyzer.getSharedFiles(Application.INSTANCE.getDataRepository());
            session.setFileAuthorsStatistics(result);
        }
        return result;
    }

    public List<FileAuthors> lazyGetModuleAuthorsStatistics(Map<String, String> parameters, FileAuthorsAnalyzer fileAuthorsAnalyzer) {
        Session session = getSession(parameters);
        List<FileAuthors> result = session.getModuleAuthorsStatistics();
        if (result == null) {
            List<FileAuthors> fileStatistics = lazyGetFileAuthorsStatistics(parameters, fileAuthorsAnalyzer);
            result = fileAuthorsAnalyzer.getSharedModules(fileStatistics);
            session.setModuleAuthorsStatistics(result);
        }
        return result;
    }

    public List<ActivitySummary<Author>> lazyGetAuthorsActivity(Map<String, String> parameters, TimelineAnalyzer timelineAnalyzer) {
        Session session = getSession(parameters);
        List<ActivitySummary<Author>> result = session.getAuthorActivity();
        if (result == null) {
            result = timelineAnalyzer.analyzeAuthorsActivity(Application.INSTANCE.getDataRepository());
            session.setAuthorActivity(result);
        }
        return result;
    }

    public List<ActivitySummary<String>> lazyGetTeamsActivity(Map<String, String> parameters, TimelineAnalyzer timelineAnalyzer) {
        Session session = getSession(parameters);
        List<ActivitySummary<String>> result = session.getTeamActivity();
        if (result == null) {
            List<ActivitySummary<Author>> authorsActivity = lazyGetAuthorsActivity(parameters, timelineAnalyzer);
            result = timelineAnalyzer.analyzeTeamsActivity(authorsActivity);
            session.setTeamActivity(result);
        }
        return result;
    }

    public ActivitySummary<String> lazyGetProjectActivity(Map<String, String> parameters, TimelineAnalyzer timelineAnalyzer) {
        Session session = getSession(parameters);
        ActivitySummary<String> result = session.getProjectActivity();
        if (result == null) {
            List<ActivitySummary<Author>> authorsActivity = lazyGetAuthorsActivity(parameters, timelineAnalyzer);
            result = timelineAnalyzer.analyzeProjectActivity(authorsActivity);
            session.setProjectActivity(result);
        }
        return result;
    }

    public Graph lazyGetFileDependencies(Map<String, String> parameters, GraphAnalyzer graphAnalyzer) {
        return lazyGetDependencies(parameters, graphAnalyzer).getFileGraph();
    }

    public Graph lazyGetModuleDependencies(Map<String, String> parameters, GraphAnalyzer graphAnalyzer) {
        return lazyGetDependencies(parameters, graphAnalyzer).getModuleGraph();
    }

    private Forest lazyGetDependencies(Map<String, String> parameters, GraphAnalyzer graphAnalyzer) {
        Session session = getSession(parameters);
        Forest result = session.getDependencies();
        if (result == null) {
            result = graphAnalyzer.getDependencies(Application.INSTANCE.getDataRepository(), 3);
            session.setDependencies(result);
        }
        return result;
    }

    public List<Author> lazyGetFilteredAuthors(Map<String, String> parameters, AuthorsAnalyzer authorsAnalyzer) {
        Session session = getSession(parameters);
        List<Author> result = session.getFilteredAuthors();
        if (result == null) {
            result = authorsAnalyzer.getAuthors(Application.INSTANCE.getDataRepository());
            session.setFilteredAuthors(result);
        }
        return result;
    }

    public Session getSession(Map<String, String> parameters) {
        return Application.INSTANCE.getSession(parameters.get(SESSION_ID));
    }

}
