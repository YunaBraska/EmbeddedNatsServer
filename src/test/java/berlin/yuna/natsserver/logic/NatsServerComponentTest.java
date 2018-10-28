package berlin.yuna.natsserver.logic;

import berlin.yuna.natsserver.annotation.EnableNatsServer;
import berlin.yuna.system.logic.SystemUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.Socket;

import static berlin.yuna.system.logic.SystemUtil.getOsType;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableNatsServer
public class NatsServerComponentTest {

    @Autowired
    private NatsServer natsServer;

    @Value("${nats.source.default}")
    private String natsSource;

    @Test
    public void natsServer_shouldDownloadUnzipAndStart() {
        natsServer.getNatsServerPath(SystemUtil.getOsType()).toFile().delete();
        assertThat(natsServer, is(notNullValue()));
        assertThat(natsServer.port(), is(4222));
        natsServer.stop();
    }

    @Test
    public void secondNatsServer_withDoublePointSeparatedProperty_shouldStartSuccessful() {
        assertNatsServerStart(4225, "--port:4225");
    }

    @Test
    public void secondNatsServer_withOutMinusProperty_shouldStartSuccessful() {
        assertNatsServerStart(4226, "port:4226");
    }

    @Test(expected = RuntimeException.class)
    public void secondNatsServer_withInvalidProperty_shouldFailToStart() {
        assertNatsServerStart(4228, "p:4228");
    }

    @Test
    public void toString_shouldPrintPortAndOs() {
        String runningNatsServer = natsServer.toString();
        assertThat(runningNatsServer, containsString(getOsType().toString()));
        assertThat(runningNatsServer, containsString("4222"));
    }

    private void assertNatsServerStart(final int port, final String... natsServerConfig) {
        NatsServer natsServer = new NatsServer(natsServerConfig).source(natsSource);
        try {
            natsServer.start();
            new Socket("localhost", port).close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            natsServer.stop();
        }
    }
}
