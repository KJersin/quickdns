/*
 * The MIT License
 *
 * Copyright 2023 Kim Jersin.
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

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.Charset;
import java.util.Map;

/**
 *
 * @author kje
 */
public class RequestBody {

    private Charset charset;

    public RequestBody(Charset charset) {
        this.charset = charset;
    }

    public HttpRequest.BodyPublisher ofForm(Map<String, String> data) {
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
    
    public Charset getCharset() {
        return charset;
    }

    private String encode(Object obj) {
        return URLEncoder.encode(obj.toString(), charset);
    }
}
