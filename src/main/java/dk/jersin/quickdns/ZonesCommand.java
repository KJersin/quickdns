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

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;
import static picocli.CommandLine.Model.CommandSpec;

/**
 *
 * @author kje
 */
@Command(
        name = "zones",
        description = "List all available Zones"
)
public class ZonesCommand implements Callable<Integer> {

    @Mixin
    private MainContext ctx;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        var zones = ctx.login();
        try {
            show(spec.commandLine().getOut(), zones.overview());
        } finally {
            ctx.logout();
        }
        
        return 0;
    }
    
    public void show(PrintWriter out, Map<String, LocalDateTime> zones) {
        out.printf("%-20s %s%n", "Zone(domain)", "Modified");
        out.println("-".repeat(20+18));
        zones.entrySet().stream()
                .forEach((entry) -> {
                    out.printf("%-20s %tD %tT%n", entry.getKey(), entry.getValue(), entry.getValue());
                });
    }

}
