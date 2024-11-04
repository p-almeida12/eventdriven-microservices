package com.microservices.common.util;

import java.util.ArrayList;
import java.util.List;

public class CollectionsUtil {

    //prevent external instantiation
    private CollectionsUtil() {
    }

    //single instance is loaded, guarantees it will be instantiated only once
    //thread-safe and efficient, without requiring explicit synchronization or complex double-checked locking
    private static class CollectionsUtilHolder {
        static final CollectionsUtil INSTANCE = new CollectionsUtil();
    }

    //provides access to the singleton instance
    public static CollectionsUtil getInstance() {
        return CollectionsUtilHolder.INSTANCE;
    }

    public <T> List<T> getListFromIterable(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }

}
