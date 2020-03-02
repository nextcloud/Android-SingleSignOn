/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 sim <git@sgougeon.fr>
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2019 Desperate Coder <echotodevnull@gmail.com>
 * SPDX-FileCopyrightText: 2018-2019 David Luhmer <david-dev@live.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.api;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nextcloud.android.sso.QueryParam;
import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.helper.Okhttp3Helper;
import com.nextcloud.android.sso.helper.ReactivexHelper;
import com.nextcloud.android.sso.helper.Retrofit2Helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Completable;
import io.reactivex.Observable;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public class NextcloudRetrofitServiceMethod<T> {

    private final static String TAG = NextcloudRetrofitServiceMethod.class.getCanonicalName();
    private final Annotation[][] parameterAnnotationsArray;


    // Upper and lower characters, digits, underscores, and hyphens, starting with a character.
    private static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
    private static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");

    private final Method method;
    private String httpMethod;
    private @Nullable String relativeUrl;
    private @Nullable Headers headers;
    private final Type returnType;
    private boolean followRedirects = false;
    private final List<QueryParam> queryParameters;

    private final NextcloudRequest.Builder requestBuilder;
    private boolean isMultipart = false;
    private boolean isFormEncoded = false;


    public NextcloudRetrofitServiceMethod(String apiEndpoint, @NonNull Method method) {
        this.method = method;
        this.returnType = method.getGenericReturnType();
        Annotation[] methodAnnotations = method.getAnnotations();
        this.parameterAnnotationsArray = filterParameterAnnotations(method.getParameterAnnotations());

        for (Annotation annotation : methodAnnotations) {
            parseMethodAnnotation(annotation);
        }

        this.queryParameters = parsePathParameters();

        if(headers == null) {
            headers = new Headers.Builder().build();
        }

        requestBuilder = new NextcloudRequest.Builder()
                .setMethod(httpMethod)
                .setHeader(headers.toMultimap())
                .setFollowRedirects(followRedirects)
                .setUrl(new File(apiEndpoint, relativeUrl).toString());


        Log.d(TAG, "NextcloudRetrofitServiceMethod() called with: apiEndpoint = [" + apiEndpoint + "], method = [" + method + "]");

    }

    /**
     * filter out empty parameter annotations (e.g. when using kotlin)
     * @param annotations
     * @return
     */
    private Annotation[][] filterParameterAnnotations(Annotation[][] annotations) {
        List<Annotation[]> res = new ArrayList<>();

        for(Annotation[] annotation : annotations) {
            if(annotation.length > 0) {
                res.add(annotation);
            }
        }

        return res.toArray(new Annotation[res.size()][]);
    }

    public T invoke(NextcloudAPI nextcloudAPI, Object[] args) throws Exception {
        //if(parameterAnnotationsArray.length != args.length) {
        if(args.length < parameterAnnotationsArray.length) { // Ignore if too many parameters are given (e.g. when using kotlin)
            throw new InvalidParameterException("Expected: " + parameterAnnotationsArray.length + " params - were: " + args.length);
        }

        final NextcloudRequest.Builder rBuilder = new NextcloudRequest.Builder(requestBuilder);

        // Copy all static query params into parameters array
        final List<QueryParam> parameters = new LinkedList<>(this.queryParameters);

        final MultipartBody.Builder multipartBuilder = isMultipart
                ? new MultipartBody.Builder()
                : null;

        // Build/parse dynamic parameters
        for (int i = 0; i < parameterAnnotationsArray.length; i++) {
            final Annotation annotation = parameterAnnotationsArray[i][0];
            if (annotation instanceof Query) {
                final String key = ((Query)annotation).value();
                if (args[i] instanceof Collection) {
                    for (Object arg : (Collection<?>) args[i]) {
                        parameters.add(new QueryParam(key, String.valueOf(arg)));
                    }
                } else {
                    parameters.add(new QueryParam(key, String.valueOf(args[i])));
                }
            } else if(annotation instanceof Body) {
                rBuilder.setRequestBody(nextcloudAPI.getGson().toJson(args[i]));
            } else if(annotation instanceof Path) {
                final String varName = "{" + ((Path) annotation).value() + "}";
                final String url = rBuilder.build().getUrl();
                rBuilder.setUrl(url.replace(varName, String.valueOf(args[i])));
            } else if(annotation instanceof Header) {
                addHeader(rBuilder, ((Header) annotation).value(), args[i]);
            } else if(annotation instanceof FieldMap) {
                if(args[i] != null) {
                    final Map<String, Object> fieldMap = (HashMap<String, Object>) args[i];
                    for (String key : fieldMap.keySet()) {
                        final Object value = fieldMap.get(key);
                        parameters.add(new QueryParam(key, value == null ? "" : value.toString()));
                    }
                }
            } else if(annotation instanceof Field) {
                if(args[i] != null) {
                    parameters.add(new QueryParam(((Field) annotation).value(), args[i].toString()));
                }
            } else if(annotation instanceof Part) {
                if (args[i] instanceof MultipartBody.Part){
                    multipartBuilder.addPart((MultipartBody.Part) args[i]);
                } else {
                    throw new IllegalArgumentException("Only MultipartBody.Part type is supported as a @Part");
                }
            } else {
                throw new UnsupportedOperationException("don't know this type yet.. [" + annotation + "]");
            }
        }

        // include multipart body as stream, set header
        if (isMultipart) {
            if(multipartBuilder == null) {
                throw new IllegalStateException("isMultipart == true, expected multipartBuilder to not be null.");
            }
            final MultipartBody multipartBody = multipartBuilder.build();
            addHeader(rBuilder, "Content-Type", MultipartBody.FORM + "; boundary=" + multipartBody.boundary());
            rBuilder.setRequestBodyAsStream(bodyToStream(multipartBody));
        }

        final NextcloudRequest request = rBuilder
                .setParameter(parameters)
                .build();


        if(this.returnType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) returnType;
            Type ownerType = type.getRawType();
            if(ownerType == Observable.class || ownerType == io.reactivex.rxjava3.core.Observable.class) {
                Type typeArgument = type.getActualTypeArguments()[0];
                Log.d(TAG, "invoke call to api using observable " + typeArgument);

                // Streaming
                if(typeArgument == ResponseBody.class) {
                    if(ownerType == Observable.class) {
                        return (T) Observable.fromCallable(() -> Okhttp3Helper.getResponseBodyFromRequestV2(nextcloudAPI, request));
                    } else {
                        return (T) io.reactivex.rxjava3.core.Observable.fromCallable(() -> Okhttp3Helper.getResponseBodyFromRequestV2(nextcloudAPI, request));
                    }
                } else if (typeArgument instanceof ParameterizedType) {
                    ParameterizedType innerType = (ParameterizedType) typeArgument;
                    Type innerOwnerType = innerType.getRawType();
                    if(innerOwnerType == ParsedResponse.class) {
                        if(ownerType == Observable.class) {
                            return (T) nextcloudAPI.performRequestObservableV2(innerType.getActualTypeArguments()[0], request);
                        } else {
                            return (T) nextcloudAPI.performRequestObservableV3(innerType.getActualTypeArguments()[0], request);
                        }
                    }
                }
                //fallback
                if(ownerType == Observable.class) {
                    return (T) nextcloudAPI.performRequestObservableV2(typeArgument, request).map(r -> r.getResponse());
                } else {
                    return (T) nextcloudAPI.performRequestObservableV3(typeArgument, request).map(r -> r.getResponse());
                }

            } else if(ownerType == Call.class) {
                Type typeArgument = type.getActualTypeArguments()[0];
                return (T) Retrofit2Helper.WrapInCall(nextcloudAPI, request, typeArgument);
            }
        } else if (this.returnType == Observable.class) {
            return (T) nextcloudAPI.performRequestObservableV2(Object.class, request).map(r -> r.getResponse());
        } else if (this.returnType == io.reactivex.rxjava3.core.Observable.class) {
            return (T) nextcloudAPI.performRequestObservableV3(Object.class, request).map(r -> r.getResponse());
        } else if (this.returnType == Completable.class) {
            return (T) ReactivexHelper.wrapInCompletable(nextcloudAPI, request);
        } else if (this.returnType == io.reactivex.rxjava3.core.Completable.class) {
            return (T) ReactivexHelper.wrapInCompletableV3(nextcloudAPI, request);
        }

        return nextcloudAPI.performRequestV2(this.returnType, request);
    }

    private void addHeader(NextcloudRequest.Builder rBuilder, String key, Object value) {
        if (key == null || value == null) {
            Log.d(TAG, "WARNING: Header not set - key or value missing! Key: " + key + " | Value: " + value);
            return;
        }
        final Map<String, List<String>> headers = rBuilder.build().getHeader();
        final List<String> arg = new ArrayList<>();
        arg.add(String.valueOf(value));
        headers.put(key, arg);
        rBuilder.setHeader(headers);
    }

    private static InputStream bodyToStream(final @NonNull RequestBody request){
        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            copy.writeTo(buffer);
            return buffer.inputStream();
        }
        catch (final IOException e) {
            throw new IllegalStateException("failed to build request-body", e);
        }
    }

    private void parseMethodAnnotation(Annotation annotation) {
        if (annotation instanceof DELETE) {
            parseHttpMethodAndPath("DELETE", ((DELETE) annotation).value(), false);
        } else if (annotation instanceof GET) {
            parseHttpMethodAndPath("GET", ((GET) annotation).value(), false);
        } else if (annotation instanceof POST) {
            parseHttpMethodAndPath("POST", ((POST) annotation).value(), true);
        } else if (annotation instanceof PUT) {
            parseHttpMethodAndPath("PUT", ((PUT) annotation).value(), true);
        } else if (annotation instanceof HEAD) {
            parseHttpMethodAndPath("HEAD", ((HEAD) annotation).value(), false);
        } else if (annotation instanceof HTTP) {
            HTTP http = (HTTP) annotation;
            parseHttpMethodAndPath(http.method(), http.path(), http.hasBody());
        } else if (annotation instanceof Multipart) {
            if (isFormEncoded) {
                throw methodError(method, "Only one encoding annotation is allowed.");
            }
            isMultipart = true;
        } else if (annotation instanceof FormUrlEncoded) {
            if (isMultipart) {
                throw methodError(method, "Only one encoding annotation is allowed.");
            }
            isFormEncoded = true;
        } else if (annotation instanceof Streaming) {
            Log.v(TAG, "streaming interface");
        } else if (annotation instanceof retrofit2.http.Headers) {
            String[] headersToParse = ((retrofit2.http.Headers) annotation).value();
            if (headersToParse.length == 0) {
                throw methodError(method, "@Headers annotation is empty.");
            }
            headers = parseHeaders(headersToParse);
        } else if(annotation instanceof NextcloudAPI.FollowRedirects) {
            followRedirects = true;
        } else {
            throw new UnsupportedOperationException(String.valueOf(annotation));
        }
    }

    private void parseHttpMethodAndPath(@Nullable String httpMethod, @NonNull String value, boolean hasBody) {
        if (this.httpMethod != null) {
            throw methodError(method, "Only one HTTP method is allowed. Found: %s and %s.",
                    this.httpMethod, httpMethod);
        }
        this.httpMethod = httpMethod;

        if (value.isEmpty()) {
            return;
        }

        this.relativeUrl = value;
    }

    private Headers parseHeaders(String[] headers) {
        final Headers.Builder builder = new Headers.Builder();
        for (String header : headers) {
            final int colon = header.indexOf(':');
            if (colon == -1 || colon == 0 || colon == header.length() - 1) {
                throw methodError(method,
                        "@Headers value must be in the form \"Name: Value\". Found: \"%s\"", header);
            }
            final String headerName = header.substring(0, colon);
            final String headerValue = header.substring(colon + 1).trim();
            if ("Content-Type".equalsIgnoreCase(headerName)) {
                try {
                    MediaType.parse(headerValue);
                } catch (IllegalArgumentException e) {
                    throw methodError(method, e, "Malformed content type: %s", headerValue);
                }
            } else {
                builder.add(headerName, headerValue);
            }
        }
        return builder.build();
    }

    /**
     * Gets the set of unique path parameters used in the given URI. If a parameter is used twice
     * in the URI, it will only show up once in the set.
     */
    private List<QueryParam> parsePathParameters() {
        final List<QueryParam> queryPairs = new LinkedList<>();

        if (this.relativeUrl == null) {
            return queryPairs;
        }

        final int idxQuery = this.relativeUrl.indexOf("?");
        if (idxQuery != -1 && idxQuery < this.relativeUrl.length() - 1) {
            // Ensure the query string does not have any named parameters.
            final String query = this.relativeUrl.substring(idxQuery + 1);

            // Check for named parameters
            final Matcher queryParamMatcher = PARAM_URL_REGEX.matcher(query);
            if (queryParamMatcher.find()) {
                throw methodError(method, "URL query string \"%s\" must not have replace block. "
                        + "For dynamic query parameters use @Query.", query);
            }

            // If none found… parse the static query parameters
            final String[] pairs = query.split("&");
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                queryPairs.add(new QueryParam(pair.substring(0, idx), pair.substring(idx + 1)));
            }

            // Remove query params from url
            this.relativeUrl = this.relativeUrl.substring(0, idxQuery);
        }
        return queryPairs;
    }

    private static RuntimeException methodError(Method method, String message, Object... args) {
        return methodError(method, null, message, args);
    }

    private static RuntimeException methodError(Method method, @Nullable Throwable cause, String message,
                                        Object... args) {
        final String formattedMessage = String.format(message, args);
        return new IllegalArgumentException(formattedMessage
                + "\n    for method "
                + method.getDeclaringClass().getSimpleName()
                + "."
                + method.getName(), cause);
    }
}
