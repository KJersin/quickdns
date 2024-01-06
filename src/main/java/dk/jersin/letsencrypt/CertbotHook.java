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
package dk.jersin.letsencrypt;

import dk.jersin.dns.ZoneRecord;
import dk.jersin.quickdns.ConnectionClient;
import dk.jersin.quickdns.services.Zone;
import dk.jersin.quickdns.services.Zones;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static java.lang.System.err;
import static java.lang.System.out;

/**
 *
 * @author kje
 */
public class CertbotHook implements Callable<Integer> {

    private static Logger logger = Logger.getGlobal();

    private Zones zones;
    private String domain;
    private String validation;
    private String token;
    private Integer remainingChallenges;
    private List<String> allDomains;
    private Optional<String> authOutput;

    public static final String ACME_CHALLENGE = "_acme-challenge";

    public CertbotHook(Zones zones, String domain, String validation) {
        this(zones, domain, validation, "", "0", domain, null);
    }

    /**
     *
     * @param zones
     * @param domain The domain being authenticated
     * @param validation The validation string
     * @param token Resource name part of the HTTP-01 challenge (HTTP-01 only)
     * @param remainingChallenges Number of challenges remaining after the
     * current challenge
     * @param allDomains A comma-separated list of all domains challenged for
     * the current
     * @param authOutput Whatever the auth script wrote to stdout
     */
    public CertbotHook(Zones zones, String domain, String validation, String token, String remainingChallenges, String allDomains, String authOutput) {
        this.zones = zones;
        this.domain = domain;
        this.validation = validation;
        this.token = token;
        this.remainingChallenges = Integer.valueOf(remainingChallenges);
        this.allDomains = Arrays.asList(allDomains.split(","));
        this.authOutput = Optional.ofNullable(authOutput);
    }

    /**
     * Get parameters from environment variables as defined in
     * https://eff-certbot.readthedocs.io/en/stable/using.html#hooks
     *
     * @param zones
     * @return
     */
    public static CertbotHook fromEnvironment(Zones zones) {
        var env = System.getenv();
        return new CertbotHook(zones,
                env.get("CERTBOT_DOMAIN"),
                env.get("CERTBOT_VALIDATION"),
                env.get("CERTBOT_TOKEN"),
                env.get("CERTBOT_REMAINING_CHALLENGES"),
                env.get("CERTBOT_ALL_DOMAINS"),
                env.getOrDefault("CERTBOT_AUTH_OUTPUT", null)
        );
    }

    /**
     * Handle the Certbot pre and post validation hooks.
     *
     * @return
     * @throws Exception
     */
    @Override
    public Integer call() throws Exception {
        // Get the for zone for the domain
        var zone = zones.zoneFor(domain);
        if (zone == null) {
            err.printf("Unable to locate zone for domain: %s\n", domain);
            return 1;
        }

        // Where in the challenge cycle?
        int exitCode = 0;
        if (authOutput.isEmpty()) {
            // Create the challenge
            var rec = createChallengeRecord(zone);
            if (waitForDnsRecord(rec)) {
                out.printf("%s succesfull inserted and succesfull requested via DNS lookup", rec.name());
                sleep(3000);
            } else {
                out.printf("Unable to do the DNS lookup of %s", rec.name());
                exitCode = 1;
            }
        } else {
            // Cleanup
            try (var conn = zone.beginEdit(zones.conn())) {
                conn.deleteByName(ACME_CHALLENGE);
                conn.commit();
            }
        }
        return exitCode;
    }

    public boolean waitForDnsRecord(ZoneRecord rec) throws URISyntaxException, IOException, InterruptedException {
        return waitForDnsRecord(domain, rec);
    }
    
    public static boolean waitForDnsRecord(String domain, ZoneRecord rec) throws URISyntaxException, IOException, InterruptedException {
        try (var conn = new ConnectionClient(
                HttpClient.newBuilder().build(), UTF_8
        )) {
            for (int n = 0; n < 21; n++) {
                if (conn.dig(rec.name() + "." + domain, rec.type())
                        .find(rec).isPresent()) {
                    return true;
                }
                sleep(7000);
            }
        }
        return false;
    }

    /**
     *
     * @param zone
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private ZoneRecord createChallengeRecord(Zone zone) throws IOException, InterruptedException {
        // The record
        var challengeRec = new ZoneRecord(
                ACME_CHALLENGE,
                300, "TXT", validation
        );

        // Insert into the zone
        try (var conn = zone.beginEdit(zones.conn())) {
            conn.create(challengeRec);
            conn.commit();
        }
        return challengeRec;
    }

    @Override
    public String toString() {
        return "CertbotHook{" + "domain=" + domain + ", validation=" + validation + ", token=" + token + ", remainingChallenges=" + remainingChallenges + ", allDomains=" + allDomains + ", authOutput=" + authOutput + '}';
    }

}
