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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author kje
 */
public class ZoneRecord {

    @Expose
    private String name;

    private Integer ttl;

    private String type;

    private Integer priority;

    @Expose
    @SerializedName("data")
    private String value;

    public ZoneRecord() {
    }

    public ZoneRecord(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public ZoneRecord(String name, Integer ttl, String Type, String value) {
        this.name = name;
        this.ttl = ttl;
        this.type = Type;
        this.value = value;
    }

    public ZoneRecord(String name, Integer ttl, String Type, Integer priority, String value) {
        this.name = name;
        this.ttl = ttl;
        this.type = Type;
        this.priority = priority;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public String value() {
        return value;
    }

    public LinkedHashMap<String, String> asMap() {
        var map = new LinkedHashMap<String, String>();
        asMap(map);
        return map;
    }

    public Map<String, String> asMap(Map<String, String> map) {
        map.put("record", name);
        map.put("ttl", ttl != null ? Integer.toString(ttl) : "");
        map.put("type", type);
        map.put("priority", priority != null ? Integer.toString(priority) : "");
        map.put("value", value);
        return map;
    }

    public void setRecord(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ZoneRecord other = (ZoneRecord) obj;
        return Objects.equals(this.value, other.value);
    }

    @Override
    public String toString() {
        return "Record{" + "name=" + name + ", ttl=" + ttl + ", Type=" + type + ", priority=" + priority + ", value=" + value + '}';
    }
}
