/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package dk.jersin.quickdns;

import java.util.concurrent.Callable;
import java.util.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 *
 * @author kje
 */
@Command(
        name = "quickdns", mixinStandardHelpOptions = true, version = "1.0",
        description = "Edit Quickdns records",
        subcommands = {
            AcmeCommand.class,
            ZonesCommand.class,
            RecordsCommand.class,
            ConfigureCommand.class,
            CertbotCommand.class
        }
)
public class MainCommand implements Callable<Integer> {

    private static Logger logger;

    static {
        // Loads logging.properties from the classpath
        String path = MainCommand.class
                .getClassLoader().getResource("logging.properties").getFile();
        System.setProperty("java.util.logging.config.file", path);
        logger = Logger.getGlobal();

        // Assume larger console
        System.setProperty("picocli.usage.width", "AUTO");
    }
    
    /**
     * Program entry.
     * Program flow is handed of to the Picocli framework.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.exit(new CommandLine(new MainCommand()).execute(args));
    }

    @Spec
    private CommandSpec spec;
    
    @CommandLine.Mixin
    private MainContext ctx;


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
        } else {
            var err = spec.commandLine().getErr();
            err.println("*** No command given and the program is NOT running as a Certbot authentication hook");
            spec.commandLine().usage(err);

            var configureCmd = ((ConfigureCommand) spec.commandLine().getSubcommands().get("configure").getCommand());
            configureCmd.show(err);
            exitCode = 2;
        }
        return exitCode;
    }
}
