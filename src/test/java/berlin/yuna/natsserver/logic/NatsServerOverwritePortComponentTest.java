package berlin.yuna.natsserver.logic;

import berlin.yuna.natsserver.annotation.EnableNatsServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.Socket;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableNatsServer(port = 4247, natsServerConfig = "port:4246")
public class NatsServerOverwritePortComponentTest {

    @Autowired
    private NatsServer natsServer;

    @Test
    public void natsServer_customPorts_shouldOverwritePortMap() throws IOException {
        new Socket("localhost", 4247).close();
        assertThat(natsServer, is(notNullValue()));
        assertThat(natsServer.port(), is(4247));
        natsServer.stop();
    }
}
