package dk.jersin.quickdns;

/*-
 * #%L
 * Quicdns - CLI to the DNS service including Certbot authentication hook functionality
 * %%
 * Copyright (C) 2023 - 2024 Kim Jersin
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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

        // Assume wide console. So use whatever space is available
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
