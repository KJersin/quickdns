/*
 * The MIT License
 *
 * Copyright 2024 kje.
 *
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
 */
package dk.jersin.quickdns;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.Callable;

import static dk.jersin.quickdns.MainContext.*;
import static java.nio.file.Files.*;
import static java.nio.file.attribute.PosixFilePermission.*;
import static java.util.Arrays.asList;
import static java.util.Set.of;
import static picocli.CommandLine.*;
import static picocli.CommandLine.Model.*;
import static picocli.CommandLine.Command;

/**
 *
 * @author kje
 */
@Command(
        name = "configure",
        description
        = "Sets the default configuration location and optional the Quickdns login and url arguments:\n"
        + "  Values set are automatically used when quickdns is run without any (or a subset) of the configuration arguments.",
        showDefaultValues = true
)
public class ConfigureCommand implements Callable<Integer> {

    @Option(names = {"-e", "--email"},
            description = "Login email"
    )
    private String email;

    @Option(names = {"-p", "--password"},
            description = "Login password",
            interactive = true
    )
    private String password;

    @Option(names = {"-k", "--check"},
            description = "Check the configuration by doing a login and showing the available zones"
    )
    private boolean check;

    @Mixin
    private MainContext ctx;

    @Spec
    private CommandSpec spec;

    public void init() throws IOException {
        // Get the configuration path as given by user input (or use default if none given)
        // and store as default in our application preferences.
        var configPath = ctx.configPath();
        ctx.prefs().put(PREF_CONFIG_PATH, configPath.toString());

        // Make sure that the configuration file exists
        if (!configPath.toFile().exists()) {
            createDirectories(configPath.getParent());
            configPath.toFile().createNewFile();

            // ... and is only readable by the user running the Quickdns application
            // chmod u=rw,go=
            setPosixFilePermissions(configPath, of(OWNER_READ, OWNER_WRITE));
        }
    }

    @Override
    public Integer call() throws Exception {
        // Load the current configuration
        var config = new Properties();
        try (var in = new FileInputStream(ctx.configPath().toFile())) {
            config.load(in);
        }

        // ... and update if changed
        if (asList(email, password, ctx.url).stream().anyMatch((arg) -> arg != null)) {
            if (email != null) {
                config.setProperty("email", email);
            }
            if (password != null) {
                config.setProperty("password", password);
            }
            if (ctx.url != null) {
                config.setProperty("url", ctx.url);
            }

            try (var out = new FileOutputStream(ctx.configPath().toFile())) {
                config.store(out, "Written by quickdns configure\nAvoid manual editing");
            }
        }

        // Show the configuration
        show(spec.commandLine().getOut());

        return 0;
    }

    public void show(PrintWriter out) throws FileNotFoundException, IOException {
        var config = new Properties();
        try (var in = new FileInputStream(ctx.configPath().toFile())) {
            config.load(in);
            out.printf("Configuration (%s):\n", ctx.configPath());
            out.printf("  email   : %s\n", config.getProperty("email", "<not-set>"));
            out.printf("  password: %s\n", config.containsKey("password") ? "***" : "<not-set>");
            out.printf("  url     : %s\n", config.getProperty("url", WWW_QUICKDNS_DK));
        }

        // If config elements are <not-set> show configuration hints
        if (asList("email", "password").stream().anyMatch((arg) -> !config.containsKey(arg))) {
            String hint = "To finish the configuration use the command:\n"
                    + "  quickdns configure";
            if (!config.containsKey("email")) {
                hint += " --email=<enter-your-login-email-here>";
            }
            if (!config.containsKey("password")) {
                hint += " --password\n"
                        + "NOTE: Don't supply your password on the command line. You will be prompted.";
            }
            out.println(hint);
        }
    }
}
