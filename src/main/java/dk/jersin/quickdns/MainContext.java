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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static picocli.CommandLine.*;

/**
 *
 * @author kje
 */
@Command(
        mixinStandardHelpOptions = true
)
public class MainContext {

    /**
     *
     */
    private Connection conn;

    @Option(names = {"-u", "--url"}, description = "QuickDns root page (default: https://www.quickdns.dk)")
    public URI uri;

    @Option(names = {"-c", "--configuration"},
            description = "Configuration containing email and password"
    )
    public Path configPath;

    public MainContext() throws URISyntaxException {
        conn = new Connection("ISO-8859-1");
        uri = new URI("https://www.quickdns.dk");
        configPath = Paths.get(System.getProperty("user.home"), ".java/.userPrefs/quick-dns.conf");
    }

    public Zones login() throws IOException, FileNotFoundException, InterruptedException {
        return conn.login(new Zones(conn), uri, configPath);
    }

    public void logout() throws IOException, InterruptedException {
        conn.logout();
    }
}