/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package dk.jersin.quickdns;

import dk.jersin.quickdns.services.Zones;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import java.nio.charset.Charset;
import static java.net.http.HttpRequest.*;
import java.net.http.HttpResponse;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.jsoup.Jsoup;

/**
 *
 * @author kje
 */
@CommandLine.Command(name = "quickdns", mixinStandardHelpOptions = true, version = "0.1",
        description = "Edit Quickdns records"
)
public class Main implements Callable<Integer>, Context {

    private static Logger log = Logger.getGlobal();

    @Parameters(index = "0", description = "What to do")
    private String cmd;

    @Option(names = {"-u", "--url"}, description = "QuickDns root page (default: https://www.quickdns.dk)")
    private URI url;

    @Option(names = {"-cf", "--configuration"}, description = "Configuration (default: ~/.java/.userPrefs/quick-dns.conf")
    private Path configPath;

    private Properties config;
    
    private URI mainPage;

    private HttpClient client;

    private RequestBody reqBody;
 
    @Override
    public URI mainPage() {
        return mainPage;
    }

    @Override
    public HttpClient client() {
        return client;
    }

    @Override
    public RequestBody requestBody() {
        return reqBody;
    }

    @Override
    public Charset charset() {
        return reqBody.getCharset();
    }

    /**
     * Init defaults like the main url, client and charset.
     * 
     * @throws URISyntaxException
     * @throws MalformedURLException 
     */
    public Main() throws URISyntaxException, MalformedURLException {
        url = new URI("https://www.quickdns.dk");
        client = HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        reqBody = new RequestBody(Charset.forName("ISO-8859-1"));
        configPath = Paths.get(System.getProperty("user.home"), ".java/.userPrefs/quick-dns.conf");
    }

    /**
     * Main program entry.
     * Login to the QuickDns site and parses on the program execution to the
     * zones service which does the bulk of the work.
     * 
     * @return
     * @throws Exception 
     */
    @Override
    public Integer call() throws Exception {
        // Load configuration
        int exitCode = 1;
        config = new Properties();
        try (var in = new FileInputStream(configPath.toFile())) {
            config.load(in);
        }

        // Login and get going
        var response = login(config.getProperty("email"), config.getProperty("password"));
        log.info("Login: " + response.statusCode());
        if (response.statusCode() == 200) {
            try {
                var zones = new Zones(this, response.body(), response.uri());
                exitCode = executeCmd(zones);
            } finally {
                log.info("Logout: " + logout());
            }
        }
        return exitCode;
    }

    public static void main(String[] args) throws URISyntaxException, MalformedURLException {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
    
    private int executeCmd(Zones zones) {
        return 0;
    }
    
    /**
     * Login to the QuichDns homepage.
     * 
     * @param name
     * @param password
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    private HttpResponse<InputStream> login(String name, String password) throws IOException, InterruptedException {
        // Request the homepage
        // This establishes the session and resolves any redirection to the main page.
        var response = client.send(newBuilder(url)
                .GET()
                .build(), ofString(reqBody.getCharset())
        );
        mainPage = response.uri();

        // The actual Login
        var login = newBuilder(mainPage.resolve("login"))
                .POST(reqBody.ofForm(Map.of(
                        "email", name,
                        "password", password
                ))).build();
        return client.send(login, ofInputStream());
    }

    /**
     * Logout
     * 
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    private int logout() throws IOException, InterruptedException {
        return client.send(
                newBuilder(mainPage.resolve("logout"))
                        .GET().build(),
                ofString()
        ).statusCode();
    }
}
