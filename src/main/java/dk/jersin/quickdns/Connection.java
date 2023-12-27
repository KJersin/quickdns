/*
 * The MIT License
 *
 * Copyright 2023 kje.
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import static java.net.http.HttpRequest.newBuilder;
import java.net.http.HttpResponse;
import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;
import org.jsoup.Jsoup;

/**
 *
 * @author kje
 */
public class Connection {

    private static Logger logger = Logger.getGlobal();

    private Properties config;

    private HttpClient client;

    private URI mainPage;

    private Charset charset;

    public Connection() {
        client = HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public <T> T login(DomFunction<T> domFunc, URI uri, Path configPath, String charset) throws FileNotFoundException, IOException, InterruptedException {
        this.charset = Charset.forName(charset);
        this.config = new Properties();
        try (var in = new FileInputStream(configPath.toFile())) {
            config.load(in);
        }

        // Login and get going
        var response = doLogin(uri, config.getProperty("email"), config.getProperty("password"));
        logger.log(INFO, "Login: {0}", response.statusCode());
        if (response.statusCode() == 200) {
            try (var in = response.body()) {
                return domFunc.load(Jsoup.parse(in, this.charset.name(), response.uri().toString()));
            }
        } else {
            return null;
        }
    }

    public <T> T get(DomFunction<T> domFunc, URI baseUri, String href) throws IOException, InterruptedException {
        logger.info(() -> baseUri.resolve(href).toString());
        var response = client.send(newBuilder(baseUri.resolve(href))
                .GET()
                .build(), ofInputStream()
        );
        try (var in = response.body()) {
            return domFunc.load(Jsoup.parse(in, charset.name(), baseUri.toString()));
        }

    }

    /**
     * Logout
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public int logout() throws IOException, InterruptedException {
        return client.send(
                newBuilder(mainPage.resolve("logout"))
                        .GET().build(),
                ofString()
        ).statusCode();
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
        mainPage = response.uri();

        // The actual Login
        return client.send(newBuilder(mainPage.resolve("login"))
                .POST(ofForm(Map.of(
                        "email", name,
                        "password", password
                ))).build(), ofInputStream()
        );
    }

    private HttpRequest.BodyPublisher ofForm(Map<String, String> data) {
        StringBuilder body = new StringBuilder();
        for (Object dataKey : data.keySet()) {
            if (body.length() > 0) {
                body.append("&");
            }
            body.append(encode(dataKey))
                    .append("=")
                    .append(encode(data.get(dataKey)));
        }
        return HttpRequest.BodyPublishers.ofString(body.toString());
    }

    private String encode(Object obj) {
        return URLEncoder.encode(obj.toString(), charset);
    }

}
