/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package dk.jersin.quickdns;

import dk.jersin.quickdns.services.Zones;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 *
 * @author kje
 */
@CommandLine.Command(name = "quickdns", mixinStandardHelpOptions = true, version = "0.1",
        description = "Edit Quickdns records"
)
public class Main implements Callable<Integer> {

    private static Logger logger = Logger.getGlobal();

    @Parameters(index = "0", description = "What to do")
    private String cmd;

    @Option(names = {"-u", "--url"}, description = "QuickDns root page (default: https://www.quickdns.dk)")
    private URI uri;

    @Option(names = {"-cf", "--configuration"}, description = "Configuration (default: ~/.java/.userPrefs/quick-dns.conf")
    private Path configPath;

    private Connection conn;

    /**
     * Init defaults like the main url, client and charset.
     *
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public Main() throws URISyntaxException, MalformedURLException {
        uri = new URI("https://www.quickdns.dk");
        configPath = Paths.get(System.getProperty("user.home"), ".java/.userPrefs/quick-dns.conf");
        conn = new Connection();
    }

    /**
     * Main program entry. Login to the QuickDns site and parses on the program
     * execution to the zones service which does the bulk of the work.
     *
     * @return
     * @throws Exception
     */
    @Override
    public Integer call() throws Exception {
        // Connect, login and load the zones
        var zones = conn.login(new Zones(conn),
                uri, configPath, "ISO-8859-1"
        );

        try {
            return executeCmd(zones);
        } finally {
            logger.log(INFO, "Logout: {0}", conn.logout());
        }
    }

    public static void main(String[] args) throws URISyntaxException, MalformedURLException {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    private int executeCmd(Zones zones) throws IOException, InterruptedException {
        zones.zoneFor("jersin.dk");
        return 0;
    }
}
