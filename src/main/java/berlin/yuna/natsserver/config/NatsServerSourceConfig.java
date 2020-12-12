package berlin.yuna.natsserver.config;

public enum NatsServerSourceConfig {

    //Streaming Server Options
    ARM("https://github.com/nats-io/nats-streaming-server/releases/download/#VERSION#/nats-streaming-server-#VERSION#-linux-arm64.zip", "[STRING] ARM SOURCE URL"),
    LINUX("https://github.com/nats-io/nats-streaming-server/releases/download/#VERSION#/nats-streaming-server-#VERSION#-linux-amd64.zip", "[STRING] LINUX SOURCE URL"),
    MAC("https://github.com/nats-io/nats-streaming-server/releases/download/#VERSION#/nats-streaming-server-#VERSION#-darwin-amd64.zip", "[STRING] MAC SOURCE URL"),
    WINDOWS("https://github.com/nats-io/nats-streaming-server/releases/download/#VERSION#/nats-streaming-server-#VERSION#-windows-amd64.zip", "[STRING] WINDOWS SOURCE URL"),
    SOLARIS(LINUX.defaultValue, "[STRING] SOLARIS SOURCE URL"),
    DEFAULT(LINUX.defaultValue, "[STRING] DEFAULT SOURCE URL"),
    ;

    private static final String DEFAULT_VERSION = "v0.19.0";

    private final String defaultValue;
    private final String description;

    NatsServerSourceConfig(String defaultValue, String description) {
        this.defaultValue = defaultValue;
        this.description = description;
    }

    public String getDefaultValue() {
        return defaultValue.replace("#VERSION#", DEFAULT_VERSION);
    }

    public String getDescription() {
        return description;
    }
}
