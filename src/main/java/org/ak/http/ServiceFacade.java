package org.ak.http;

import org.ak.http.processor.*;
import org.ak.step1.git.builder.IExploreBuilder;
import org.ak.step2.data.Author;
import org.ak.step2.data.DataRepository;
import org.ak.step3.AuthorsAnalyzer;
import org.ak.step3.FileAuthorsAnalyzer;
import org.ak.step3.FileCommitsAnalyzer;
import org.ak.step3.data.*;
import org.ak.step3.GraphAnalyzer;
import org.ak.step3.data.ActivitySummary;
import org.ak.step3.data.AgeStatistics;
import org.ak.step3.TimelineAnalyzer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrew on 14.10.2016.
 */
public enum ServiceFacade {
    INSTANCE;

    public static final String FORM_RETURN_PAGE = "return_page";
    public static String PATH = "/PATH/";
    public static final String SESSION_ID = "JSESSIONID";

    public String processTemplate(Map<String, String> parameters) {
        return new TemplateProcessor(parameters).processTemplate(parameters.get(PATH));
    }

    public String get500Content(Exception e) {
        return ContentProcessor.INSTANCE.get500Content(e);
    }

    public String get404Content(String path) {
        return ContentProcessor.INSTANCE.get404Content(path);
    }

    public String getStringContent(String path) throws IOException {
        return ContentProcessor.INSTANCE.getStringContent(path);
    }

    public byte[] getBinaryContent(String path) throws IOException {
        return ContentProcessor.INSTANCE.getBinaryContent(path);
    }

    public ContentType getType(String path) {
        return ContentProcessor.INSTANCE.getType(path);
    }

    public boolean isDateFilterEqual(TemplateProcessor processor, String period) {
        return period != null && period.equals(SessionFacade.INSTANCE.getDateFilter(processor.getParameters()));
    }

    public String getFileMask(TemplateProcessor processor, String ignored) {
        return SessionFacade.INSTANCE.getFileMask(processor.getParameters());
    }

    public String filterStatistics(Map<String, String> parameters) {
        String period = parameters.get("period");
        if (period != null) {
            SessionFacade.INSTANCE.setDateFilter(parameters, period);
        }

        String setFileMask = parameters.get("set_filemask");
        if (setFileMask != null && setFileMask.equals("true")) {
            String fileMask = parameters.get("filemask");
            SessionFacade.INSTANCE.setFileMask(parameters, fileMask);
        }

        String returnPage = parameters.get(FORM_RETURN_PAGE);
        if (returnPage == null || returnPage.length() == 0) {
            parameters.put(FORM_RETURN_PAGE, IExploreBuilder.START_PAGE);
        }

        return null;
    }

