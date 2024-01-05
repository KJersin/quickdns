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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dk.jersin.dns.Resolve;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import org.jsoup.Jsoup;

import static java.net.URLEncoder.encode;
import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author kje
 */
public class ConnectionClient implements Closeable {

    private static Logger logger = Logger.getGlobal();

    protected HttpClient client;

    protected Charset charset;

    public ConnectionClient() {
        this(HttpClient.newBuilder().build(), UTF_8);
    }

    public ConnectionClient(HttpClient client, Charset charset) {
        this.client = client;
        this.charset = charset;
    }

    public Charset charset() {
        return charset;
    }

    /**
     * Make a GET request discarding response body.
     * 
     * @param baseUri
     * @param href
     * @param referer
     * @throws IOException
     * @throws InterruptedException 
     */
    public void get(URI baseUri, String href, String referer) throws IOException, InterruptedException {
        var response = client.send(newBuilder(baseUri.resolve(href))
                .header("Referer", baseUri.resolve(referer).toString())
                .GET()
                .build(), ofString(charset)
        );
        checkedBody(response);
    }

    /**
     * Make a GET request discarding response body.
     * 
     * @param baseUri
     * @param href
     * @param referer
     * @param urlArgs
     * @throws IOException
     * @throws InterruptedException 
     */
    public void get(URI baseUri, String href, String referer, Map<String, String> urlArgs) throws IOException, InterruptedException {
        get(baseUri, href + "?" + encodeMap(urlArgs), referer);
    }

    /**
     * Make a GET request parsing the response body using {@link Jsoup} and calling
     * the supplied {@link DomFunction<T>} to load in the parsed data.
     * 
     * @param <T>
     * @param domFunc
     * @param baseUri
     * @param href
     * @param referer
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public <T> T get(DomFunction<T> domFunc, URI baseUri, String href, String referer) throws IOException, InterruptedException {
        var response = client.send(newBuilder(baseUri.resolve(href))
                .header("Referer", baseUri.resolve(referer).toString())
                .GET()
                .build(), ofInputStream()
        );
        try (var in = checkedBody(response)) {
            return domFunc.load(Jsoup.parse(in, charset.name(), baseUri.toString()));
        }
    }

    /**
     * Make a GET request parsing the response body using {@link Jsoup} and calling
     * the supplied {@link DomFunction<T>} to load in the parsed data.
     * 
     * @param <T>
     * @param domFunc
     * @param baseUri
     * @param href
     * @param referer
     * @param urlArgs
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public <T> T get(DomFunction<T> domFunc, URI baseUri, String href, String referer, Map<String, String> urlArgs) throws IOException, InterruptedException {
        return get(domFunc, baseUri, href + "?" + encodeMap(urlArgs), referer);
    }

    /**
     * Call Googles dns service to lookup dns records.
     * 
     * @param name
     * @param type
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException 
     */
    public Resolve dig(String name, String type) throws URISyntaxException, IOException, InterruptedException {
        var uri = new URI("https", "dns.google", "/resolve",
                encodeMap(Map.of("name", name, "type", type)), null
        );
        var response = client.send(newBuilder(uri)
                .GET()
                .build(), ofInputStream()
        );

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        try (var reader = new InputStreamReader(checkedBody(response), UTF_8)) {
            return gson.fromJson(reader, Resolve.class);
        }
    }

    protected HttpRequest.BodyPublisher ofForm(Map<String, String> data, Charset charset) {
        return HttpRequest.BodyPublishers.ofString(encodeMap(data, charset));
    }

    protected String encodeMap(Map<String, String> data) {
        return encodeMap(data, UTF_8);
    }

    protected String encodeMap(Map<String, String> data, Charset charset) {
        StringBuilder res = new StringBuilder();
        for (String dataKey : data.keySet()) {
            if (res.length() > 0) {
                res.append("&");
            }
            res.append(encode(dataKey, charset))
                    .append("=")
                    .append(encode(data.get(dataKey), charset));
        }
        return res.toString();
    }
    
    protected <T> T checkedBody(HttpResponse<T> response) throws IOException {
        switch (response.statusCode()) {
            case 200:
            case 302:
                return response.body();
            default:
                throw new IOException(response.toString());
        }
    }

    @Override
    public void close() throws IOException {
    }

}
