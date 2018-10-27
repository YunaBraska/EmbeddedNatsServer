package berlin.yuna.natsserver.logic;

import berlin.yuna.natsserver.config.NatsServerConfig;
import berlin.yuna.natsserver.config.NatsServerSourceConfig;
import berlin.yuna.system.logic.SystemUtil;
import berlin.yuna.system.logic.SystemUtil.OperatingSystem;
import berlin.yuna.system.logic.Terminal;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingFormatArgumentException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static berlin.yuna.natsserver.config.NatsServerConfig.PORT;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.WINDOWS;
import static berlin.yuna.system.logic.SystemUtil.getOsType;
import static berlin.yuna.system.logic.SystemUtil.killProcessByName;
import static java.nio.channels.Channels.newChannel;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static java.util.Comparator.comparingLong;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * {@link NatsServer}
 *
 * @author Yuna Morgenstern
 * @see OperatingSystem
 * @see NatsServer
 * @since 1.0
 */
public class NatsServer implements DisposableBean {

    /**
     * simpleName from {@link NatsServer} class
     */
    private static final Logger LOG = getLogger(NatsServer.class);
    public static final String BEAN_NAME = NatsServer.class.getSimpleName();
    private static final OperatingSystem OPERATING_SYSTEM = getOsType();
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    private Process process;
    private String natsServerSource = NatsServerSourceConfig.LINUX.getDefaultValue();
    private Map<NatsServerConfig, String> natsServerConfig = getDefaultConfig();

    /**
     * Create {@link NatsServer} without any start able configuration
     */
    public NatsServer() {
    }

    /**
     * Create {@link NatsServer} with simplest start able configuration
     *
     * @param port start port - common default port is 4222
     */
    public NatsServer(int port) {
        natsServerConfig.put(PORT, String.valueOf(port));
    }

    /**
     * Create custom {@link NatsServer} with simplest configuration {@link NatsServer#setNatsServerConfig(String...)}
     *
     * @param natsServerConfig passes the original parameters to the server. example: port:4222, user:admin, password:admin
     */
    public NatsServer(String... natsServerConfig) {
        this.setNatsServerConfig(natsServerConfig);
    }

    /**
     * GetNatServerConfig
     *
     * @return the {@link NatsServer} configuration but not the config of the real PID
     */
    public Map<NatsServerConfig, String> getNatsServerConfig() {
        return natsServerConfig;
    }

    /**
     * Passes the original parameters to the server on startup
     *
     * @param natsServerConfig passes the original parameters to the server.
     * @see NatsServer#setNatsServerConfig(String...)
     * @see NatsServerConfig
     */
    public void setNatsServerConfig(final Map<NatsServerConfig, String> natsServerConfig) {
        this.natsServerConfig = natsServerConfig;
    }

    /**
     * Passes the original parameters to the server on startup
     *
     * @param natsServerConfigArray example: port:4222, user:admin, password:admin
     * @see NatsServerConfig
     */
    public void setNatsServerConfig(final String... natsServerConfigArray) {
        for (String property : natsServerConfigArray) {
            String[] pair = property.split(":");
            if (!StringUtils.hasText(property) || pair.length != 2) {
                LOG.error("Could not parse property [{}] pair length [{}]", property, pair.length);
                continue;
            }
            natsServerConfig.put(NatsServerConfig.valueOf(pair[0].toUpperCase().replace("-", "")), pair[1]);
        }
    }

    /**
     * Starts the server in {@link ProcessBuilder} with the given parameterConfig {@link NatsServer#setNatsServerConfig(String...)}
     *
     * @throws IOException              if {@link NatsServer} is not found or unsupported on the {@link OperatingSystem}
     * @throws BindException            if port is already taken
     * @throws PortUnreachableException if {@link NatsServer} is not starting cause port is not free
     */
    public void start() throws IOException {
        if (process != null) {
            LOG.error("[{}] is already running", BEAN_NAME);
            return;
        }

        if (!waitForPort(true)) {
            throw new BindException("Address already in use [" + getPort() + "]");
        }

        Path natsServerPath = getNatsServerPath(OPERATING_SYSTEM);
        SystemUtil.setFilePermissions(natsServerPath, OWNER_EXECUTE, OTHERS_EXECUTE, OWNER_READ, OTHERS_READ, OWNER_WRITE, OTHERS_WRITE);
        LOG.info("Starting [{}] port [{}] version [{}-{}]", BEAN_NAME, getPort(), OPERATING_SYSTEM);

        String command = prepareCommand(natsServerPath);

        LOG.debug(command);

        process = new Terminal().timeoutMs(10000).breakOnError(false).execute(command).process();

        if (!waitForPort(false)) {
            throw new PortUnreachableException(BEAN_NAME + "failed to start.");
        }
        LOG.info("Started [{}] port [{}] version [{}-{}]", BEAN_NAME, getPort(), OPERATING_SYSTEM);
    }

