package berlin.yuna.natsserver.annotation;

import berlin.yuna.natsserver.logic.NatsServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;

import static berlin.yuna.natsserver.config.NatsServerConfig.PORT;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@DisplayName("ContextCustomizerTest")
public class EnableNatsServerContextCustomizerTest {

    @Autowired
    private ConfigurableApplicationContext context;

    @Test
    @DisplayName("with invalid port [FAIL]")
    public void runCustomizer_withInvalidPort_shouldNotStartNatsServer() {
        EnableNatsServer enableNatsServer = mock(EnableNatsServer.class);
        when(enableNatsServer.natsServerConfig()).thenReturn(new String[]{PORT + ":invalidPortValue"});
        EnableNatsServerContextCustomizer customizer = new EnableNatsServerContextCustomizer(enableNatsServer);
        assertThrows(
                IllegalArgumentException.class,
                () -> customizer.customizeContext(context, mock(MergedContextConfiguration.class)),
                "Failed to initialise EnableNatsServer"
        );
        assertThrows(
                NoSuchBeanDefinitionException.class,
                () -> context.getBean(NatsServer.class),
                "No qualifying bean of type"
        );
    }
}