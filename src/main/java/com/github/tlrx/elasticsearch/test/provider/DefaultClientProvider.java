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

import com.google.common.base.Preconditions;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;

/**
 * DefaultClientProvider uses a given {@link Client} instance. This provider is useful when the client is instantiated
 * by a Dependency Injection framework.
 */
public class DefaultClientProvider implements ClientProvider {

    private final Client client;
    private final boolean closeOnTerminate;

    public DefaultClientProvider(Client client, boolean closeClientOnTerminate) {
        Preconditions.checkNotNull(client, "No Client specified");
        this.closeOnTerminate = closeClientOnTerminate;
        this.client = client;
    }

    @Override
    public void open() {
        // Wait for Yellow status
        client().admin().cluster()
                .prepareHealth()
                .setWaitForYellowStatus()
                .setTimeout(TimeValue.timeValueMinutes(1))
                .execute()
                .actionGet();
    }

    @Override
    public Client client() {
        return client;
    }

    @Override
    public void close() {
        if (closeOnTerminate) {
            client().close();
        }
    }
}