    /**
     * Stops the {@link ProcessBuilder} and kills the {@link NatsServer}
     * Only a log error will occur if the {@link NatsServer} were never started
     *
     * @throws RuntimeException as {@link InterruptedException} if shutdown is interrupted
     */
    public void stop() {
        try {
            LOG.info("Stopping [{}]", BEAN_NAME);
            process.destroy();
            process.waitFor();
            killProcessByName(getNatsServerPath(OPERATING_SYSTEM).getFileName().toString());
        } catch (NullPointerException | InterruptedException e) {
            LOG.warn("Could not stop [{}] cause cant find process", BEAN_NAME);
        } finally {
            LOG.info("Stopped [{}]", BEAN_NAME);
        }
    }

    /**
     * Gets the port out of the configuration not from the real PID
     *
     * @return configured port of the server
     * @throws RuntimeException with {@link ConnectException} when there is no port configured
     */
    public int getPort() {
        String port = natsServerConfig.get(PORT);
        if (port != null) {
            return Integer.valueOf(port);
        }
        throw new MissingFormatArgumentException("Could not initialise port" + BEAN_NAME);
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

    /**
     * Gets Nats server path
     *
     * @return Resource/{SIMPLE_CLASS_NAME}/{NATS_SERVER_VERSION}/{OPERATING_SYSTEM}/{SIMPLE_CLASS_NAME}
     */
    Path getNatsServerPath(final OperatingSystem operatingSystem) {
        StringBuilder targetPath = new StringBuilder();
        targetPath.append(BEAN_NAME.toLowerCase()).append(File.separator);
        targetPath.append(BEAN_NAME.toLowerCase()).append((operatingSystem == WINDOWS ? ".exe" : ""));

        return downloadNats(targetPath);
    }

    /**
     * Url to find nats server source
     *
     * @param natsServerUrl url of the source {@link berlin.yuna.natsserver.config.NatsServerSourceConfig}
     */
    public void setNatsServerSource(final String natsServerUrl) {
        this.natsServerSource = natsServerUrl;
    }

    private Path downloadNats(final StringBuilder targetPath) {
        File tmpFile = new File(TMP_DIR, (new File(targetPath.toString())).getName());
        if (!tmpFile.exists()) {
            try {
                File zipFile = new File(tmpFile.getParent(), tmpFile.getName() + ".zip");
                tmpFile.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(zipFile);
                fos.getChannel().transferFrom(newChannel(new URL(natsServerSource).openStream()), 0, Long.MAX_VALUE);
                return unzip(zipFile, tmpFile);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return tmpFile.toPath();
    }

    private Path unzip(final File source, final File target) throws IOException {
        final ZipFile zipFile = new ZipFile(source);
        ZipEntry max = zipFile.stream().max(comparingLong(ZipEntry::getSize)).get();
        Files.copy(zipFile.getInputStream(max), target.toPath());
        source.delete();
        return target.toPath();
    }

    private boolean waitForPort(boolean isFree) {
        final long start = System.currentTimeMillis();
        long timeout = SECONDS.toMillis(10);

        while (System.currentTimeMillis() - start < timeout) {
            if (isPortAvailable(getPort()) == isFree) {
                return true;
            }
            Thread.yield();
        }
        return false;
    }

    private static boolean isPortAvailable(int port) {
        try {
            new Socket("localhost", port).close();
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    private String prepareCommand(Path natsServerPath) {
        StringBuilder command = new StringBuilder();
        command.append(natsServerPath.toString());
        for (Entry<NatsServerConfig, String> entry : getNatsServerConfig().entrySet()) {
            String key = entry.getKey().getKey();

            if (isEmpty(entry.getValue())) {
                LOG.warn("Skipping property [{}] with value [{}]", key, entry.getValue());
                continue;
            }

            command.append(" ");

            command.append(key);
            command.append(entry.getValue().trim().toLowerCase());
        }
        return command.toString();
    }

    private Map<NatsServerConfig, String> getDefaultConfig() {
        Map<NatsServerConfig, String> defaultConfig = new HashMap<>();
        for (NatsServerConfig natsServerConfig : NatsServerConfig.values()) {
            if (natsServerConfig.getDefaultValue() != null) {
                defaultConfig.put(natsServerConfig, natsServerConfig.getDefaultValue().toString());
            }
        }
        return defaultConfig;
    }

    @Override
    public String toString() {
        return BEAN_NAME + "{" +
                "NATS_SERVER_VERSION=" + natsServerSource +
                ", OPERATING_SYSTEM=" + OPERATING_SYSTEM +
                ", port=" + getPort() +
                '}';
    }
}