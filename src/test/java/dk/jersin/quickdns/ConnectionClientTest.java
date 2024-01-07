package dk.jersin.quickdns;

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

import dk.jersin.dns.Resolve;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author kje
 */
public class ConnectionClientTest {

    public ConnectionClientTest() {
    }

    @Test
    public void testCharset() {
    }

    @Test
    public void testGet_3args() throws Exception {
    }

    @Test
    public void testGet_4args_1() throws Exception {
    }

    @Test
    public void testGet_4args_2() throws Exception {
    }

    @Test
    public void testGet_5args() throws Exception {
    }

    @Test
    public void testDig() throws Exception {
        var conn = new ConnectionClient();
        Resolve dig = conn.dig("_acme-challenge.grinn.dk", "TXT");
        int tt = 42;
    }

    @Test
    public void testClose() throws Exception {
    }

}
