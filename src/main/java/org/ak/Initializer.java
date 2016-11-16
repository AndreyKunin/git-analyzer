package org.ak;

import org.ak.http.Application;
import org.ak.http.GitAnalyzerServer;
import org.ak.http.ServiceFacade;
import org.ak.http.handler.*;
import org.ak.step1.StatisticsCollector;
import org.ak.step1.data.RawRepository;
import org.ak.step1.git.Subprocess;
import org.ak.step1.git.SubprocessException;
import org.ak.step1.git.builder.IExploreBuilder;
import org.ak.step2.RepositoryBuilder;
import org.ak.step2.data.DataRepository;
import org.ak.util.Configuration;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Andrew on 14.11.2016.
 */
public class Initializer {

    private static final String SERVER_HOST = Configuration.INSTANCE.getString("HTTP.server.host");
    private static final int LISTENING_PORT = Configuration.INSTANCE.getInt("HTTP.server.port", 8000);

    public static final String JIRA_PREFIX = Configuration.INSTANCE.getString("GIT.jira.prefix");
    public static final int UPDATE_INTERVAL = Configuration.INSTANCE.getInt("GIT.update.interval.hours", 24) * 60 * 60 * 1000;

    public GitAnalyzerServer startServer() throws IOException {
        GitAnalyzerServer server = new GitAnalyzerServer();
        server.createServer(LISTENING_PORT);
        server.bindHandler("/", new RootHandler());
        server.bindHandler("/contributors.html", new TemplateHandler());
        server.bindHandler("/cost_analysis.html", new TemplateHandler());
        server.bindHandler("/implicit_dependencies.html", new TemplateHandler());
        server.bindHandler("/merge_bottlenecks.html", new TemplateHandler());
        server.bindHandler("/stability_analysis.html", new TemplateHandler());
        server.bindHandler("/activity_analysis.html", new TemplateHandler());
        server.bindHandler("/tables/hot_files.json", new FunctionHandler(ServiceFacade.INSTANCE::getHotFiles));
        server.bindHandler("/reports/hot_files.csv", new DownloadFunctionHandler(ServiceFacade.INSTANCE::getHotFilesCSV));
        server.bindHandler("/tables/hot_modules.json", new FunctionHandler(ServiceFacade.INSTANCE::getHotModules));
        server.bindHandler("/reports/hot_modules.csv", new DownloadFunctionHandler(ServiceFacade.INSTANCE::getHotModulesCSV));
        server.bindHandler("/tables/shared_files.json", new FunctionHandler(ServiceFacade.INSTANCE::getSharedFiles));
        server.bindHandler("/reports/shared_files.csv", new DownloadFunctionHandler(ServiceFacade.INSTANCE::getSharedFilesCSV));
        server.bindHandler("/tables/shared_modules.json", new FunctionHandler(ServiceFacade.INSTANCE::getSharedModules));
        server.bindHandler("/reports/shared_modules.csv", new DownloadFunctionHandler(ServiceFacade.INSTANCE::getSharedModulesCSV));
        server.bindHandler("/tables/contributors.json", new FunctionHandler(ServiceFacade.INSTANCE::getContributors));
        server.bindHandler("/reports/contributors.csv", new DownloadFunctionHandler(ServiceFacade.INSTANCE::getContributorsCSV));
        server.bindHandler("/tables/teams.json", new FunctionHandler(ServiceFacade.INSTANCE::getContributorTeams));
        server.bindHandler("/reports/teams.csv", new DownloadFunctionHandler(ServiceFacade.INSTANCE::getContributorTeamsCSV));
        server.bindHandler("/tables/teamlist.json", new FunctionHandler(ServiceFacade.INSTANCE::getTeams));
        server.bindHandler("/tables/authorlist.json", new FunctionHandler(ServiceFacade.INSTANCE::getAuthors));
        server.bindHandler("/tables/top_files.json", new FunctionHandler(ServiceFacade.INSTANCE::getTopFiles));
        server.bindHandler("/tables/top_modules.json", new FunctionHandler(ServiceFacade.INSTANCE::getTopModules));
        server.bindHandler("/tables/top_team_files.json", new FunctionHandler(ServiceFacade.INSTANCE::getTopTeamFiles));
        server.bindHandler("/tables/top_team_modules.json", new FunctionHandler(ServiceFacade.INSTANCE::getTopTeamModules));
        server.bindHandler("/tables/top_authors.json", new FunctionHandler(ServiceFacade.INSTANCE::getTopAuthors));
        server.bindHandler("/tables/top_teams.json", new FunctionHandler(ServiceFacade.INSTANCE::getTopTeams));
        server.bindHandler("/tables/files_age.json", new FunctionHandler(ServiceFacade.INSTANCE::getFilesAgeTable));
        server.bindHandler("/reports/files_age.csv", new DownloadFunctionHandler(ServiceFacade.INSTANCE::getFilesAgeTableCSV));
        server.bindHandler("/tables/modules_age.json", new FunctionHandler(ServiceFacade.INSTANCE::getModulesAgeTable));
        server.bindHandler("/reports/modules_age.csv", new DownloadFunctionHandler(ServiceFacade.INSTANCE::getModulesAgeTableCSV));
        server.bindHandler("/charts/activity_chart.json", new FunctionHandler(ServiceFacade.INSTANCE::getActivity));
        server.bindHandler("/tables/linked_files.json", new FunctionHandler(ServiceFacade.INSTANCE::getFileDependencies));
        server.bindHandler("/reports/linked_files.csv", new DownloadFunctionHandler(ServiceFacade.INSTANCE::getFileDependenciesCSV));
        server.bindHandler("/tables/linked_modules.json", new FunctionHandler(ServiceFacade.INSTANCE::getModuleDependencies));
        server.bindHandler("/reports/linked_modules.csv", new DownloadFunctionHandler(ServiceFacade.INSTANCE::getModuleDependenciesCSV));
        server.bindHandler("/actions/filter", new RedirectFunctionHandler(ServiceFacade.INSTANCE::filterStatistics));
        server.startServer();
        return server;
    }

