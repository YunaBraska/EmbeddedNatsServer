package berlin.yuna.natsserver.logic;

import berlin.yuna.clu.logic.Terminal;
import berlin.yuna.natsserver.config.NatsServerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static berlin.yuna.clu.logic.SystemUtil.getOsType;
import static java.util.Arrays.stream;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

@SpringBootTest
@Tag("IntegrationTest")
@DisplayName("NatsServer ConfigTest")
public class NatsServerConfigComponentTest {

    @Value("${nats.source.default}")
    private String natsSource;

    @Test
    @DisplayName("Compare nats with java config")
    public void compareNatsServerConfig() {
        Path natsServerPath = new NatsServer(4248).source(natsSource).getNatsServerPath(getOsType());

        StringBuilder console = new StringBuilder();

        Terminal terminal = new Terminal().timeoutMs(10000).execute(natsServerPath.toString() + " --help");
        console.append(terminal.consoleInfo()).append(terminal.consoleError());

        List<String> consoleConfigKeys = readConfigKeys(console.toString());
        List<String> javaConfigKeys = stream(NatsServerConfig.values()).map(Enum::name).collect(Collectors.toList());

        Set<String> missingConfigInJava = getNotMatchingEntities(consoleConfigKeys, javaConfigKeys);
        assertThat(missingConfigInJava, is(empty()));

        Set<String> missingConfigInConsole = getNotMatchingEntities(javaConfigKeys, consoleConfigKeys);
        assertThat(missingConfigInConsole, is(empty()));
    }

    @Test
    @DisplayName("Compare config key with one dash")
    public void getKey_WithOneDash_ShouldBeSuccessful() {
        assertThat(NatsServerConfig.SECURE.getKey(), is(equalTo("-secure ")));
    }

    @Test
    @DisplayName("Compare config key with equal sign")
    public void getKey_WithBoolean_ShouldAddOneEqualSign() {
        assertThat(NatsServerConfig.CLUSTERED.getKey(), is(equalTo("--clustered=")));
    }

    private Set<String> getNotMatchingEntities(List<String> list1, List<String> list2) {
        Set<String> noMatches = new HashSet<>();
        for (String entity : list1) {
            if (!list2.contains(entity)) {
                noMatches.add(entity);
            }
        }
        return noMatches;
    }

    private List<String> readConfigKeys(String console) {
        List<String> allMatches = new ArrayList<>();
        Matcher m = Pattern.compile("-([a-z_]*)(\\s+|=)<[^,]").matcher(console);
        while (m.find()) {
            allMatches.add(m.group(1).toUpperCase());
        }
        return allMatches;
    }
}