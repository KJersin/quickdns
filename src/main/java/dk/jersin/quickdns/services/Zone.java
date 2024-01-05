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
import dk.jersin.dns.ZoneRecord;
import dk.jersin.quickdns.Connection;
import dk.jersin.quickdns.ConnectionClient;
import java.io.Closeable;
import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;

/*
Jeg er ved at automatiserer processen med at få et letsencrypt (certbot) certifikat via DNS autorasation. Men jeg får en 500 intern serverfejl når jeg forsøger at gemme min "_acme-challenge" record.
Bemærk: Jeg forsøger efter bedste evne at simulerer serverkaldene som de udføres i zones.js fra min egen client. Og "initial" og "edit" (row=-1) går godt, mens "save=1" fejler.
Har I mulighed for at pege mig i retning af hvad jeg har misset (Jeres server log må være fyldt med en stribe fejl 500 - jeg beklager).
/Kim
 */
/**
 *
 * @author kje
 */
public class Zone implements DomFunction<Zone> {

    private static Logger logger = Logger.getGlobal();

    private static Pattern initArgPattern = Pattern.compile(
            ".*init\\('([^']*)'.*"
    );

    private URI baseUri;

    private int id;

    private String zKey;

    private String domain;

    private LocalDateTime modified;

    private ArrayList<ZoneRecord> records;

    protected Zone(String id, String domain, String modified) {
        this.id = Integer.parseInt(id);
        this.domain = domain;
        this.modified = LocalDateTime.parse(modified);
    }

    public String domain() {
        return domain;
    }

    @Override
    public Zone load(Document doc) {
        baseUri = URI.create(doc.baseUri());
        zKey = extractInitArg(doc.getElementsByTag("body").first().attr("onload"));
        records = new ArrayList<>();
        doc.getElementById("zone_table").getElementsByTag("tr").forEach((row) -> {
            var rec = new ZoneRecord(
                    row.child(0).text().trim(),
                    row.child(2).text().trim(),
                    row.child(4).text().trim()
            );
            records.add(rec);
        });
        return this;
    }

    /**
     * Initiate an edit session.
     *
     * @param conn
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public EditConnection beginEdit(Connection conn) throws IOException, InterruptedException {
        return new EditConnection(conn);
    }

    @Override
    public String toString() {
        return "Zone{" + "id=" + id + ", domain=" + domain + ", modified=" + modified + '}';
    }

    private String extractInitArg(String onloadArg) {
        logger.info(onloadArg);
        var matcher = initArgPattern.matcher(onloadArg);
        return matcher.matches() ? matcher.group(1) : null;

    }

    /**
     *
     */
    public class EditConnection extends ConnectionClient implements Closeable {

        private AtomicInteger seq;

        public EditConnection(Connection conn) throws IOException, InterruptedException {
            super(HttpClient.newBuilder()
                    .cookieHandler(conn.cookieHandler().get())
                    .build(),
                    conn.charset()
            );
            init();
        }

        public void deleteRow(int row) throws IOException, InterruptedException {
            get(baseUri, "/submitzonechange", "/editzone?id=" + id, submitArgs("delete", row));
        }

        public List<ZoneRecord> deleteByName(String name) throws IOException, InterruptedException {
            var deleted = new LinkedList<ZoneRecord>();
            for (int row = 0; row < records.size(); row++) {
                if (name.equals(records.get(row).name())) {
                    deleteRow(row);
                    deleted.add(records.get(row));
                }
            }
            return deleted;
        }

        public ZoneRecord create(ZoneRecord rec) throws IOException, InterruptedException {
            get((doc) -> checkResponse(doc),
                    baseUri, "/submitzonechange", "/editzone?id=" + id, rec.asMap(submitArgs("edit", -1))
            );
            return rec;
        }

        public void commit() throws IOException, InterruptedException {
            if (seq.get() > 0) {
                get(baseUri, "/editzonedone", "/editzone?id=" + id, editArgs(1));
                seq.set(-1);
            }
        }

        public void discard() throws IOException, InterruptedException {
            if (seq.get() >= 0) {
                get(baseUri, "/editzonedone", "/editzone?id=" + id, editArgs(0));
            }
        }

        @Override
        public void close() throws IOException {
            try {
                discard();
            } catch (InterruptedException ex) {
                throw new IOException(ex);
            }
        }

        /**
         * Init
         *
         * @throws IOException
         * @throws InterruptedException
         */
        private void init() throws IOException, InterruptedException {
            this.seq = new AtomicInteger(-1);
            get((doc) -> checkResponse(doc),
                    baseUri, "/submitzonechange", "/editzone?id=" + id, submitArgs("initial")
            );
        }

        private LinkedHashMap<String, String> editArgs(Integer save) {
            var args = new LinkedHashMap<String, String>();
            args.put("save", save.toString());
            args.put("seq", Integer.toString(seq.get()));
            args.put("zkey", zKey);
            return args;
        }

        private LinkedHashMap<String, String> submitArgs(String action) {
            var args = new LinkedHashMap<String, String>();
            args.put("action", action);
            args.put("seq", Integer.toString(seq.incrementAndGet()));
            args.put("zkey", zKey);
            return args;
        }

        private LinkedHashMap<String, String> submitArgs(String action, Integer row) {
            var args = submitArgs(action);
            args.put("row", row.toString());
            return args;
        }

        private Boolean checkResponse(Document doc) {
            return "Status: Ingen fejl i zonen".equals(
                    doc.getElementsByTag("response").first().getElementsByTag("status").first().text()
            );
        }
    }
}
