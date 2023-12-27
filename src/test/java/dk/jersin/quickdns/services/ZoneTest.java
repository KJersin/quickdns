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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author kje
 */
public class ZoneTest {

    private static Path htmlPage;

    public ZoneTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        htmlPage = Paths.get("")
                .toAbsolutePath()
                .resolve("downloads")
                .resolve("QuickDNS.dk - Ret zone.html");
    }

    @AfterAll
    public static void tearDownClass() {
    }

    /**
     * Test of toString method, of class Zone.
     */
    @Test
    public void testLoad() throws FileNotFoundException, IOException {
        var instance = new Zone("6137", "/editzone?id=6137,", "jersin.dk", "2023-12-25T19:44:12");
        try (var in = new FileInputStream(htmlPage.toFile())) {
            instance.load(in, ZonesTest.ctx().charset(), URI.create("https://example.com"));
        }
    }

}
