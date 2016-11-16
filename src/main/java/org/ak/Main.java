package org.ak;

import org.ak.http.GitAnalyzerServer;
import org.ak.step1.data.RawRepository;

public class Main {


    public static void main(String[] args) throws Exception {

        displayHeader();

        Initializer initializer = new Initializer();

        //Step 1. Gathering full statistics from GIT.
        RawRepository rawRepository = initializer.loadRawRepository();
        if (rawRepository == null) {
            displayFooter();
            return;
        }

        //Step 2. Building data repository.
        initializer.buildDataRepository(rawRepository);

        //Step 3. Schedule incremental update.
        initializer.scheduleUpdates();

        //Step 4. Starting HTTP server.
        GitAnalyzerServer server = initializer.startServer();

        //Step 5. Starting client browser.
        initializer.startClient(server);

    }

    private static void displayHeader() {
        System.out.println("==============================================================================");
        System.out.println("==   Git Analyzer v 0.2                                                     ==");
        System.out.println("==============================================================================");
    }

    private static void displayFooter() {
        System.out.println("==============================================================================");
    }
}
