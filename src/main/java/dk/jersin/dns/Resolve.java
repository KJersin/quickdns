package dk.jersin.dns;

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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

/**
 *
 * @author kje
 */
public class Resolve {

    @Expose
    @SerializedName("Status")
    private int status;

    @Expose
    @SerializedName("Answer")
    private List<ZoneRecord> answer = emptyList();

    @Expose
    @SerializedName("Comment")
    private String comment;

    public Optional<ZoneRecord> find(ZoneRecord record) {
        return answer.stream()
                .filter(record::equals)
                .findFirst();
    }
}
