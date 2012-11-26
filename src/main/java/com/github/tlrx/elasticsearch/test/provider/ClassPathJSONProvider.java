/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.tlrx.elasticsearch.test.provider;

import com.github.tlrx.elasticsearch.test.EsSetupRuntimeException;
import org.elasticsearch.common.Preconditions;
import org.elasticsearch.common.io.Streams;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A ClassPathJSONProvider is able to load a file from the classpath and returns its content as a JSON String.
 */
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
        return toString();
    }

    @Override
    public String toString() {
        try {
            if (klass != null) {
                InputStream inputStream = klass.getResourceAsStream(path);
                if (inputStream == null) {
                    throw new FileNotFoundException("Resource [" + path + "] not found in classpath with class  [" + klass.getName() + "]");
                }
                return Streams.copyToString(new InputStreamReader(inputStream, "UTF-8"));
            } else {
                return Streams.copyToStringFromClasspath(classLoader, path);
            }
        } catch (IOException e) {
            throw new EsSetupRuntimeException(e);
        }
    }
}
