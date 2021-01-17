package berlin.yuna.natsserver.embedded.logic;

import berlin.yuna.clu.logic.SystemUtil;
import berlin.yuna.natsserver.config.NatsServerSourceConfig;
import berlin.yuna.natsserver.logic.Nats;
import org.springframework.beans.factory.DisposableBean;

import java.io.File;
import java.net.ConnectException;
import java.nio.file.Path;

import static berlin.yuna.natsserver.config.NatsServerConfig.PORT;

/**
 * {@link NatsServer}
 *
 * @author Yuna Morgenstern
 * @see SystemUtil.OperatingSystem
 * @see NatsServer
 * @since 1.0
 */
public class NatsServer extends Nats implements DisposableBean {

    public static final String BEAN_NAME = NatsServer.class.getSimpleName();

    /**
     * Create custom {@link NatsServer} with simplest configuration {@link NatsServer#setNatsServerConfig(String...)}
     *
     * @param natsServerConfig passes the original parameters to the server. example: port:4222, user:admin, password:admin
     */
    public NatsServer(final String... natsServerConfig) {
        this.setNatsServerConfig(natsServerConfig);
    }

    /**
     * Sets the port out of the configuration not from the real PID
     *
     * @return {@link NatsServer}
     * @throws RuntimeException with {@link ConnectException} when there is no port configured
     */
    public NatsServer port(int port) {
        getNatsServerConfig().put(PORT, String.valueOf(port));
        return this;
    }

    /**
     * Url to find nats server source
     *
     * @param natsServerUrl url of the source {@link NatsServerSourceConfig}
     * @return {@link NatsServer}
     */
    public NatsServer source(final String natsServerUrl) {
        super.source(natsServerUrl);
        return this;
    }

    protected Path getNatsServerPath(SystemUtil.OperatingSystem operatingSystem) {
        return super.getNatsServerPath(operatingSystem);
    }

    /**
     * Simply stops the {@link NatsServer}
     *
     * @see NatsServer#stop()
     */
    @Override
    public void destroy() {
        stop();
    }
}