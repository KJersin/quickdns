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

import dk.jersin.letsencrypt.CertbotHook;
import java.util.concurrent.Callable;
import picocli.CommandLine;

import static picocli.CommandLine.*;
import static picocli.CommandLine.Model.*;

/**
 *
 * @author kje
 */
@Command(
        name = "acme",
        description = "Inserts or removes _acme-challenge record(s)",
        subcommands = {
            AcmeCommand.InsertCommand.class
        }
)
public class AcmeCommand {

    @Option(names = {"-d", "--domain"}, required = true)
    String domain;

    @Mixin
    private MainContext ctx;

    @Spec
    private CommandSpec spec;

    @Command(
            name = "insert",
            description = "Insert a \"_acme-challenge\" TXT record."
    )
    public static class InsertCommand implements Runnable {

        @Parameters(arity = "1", description = "The challenge value to be inserted")
        String challengeValue;

        @Override
        public void run() {
        }
    }
}
