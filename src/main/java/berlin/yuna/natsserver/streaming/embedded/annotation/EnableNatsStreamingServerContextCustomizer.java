package berlin.yuna.natsserver.streaming.embedded.annotation;

import berlin.yuna.clu.logic.SystemUtil;
import berlin.yuna.natsserver.config.NatsStreamingConfig;
import berlin.yuna.natsserver.streaming.embedded.logic.NatsStreamingServer;
import berlin.yuna.natsserver.streaming.embedded.model.exception.NatsStreamingStartException;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

import static berlin.yuna.natsserver.config.NatsStreamingConfig.PORT;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StringUtils.hasText;

class EnableNatsStreamingServerContextCustomizer implements ContextCustomizer {

    private final EnableNatsStreamingServer enableNatsServer;
    private static final Logger LOG = getLogger(EnableNatsStreamingServerContextCustomizer.class);

    /**
     * Sets the source with parameter {@link EnableNatsStreamingServer} {@link EnableNatsStreamingServerContextCustomizer#customizeContext(ConfigurableApplicationContext, MergedContextConfiguration)}
     *
     * @param enableNatsServer {@link EnableNatsStreamingServer} annotation class
     */
    EnableNatsStreamingServerContextCustomizer(final EnableNatsStreamingServer enableNatsServer) {
        this.enableNatsServer = enableNatsServer;
    }

    /**
     * customizeContext will start register {@link NatsStreamingServer} with bean name {@link NatsStreamingServer#BEAN_NAME} to the spring test context
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
            LOG.debug("Skipping [{}] cause its not defined", EnableNatsStreamingServer.class.getSimpleName());
            return;
        }

        final NatsStreamingServer natsServerBean = new NatsStreamingServer(enableNatsServer.timeoutMs());
        natsServerBean.setConfig(enableNatsServer.config());
        natsServerBean.port(overwritePort(natsServerBean));
        String sourceUrl = overwriteSourceUrl(environment, natsServerBean.source());
        natsServerBean.source(!hasText(sourceUrl) ? natsServerBean.source() : sourceUrl);
        natsServerBean.setConfig(mergeConfig(environment, natsServerBean.getConfig()));

        try {
            natsServerBean.start(enableNatsServer.timeoutMs());
        } catch (Exception e) {
            natsServerBean.stop(enableNatsServer.timeoutMs());
            throw new NatsStreamingStartException("Failed to initialise " + EnableNatsStreamingServer.class.getSimpleName(), e);
        }

        beanFactory.initializeBean(natsServerBean, NatsStreamingServer.BEAN_NAME);
        beanFactory.registerSingleton(NatsStreamingServer.BEAN_NAME, natsServerBean);
        ((DefaultSingletonBeanRegistry) beanFactory).registerDisposableBean(NatsStreamingServer.BEAN_NAME, natsServerBean);
    }

    private String overwriteSourceUrl(final ConfigurableEnvironment environment, final String fallback) {
        return environment.getProperty("nats.streaming.source.default", environment.getProperty("nats.streaming.source." + SystemUtil.getOsType().toString().toLowerCase(), fallback));
    }

    private int overwritePort(final NatsStreamingServer natsServerBean) {
        if (enableNatsServer.randomPort()) {
            return -1;
        }
        return enableNatsServer.port() > 0 && enableNatsServer.port() != (Integer) PORT.getDefaultValue() ? enableNatsServer.port() : natsServerBean.port();
    }

    private Map<NatsStreamingConfig, String> mergeConfig(final ConfigurableEnvironment environment, final Map<NatsStreamingConfig, String> originalConfig) {
        Map<NatsStreamingConfig, String> mergedConfig = new HashMap<>(originalConfig);
        for (NatsStreamingConfig NatsStreamingConfig : NatsStreamingConfig.values()) {
            String key = "nats.streaming.server." + NatsStreamingConfig.name().toLowerCase();
            String value = environment.getProperty(key);
            if (hasText(value) && !mergedConfig.containsKey(NatsStreamingConfig)) {
                mergedConfig.put(NatsStreamingConfig, value);
            }
        }
        return mergedConfig;
    }
}
