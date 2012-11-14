package com.github.tlrx.elasticsearch.test.request;

import com.github.tlrx.elasticsearch.test.EsSetupRuntimeException;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;

import java.io.IOException;
import java.util.Map;

public class CreateTemplate implements Request<Void> {

    private final PutIndexTemplateRequest request;

    public CreateTemplate(String name) {
        this.request = new PutIndexTemplateRequest(name);
    }

    public CreateTemplate withTemplate(String template) {
        request.template(template);
        return this;
    }

    public CreateTemplate withSettings(String resourceName) {
        Settings settings = ImmutableSettings.settingsBuilder()
                .loadFromClasspath(resourceName)
                .build();
        withSettings(settings);
        return this;
    }

    public CreateTemplate withSettings(Settings settings) {
        request.settings(settings);
        return this;
    }

    public CreateTemplate withMapping(String type, String resourceName) {
        try {
            String mapping = Streams.copyToStringFromClasspath(getClass().getClassLoader(), resourceName);
            request.mapping(type, mapping);
        } catch (IOException e) {
            throw new EsSetupRuntimeException(e);
        }
        return this;
    }

    public CreateTemplate withMapping(String type, Map mapping) {
        request.mapping(type, mapping);
        return this;
    }

    public CreateTemplate withSource(String resourceName) {
        try {
            String source = Streams.copyToStringFromClasspath(getClass().getClassLoader(), resourceName);
            request.source(source);
        } catch (IOException e) {
            throw new EsSetupRuntimeException(e);
        }
        return this;
    }

    @Override
    public Void execute(final Client client) throws ElasticSearchException {
        try {
            PutIndexTemplateResponse response = client.admin().indices().execute(PutIndexTemplateAction.INSTANCE, request).get();

            if (!response.acknowledged()) {
                throw new EsSetupRuntimeException("Exception when putting index template");
            }
        } catch (Exception e) {
            throw new EsSetupRuntimeException(e);
        }
        return null;
    }
}
