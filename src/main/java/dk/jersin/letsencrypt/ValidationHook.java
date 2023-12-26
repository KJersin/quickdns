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

import dk.jersin.quickdns.services.Zones;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author kje
 */
public class ValidationHook {
    
    private String domain;
    private String validation;
    private String token;
    private Integer remainingChallenges;
    private List<String> allDomains;
    private Optional<String> authOutput;

    public ValidationHook(String domain, String validation, String token, Integer remainingChallenges, List<String> allDomains) {
        this.domain = domain;
        this.validation = validation;
        this.token = token;
        this.remainingChallenges = remainingChallenges;
        this.allDomains = allDomains;
        this.authOutput = Optional.empty();
    }
    
    public ValidationHook(String domain, String validation, String token, Integer remainingChallenges, List<String> allDomains, String authOutput) {
        this.domain = domain;
        this.validation = validation;
        this.token = token;
        this.remainingChallenges = remainingChallenges;
        this.allDomains = allDomains;
        this.authOutput = Optional.of(authOutput);
    }

    /**
     * TODO: Get parameters from environment variables as defined in https://eff-certbot.readthedocs.io/en/stable/using.html#hooks
     * @param quickDns
     * @param env
     * @return 
     */
    public static ValidationHook fromMap(Zones quickDns, Map<String, String> env) {
        System.getenv();
        return null;
    }

    /**
     * TODO: Get parameters from environment variables as defined in https://eff-certbot.readthedocs.io/en/stable/using.html#hooks
     * @param quickDns
     * @return 
     */
    public static ValidationHook fromEnvironment(Zones quickDns) {
        return fromMap(quickDns, System.getenv());
    }
}
