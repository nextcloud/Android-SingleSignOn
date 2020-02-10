package retrofit2;

import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.api.NextcloudRetrofitServiceMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class NextcloudRetrofitApiBuilder {

    private final Map<Method, NextcloudRetrofitServiceMethod<?>> serviceMethodCache = new ConcurrentHashMap<>();

    private final NextcloudAPI mNextcloudAPI;
    private final String mApiEndpoint;

    public NextcloudRetrofitApiBuilder(NextcloudAPI nextcloudAPI, String apiEndpoint) {
        this.mNextcloudAPI = nextcloudAPI;
        this.mApiEndpoint = apiEndpoint;
    }

    @SuppressWarnings("unchecked") // Single-interface proxy creation guarded by parameter safety.
    public <T> T create(final Class<T> service) {
        validateServiceInterface(service);
        return (T) Proxy.newProxyInstance(
                service.getClassLoader(),
                new Class<?>[]{service},
                (proxy, method, args) -> loadServiceMethod(method).invoke(mNextcloudAPI, args != null ? args : new Object[0]));
    }

    private NextcloudRetrofitServiceMethod<?> loadServiceMethod(Method method) {
        NextcloudRetrofitServiceMethod<?> result = serviceMethodCache.get(method);
        if (result != null) return result;

        synchronized (serviceMethodCache) {
            result = serviceMethodCache.get(method);
            if (result == null) {
                result = new NextcloudRetrofitServiceMethod(mApiEndpoint, method);
                serviceMethodCache.put(method, result);
            }
        }
        return result;
    }

    private static <T> void validateServiceInterface(Class<T> service) {
        if (!service.isInterface()) {
            throw new IllegalArgumentException("API declarations must be interfaces.");
        }
        // Prevent API interfaces from extending other interfaces. This not only avoids a bug in
        // Android (http://b.android.com/58753) but it forces composition of API declarations which is
        // the recommended pattern.
        if (service.getInterfaces().length > 0) {
            throw new IllegalArgumentException("API interfaces must not extend other interfaces.");
        }
    }
}
