package no.ntnu.okse.clients;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.util.List;

abstract class CommandClient {
    @Parameter(names = {"--host"}, description = "Server hostname")
    public String host = "localhost";

    @Parameter(names = {"--topic", "-t"}, description = "Topic", required = true)
    public List<String> topics;

    @Parameter(names = {"--verbose", "-v"}, description = "Verbose")
    public boolean verbose = false;

    @Parameter(names = {"--help", "-h"}, help = true, hidden = true)
    public boolean help;

    protected static void launch(CommandClient client, String[] args) {
        JCommander jCommander = new JCommander(client);
        try {
            jCommander.parse(args);
        }
        catch (ParameterException e) {
            // Required parameter missing
            System.out.println(e.getMessage());
            jCommander.usage();
            return;
        }
        // Show usage when help parameter is specified
        if(client.help) {
            jCommander.usage();
            return;
        }
        client.run();
    }

    protected abstract void createClient();

    protected abstract TestClient getClient();

    public abstract void run();

    void initLogger() {
        PatternLayout layout = new PatternLayout("%d{yyyy-MM-dd - HH:mm:ss.SSS} [%p] (%t) %c: - %m%n");
        ConsoleAppender appender = new ConsoleAppender(layout, "System.out");
        Logger.getRootLogger().addAppender(appender);
        Level logLevel = verbose ? Level.DEBUG : Level.WARN;
        Logger.getRootLogger().setLevel(logLevel);
    }
}
