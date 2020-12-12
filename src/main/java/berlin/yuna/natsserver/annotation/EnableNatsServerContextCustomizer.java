package berlin.yuna.natsserver.annotation;

import berlin.yuna.clu.logic.SystemUtil;
import berlin.yuna.natsserver.config.NatsServerConfig;
import berlin.yuna.natsserver.logic.NatsServer;
import berlin.yuna.natsserver.model.exception.NatsStartException;
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

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StringUtils.isEmpty;

class EnableNatsServerContextCustomizer implements ContextCustomizer {

    private final EnableNatsServer enableNatsServer;
    private static final Logger LOG = getLogger(EnableNatsServerContextCustomizer.class);

    /**
     * Sets the source with parameter {@link EnableNatsServer} {@link EnableNatsServerContextCustomizer#customizeContext(ConfigurableApplicationContext, MergedContextConfiguration)}
     *
     * @param enableNatsServer {@link EnableNatsServer} annotation class
     */
    EnableNatsServerContextCustomizer(EnableNatsServer enableNatsServer) {
        this.enableNatsServer = enableNatsServer;
    }

    /**
     * customizeContext will start register {@link NatsServer} with bean name {@link NatsServer#BEAN_NAME} to the spring test context
     *
     * @param context      {@link ConfigurableApplicationContext}
     * @param mergedConfig {@link MergedContextConfiguration} is not in use
     */
    @Override
    public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        Assert.isInstanceOf(DefaultSingletonBeanRegistry.class, beanFactory);
        ConfigurableEnvironment environment = context.getEnvironment();

        if (enableNatsServer == null) {
            LOG.debug("Skipping [{}] cause its not defined", EnableNatsServer.class.getSimpleName());
            return;
        }

        NatsServer natsServerBean = new NatsServer(enableNatsServer.natsServerConfig());
        natsServerBean.port(overwritePort(natsServerBean));
        String sourceUrl = overwriteSourceUrl(environment, natsServerBean.source());
        natsServerBean.source(isEmpty(sourceUrl) ? natsServerBean.source() : sourceUrl);
        natsServerBean.setNatsServerConfig(mergeConfig(environment, natsServerBean.getNatsServerConfig()));

        try {
            natsServerBean.start();
        } catch (Exception e) {
            natsServerBean.stop();
            throw new NatsStartException("Failed to initialise " + EnableNatsServer.class.getSimpleName(), e);
        }

        beanFactory.initializeBean(natsServerBean, NatsServer.BEAN_NAME);
        beanFactory.registerSingleton(NatsServer.BEAN_NAME, natsServerBean);
        ((DefaultSingletonBeanRegistry) beanFactory).registerDisposableBean(NatsServer.BEAN_NAME, natsServerBean);
    }

    private String overwriteSourceUrl(final ConfigurableEnvironment environment, final String fallback) {
        return environment.getProperty("nats.source.default", environment.getProperty("nats.source." + SystemUtil.getOsType().toString().toLowerCase(), fallback));
    }

    private int overwritePort(NatsServer natsServerBean) {
        return enableNatsServer.port() != (Integer) NatsServerConfig.PORT.getDefaultValue() ? enableNatsServer.port() : natsServerBean.port();
    }

    private Map<NatsServerConfig, String> mergeConfig(final ConfigurableEnvironment environment, final Map<NatsServerConfig, String> originalConfig) {
        Map<NatsServerConfig, String> mergedConfig = new HashMap<>(originalConfig);
        for (NatsServerConfig natsServerConfig : NatsServerConfig.values()) {
            String key = "nats.server." + natsServerConfig.name().toLowerCase();
            String value = environment.getProperty(key);
            if (!isEmpty(value) && !mergedConfig.containsKey(natsServerConfig)) {
                mergedConfig.put(natsServerConfig, value);
            }
        }
        return mergedConfig;
    }
}
