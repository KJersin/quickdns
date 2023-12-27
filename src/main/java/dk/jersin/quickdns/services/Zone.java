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

import java.time.LocalDateTime;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import dk.jersin.quickdns.DomFunction;
import org.jsoup.nodes.Element;
import dk.jersin.dns.Record;

/**
 *
 * @author kje
 */
public class Zone implements DomFunction<Zone> {

    private static Logger logger = Logger.getGlobal();

    private int id;

    private String domain;

    private LocalDateTime modified;

    protected Zone(String id, String domain, String modified) {
        this.id = Integer.parseInt(id);
        this.domain = domain;
        this.modified = LocalDateTime.parse(modified);
    }
    
    @Override
    public Zone load(Document doc) {
        doc.getElementById("zone_table").getElementsByTag("tr").forEach((row) -> {
            if (!row.hasClass("listheader")) {
                var rec = newRecord(row);
                logger.info(() -> rec.toString());
                
            }
        });
        return this;
    }

    @Override
    public String toString() {
        return "Zone{" + "id=" + id + ", domain=" + domain + ", modified=" + modified + '}';
    }
    
    private Record newRecord(Element row) {
        return new Record(
                row.child(0).text().trim(),
                row.child(2).text().trim(),
                row.child(4).text().trim()
        );
    }
}
