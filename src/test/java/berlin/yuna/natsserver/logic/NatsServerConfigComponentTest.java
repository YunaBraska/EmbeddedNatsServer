package berlin.yuna.natsserver.logic;

import berlin.yuna.clu.logic.Terminal;
import berlin.yuna.natsserver.config.NatsServerConfig;
import berlin.yuna.natsserver.config.NatsServerSourceConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static berlin.yuna.clu.logic.SystemUtil.getOsType;
import static berlin.yuna.clu.logic.SystemUtil.readFile;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

@SpringBootTest
@Tag("IntegrationTest")
@DisplayName("NatsServer ConfigTest")
class NatsServerConfigComponentTest {

    @Test
    @DisplayName("Compare nats with java config")
    void compareNatsServerConfig() throws IOException {
        updateNatsVersion();
        Files.deleteIfExists(new NatsServer().getNatsServerPath(getOsType()));
        Path natsServerPath = new NatsServer(4248).getNatsServerPath(getOsType());

        StringBuilder console = new StringBuilder();

        Terminal terminal = new Terminal().timeoutMs(10000).execute(natsServerPath.toString() + " --help");
        console.append(terminal.consoleInfo()).append(terminal.consoleError());

        List<String> consoleConfigKeys = readConfigKeys(console.toString());
        List<String> javaConfigKeys = stream(NatsServerConfig.values()).map(Enum::name).collect(Collectors.toList());

        Set<String> missingConfigInJava = getNotMatchingEntities(consoleConfigKeys, javaConfigKeys);

        Set<String> missingConfigInConsole = getNotMatchingEntities(javaConfigKeys, consoleConfigKeys);
        assertThat("Missing config in java \n" + console.toString(), missingConfigInJava, is(empty()));
        assertThat("Config was removed by nats", missingConfigInConsole, is(empty()));
    }

    @Test
    @DisplayName("Compare config key with one dash")
    void getKey_WithOneDash_ShouldBeSuccessful() {
        assertThat(NatsServerConfig.SECURE.getKey(), is(equalTo("-secure ")));
    }

    @Test
    @DisplayName("Compare config key with equal sign")
    void getKey_WithBoolean_ShouldAddOneEqualSign() {
        assertThat(NatsServerConfig.CLUSTERED.getKey(), is(equalTo("--clustered=")));
    }

    private void updateNatsVersion() throws IOException {
        final Path configPath = Files.walk(FileSystems.getDefault().getPath(System.getProperty("user.dir")), 99).filter(path -> path.getFileName().toString().equalsIgnoreCase(NatsServerSourceConfig.class.getSimpleName() + ".java")).findFirst().orElse(null);
        URL url = new URL("https://api.github.com/repos/nats-io/nats-streaming-server/releases/latest");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        final String json = read(con.getInputStream());
        final Matcher matcher = Pattern.compile("\"tag_name\":\"(?<version>.*?)\"").matcher(json);
        if (matcher.find()) {
            String content = readFile(requireNonNull(configPath));
            content = content.replaceAll("DEFAULT_VERSION.*=.*", "DEFAULT_VERSION = \"" + matcher.group("version") + "\";");
            Files.write(configPath, content.getBytes());
        } else {
            throw new  IllegalStateException("Could not update nats server version");
        }
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

    public static String read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input, UTF_8))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }
}