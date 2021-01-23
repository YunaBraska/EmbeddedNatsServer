package berlin.yuna.natsserver.streaming.embedded.util;


import berlin.yuna.configmetadata.model.ConfigurationMetadata;
import berlin.yuna.configmetadata.model.Groups;
import berlin.yuna.natsserver.config.NatsStreamingConfig;
import berlin.yuna.natsserver.config.NatsStreamingSourceConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@Tag("UnitTest")
class ConfigMetadataIntegrationTest {

    @Test
    @DisplayName("Generate spring boot autocompletion")
    void generate() throws IOException {
        ConfigurationMetadata metadata = new ConfigurationMetadata("nats.streaming.server", NatsStreamingConfig.class);
        for (NatsStreamingConfig config : NatsStreamingConfig.values()) {
            String name = config.name().toLowerCase();
            String desc = config.getDescription();
            metadata.newProperties().name(name).description(parseDesc(desc)).type(parseType(desc)).defaultValue(config.getDefaultValue());
        }

        Groups groups = metadata.newGroups("nats.streaming.source", NatsStreamingSourceConfig.class);
        for (NatsStreamingSourceConfig config : NatsStreamingSourceConfig.values()) {
            String name = config.name().toLowerCase();
            String desc = config.getDescription();
            metadata.newProperties().name(groups, name).description(parseDesc(desc)).type(parseType(desc)).defaultValue(config.getDefaultValue());
        }

        Path generated = metadata.generate();
        assertThat(generated, is(notNullValue()));
    }

    private String parseDesc(final String description) {
        return description.substring(description.indexOf(']') + 1).trim();
    }

    private Class<?> parseType(final String description) {
        String goType = description.replace("-", "");
        goType = goType.substring(1, goType.indexOf(']')).trim();
        switch (goType) {
            case "INT":
                return Integer.class;
            case "SIZE":
                return Long.class;
            case "BOOL":
                return Boolean.class;
            default:
                return String.class;
        }
    }
}