    public String getFileStatisticsForPlot(TemplateProcessor processor, String axe) {
        FileCommitsAnalyzer fileCommitsAnalyzer = new FileCommitsAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(processor.getParameters()))
                .cast();
        FileStatistics fileStatistics = SessionFacade.INSTANCE.lazyGetFileStatistics(processor.getParameters(), fileCommitsAnalyzer);
        FileStatisticsProcessor statisticsProcessor = new FileStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat());
        switch (axe) {
            case "x":
                return statisticsProcessor.getXAxe(fileStatistics, 30);
            case "y":
                return statisticsProcessor.getYAxe(fileStatistics, 30);
            case "y_ideal":
                return statisticsProcessor.getIdealAxe(fileStatistics, 30);
        }
        return null;
    }

    public String getModuleStatisticsForPlot(TemplateProcessor processor, String axe) {
        FileCommitsAnalyzer fileCommitsAnalyzer = new FileCommitsAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(processor.getParameters()))
                .cast();
        FileStatistics moduleStatistics = SessionFacade.INSTANCE.lazyGetModuleStatistics(processor.getParameters(), fileCommitsAnalyzer);
        FileStatisticsProcessor statisticsProcessor = new FileStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat());
        switch (axe) {
            case "x":
                return statisticsProcessor.getXAxe(moduleStatistics, 30);
            case "y":
                return statisticsProcessor.getYAxe(moduleStatistics, 30);
            case "y_ideal":
                return statisticsProcessor.getIdealAxe(moduleStatistics, 30);
        }
        return null;
    }

    public String getHotFiles(Map<String, String> parameters) {
        FileCommitsAnalyzer fileCommitsAnalyzer = new FileCommitsAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        FileStatistics fileStatistics = SessionFacade.INSTANCE.lazyGetFileStatistics(parameters, fileCommitsAnalyzer);
        FileStatisticsProcessor processor = new FileStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getHotFiles(fileStatistics, "path");
    }

    public String getHotFilesCSV(Map<String, String> parameters) {
        FileCommitsAnalyzer fileCommitsAnalyzer = new FileCommitsAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        FileStatistics fileStatistics = SessionFacade.INSTANCE.lazyGetFileStatistics(parameters, fileCommitsAnalyzer);
        FileStatisticsProcessor processor = new FileStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getHotFilesCSV(fileStatistics);
    }

    public String getHotModules(Map<String, String> parameters) {
        FileCommitsAnalyzer fileCommitsAnalyzer = new FileCommitsAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        FileStatistics moduleStatistics = SessionFacade.INSTANCE.lazyGetModuleStatistics(parameters, fileCommitsAnalyzer);
        FileStatisticsProcessor processor = new FileStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getHotFiles(moduleStatistics, "moduleName");
    }

    public String getHotModulesCSV(Map<String, String> parameters) {
        FileCommitsAnalyzer fileCommitsAnalyzer = new FileCommitsAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        FileStatistics moduleStatistics = SessionFacade.INSTANCE.lazyGetModuleStatistics(parameters, fileCommitsAnalyzer);
        FileStatisticsProcessor processor = new FileStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getHotFilesCSV(moduleStatistics);
    }

    public String getSharedModulesForBar(TemplateProcessor processor, String axe) {
        FileAuthorsAnalyzer fileAuthorsAnalyzer = new FileAuthorsAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(processor.getParameters()))
                .cast();
        List<FileAuthors> moduleStatistics = SessionFacade.INSTANCE.lazyGetModuleAuthorsStatistics(processor.getParameters(), fileAuthorsAnalyzer);
        SharedFilesStatisticsProcessor statisticsProcessor = new SharedFilesStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat());
        if (axe.equals("x")) {
            return statisticsProcessor.getAxeX(moduleStatistics, 30);
        } else if (axe.equals("y")) {
            return statisticsProcessor.getAxeY(moduleStatistics, 30);
        }
        return null;
    }

    public String getContributionsByLocation(TemplateProcessor processor, String type) {
        AuthorsAnalyzer authorsAnalyzer = new AuthorsAnalyzer()
                .setFileMask(SessionFacade.INSTANCE.getFileMaskPattern(processor.getParameters()))
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(processor.getParameters()))
                .cast();
        AuthorGroupStatistics contributions = SessionFacade.INSTANCE.lazyGetLocationStatistics(processor.getParameters(), authorsAnalyzer);
        AuthorStatisticsProcessor statisticsProcessor = new AuthorStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat());
        switch (type) {
            case "abs":
                return statisticsProcessor.getGroupWeightsMapAbs(contributions);
            case "norm":
                return statisticsProcessor.getGroupWeightsMapNorm(contributions);
            case "eff":
                return statisticsProcessor.getGroupWeightsMapEff(contributions);
        }
        return null;
    }

    public String getAgeSeparator(TemplateProcessor processor, String type) {
        TimelineAnalyzer timelineAnalyzer = new TimelineAnalyzer();
        if (type.equals("files")) {
            List<AgeStatistics> ages = SessionFacade.INSTANCE.lazyGetFilesAgeStatistics(processor.getParameters(), timelineAnalyzer);
            return new TimelineProcessor(Application.INSTANCE.getDefaultNumberFormat()).getAgeSeparator(ages);
        }
        if (type.equals("modules")) {
            List<AgeStatistics> ages = SessionFacade.INSTANCE.lazyGetModulesAgeStatistics(processor.getParameters(), timelineAnalyzer);
            return new TimelineProcessor(Application.INSTANCE.getDefaultNumberFormat()).getAgeSeparator(ages);
        }
        return null;
    }

    public String getAgeComparison(TemplateProcessor processor, String type) {
        TimelineAnalyzer timelineAnalyzer = new TimelineAnalyzer();
        if (type.equals("files")) {
            List<AgeStatistics> ages = SessionFacade.INSTANCE.lazyGetFilesAgeStatistics(processor.getParameters(), timelineAnalyzer);
            return new TimelineProcessor(Application.INSTANCE.getDefaultNumberFormat()).getAgeComparison(ages);
        }
        if (type.equals("modules")) {
            List<AgeStatistics> ages = SessionFacade.INSTANCE.lazyGetModulesAgeStatistics(processor.getParameters(), timelineAnalyzer);
            return new TimelineProcessor(Application.INSTANCE.getDefaultNumberFormat()).getAgeComparison(ages);
        }
        return null;
    }

    public String getProjectActivity(TemplateProcessor processor, String type) {
        TimelineAnalyzer timelineAnalyzer = new TimelineAnalyzer();
        if (type.equals("x")) {
            ActivitySummary<String> activity = SessionFacade.INSTANCE.lazyGetProjectActivity(processor.getParameters(), timelineAnalyzer);
            return new TimelineProcessor(Application.INSTANCE.getDefaultNumberFormat()).getActivityLabels(activity);
        }
        if (type.equals("y")) {
            ActivitySummary<String> activity = SessionFacade.INSTANCE.lazyGetProjectActivity(processor.getParameters(), timelineAnalyzer);
            return new TimelineProcessor(Application.INSTANCE.getDefaultNumberFormat()).getActivityValues(activity);
        }
        return null;
    }

    public String getFilesAgeTable(Map<String, String> parameters) {
        TimelineAnalyzer timelineAnalyzer = new TimelineAnalyzer();
        List<AgeStatistics> ages = SessionFacade.INSTANCE.lazyGetFilesAgeStatistics(parameters, timelineAnalyzer);
        TimelineProcessor processor = new TimelineProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getAges(ages);
    }

    public String getFilesAgeTableCSV(Map<String, String> parameters) {
        TimelineAnalyzer timelineAnalyzer = new TimelineAnalyzer();
        List<AgeStatistics> ages = SessionFacade.INSTANCE.lazyGetFilesAgeStatistics(parameters, timelineAnalyzer);
        TimelineProcessor processor = new TimelineProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getAgesCSV(ages);
    }

    public String getModulesAgeTable(Map<String, String> parameters) {
        TimelineAnalyzer timelineAnalyzer = new TimelineAnalyzer();
        List<AgeStatistics> ages = SessionFacade.INSTANCE.lazyGetModulesAgeStatistics(parameters, timelineAnalyzer);
        TimelineProcessor processor = new TimelineProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getAges(ages);
    }

    public String getModulesAgeTableCSV(Map<String, String> parameters) {
        TimelineAnalyzer timelineAnalyzer = new TimelineAnalyzer();
        List<AgeStatistics> ages = SessionFacade.INSTANCE.lazyGetModulesAgeStatistics(parameters, timelineAnalyzer);
        TimelineProcessor processor = new TimelineProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getAgesCSV(ages);
    }

    public String getActivity(Map<String, String> parameters) {
        String type = parameters.get("type");
        String name = parameters.get("name");
        TimelineAnalyzer timelineAnalyzer = new TimelineAnalyzer();
        if (type.equals("team")) {
            List<ActivitySummary<String>> activityList = SessionFacade.INSTANCE.lazyGetTeamsActivity(parameters, timelineAnalyzer);
            ActivitySummary<String> activity = activityList.stream().filter(a -> name.equals(a.getEntity())).findFirst().orElse(null);
            if (activity != null) {
                return new TimelineProcessor(Application.INSTANCE.getDefaultNumberFormat()).getActivity(activity);
            }
        }
        if (type.equals("author")) {
            List<ActivitySummary<Author>> activityList = SessionFacade.INSTANCE.lazyGetAuthorsActivity(parameters, timelineAnalyzer);
            Author author = Application.INSTANCE.getDataRepository().findAuthor(name);
            if (author != null) {
                ActivitySummary<Author> activity = activityList.stream().filter(a -> author.equals(a.getEntity())).findFirst().orElse(null);
                if (activity != null) {
                    return new TimelineProcessor(Application.INSTANCE.getDefaultNumberFormat()).getActivity(activity);
                }
            }
        }
        return null;
    }

    public String getContributionsByTeam(TemplateProcessor processor, String type) {
        AuthorsAnalyzer authorsAnalyzer = new AuthorsAnalyzer()
                .setFileMask(SessionFacade.INSTANCE.getFileMaskPattern(processor.getParameters()))
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(processor.getParameters()))
                .cast();
        AuthorGroupStatistics contributions = SessionFacade.INSTANCE.lazyGetTeamStatistics(processor.getParameters(), authorsAnalyzer);
        AuthorStatisticsProcessor statisticsProcessor = new AuthorStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat());
        switch (type) {
            case "label":
                return statisticsProcessor.getGroupWeightsChartLabels(contributions);
            case "abs":
                return statisticsProcessor.getGroupWeightsChartAbs(contributions);
            case "norm":
                return statisticsProcessor.getGroupWeightsChartNorm(contributions);
            case "eff":
                return statisticsProcessor.getGroupWeightsChartEff(contributions);
        }
        return null;
    }

    public String getSharedFiles(Map<String, String> parameters) {
        FileAuthorsAnalyzer fileAuthorsAnalyzer = new FileAuthorsAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        List<FileAuthors> fileStatistics = SessionFacade.INSTANCE.lazyGetFileAuthorsStatistics(parameters, fileAuthorsAnalyzer);
        fileStatistics = fileAuthorsAnalyzer.filter(fileStatistics, 5);
        SharedFilesStatisticsProcessor processor = new SharedFilesStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getSharedFiles(fileStatistics, "path");
    }

    public String getSharedFilesCSV(Map<String, String> parameters) {
        FileAuthorsAnalyzer fileAuthorsAnalyzer = new FileAuthorsAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        List<FileAuthors> fileStatistics = SessionFacade.INSTANCE.lazyGetFileAuthorsStatistics(parameters, fileAuthorsAnalyzer);
        fileStatistics = fileAuthorsAnalyzer.filter(fileStatistics, 5);
        SharedFilesStatisticsProcessor processor = new SharedFilesStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getSharedFilesCSV(fileStatistics);
    }

    public String getSharedModules(Map<String, String> parameters) {
        FileAuthorsAnalyzer fileAuthorsAnalyzer = new FileAuthorsAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        List<FileAuthors> moduleStatistics = SessionFacade.INSTANCE.lazyGetModuleAuthorsStatistics(parameters, fileAuthorsAnalyzer);
        moduleStatistics = fileAuthorsAnalyzer.filter(moduleStatistics, 5);
        SharedFilesStatisticsProcessor processor = new SharedFilesStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getSharedFiles(moduleStatistics, "moduleName");
    }

    public String getSharedModulesCSV(Map<String, String> parameters) {
        FileAuthorsAnalyzer fileAuthorsAnalyzer = new FileAuthorsAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        List<FileAuthors> moduleStatistics = SessionFacade.INSTANCE.lazyGetModuleAuthorsStatistics(parameters, fileAuthorsAnalyzer);
        moduleStatistics = fileAuthorsAnalyzer.filter(moduleStatistics, 5);
        SharedFilesStatisticsProcessor processor = new SharedFilesStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getSharedFilesCSV(moduleStatistics);
    }

    public String getContributors(Map<String, String> parameters) {
        AuthorsAnalyzer authorsAnalyzer = new AuthorsAnalyzer()
                .setFileMask(SessionFacade.INSTANCE.getFileMaskPattern(parameters))
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        AuthorStatistics authorStatistics = SessionFacade.INSTANCE.lazyGetAuthorStatistics(parameters, authorsAnalyzer);
        AuthorStatisticsProcessor processor = new AuthorStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getTopContributors(authorStatistics);
    }

    public String getContributorsCSV(Map<String, String> parameters) {
        AuthorsAnalyzer authorsAnalyzer = new AuthorsAnalyzer()
                .setFileMask(SessionFacade.INSTANCE.getFileMaskPattern(parameters))
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        AuthorStatistics authorStatistics = SessionFacade.INSTANCE.lazyGetAuthorStatistics(parameters, authorsAnalyzer);
        AuthorStatisticsProcessor processor = new AuthorStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getTopContributorsCSV(authorStatistics);
    }

    public String getContributorTeams(Map<String, String> parameters) {
        AuthorsAnalyzer authorsAnalyzer = new AuthorsAnalyzer()
                .setFileMask(SessionFacade.INSTANCE.getFileMaskPattern(parameters))
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        AuthorStatistics authorStatistics = SessionFacade.INSTANCE.lazyGetAuthorStatistics(parameters, authorsAnalyzer);
        AuthorGroupStatistics contributions = authorsAnalyzer.getContributions(authorStatistics, as -> as.getAuthor().getTeam());
        AuthorStatisticsProcessor processor = new AuthorStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getTopGroups(contributions);
    }

    public String getContributorTeamsCSV(Map<String, String> parameters) {
        AuthorsAnalyzer authorsAnalyzer = new AuthorsAnalyzer()
                .setFileMask(SessionFacade.INSTANCE.getFileMaskPattern(parameters))
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        AuthorStatistics authorStatistics = SessionFacade.INSTANCE.lazyGetAuthorStatistics(parameters, authorsAnalyzer);
        AuthorGroupStatistics contributions = authorsAnalyzer.getContributions(authorStatistics, as -> as.getAuthor().getTeam());
        AuthorStatisticsProcessor processor = new AuthorStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getTopGroupsCSV(contributions);
    }

    public String getTeams(Map<String, String> parameters) {
        AuthorsAnalyzer authorsAnalyzer = new AuthorsAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        List<Author> authors = SessionFacade.INSTANCE.lazyGetFilteredAuthors(parameters, authorsAnalyzer);
        List<String> teams = authorsAnalyzer.getTeams(authors);
        return new AuthorStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat()).getTeamsList(teams);
    }

    public String getAuthors(Map<String, String> parameters) {
        AuthorsAnalyzer authorsAnalyzer = new AuthorsAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        List<Author> authors = SessionFacade.INSTANCE.lazyGetFilteredAuthors(parameters, authorsAnalyzer);
        return new AuthorStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat()).getAuthorsList(authors);
    }

    public String getTopAuthors(Map<String, String> parameters) {
        AuthorsAnalyzer authorsAnalyzer = new AuthorsAnalyzer()
                .setPath(parameters.get("path"))
                .setModuleName(parameters.get("moduleName"))
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        AuthorStatistics authorStatistics = authorsAnalyzer.getAllContributors(Application.INSTANCE.getDataRepository());
        return new AuthorStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat()).getTopAuthors(authorStatistics);
    }

    public String getTopTeams(Map<String, String> parameters) {
        AuthorsAnalyzer authorsAnalyzer = new AuthorsAnalyzer()
                .setPath(parameters.get("path"))
                .setModuleName(parameters.get("moduleName"))
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        AuthorStatistics authorStatistics = authorsAnalyzer.getAllContributors(Application.INSTANCE.getDataRepository());
        AuthorGroupStatistics authorGroupStatistics = authorsAnalyzer.getContributions(authorStatistics, as -> as.getAuthor().getTeam());
        return new AuthorStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat()).getTopTeams(authorGroupStatistics);
    }

    public String getTopFiles(Map<String, String> parameters) {
        DataRepository dataRepository = Application.INSTANCE.getDataRepository();
        Author author = dataRepository.findAuthor(parameters.get("author"));
        if (author == null) {
            return "[]";
        }
        FileCommitsAnalyzer fileCommitsAnalyzer = new FileCommitsAnalyzer()
                .setAuthor(author)
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        FileStatistics fileStatistics = fileCommitsAnalyzer.getFileStatistics(dataRepository);
        return new FileStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat()).getTopFiles(fileStatistics);
    }

    public String getTopModules(Map<String, String> parameters) {
        DataRepository dataRepository = Application.INSTANCE.getDataRepository();
        Author author = dataRepository.findAuthor(parameters.get("author"));
        if (author == null) {
            return "[]";
        }
        FileCommitsAnalyzer fileCommitsAnalyzer = new FileCommitsAnalyzer()
                .setAuthor(author)
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        FileStatistics fileStatistics = fileCommitsAnalyzer.getFileStatistics(dataRepository);
        FileStatistics moduleStatistics = fileCommitsAnalyzer.getModuleStatistics(fileStatistics);
        return new FileStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat()).getTopFiles(moduleStatistics);
    }

    public String getTopTeamFiles(Map<String, String> parameters) {
        DataRepository dataRepository = Application.INSTANCE.getDataRepository();
        String team = parameters.get("team").trim();
        FileCommitsAnalyzer fileCommitsAnalyzer = new FileCommitsAnalyzer()
                .setTeam(team)
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        FileStatistics fileStatistics = fileCommitsAnalyzer.getFileStatistics(dataRepository);
        return new FileStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat()).getTopFiles(fileStatistics);
    }

    public String getTopTeamModules(Map<String, String> parameters) {
        DataRepository dataRepository = Application.INSTANCE.getDataRepository();
        String team = parameters.get("team").trim();
        FileCommitsAnalyzer fileCommitsAnalyzer = new FileCommitsAnalyzer()
                .setTeam(team)
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        FileStatistics fileStatistics = fileCommitsAnalyzer.getFileStatistics(dataRepository);
        FileStatistics moduleStatistics = fileCommitsAnalyzer.getModuleStatistics(fileStatistics);
        return new FileStatisticsProcessor(Application.INSTANCE.getDefaultNumberFormat()).getTopFiles(moduleStatistics);
    }

    public String getFileDependencies(Map<String, String> parameters) {
        GraphAnalyzer graphAnalyzer = new GraphAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        Graph dependencies = SessionFacade.INSTANCE.lazyGetFileDependencies(parameters, graphAnalyzer);
        GraphProcessor processor = new GraphProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getTableData(dependencies);
    }

    public String getFileDependenciesCSV(Map<String, String> parameters) {
        GraphAnalyzer graphAnalyzer = new GraphAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        Graph dependencies = SessionFacade.INSTANCE.lazyGetFileDependencies(parameters, graphAnalyzer);
        GraphProcessor processor = new GraphProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getCSVData(dependencies);
    }

    public String getModuleDependencies(Map<String, String> parameters) {
        GraphAnalyzer graphAnalyzer = new GraphAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        Graph dependencies = SessionFacade.INSTANCE.lazyGetModuleDependencies(parameters, graphAnalyzer);
        GraphProcessor processor = new GraphProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getTableData(dependencies);
    }

    public String getModuleDependenciesCSV(Map<String, String> parameters) {
        GraphAnalyzer graphAnalyzer = new GraphAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(parameters))
                .cast();
        Graph dependencies = SessionFacade.INSTANCE.lazyGetModuleDependencies(parameters, graphAnalyzer);
        GraphProcessor processor = new GraphProcessor(Application.INSTANCE.getDefaultNumberFormat());
        return processor.getCSVData(dependencies);
    }

    public String getImplicitDependencies(TemplateProcessor processor, String type) {
        GraphAnalyzer graphAnalyzer = new GraphAnalyzer()
                .setDateFrom(SessionFacade.INSTANCE.getDateFrom(processor.getParameters()))
                .cast();
        Graph dependencies = SessionFacade.INSTANCE.lazyGetModuleDependencies(processor.getParameters(), graphAnalyzer);
        return new GraphProcessor(Application.INSTANCE.getDefaultNumberFormat()).getGraphData(dependencies, type);
    }
}
