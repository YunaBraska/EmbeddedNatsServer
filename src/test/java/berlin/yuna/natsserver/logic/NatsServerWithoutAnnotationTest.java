package berlin.yuna.natsserver.logic;

import berlin.yuna.clu.logic.SystemUtil;
import berlin.yuna.natsserver.config.NatsServerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingFormatArgumentException;

import static berlin.yuna.clu.logic.SystemUtil.OperatingSystem.WINDOWS;
import static berlin.yuna.natsserver.config.NatsServerConfig.AUTH;
import static berlin.yuna.natsserver.config.NatsServerConfig.MAX_AGE;
import static berlin.yuna.natsserver.config.NatsServerConfig.PASS;
import static berlin.yuna.natsserver.config.NatsServerConfig.PORT;
import static berlin.yuna.natsserver.config.NatsServerConfig.USER;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Tag("IntegrationTest")
@DisplayName("NatsServer plain java")
public class NatsServerWithoutAnnotationTest {

    @Value("${nats.source.default}")
    private String natsSource;

    @Test
    @DisplayName("No start without annotation")
    public void natsServer_withoutAnnotation_shouldNotBeStarted() {
        assertThrows(
                ConnectException.class,
                () -> new Socket("localhost", 4245).close(),
                "Connection refused"
        );
    }

    @Test
    @DisplayName("Default config")
    public void natsServer_withoutConfig_shouldStartWithDefaultValues() throws Exception {
        NatsServer natsServer = new NatsServer().port(4238).source(natsSource);
        assertThat(natsServer.source(), is(equalTo(natsSource)));
        natsServer.start();
        natsServer.stop();
    }

    @Test
    @DisplayName("Setup config")
    public void natsServer_configureConfig_shouldNotOverwriteOldConfig() {
        NatsServer natsServer = new NatsServer(4240).source(natsSource);
        natsServer.setNatsServerConfig("user:adminUser", "PAss:adminPw");

        assertThat(natsServer.getNatsServerConfig().get(USER), is(equalTo("adminUser")));
        assertThat(natsServer.getNatsServerConfig().get(PASS), is(equalTo("adminPw")));

        natsServer.setNatsServerConfig("user:newUser");
        assertThat(natsServer.getNatsServerConfig().get(USER), is(equalTo("newUser")));
        assertThat(natsServer.getNatsServerConfig().get(PASS), is("adminPw"));

        Map<NatsServerConfig, String> newConfig = new HashMap<>();
        newConfig.put(USER, "oldUser");
        natsServer.setNatsServerConfig(newConfig);
        assertThat(natsServer.getNatsServerConfig().get(USER), is(equalTo("oldUser")));
    }

    @Test
    @DisplayName("Unknown config is ignored")
    public void natsServer_invalidConfig_shouldNotRunIntroException() {
        NatsServer natsServer = new NatsServer(4240).source(natsSource);
        natsServer.setNatsServerConfig("user:adminUser:password", " ", "auth:isValid", "");
        assertThat(natsServer.getNatsServerConfig().size(), is(22));
        assertThat(natsServer.getNatsServerConfig().get(AUTH), is(equalTo("isValid")));
    }

    @Test
    @DisplayName("Duplicate starts will be ignored")
    public void natsServer_duplicateStart_shouldNotRunIntroExceptionOrInterrupt() throws IOException {
        NatsServer natsServer = new NatsServer(4231).source(natsSource);
        natsServer.start();
        natsServer.start();
        natsServer.stop();
    }

    @Test
    @DisplayName("Unknown config [FAIL]")
    public void natsServer_withWrongConfig_shouldNotStartAndThrowException() throws IOException {
        assertThrows(
                IllegalArgumentException.class,
                () -> new NatsServer("unknown:config", "port:4232").source(natsSource),
                "No enum constant"
        );
//        natsServer.start();
    }

    @Test
    @DisplayName("Duplicate instances [FAIL]")
    public void natsServer_asTwoInstances_shouldThrowBindException() {
        NatsServer natsServer_one = new NatsServer(4233).source(natsSource);
        NatsServer natsServer_two = new NatsServer(4233).source(natsSource);
        Exception exception = null;
        try {
            natsServer_one.start();
            natsServer_two.start();
        } catch (Exception e) {
            exception = e;
        } finally {
            natsServer_one.stop();
            natsServer_two.stop();
        }
        assertThat(exception.getClass().getSimpleName(), is(equalTo(BindException.class.getSimpleName())));
    }

    @Test
    @DisplayName("Stop without start will be ignored")
    public void natsServer_stopWithoutStart_shouldNotRunIntroExceptionOrInterrupt() {
        NatsServer natsServer = new NatsServer(4241).source(natsSource);
        natsServer.stop();
    }

//    @Test
//    public void natsServer_withoutPort_shouldThrowMissingFormatArgumentException() {
//        NatsServer natsServer = new NatsServer(4242).source(natsSource);
//        natsServer.stop();
//    }

    @Test
    @DisplayName("Config port with NULL [FAIL]")
    public void natsServer_withNullablePortValue_shouldThrowMissingFormatArgumentException() {
        NatsServer natsServer = new NatsServer(4243).source(natsSource);
        natsServer.getNatsServerConfig().put(PORT, null);
        assertThrows(
                MissingFormatArgumentException.class,
                natsServer::port,
                "Could not initialise port"
        );
    }

    @Test
    @DisplayName("Configure with NULL value should be ignored")
    public void natsServer_withNullableConfigValue_shouldNotRunIntroExceptionOrInterrupt() throws IOException {
        NatsServer natsServer = new NatsServer(4236).source(natsSource);
        natsServer.getNatsServerConfig().put(MAX_AGE, null);
        natsServer.start();
        natsServer.stop();
    }

    @Test
    @DisplayName("Configure with NULL invalid value [FAIL]")
    public void natsServer_withInvalidConfigValue_shouldNotRunIntroExceptionOrInterrupt() throws IOException {
        NatsServer natsServer = new NatsServer(MAX_AGE + ":invalidValue", PORT + ":4237").source(natsSource);
        assertThrows(
                PortUnreachableException.class,
                natsServer::start,
                "NatsServer failed to start"
        );
        natsServer.stop();
    }

    @Test
    @DisplayName("Validate Windows path")
    public void natsServerOnWindows_shouldAddExeToPath() {
        NatsServer natsServer = new NatsServer(4244).source(natsSource);
        String windowsNatsServerPath = natsServer.getNatsServerPath(WINDOWS).toString();
        String expectedExe = NatsServer.BEAN_NAME.toLowerCase() + ".exe";
        assertThat(windowsNatsServerPath, containsString(expectedExe));
    }

    @Test
    @DisplayName("Config without url [FAIL]")
    public void natsServerWithoutSourceUrl_shouldThrowException() throws IOException {
        NatsServer natsServer = new NatsServer(4239).source(natsSource);
        natsServer.getNatsServerPath(SystemUtil.getOsType()).toFile().delete();
        natsServer.source(null);
        assertThrows(
                RuntimeException.class,
                natsServer::start,
                "Could not initialise port"
        );
    }
}
