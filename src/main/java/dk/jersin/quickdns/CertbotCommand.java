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

import dk.jersin.letsencrypt.CertbotHook;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;
import static picocli.CommandLine.Model.*;

/**
 *
 * @author kje
 */
@Command(
        name = "certbot",
        description
        = "Handles the Certbot authentication hooks by using arguments from the CERTBOT_* environment variables.\n"
        + "See: https://eff-certbot.readthedocs.io/en/stable/using.html#hooks \n"
        + "Also: Please use the bash script quickdns-certbot-auth to avoid running as root."
)
public class CertbotCommand implements Callable<Integer> {

    @Mixin
    private MainContext ctx;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        // Connect, login and load the zones
        int exitCode = 0;
        var zones = ctx.login();
        try {
            // Execute the hook
            exitCode = CertbotHook.fromEnvironment(zones).call();
        } finally {
            ctx.logout();
        }
        return exitCode;
    }

}
