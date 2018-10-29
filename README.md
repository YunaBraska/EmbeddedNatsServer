# Nats Streaming Server Embedded

[![License][License-Image]][License-Url]
[![Build][Build-Status-Image]][Build-Status-Url] 
[![Coverage][Coverage-image]][Coverage-Url] 
[![Maintainable][Maintainable-image]][Maintainable-Url] 
[![Central][Central-image]][Central-Url] 
[![Javadoc][javadoc-image]][javadoc-Url]
[![Gitter][Gitter-image]][Gitter-Url] 

### Description
Embedded [Nats streaming server](https://github.com/nats-io/nats-streaming-server) for testing which contains the original [Nats streaming server](https://github.com/nats-io/nats-streaming-server) 

### Usage
* One annotation to setup the powerful [Nats streaming server](https://github.com/nats-io/nats-streaming-server)
```java
@SpringBootTest
@RunWith(SpringRunner.class)
@EnableNatsServer(port = 4222, natsServerConfig = {"user:admin", "pass:admin"})
public class SomeTest {
    [...]
}
```
* See [NatsServerConfig](https://github.com/YunaBraska/nats-streaming-server-embedded/blob/master/src/main/java/berlin/yuna/natsserver/config/NatsServerConfig.java) class for available properties
* @EnableNatsServer is also reading spring config
* @EnableNatsServer parameters are overwriting the spring properties
```yaml
nats:
  server:
    hb_fail_count: 3
```

```properties
nats.server.hb_fail_count=3
```

* See [NatsServerSourceConfig](https://github.com/YunaBraska/nats-streaming-server-embedded/blob/master/src/main/java/berlin/yuna/natsserver/config/NatsServerSourceConfig.java) class for optional available nats version configuration
```yaml
nats:
  source:
    mac: "https://nats-mac.zip"
    linux: "https://nats-linux.zip"
    solaris: "https://nats-solaris.zip"
    windows: "https://nats-windows.zip"
    default: "file://${user.dir}/nats-foo-bar.zip"
```

### TODO
* No autocompletion for NatsServerSourceConfig - MetaConfigurator need update for multiple groups

![nats-streaming-server-embedded](src/test/resources/banner.png "nats-streaming-server-embedded")

[License-Url]: https://www.apache.org/licenses/LICENSE-2.0
[License-Image]: https://img.shields.io/badge/License-Apache2-blue.svg
[github-release]: https://github.com/YunaBraska/nats-streaming-server-embedded
[Build-Status-Url]: https://travis-ci.org/YunaBraska/nats-streaming-server-embedded
[Build-Status-Image]: https://travis-ci.org/YunaBraska/nats-streaming-server-embedded.svg?branch=master
[Coverage-Url]: https://codecov.io/gh/YunaBraska/nats-streaming-server-embedded?branch=master
[Coverage-image]: https://codecov.io/gh/YunaBraska/nats-streaming-server-embedded/branch/master/graphs/badge.svg
[Version-url]: https://github.com/YunaBraska/nats-streaming-server-embedded
[Version-image]: https://badge.fury.io/gh/YunaBraska%2Fnats-streaming-server-embedded.svg
[Central-url]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22nats-streaming-server-embedded%22
[Central-image]: https://maven-badges.herokuapp.com/maven-central/berlin.yuna/nats-streaming-server-embedded/badge.svg
[Maintainable-Url]: https://codeclimate.com/github/YunaBraska/nats-streaming-server-embedded
[Maintainable-image]: https://codeclimate.com/github/YunaBraska/nats-streaming-server-embedded.svg
[Gitter-Url]: https://gitter.im/nats-streaming-server-embedded/Lobby
[Gitter-image]: https://img.shields.io/badge/gitter-join%20chat%20%E2%86%92-brightgreen.svg
[Javadoc-url]: http://javadoc.io/doc/berlin.yuna/nats-streaming-server-embedded
[Javadoc-image]: http://javadoc.io/badge/berlin.yuna/nats-streaming-server-embedded.svg