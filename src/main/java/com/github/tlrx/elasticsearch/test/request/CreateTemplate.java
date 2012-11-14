package com.github.tlrx.elasticsearch.test.request;

import com.github.tlrx.elasticsearch.test.EsSetupRuntimeException;
import com.github.tlrx.elasticsearch.test.provider.JSONProvider;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;

import java.util.Map;

public class CreateTemplate implements Request<Void> {

    private final PutIndexTemplateRequest request;
    private String name;

    public CreateTemplate(String name) {
        this.name = name;
        this.request = new PutIndexTemplateRequest(name);
    }

    public CreateTemplate withTemplate(String template) {
        request.template(template);
        return this;
    }

    public CreateTemplate withSettings(Settings settings) {
        request.settings(settings);
        return this;
    }

    public CreateTemplate withSettings(String source) {
        Settings settings = ImmutableSettings.settingsBuilder()
                .loadFromSource(source)
                .build();
        withSettings(settings);
        return this;
    }

    public CreateTemplate withSettings(JSONProvider jsonProvider) {
        withSettings(jsonProvider.toJson());
        return this;
    }

    public CreateTemplate withMapping(String type, Map mapping) {
        request.mapping(type, mapping);
        return this;
    }

    public CreateTemplate withMapping(String type, String source) {
        request.mapping(type, source);
        return this;
    }

    public CreateTemplate withMapping(String type, JSONProvider jsonProvider) {
        withMapping(type, jsonProvider.toJson());
        return this;
    }

    public CreateTemplate withSource(String source) {
        request.source(source);
        return this;
    }

    public CreateTemplate withSource(JSONProvider jsonProvider) {
        request.source(jsonProvider.toJson());
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

    @Override
    public String toString() {
        return "create template [" +
                "name='" + name + '\'' +
                ']';
    }
}
