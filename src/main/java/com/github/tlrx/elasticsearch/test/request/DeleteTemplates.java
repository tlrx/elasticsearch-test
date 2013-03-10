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
package com.github.tlrx.elasticsearch.test.request;

import com.github.tlrx.elasticsearch.test.EsSetupRuntimeException;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.cluster.state.ClusterStateAction;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequestBuilder;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateAction;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateResponse;
import org.elasticsearch.client.Client;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * A {@link com.github.tlrx.elasticsearch.test.request.Request} used to delete one, many templates or all templates.
 */
public class DeleteTemplates implements Request<Void> {
    /**
     * Templates to delete
     */
    private final String[] templates;
    /**
     * Fail-fast mode (disabled by default)
     */
    private boolean failFast = false;

    /**
     * Constructor
     *
     * @param templates Templates to delete, if no template is specified all templates will be used
     */
    public DeleteTemplates(String... templates) {
        this.templates = templates;
    }

    /**
     * Enables fail-fast mode (if an template can not be deleted, others won't)
     */
    public DeleteTemplates failFast() {
        this.failFast = true;
        return this;
    }

    /**
     * Get the names of templates to delete, if no template is provided the complete list of templates is retrieved
     */
    private Collection<String> getTemplates(Client client) {
        Collection<String> templatesColl;
        if (this.templates==null || this.templates.length==0) {
            // Retrieve all templates
            ClusterStateRequestBuilder clusterStateRequestBuilder =
                    ClusterStateAction.INSTANCE.newRequestBuilder(client.admin().cluster())
                            .setFilterAll().setFilterMetaData(false);
            ClusterStateResponse clusterStateResponse = clusterStateRequestBuilder.execute().actionGet();
            templatesColl = clusterStateResponse.getState().getMetaData().getTemplates().keySet();
        } else {
            // Use provided templates
            templatesColl = Arrays.asList(templates);
        }
        return templatesColl;
    }
    @Override
    public Void execute(Client client) throws ElasticSearchException {
        Set<String> unacknowledgedTemplates = new HashSet<String>();
        EsSetupRuntimeException runtimeException = null;
        for (String template : getTemplates(client)) {
            try {
                DeleteIndexTemplateRequest request = new DeleteIndexTemplateRequest(template);
                DeleteIndexTemplateResponse response = client.admin().indices()
                        .execute(DeleteIndexTemplateAction.INSTANCE, request).get();
                if (!response.isAcknowledged()) {
                    if (failFast) {
                        throw new EsSetupRuntimeException("Exception when deleting index template: " + template);
                    } else {
                        unacknowledgedTemplates.add(template);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                runtimeException = handleException(runtimeException, e);
            } catch (ExecutionException e) {
                runtimeException = handleException(runtimeException, e);
            }
        }
        if (runtimeException != null) {
            throw runtimeException;
        } else if (!unacknowledgedTemplates.isEmpty()) {
            throw new EsSetupRuntimeException("Exception when deleting index template: " + unacknowledgedTemplates);
        }
        return null;
    }

    private EsSetupRuntimeException handleException(EsSetupRuntimeException currentException, Exception newException) {
        if (failFast) {
            throw new EsSetupRuntimeException(newException);
        } else if (currentException == null) {
            currentException = new EsSetupRuntimeException(newException);
        }
        return currentException;
    }

    @Override
    public String toString() {
        return "delete templates [" +
                "templates=" + (templates == null ? null : Arrays.asList(templates)) +
                ']';
    }
}
