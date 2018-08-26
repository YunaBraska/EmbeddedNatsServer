package berlin.yuna.natsserver.logic;

import berlin.yuna.natsserver.config.NatsServerConfig;
import berlin.yuna.system.logic.Terminal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static berlin.yuna.system.logic.SystemUtil.getOsType;
import static java.util.Arrays.stream;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NatsServerConfigComponentTest {

    @Test
    public void compareNatsServerConfig() {
        Path natsServerPath = new NatsServer().getNatsServerPath(getOsType());

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
    public void getKey_WithOneMinus_ShouldBeSuccessful() {
        assertThat(NatsServerConfig.SECURE.getKey(), is(equalTo("-secure ")));
    }

    @Test
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