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

import dk.jersin.quickdns.services.Zones;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.prefs.Preferences;

import static java.util.prefs.Preferences.userNodeForPackage;
import static picocli.CommandLine.*;

/**
 *
 * @author kje
 */
@Command
public class MainContext {

    public static final String PREF_CONFIG_PATH = "ConfigPath";

    public static final String WWW_QUICKDNS_DK = "https://www.quickdns.dk";

    public static final String CONF_REL_TO_USER_HOME = ".java/.userPrefs/quickdns.conf";

    /**
     *
     */
    private Connection conn;

    @Option(names = {"-u", "--url"},
            description = "Root page\n"
                    + "  Default: " + WWW_QUICKDNS_DK
    )
    public String url;

    @Option(names = {"-c", "--configuration"},
            description
            = "Configuration containing Quickdns login information\n"
            + "  Default: ~/" + CONF_REL_TO_USER_HOME + "\n"
            + "Note: File is readable only by the user running the application."
    )
    private Path configPath;

    public Preferences prefs() {
        return userNodeForPackage(this.getClass());
    }

    /**
     * Get main connection.
     *
     * @return
     */
    public Connection connection() {
        if (conn == null) {
            synchronized (this) {
                if (conn == null) {
                    conn = new Connection("ISO-8859-1");
                }
            }
        }
        return conn;
    }

    public Path configPath() {
        if (configPath == null) {
            synchronized (this) {
                if (configPath == null) {
                    configPath = Paths.get(prefs().get(PREF_CONFIG_PATH,
                            Paths.get(System.getProperty("user.home"), CONF_REL_TO_USER_HOME).toString()
                    ));
                }
            }
        }
        return configPath;
    }

    public Zones login() throws IOException, FileNotFoundException, InterruptedException {
        // Load the configuration
        var config = new Properties();
        try (var in = new FileInputStream(configPath().toFile())) {
            config.load(in);

            // The url in the following order of precedence 
            // 1) the command line
            // 2) from the configuration file
            // 3) the default https://www.quickdns.dk
            if (url != null) {
                config.setProperty("url", url);
            }
            config.setProperty("url", config.getProperty("url", WWW_QUICKDNS_DK));

            // Login and return the Zones read from the main page
            return connection().login(new Zones(connection()), config);
        }
    }

    public void logout() throws IOException, InterruptedException {
        if (conn != null) {
            conn.logout();
        }
    }
}
