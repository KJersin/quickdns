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
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import dk.jersin.quickdns.DomFunction;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;


/**
 *
 * @author kje
 */
public class Zones implements DomFunction<Zones> {

    private static Logger logger = Logger.getGlobal();

    private Connection conn;

    private Map<String, ZoneElm> elements;

    private URI baseUri;

    public Zones(Connection conn) {
        this.elements = new LinkedHashMap<>();
        this.conn = conn;
    }

    /**
     * Get an overview of the zones.
     * 
     * @return 
     */
    public Map<String, LocalDateTime> overview() {
        var res = new LinkedHashMap<String, LocalDateTime>();
        elements.entrySet().stream()
                .forEach((entry) -> {
                    res.put(entry.getKey(), entry.getValue().modified());
                });
        return res;
    }

    /**
     * Load the elements from the html page.
     *
     * @param doc
     * @return
     */
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
                elements.put(aElm.text(), zone);
            }
        });
        return this;
    }

    /**
     * Get zone by domain name. The zone data (zone records) are
     * {@link Zone::load}ed from quickdns.
     *
     * @param domain
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws NoSuchElementException
     */
    public Zone zoneFor(String domain) throws IOException, InterruptedException {
        if (elements.containsKey(domain)) {
            var zoneElm = elements.get(domain);
            return conn.get(zoneElm.value, baseUri, zoneElm.editPath, "/zones");
        } else {
            throw new NoSuchElementException(domain);
        }
    }

    public Connection conn() {
        return conn;
    }

    public static class ZoneOverview {

    }

    private static class ZoneElm {

        String editPath;

        Zone value;

        ZoneElm(String editPath, Zone value) {
            this.editPath = editPath;
            this.value = value;
        }

        LocalDateTime modified() {
            return value.modified();
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

}
