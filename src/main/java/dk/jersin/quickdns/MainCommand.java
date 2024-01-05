/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package dk.jersin.quickdns;

import dk.jersin.letsencrypt.CertbotHook;
import dk.jersin.quickdns.services.Zones;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import static java.util.logging.Level.INFO;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Spec;
import static picocli.CommandLine.Model.CommandSpec;

/**
 *
 * @author kje
 */
@Command(
        name = "quickdns", mixinStandardHelpOptions = true, version = "0.1",
        description = "Edit Quickdns records",
        subcommands = {
            AcmeCommand.class,
            CertbotCommand.class
        }
)
public class MainCommand implements Callable<Integer> {

    private static Logger logger = Logger.getGlobal();

    static {
        // must set before the Logger
        // loads logging.properties from the classpath
        String path = MainCommand.class
                .getClassLoader().getResource("logging.properties").getFile();
        System.setProperty("java.util.logging.config.file", path);

        //
        java.security.Security.setProperty("networkaddress.cache.ttl", "0");

        System.setProperty("picocli.usage.width", Integer.toString(120));
    }

    @Spec
    private CommandSpec spec;
    
    @CommandLine.Mixin
    private MainContext ctx;

    /**
     * Program entry.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.exit(new CommandLine(new MainCommand()).execute(args));
    }

    /**
     * Program called without any commands.
     *
     * @return @throws Exception
     */
    @Override
    public Integer call() throws Exception {
        int exitCode = 0;
        if (System.getenv().containsKey("CERTBOT_DOMAIN")) {
            exitCode = spec.commandLine().getSubcommands().get("certbot").execute();
        }
        return exitCode;
    }
}
