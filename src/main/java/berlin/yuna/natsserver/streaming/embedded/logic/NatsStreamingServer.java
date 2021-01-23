package berlin.yuna.natsserver.streaming.embedded.logic;

import berlin.yuna.clu.logic.SystemUtil;
import berlin.yuna.natsserver.config.NatsStreamingSourceConfig;
import berlin.yuna.natsserver.logic.NatsStreaming;
import org.springframework.beans.factory.DisposableBean;

import java.net.ConnectException;
import java.nio.file.Path;

/**
 * {@link NatsStreamingServer}
 */
public class NatsStreamingServer extends NatsStreaming implements DisposableBean {

    public static final String BEAN_NAME = NatsStreamingServer.class.getSimpleName();
    private final long timeoutMs;

    /**
     * Create custom {@link NatsStreamingServer} with simplest configuration {@link NatsStreamingServer#setConfig(String...)}
     *
     * @param timeoutMs        tear down timeout
     * @param natsServerConfig passes the original parameters to the server. example: port:4222, user:admin, password:admin
     */
    public NatsStreamingServer(final long timeoutMs, final String... natsServerConfig) {
        super(natsServerConfig);
        this.timeoutMs = timeoutMs;
    }

    /**
     * Create {@link NatsStreamingServer} with simplest start able configuration
     *
     * @param timeoutMs tear down timeout
     */
    public NatsStreamingServer(final long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    /**
     * Sets the port out of the configuration
     *
     * @param port {@code -1} for random port
     * @return {@link NatsStreamingServer}
     * @throws RuntimeException with {@link ConnectException} when there is no port configured
     */
    public NatsStreamingServer port(final int port) {
        super.port(port);
        return this;
    }

    /**
     * Url to find nats server source
     *
     * @param natsServerUrl url of the source {@link NatsStreamingSourceConfig}
     * @return {@link NatsStreamingServer}
     */
    public NatsStreamingServer source(final String natsServerUrl) {
        super.source(natsServerUrl);
        return this;
    }

    protected Path getNatsServerPath(SystemUtil.OperatingSystem operatingSystem) {
        return super.getNatsServerPath(operatingSystem);
    }

    /**
     * Simply stops the {@link NatsStreamingServer}
     *
     * @see NatsStreamingServer#stop()
     */
    @Override
    public void destroy() {
        stop(timeoutMs);
    }
}