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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;
import org.jsoup.Jsoup;

import static java.net.http.HttpClient.Redirect.NORMAL;
import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

/**
 *
 * @author kje
 */
public class Connection extends ConnectionClient {

    private static Logger logger = Logger.getGlobal();

    private Properties config;

    private URI mainPage;

    public Connection(String charset) {
        super(HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .followRedirects(NORMAL)
                .build(),
                Charset.forName(charset)
        );
    }

    public String sessionid() {
        return ((CookieManager) client.cookieHandler().get()).getCookieStore().getCookies().stream()
                .filter((cookie) -> cookie.getName().equals("sessionid"))
                .findFirst().get().getValue();
    }

    public Optional<CookieHandler> cookieHandler() {
        return client.cookieHandler();
    }

    public <T> T login(DomFunction<T> domFunc, Properties config) throws FileNotFoundException, IOException, InterruptedException {
        // Login and get going
        var response = doLogin(URI.create(config.getProperty("url")), config.getProperty("email"), config.getProperty("password"));
        try (var in = checkedBody(response)) {
            return domFunc.load(Jsoup.parse(in, charset.name(), response.uri().toString()));
        }
    }

    public void get(String href, String referer) throws IOException, InterruptedException {
        get(mainPage, href, referer);
    }

    /**
     * Logout
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public int logout() throws IOException, InterruptedException {
        return client.send(newBuilder(mainPage.resolve("logout"))
                .GET()
                .build(), ofString()
        ).statusCode();
    }

    public HttpClient client() {
        return client;
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
    private HttpResponse<InputStream> doLogin(URI uri, String name, String password) throws IOException, InterruptedException {
        // Request the homepage
        // This establishes the session and resolves any redirection to the main page.
        var response = client.send(newBuilder(uri)
                .GET()
                .build(), ofString(charset)
        );
        checkedBody(response);
        mainPage = response.uri();

        // The actual Login
        var loginUri = mainPage.resolve("login");
        return client.send(newBuilder(loginUri)
                .POST(ofForm(Map.of(
                        "email", name,
                        "password", password
                ), charset))
                .build(), ofInputStream()
        );
    }
}
