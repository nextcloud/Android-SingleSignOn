package com.nextcloud.android.sso.helper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Function;

@androidx.annotation.Discouraged(message = "Do not publish this class.\n" +
        "This must not be returned or a parameter of a published function.")
public class InternalOption<T> {
    @Nullable private final T val;

     public InternalOption(@Nullable T val) {
         this.val = val;
     }
     
     public T orElse(@NonNull T fallback) {
         if (val == null) {
             return fallback;
         } else {
             return val;
         }
     }

     public T get() {
         if (val == null) {
             throw new java.util.NoSuchElementException();
         } else {
             return val;
         }
     }

    // Use androidx function
     public <A> InternalOption<A> map(Function<T, A> mapper) {
         return new InternalOption<>(mapper.apply(val));
     }

    // Use androidx function
    public <B> void ifPresent(Function<T, B> mapper) {
         if (val != null) {
             mapper.apply(val);
         }
    }

    public boolean isPresent() {
        return val != null;
    }
}