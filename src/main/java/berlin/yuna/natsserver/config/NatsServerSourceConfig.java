package berlin.yuna.natsserver.config;

public enum NatsServerSourceConfig {

    //Streaming Server Options
    ARM("https://github.com/nats-io/nats-streaming-server/releases/download/v0.11.2/nats-streaming-server-v0.11.2-linux-arm64.zip", "[STRING] ARM SOURCE URL"),
    LINUX("https://github.com/nats-io/nats-streaming-server/releases/download/v0.11.2/nats-streaming-server-v0.11.2-linux-amd64.zip", "[STRING] LINUX SOURCE URL"),
    MAC("https://github.com/nats-io/nats-streaming-server/releases/download/v0.11.2/nats-streaming-server-v0.11.2-darwin-amd64.zip", "[STRING] MAC SOURCE URL"),
    WINDOWS("https://github.com/nats-io/nats-streaming-server/releases/download/v0.11.2/nats-streaming-server-v0.11.2-windows-amd64.zip", "[STRING] WINDOWS SOURCE URL"),
    SOLARIS(LINUX.defaultValue, "[STRING] SOLARIS SOURCE URL"),
    DEFAULT(LINUX.defaultValue, "[STRING] DEFAULT SOURCE URL"),
    ;

    private final String defaultValue;
    private final String description;

    NatsServerSourceConfig(String defaultValue, String description) {
        this.defaultValue = defaultValue;
        this.description = description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }
}
