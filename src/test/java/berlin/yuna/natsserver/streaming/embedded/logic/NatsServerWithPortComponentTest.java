package berlin.yuna.natsserver.streaming.embedded.logic;

import berlin.yuna.natsserver.streaming.embedded.annotation.EnableNatsStreamingServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.Socket;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest
@Tag("IntegrationTest")
@EnableNatsStreamingServer(port = 4234)
@DisplayName("NatsServer AutoConfig port test")
class NatsServerWithPortComponentTest {

    @Autowired
    private NatsStreamingServer natsServer;

    @Test
    @DisplayName("Accept custom port")
    void natsServer_withCustomPort_shouldStartWithCustomPort() throws IOException {
        new Socket("localhost", 4234).close();
        assertThat(natsServer, is(notNullValue()));
        assertThat(natsServer.port(), is(4234));
        natsServer.stop();
    }
}
