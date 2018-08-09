package berlin.yuna.natsserver.logic;

import berlin.yuna.natsserver.config.NatsServerConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingFormatArgumentException;

import static berlin.yuna.natsserver.config.NatsServerConfig.AUTH;
import static berlin.yuna.natsserver.config.NatsServerConfig.MAX_AGE;
import static berlin.yuna.natsserver.config.NatsServerConfig.PASS;
import static berlin.yuna.natsserver.config.NatsServerConfig.PORT;
import static berlin.yuna.natsserver.config.NatsServerConfig.USER;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.WINDOWS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class NatsServerWithoutAnnotationTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    public void natsServer_withoutAnnotation_shouldNotBeStarted() throws Exception {
        expectedException.expect(ConnectException.class);
        new Socket("localhost", 4222).close();
    }

    @Test
    public void natsServer_withoutConfig_shouldStartWithDefaultValues() throws Exception {
        NatsServer natsServer = new NatsServer(4238);
        natsServer.start();
        natsServer.stop();
    }

    @Test
    public void natsServer_configureConfig_shouldNotOverwriteOldConfig() {
        NatsServer natsServer = new NatsServer();
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
    public void natsServer_invalidConfig_shouldNotRunIntroException() {
        NatsServer natsServer = new NatsServer();
        natsServer.setNatsServerConfig("user:adminUser:password", " ", "auth:isValid", "");
        assertThat(natsServer.getNatsServerConfig().size(), is(22));
        assertThat(natsServer.getNatsServerConfig().get(AUTH), is(equalTo("isValid")));
    }

    @Test
    public void natsServer_duplicateStart_shouldNotRunIntroExceptionOrInterrupt() throws IOException {
        NatsServer natsServer = new NatsServer(4231);
        natsServer.start();
        natsServer.start();
        natsServer.stop();
    }

    @Test
    public void natsServer_withWrongConfig_shouldNotStartAndThrowException() throws IOException {
        expectedException.expect(IllegalArgumentException.class);
        NatsServer natsServer = new NatsServer("unknown:config", "port:4232");
        natsServer.start();
    }

    @Test
    public void natsServer_asTwoInstances_shouldThrowBindException() {
        NatsServer natsServer_one = new NatsServer(4233);
        NatsServer natsServer_two = new NatsServer(4233);
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
    public void natsServer_stopWithoutStart_shouldNotRunIntroExceptionOrInterrupt() {
        NatsServer natsServer = new NatsServer();
        natsServer.stop();
    }

    @Test
    public void natsServer_withoutPort_shouldThrowMissingFormatArgumentException() {
        NatsServer natsServer = new NatsServer();
        natsServer.stop();
    }

    @Test
    public void natsServer_withNullablePortValue_shouldThrowMissingFormatArgumentException() {
        expectedException.expect(MissingFormatArgumentException.class);
        NatsServer natsServer = new NatsServer();
        natsServer.getNatsServerConfig().put(PORT, null);
        natsServer.getPort();
    }

    @Test
    public void natsServer_withNullableConfigValue_shouldNotRunIntroExceptionOrInterrupt() throws IOException {
        NatsServer natsServer = new NatsServer(4236);
        natsServer.getNatsServerConfig().put(MAX_AGE, null);
        natsServer.start();
        natsServer.stop();
    }

    @Test
    public void natsServer_withInvalidConfigValue_shouldNotRunIntroExceptionOrInterrupt() throws IOException {
        expectedException.expect(PortUnreachableException.class);
        NatsServer natsServer = new NatsServer(MAX_AGE + ":invalidValue", PORT + ":4237");
        natsServer.start();
        natsServer.stop();
    }

    @Test
    public void natsServerOnWindows_shouldAddExeToPath() {
        String windowsNatsServerPath = new NatsServer().getNatsServerPath(WINDOWS).toString();
        String expectedExe = NatsServer.BEAN_NAME.toLowerCase() + ".exe";
        assertThat(windowsNatsServerPath, containsString(expectedExe));
    }
}
