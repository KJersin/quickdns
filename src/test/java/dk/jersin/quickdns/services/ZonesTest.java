package dk.jersin.quickdns.services;

/*-
 * #%L
 * Quicdns - CLI to the DNS service including Certbot authentication hook functionality
 * %%
 * Copyright (C) 2023 - 2024 Kim Jersin
 * %%
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
 * #L%
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author kje
 */
public class ZonesTest {
    
    private static Path htmlPage;
    
    public ZonesTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
        htmlPage = Paths.get("")
                .toAbsolutePath()
                .resolve("src/test/pages")
                .resolve("QuickDNS.dk_Mine_zoner.html");
    }
    
    @AfterAll
    public static void tearDownClass() {
    }

    /**
     * Test of zoneFor method, of class Zones.
     */
    @Test
    public void testZoneFor() throws FileNotFoundException, IOException, InterruptedException {
        var zones = new Zones(null);
        
        zones.load(Jsoup.parse(htmlPage.toFile(), "ISO-8859-1"));
        
        int tt = 42;
    }
    
}
