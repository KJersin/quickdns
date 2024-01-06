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

import dk.jersin.quickdns.services.Zones;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;
import static picocli.CommandLine.Model.CommandSpec;

/**
 *
 * @author kje
 */
@Command(
        name = "records",
        description = "List records for one or more domains"
)
public class RecordsCommand implements Callable<Integer> {

    @Parameters(
            arity = "1..*",
            description = "Domain name(s) to query for records."
    )
    private List<String> domains;

    @Mixin
    private MainContext ctx;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        var zones = ctx.login();
        try {
            show(spec.commandLine().getOut(), zones);
        } finally {
            ctx.logout();
        }

        return 0;
    }

    private void show(PrintWriter out, Zones zones) throws IOException, InterruptedException {
        var sep = "";
        for (var domain : domains) {
            var zone = zones.zoneFor(domain);
            out.printf("%s%s:%n", sep, domain);
            zone.records().stream()
                    .filter((rec) -> !"Type".equals(rec.type()))
                    .forEach((rec) -> {
                        out.printf("  %s%n", rec.toString());
                    });
            sep = "\n";
        }
    }
}
