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
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author kje
 */
public class CertbotHookTest {

    public CertbotHookTest() {
    }

    @Test
    public void testFromEnvironment() {
    }

    @Test
    public void testCall() throws Exception {
    }

    public void testWaitForDnsRecord() throws Exception {
        // https://dns.google/resolve?name=_acme-challenge.grinn.dk.&type=TXT
        
        var rec = new ZoneRecord(
                "_acme-challenge",
                300, "TXT", "IYLMBsNjUPD2Tmd6QMoQwWA_Bj4UH_LN3hjdz9bKjQY"
        );
        var hook = new CertbotHook(null, "grinn.dk", rec.value());
        hook.waitForDnsRecord(rec);
    }

    @Test
    public void testToString() {
    }

}
