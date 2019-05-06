package com.nextcloud.android.sso.api;

import androidx.annotation.Nullable;
import android.util.Log;

import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.helper.Okhttp3Helper;
import com.nextcloud.android.sso.helper.ReactivexHelper;
import com.nextcloud.android.sso.helper.Retrofit2Helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Completable;
import io.reactivex.Observable;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public class NextcloudRetrofitServiceMethod<T> {

    private static String TAG = NextcloudRetrofitServiceMethod.class.getCanonicalName();
    private final Annotation[][] parameterAnnotationsArray;


    // Upper and lower characters, digits, underscores, and hyphens, starting with a character.
    private static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
    private static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");
    //private static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);

    private Method method;
    private String httpMethod;
    private @Nullable String relativeUrl;
    private @Nullable Headers headers;
    private Type returnType;
    private boolean followRedirects = false;
    //private boolean formUrlEncoded = false;

    private final NextcloudRequest.Builder requestBuilder;


    public NextcloudRetrofitServiceMethod(String apiEndpoint, Method method) {
        this.method = method;
        this.returnType = method.getGenericReturnType();
        Annotation[] methodAnnotations = method.getAnnotations();
        //Type[] parameterTypes = method.getGenericParameterTypes();
        this.parameterAnnotationsArray = method.getParameterAnnotations();

        for (Annotation annotation : methodAnnotations) {
            parseMethodAnnotation(annotation);
        }

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

    public T invoke(NextcloudAPI nextcloudAPI, Object[] args) throws Exception {
        if(parameterAnnotationsArray.length != args.length) {
            throw new InvalidParameterException("Expected: " + parameterAnnotationsArray.length + " params - were: " + args.length);
        }

        //NextcloudRequest.Builder rBuilder = (NextcloudRequest.Builder) requestBuilder.clone();
        NextcloudRequest.Builder rBuilder = cloneSerializable(requestBuilder);


        Map<String, String> parameters = new HashMap<>();

        for(int i = 0; i < parameterAnnotationsArray.length; i++) {
            Annotation annotation = parameterAnnotationsArray[i][0];

            if(annotation instanceof Query) {
                parameters.put(((Query)annotation).value(), String.valueOf(args[i]));
            } else if(annotation instanceof Body) {
                rBuilder.setRequestBody(nextcloudAPI.getGson().toJson(args[i]));
            } else if(annotation instanceof Path) {
                String varName = "{" + ((Path)annotation).value() + "}";
                String url = rBuilder.build().getUrl();
                rBuilder.setUrl(url.replace(varName, String.valueOf(args[i])));
            } else if(annotation instanceof Header) {
                Map<String, List<String>> headers = rBuilder.build().getHeader();
                List<String> arg = new ArrayList<>();
                if(args[i] != null) {
                    arg.add(String.valueOf(args[i]));
                    headers.put(((Header) annotation).value(), arg);
                }
                rBuilder.setHeader(headers);
            } else if(annotation instanceof FieldMap) {
                if(args[i] != null) {
                    Map<String, Object> fieldMap = (HashMap<String, Object>) args[i];
                    for (String key : fieldMap.keySet()) {
                        parameters.put(key, fieldMap.get(key).toString());
                    }
                }
            } else if(annotation instanceof Field) {
                if(args[i] != null) {
                    String field = args[i].toString();
                    parameters.put(((Field)annotation).value(), field);
                }
            } else {
                throw new UnsupportedOperationException("don't know this type yet.. [" + String.valueOf(annotation) + "]");
            }
        }

        NextcloudRequest request = rBuilder
                .setParameter(parameters)
                .build();


        if(this.returnType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) returnType;
            Type ownerType = type.getRawType();
            if(ownerType == Observable.class) {
                Type typeArgument = type.getActualTypeArguments()[0];
                Log.d(TAG, "invoke call to api using observable " + typeArgument);

                // Streaming
                if(typeArgument == ResponseBody.class) {
                    return (T) Observable.just(Okhttp3Helper.getResponseBodyFromRequest(nextcloudAPI, request));
                } else {
                    return (T) nextcloudAPI.performRequestObservable(typeArgument, request);
                }
            } else if(ownerType == Call.class) {
                Type typeArgument = type.getActualTypeArguments()[0];
                return (T) Retrofit2Helper.WrapInCall(nextcloudAPI, request, typeArgument);
            }
        } else if(this.returnType == Observable.class) {
            return (T) nextcloudAPI.performRequestObservable(Object.class, request);
        } else if (this.returnType == Completable.class) {
            return (T) ReactivexHelper.wrapInCompletable(nextcloudAPI, request);
        }

        return nextcloudAPI.performRequest(this.returnType, request);
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
        } else if (annotation instanceof Streaming) {
            Log.v(TAG, "streaming interface");
        } else if (annotation instanceof retrofit2.http.Headers) {
            String[] headersToParse = ((retrofit2.http.Headers) annotation).value();
            if (headersToParse.length == 0) {
                throw methodError(method, "@Headers annotation is empty.");
            }
            headers = parseHeaders(headersToParse);
        } else if(annotation instanceof FormUrlEncoded) {
            //formUrlEncoded = true;
            Log.v(TAG, "FormUrlEncoded request");
        } else if(annotation instanceof NextcloudAPI.FollowRedirects) {
            followRedirects = true;
        } else {
            throw new UnsupportedOperationException(String.valueOf(annotation));
        }

        /*
        else if (annotation instanceof HEAD) {
            parseHttpMethodAndPath("HEAD", ((HEAD) annotation).value(), false);
        } else if (annotation instanceof PATCH) {
            parseHttpMethodAndPath("PATCH", ((PATCH) annotation).value(), true);
        } else if (annotation instanceof OPTIONS) {
            parseHttpMethodAndPath("OPTIONS", ((OPTIONS) annotation).value(), false);
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
        }
        */
    }

    private void parseHttpMethodAndPath(String httpMethod, String value, boolean hasBody) {
        if (this.httpMethod != null) {
            throw methodError(method, "Only one HTTP method is allowed. Found: %s and %s.",
                    this.httpMethod, httpMethod);
        }
        this.httpMethod = httpMethod;

        if (value.isEmpty()) {
            return;
        }

        // Get the relative URL path and existing query string, if present.
        int question = value.indexOf('?');
        if (question != -1 && question < value.length() - 1) {
            // Ensure the query string does not have any named parameters.
            String queryParams = value.substring(question + 1);
            Matcher queryParamMatcher = PARAM_URL_REGEX.matcher(queryParams);
            if (queryParamMatcher.find()) {
                throw methodError(method, "URL query string \"%s\" must not have replace block. "
                        + "For dynamic query parameters use @Query.", queryParams);
            }
        }

        this.relativeUrl = value;
        //Set<String> relativeUrlParamNames = parsePathParameters(value);
    }

    private Headers parseHeaders(String[] headers) {
        Headers.Builder builder = new Headers.Builder();
        for (String header : headers) {
            int colon = header.indexOf(':');
            if (colon == -1 || colon == 0 || colon == header.length() - 1) {
                throw methodError(method,
                        "@Headers value must be in the form \"Name: Value\". Found: \"%s\"", header);
            }
            String headerName = header.substring(0, colon);
            String headerValue = header.substring(colon + 1).trim();
            if ("Content-Type".equalsIgnoreCase(headerName)) {
                try {
                    MediaType.parse(headerValue);
                    //MediaType contentType = MediaType.parse(headerValue);
                    //Log.v(TAG, contentType.toString());
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
    /*
    private static Set<String> parsePathParameters(String path) {
        Matcher m = PARAM_URL_REGEX.matcher(path);
        Set<String> patterns = new LinkedHashSet<>();
        while (m.find()) {
            patterns.add(m.group(1));
        }
        return patterns;
    }
    */





    private static RuntimeException methodError(Method method, String message, Object... args) {
        return methodError(method, null, message, args);
    }

    private static RuntimeException methodError(Method method, @Nullable Throwable cause, String message,
                                        Object... args) {
        String formattedMessage = String.format(message, args);
        return new IllegalArgumentException(formattedMessage
                + "\n    for method "
                + method.getDeclaringClass().getSimpleName()
                + "."
                + method.getName(), cause);
    }

    /*
    private static RuntimeException parameterError(Method method,
                                           Throwable cause, int p, String message, Object... args) {
        return methodError(method, cause, message + " (parameter #" + (p + 1) + ")", args);
    }

    private static RuntimeException parameterError(Method method, int p, String message, Object... args) {
        return methodError(method, message + " (parameter #" + (p + 1) + ")", args);
    }
    */



    private static <T extends Serializable> T cloneSerializable(T o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject(o);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()) );
        T res  = null;
        try {
            res = (T) ois.readObject();
        } catch (ClassNotFoundException e) {
            // Can't happen as we just clone an object..
            Log.e(TAG, "ClassNotFoundException", e);
        }
        ois.close();

        return res;
    }
}
