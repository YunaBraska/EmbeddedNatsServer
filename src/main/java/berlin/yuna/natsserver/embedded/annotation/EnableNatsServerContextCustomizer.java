package berlin.yuna.natsserver.embedded.annotation;

import berlin.yuna.clu.logic.SystemUtil;
import berlin.yuna.natsserver.config.NatsServerConfig;
import berlin.yuna.natsserver.embedded.logic.NatsServer;
import berlin.yuna.natsserver.embedded.model.exception.NatsStartException;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.util.Assert;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static berlin.yuna.natsserver.config.NatsServerConfig.PORT;
import static berlin.yuna.natsserver.embedded.logic.NatsServer.BEAN_NAME;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StringUtils.hasText;

class EnableNatsServerContextCustomizer implements ContextCustomizer {

    private final EnableNatsServer enableNatsServer;
    private static final Logger LOG = getLogger(EnableNatsServerContextCustomizer.class);

    /**
     * Sets the source with parameter {@link EnableNatsServer} {@link EnableNatsServerContextCustomizer#customizeContext(ConfigurableApplicationContext, MergedContextConfiguration)}
     *
     * @param enableNatsServer {@link EnableNatsServer} annotation class
     */
    EnableNatsServerContextCustomizer(final EnableNatsServer enableNatsServer) {
        this.enableNatsServer = enableNatsServer;
    }

    /**
     * customizeContext will start register {@link NatsServer} with bean name {@link NatsServer#BEAN_NAME} to the spring test context
     *
     * @param context      {@link ConfigurableApplicationContext}
     * @param mergedConfig {@link MergedContextConfiguration} is not in use
     */
    @Override
    public void customizeContext(final ConfigurableApplicationContext context, final MergedContextConfiguration mergedConfig) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        Assert.isInstanceOf(DefaultSingletonBeanRegistry.class, beanFactory);
        ConfigurableEnvironment environment = context.getEnvironment();

        if (enableNatsServer == null) {
            LOG.debug("Skipping [{}] cause its not defined", EnableNatsServer.class.getSimpleName());
            return;
        }

        final NatsServer natsServerBean = new NatsServer(enableNatsServer.timeoutMs());
        natsServerBean.setNatsServerConfig(enableNatsServer.natsServerConfig());
        natsServerBean.port(overwritePort(natsServerBean));
        String sourceUrl = overwriteSourceUrl(environment, natsServerBean.source());
        natsServerBean.source(!hasText(sourceUrl) ? natsServerBean.source() : sourceUrl);
        natsServerBean.setNatsServerConfig(mergeConfig(environment, natsServerBean.getNatsServerConfig()));

        try {
            natsServerBean.start(enableNatsServer.timeoutMs());
        } catch (Exception e) {
            natsServerBean.stop(enableNatsServer.timeoutMs());
            throw new NatsStartException("Failed to initialise " + EnableNatsServer.class.getSimpleName(), e);
        }

        beanFactory.initializeBean(natsServerBean, BEAN_NAME);
        beanFactory.registerSingleton(BEAN_NAME, natsServerBean);
        ((DefaultSingletonBeanRegistry) beanFactory).registerDisposableBean(BEAN_NAME, natsServerBean);
    }

    private String overwriteSourceUrl(final ConfigurableEnvironment environment, final String fallback) {
        return environment.getProperty("nats.source.default", environment.getProperty("nats.source." + SystemUtil.getOsType().toString().toLowerCase(), fallback));
    }

    private int overwritePort(final NatsServer natsServerBean) {
        if(enableNatsServer.randomPort()){
            return getNextFreePort();
        }
        return enableNatsServer.port() > 0 && enableNatsServer.port() != (Integer) PORT.getDefaultValue() ? enableNatsServer.port() : natsServerBean.port();
    }

    private Map<NatsServerConfig, String> mergeConfig(final ConfigurableEnvironment environment, final Map<NatsServerConfig, String> originalConfig) {
        Map<NatsServerConfig, String> mergedConfig = new HashMap<>(originalConfig);
        for (NatsServerConfig natsServerConfig : NatsServerConfig.values()) {
            String key = "nats.server." + natsServerConfig.name().toLowerCase();
            String value = environment.getProperty(key);
            if (hasText(value) && !mergedConfig.containsKey(natsServerConfig)) {
                mergedConfig.put(natsServerConfig, value);
            }
        }
        return mergedConfig;
    }

    private int getNextFreePort() {
        final Random random = new Random();
        for (int i = 0; i < 64; i++) {
            final int port = random.nextInt(277) + 4223;
            if (!isPortInUse(port)) {
                return port;
            }
        }
        throw new IllegalStateException("Could not find any free port");
    }

    private boolean isPortInUse(final int portNumber) {
        try {
            new Socket("localhost", portNumber).close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
