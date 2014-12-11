package com.davidehrmann.nodejava.script;

public interface AdapterFactory<R> {
    Object JAVA_UNDEFINED = new Object() {
        @Override
        public String toString() {
            return "Java Undefined";
        }
    };

    R fromJava(Object o);

    /**
     * @param r Native result object from script engine
     * @return Either a List, Map, String, Double, or Function. Objects previously adapted with fromjava will be
     * unwrapped.
     */
    Object toJava(R r);
}