    public void startClient(GitAnalyzerServer server) throws SubprocessException {
        String[] startBrowserCommand = new IExploreBuilder().setHost(SERVER_HOST).setPort(LISTENING_PORT).buildCommand();
        System.out.println("Starting browser at " + IExploreBuilder.START_PAGE + ".");
        Subprocess.start(new Initializer.ShutdownCallback(server)::exit, startBrowserCommand);
    }

    public void buildDataRepository(RawRepository rawRepository) {
        System.out.println("Building in-memory repository.");
        RepositoryBuilder repositoryBuilder = new RepositoryBuilder();
        DataRepository dataRepository = repositoryBuilder.build(rawRepository, JIRA_PREFIX);
        Application.INSTANCE.setDataRepository(dataRepository);
    }

    public RawRepository loadRawRepository() throws IOException {
        RawRepository rawRepository = RawRepository.restore();
        if (rawRepository == null) {
            rawRepository = buildRawRepository(null);
            if (rawRepository != null) {
                rawRepository.persist();
            }
        }
        return rawRepository;
    }

    public RawRepository updateRawRepository(Date dateFrom) throws IOException {
        RawRepository originalRepository = RawRepository.restore();
        RawRepository rawRepository = buildRawRepository(dateFrom);
        if (rawRepository != null) {
            originalRepository.merge(rawRepository);
            originalRepository.persist();
        }
        return originalRepository;
    }

    public void scheduleUpdates() {
        TimerTask timerTask = new RepositoryUpdateTask();
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, UPDATE_INTERVAL, UPDATE_INTERVAL);
    }

    private RawRepository buildRawRepository(Date dateFrom) {
        RawRepository rawRepository = null;
        System.out.println("Gathering GIT statistics.");
        try {
            rawRepository = new StatisticsCollector().collect(dateFrom);
        } catch (SubprocessException ex) {
            ex.printStackTrace(System.out);
        }
        return rawRepository;
    }

    private static class ShutdownCallback {

        GitAnalyzerServer server;

        ShutdownCallback(GitAnalyzerServer server) {
            this.server = server;
        }

        void exit(Integer browserExitCode) {
            System.out.println("Browser exited with code " + browserExitCode + ".");
            server.closeServer();
        }
    }

    private class RepositoryUpdateTask extends TimerTask {

        @Override
        public void run() {
            try {
                DataRepository dataRepository = Application.INSTANCE.getDataRepository();
                if (dataRepository == null) {
                    System.out.println("Incremental update cancelled as data repository is still not built.");
                    return;
                }
                RawRepository rawRepository = updateRawRepository(dataRepository.getBuildDate());
                if (rawRepository != null) {
                    buildDataRepository(rawRepository);
                }
            } catch (Throwable t) {
                System.out.println("Error during incremental update: " + t.getMessage());
                t.printStackTrace(System.out);
            }
        }
    }
}
