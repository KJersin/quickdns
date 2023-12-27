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
package dk.jersin.quickdns.services;

import dk.jersin.quickdns.Context;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;

/**
 *
 * @author kje
 */
public class Zones {

    private static Logger log = Logger.getGlobal();

    private Context ctx;
    
    private URI baseUri;

    private Map<String, Zone> zones;

    public Zones(Context ctx, InputStream in, URI baseUri) throws IOException {
        this.ctx = ctx;
        this.baseUri= baseUri;
        this.zones = new LinkedHashMap<>();

        var rows = Jsoup.parse(in, ctx.charset().name(), baseUri.toString())
                .getElementById("zone_table")
                .getElementsByTag("tr");
        rows.forEach((row) -> {
            if (row.hasAttr("zoneid")) {
                var zoneElm = row.getElementsByTag("a").first();
                zones.put(zoneElm.text(), new Zone(
                        row.attr("zoneid"),
                        zoneElm.attr("href"),
                        zoneElm.text(),
                        row.child(4).text().replace(' ', 'T')
                ));
            }
        });
    }

    public Optional<Zone> zoneFor(String domain) {
        if (zones.containsKey(domain))
            return Optional.of(zones.get(domain).load(ctx, baseUri));
        return Optional.empty();
    }
}
