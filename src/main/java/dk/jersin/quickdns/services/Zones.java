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

import dk.jersin.quickdns.Connection;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import dk.jersin.quickdns.DomFunction;

/**
 *
 * @author kje
 */
public class Zones implements DomFunction<Zones> {

    private static Logger logger = Logger.getGlobal();

    private Connection conn;

    private Map<String, ZoneElm> zones;

    private URI baseUri;

    public Zones(Connection conn) {
        this.zones = new LinkedHashMap<>();
        this.conn = conn;
    }

    @Override
    public Zones load(Document doc) {
        baseUri = URI.create(doc.baseUri());
        var rows = doc.getElementById("zone_table")
                .getElementsByTag("tr");

        rows.forEach((row) -> {
            if (row.hasAttr("zoneid")) {
                var aElm = row.getElementsByTag("a").first();
                var zone = new ZoneElm(
                        aElm.attr("href"),
                        new Zone(
                                row.attr("zoneid"),
                                aElm.text(),
                                row.child(4).text().replace(' ', 'T')
                        )
                );
                logger.info(() -> zone.toString());
                zones.put(aElm.text(), zone);
            }
        });
        return this;
    }

    public Zone zoneFor(String domain) throws IOException, InterruptedException {
        if (zones.containsKey(domain)) {
            var zoneElm = zones.get(domain);
            return conn.get(zoneElm.value, baseUri, zoneElm.editPath, "/zones");
        }
        return null;
    }
    
    public Connection conn() {
        return conn;
    }

    private static class ZoneElm {

        String editPath;

        Zone value;

        ZoneElm(String editPath, Zone value) {
            this.editPath = editPath;
            this.value = value;
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }
}
