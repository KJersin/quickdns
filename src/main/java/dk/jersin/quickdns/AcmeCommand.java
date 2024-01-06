/*
 * The MIT License
 *
 * Copyright 2024 kje.
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

import dk.jersin.dns.ZoneRecord;
import dk.jersin.letsencrypt.CertbotHook;
import dk.jersin.quickdns.services.Zone;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;

import static dk.jersin.letsencrypt.CertbotHook.ACME_CHALLENGE;
import static dk.jersin.letsencrypt.CertbotHook.waitForDnsRecord;
import static picocli.CommandLine.*;
import static picocli.CommandLine.Model.*;

/**
 *
 * @author kje
 */
@Command(
        name = "acme",
        description
        = "Inserts, removes or lists _acme-challenge record(s)\n"
        + "NOTE: If both --insert and --clear are specified then --clear is excuted before --insert\n"
        + "      If neither --insert nor --clear is specified the current (if any) _acme-challenge record(s) are listed"
)
public class AcmeCommand implements Callable<Integer> {

    @Option(names = {"-i", "--insert"},
            description = "Insert an _acme-challenge TXT record"
    )
    private String validation;

    @Option(names = {"-r", "--clear"},
            description = "Removes all existing _acme-challenge TXT record(s)"
    )
    private boolean clear;

    @Option(names = {"-w", "--wait"},
            description
            = "Wait for the inserted _acme-challenge record to show up on https://dns.google/resolve\n"
            + "A maximum waittime of 147 seconds is inforced. In which case the program will exit with code 147\n"
            + "NOTE: Only valid in combination with --insert"
    )
    private boolean wait;

    @Option(names = {"-l", "--list"},
            description = "List _acme-challenge TXT record(s)"
    )
    private boolean list;

    @Parameters(
            arity = "1",
            description = "Domain of the records"
    )
    private String domain;

    @Mixin
    private MainContext ctx;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        int exitCode = 0;
        var zones = ctx.login();
        try {
            // Clear challenges?
            if (clear) {
                var zone = zones.zoneFor(domain);
                try (var records = zone.beginEdit(zones.conn())) {
                    records.deleteByName(ACME_CHALLENGE);
                    records.commit();
                }
            }

            // Insert challenge?
            if (validation != null) {
                ZoneRecord rec;
                var zone = zones.zoneFor(domain);
                try (var records = zone.beginEdit(zones.conn())) {
                    rec = records.create(validation);
                    records.commit();
                }
                if (wait) {
                    exitCode = waitForDnsRecord(domain, rec) ? exitCode : 147;
                }
            }

            // List challenge
            if ((!clear && validation == null) || list) {
                show(spec.commandLine().getOut(), zones.zoneFor(domain));
            }
        } finally {
            ctx.logout();
        }

        return exitCode;
    }

    private void show(PrintWriter out, Zone zone) {
        zone.records().stream()
                .filter((rec) -> ACME_CHALLENGE.equals(rec.name()))
                .forEach((rec) -> {
                    out.printf("%s%n", rec.toString());
                });
    }
}
