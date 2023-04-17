/*
 *  Nextcloud SingleSignOn
 *
 *  @author David Luhmer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextcloud.android.sso.aidl;


import androidx.core.util.ObjectsCompat;
import androidx.core.util.Pair;

import com.nextcloud.android.sso.QueryParam;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.ToString;

@ToString
public class NextcloudRequest implements Serializable {

    private static final long serialVersionUID = 215521212534240L; //assign a long value

    private String method;
    private Map<String, List<String>> header = new HashMap<>();
    private Map<String, String> parameter = new HashMap<>();
    private String requestBody;
    private String url;
    private String token;
    private String packageName;
    private String accountName;
    private transient InputStream bodyAsStream = null;
    private boolean followRedirects;
    private Collection<QueryParam> parameterV2 = new LinkedList<>();

    private NextcloudRequest() { }

    public NextcloudRequest(NextcloudRequest ncr) {
        this.method = ncr.method;
        this.requestBody = ncr.requestBody;
        this.url = ncr.url;
        this.token = ncr.token;
        this.packageName = ncr.packageName;
        this.accountName = ncr.accountName;
        this.followRedirects = ncr.followRedirects;
        header = new HashMap<>(ncr.header);
        parameter = new HashMap<>(ncr.parameter);
        parameterV2 = new ArrayList<>(ncr.parameterV2);
        bodyAsStream = ncr.bodyAsStream;

    }

    public static class Builder implements Serializable {

        private static final long serialVersionUID = 2121321432424242L; //assign a long value

        private final NextcloudRequest ncr;

        public Builder() {
            ncr = new NextcloudRequest();
        }

        public Builder(Builder cloneSource) {
            ncr = new NextcloudRequest(cloneSource.ncr);
        }

        public NextcloudRequest build() {
            return ncr;
        }

        public Builder setMethod(String method) {
            ncr.method = method;
            return this;
        }

        public Builder setHeader(Map<String, List<String>> header) {
            ncr.header = header;
            return this;
        }

        /**
         * Sets the parameters for this request.
         * All existing parameters will be wiped!
         *
         * @param parameter new set of parameters
         * @return this (Builder)
         */
        public Builder setParameter(Collection<QueryParam> parameter) {
            ncr.parameterV2 = parameter;
            ncr.parameter = new HashMap<>();
            for (QueryParam pair : parameter) {
                ncr.parameter.put(pair.key, pair.value);
            }
            return this;
        }

        public Builder addParameter(Collection<QueryParam> parameter) {
            for (QueryParam param : parameter) {
                nullCheck(param);
                ncr.parameterV2.add(param);
                ncr.parameter.put(param.key, param.value);
            }
            return this;
        }

        public Builder addParameter(QueryParam parameter) {
            nullCheck(parameter);
            ncr.parameter.put(parameter.key, parameter.value);
            ncr.parameterV2.add(parameter);
            return this;
        }

        private static void nullCheck(Object o) {
            if (o == null) {
                throw new IllegalArgumentException("null is not allowed here");
            }
        }

        public Builder clearParameter() {
            ncr.parameterV2.clear();
            ncr.parameter.clear();
            return this;
        }

        /**
         * Remove a parameter by pair.
         * This method calls the remove() method of a list!
         * @param parameter
         * @return this (Builder)
         */
        public Builder removeParameter(Pair<String, String> parameter) {
            ncr.parameterV2.remove(parameter);
            ncr.parameter.remove(parameter.first);
            return this;
        }
        /**
         * Removes all parameters with the specified key.
         * If the key doesn't exist, the parameters won't be modified.
         * @param key key of the parameter
         * @return this (Builder)
         */
        public Builder removeParameter(String key) {
            if (key == null) {
                throw new IllegalArgumentException("null keys shouldn't be added as parameters at all");
            }
            for (QueryParam pair : ncr.parameterV2) {
                if (pair != null && key.equals(pair.key)) {
                    ncr.parameterV2.remove(pair);
                }
            }
            ncr.parameter.remove(key);
            return this;
        }


        public Builder setRequestBody(String requestBody) {
            ncr.requestBody = requestBody;
            return this;
        }
        public Builder setRequestBodyAsStream(InputStream requestBody) {
            ncr.bodyAsStream = requestBody;
            return this;
        }

        public Builder setUrl(String url) {
            ncr.url = url;
            return this;
        }

        public Builder setToken(String token) {
            ncr.token = token;
            return this;
        }

        public Builder setAccountName(String accountName) {
            ncr.accountName = accountName;
            return this;
        }

        /**
         * Default value: true
         * @param followRedirects
         * @return
         */
        public Builder setFollowRedirects(boolean followRedirects) {
            ncr.followRedirects = followRedirects;
            return this;
        }
    }

    public String getMethod() {
        return this.method;
    }

    public Map<String, List<String>> getHeader() {
        return this.header;
    }

    public Collection<QueryParam> getParameterV2() {
        return this.parameterV2;
    }

    public String getRequestBody() {
        return this.requestBody;
    }

    public String getUrl() {
        return this.url;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAccountName() {
        return this.accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public boolean isFollowRedirects() {
        return this.followRedirects;
    }

    public InputStream getBodyAsStream() {
        return bodyAsStream;
    }

    public void setBodyAsStream(InputStream bodyAsStream) {
        this.bodyAsStream = bodyAsStream;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof NextcloudRequest)) {
            return false;
        }

        NextcloudRequest rq = (NextcloudRequest)obj;
        boolean equal;
        equal  = ObjectsCompat.equals(this.accountName, rq.accountName);
        equal &= ObjectsCompat.equals(this.header, rq.header);
        equal &= ObjectsCompat.equals(this.method, rq.method);
        equal &= ObjectsCompat.equals(this.packageName, rq.packageName);
        equal &= (
                ObjectsCompat.equals(this.parameterV2, rq.parameterV2) ||
                (
                    this.parameterV2 != null && rq.parameterV2 != null
                    && this.parameterV2.size() == rq.parameterV2.size()
                    && this.parameterV2.containsAll(rq.parameterV2)
                )
        );
        equal &= ObjectsCompat.equals(this.parameter, rq.parameter);
        equal &= ObjectsCompat.equals(this.requestBody, rq.requestBody);
        equal &= ObjectsCompat.equals(this.token, rq.token);
        equal &= ObjectsCompat.equals(this.url, rq.url);
        equal &= ObjectsCompat.equals(this.followRedirects, rq.followRedirects);

        return equal;
    }

}
