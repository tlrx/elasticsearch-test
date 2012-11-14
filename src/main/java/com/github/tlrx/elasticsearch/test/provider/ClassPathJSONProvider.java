package com.github.tlrx.elasticsearch.test.provider;

import com.github.tlrx.elasticsearch.test.EsSetupRuntimeException;
import org.elasticsearch.common.Preconditions;
import org.elasticsearch.common.io.Streams;

import java.io.IOException;
import java.io.InputStreamReader;

public class ClassPathJSONProvider implements JSONProvider {

    private Class klass;
    private ClassLoader classLoader;
    private String path;

    public ClassPathJSONProvider(ClassLoader classLoader, String path) {
        Preconditions.checkNotNull(classLoader, "No ClassLoader specified");
        this.classLoader = classLoader;
        this.path = path;
    }

    public ClassPathJSONProvider(Class klass, String path) {
        Preconditions.checkNotNull(klass, "No Class specified");
        this.klass = klass;
        this.path = path;
    }

    @Override
    public String toJson() {
        try {
            if (klass != null) {
                return Streams.copyToString(new InputStreamReader(klass.getResourceAsStream(path), "UTF-8"));
            } else {
                return Streams.copyToStringFromClasspath(classLoader, path);
            }
        } catch (IOException e) {
            throw new EsSetupRuntimeException(e);
        }
    }
}
