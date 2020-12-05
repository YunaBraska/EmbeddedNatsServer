package berlin.yuna.natsserver.logic;

import berlin.yuna.natsserver.annotation.EnableNatsServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.Socket;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest
@EnableNatsServer(port = 4247, natsServerConfig = "port:4246")
@DisplayName("NatsServer overwrite AutoConfig port test")
public class NatsServerOverwritePortComponentTest {

    @Autowired
    private NatsServer natsServer;

    @Test
    @DisplayName("Custom port overwrites map")
    public void natsServer_customPorts_shouldOverwritePortMap() throws IOException {
        new Socket("localhost", 4247).close();
        assertThat(natsServer, is(notNullValue()));
        assertThat(natsServer.port(), is(4247));
        natsServer.stop();
    }
}
