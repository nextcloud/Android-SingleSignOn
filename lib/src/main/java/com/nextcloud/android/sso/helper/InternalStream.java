package com.nextcloud.android.sso.helper;

import androidx.core.util.Function;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class InternalStream<T> {
    private final Collection<T> collection;

    public InternalStream(Collection<T> list) {
        this.collection = list;
    }

    // Use androidx function
    public <A> InternalStream<A> map(Function<T, A> mapper) {
        Collection<A> out = new LinkedList<>();
        for (T t : collection) {
            out.add(mapper.apply(t));
        }
        return new InternalStream<>(out);
    }

    // Use androidx function
    public <A, B> Map<A, B> collectMap(Function<T, A> keyCollector, Function<T, B> valueCollector) {
        Map<A, B> out = new HashMap<>();
        for (T t : collection) {
            out.put(keyCollector.apply(t), valueCollector.apply(t));
        }
        return out;
    }

    // Use androidx function
    public InternalStream<T> filter(Function<T, Boolean> filter) {
        for (T t : collection) {
            if (!filter.apply(t)) {
                collection.remove(t);
            }
        }
        return this;
    }

    public Boolean anyMatch(Function<T, Boolean> filter) {
        return this.filter(filter).findFirst().isPresent();
    }

    public InternalOption<T> findFirst() {
        Iterator<T> iterator = collection.iterator();
        if (iterator.hasNext()) {
            return new InternalOption<>(iterator.next());
        }
        return new InternalOption<>(null);
    }

    public T[] toArray(Function<Integer, T[]> generator) {
        T[] out = generator.apply(collection.size());
        int i = 0;
        for (T t : collection) {
            out[i++] = t;
        }
        return out;
    }
}
