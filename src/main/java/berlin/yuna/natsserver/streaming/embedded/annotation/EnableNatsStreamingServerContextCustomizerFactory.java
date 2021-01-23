package berlin.yuna.natsserver.streaming.embedded.annotation;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

import java.util.List;

class EnableNatsStreamingServerContextCustomizerFactory implements ContextCustomizerFactory {

    /**
     * @param testClass        current test class with {@link EnableNatsStreamingServer} annotation
     * @param configAttributes {@link ContextConfigurationAttributes} not in use
     * @return {@link EnableNatsStreamingServerContextCustomizer}
     */
    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        EnableNatsStreamingServer enableNatsServer = AnnotatedElementUtils.findMergedAnnotation(testClass, EnableNatsStreamingServer.class);
        return new EnableNatsStreamingServerContextCustomizer(enableNatsServer);
    }

}