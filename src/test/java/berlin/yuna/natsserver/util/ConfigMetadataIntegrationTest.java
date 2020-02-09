package berlin.yuna.natsserver.util;


import berlin.yuna.configmetadata.model.ConfigurationMetadata;
import berlin.yuna.configmetadata.model.Groups;
import berlin.yuna.natsserver.config.NatsServerConfig;
import berlin.yuna.natsserver.config.NatsServerSourceConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConfigMetadataIntegrationTest {

    @Test
    public void generate() throws IOException {
        ConfigurationMetadata metadata = new ConfigurationMetadata("nats.server", NatsServerConfig.class);
        for (NatsServerConfig config : NatsServerConfig.values()) {
            String name = config.name().toLowerCase();
            String desc = config.getDescription();
            metadata.newProperties().name(name).description(parseDesc(desc)).type(parseType(desc)).defaultValue(config.getDefaultValue());
        }

        Groups groups = metadata.newGroups("nats.source", NatsServerSourceConfig.class);
        for (NatsServerSourceConfig config : NatsServerSourceConfig.values()) {
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

    private Class parseType(final String description) {
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
