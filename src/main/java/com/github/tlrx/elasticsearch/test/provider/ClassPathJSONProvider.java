package com.github.tlrx.elasticsearch.test.provider;

import com.github.tlrx.elasticsearch.test.EsSetupRuntimeException;
import org.elasticsearch.common.io.Streams;

import java.io.IOException;

public class ClassPathJSONProvider implements JSONProvider {

    private ClassLoader classLoader;
    private String path;

    public ClassPathJSONProvider(ClassLoader classLoader, String path) {
        this.classLoader = classLoader;
        this.path = path;
    }

    @Override
    public String toJson() {
        try {
            return Streams.copyToStringFromClasspath(classLoader, path);
        } catch (IOException e) {
            throw new EsSetupRuntimeException(e);
        }
    }
}
