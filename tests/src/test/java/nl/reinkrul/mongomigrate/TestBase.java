package nl.reinkrul.mongomigrate;

import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.net.ServerSocket;

import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public class TestBase {

    protected final String mongoHost = "localhost";
    protected int mongoPort;

    private MongodProcess mongoProcess;

    @Before
    public void setUp() throws Exception {
        final MongodStarter starter = MongodStarter.getDefaultInstance();
        mongoPort = findOpenPort();
        final IMongodConfig config = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(mongoHost, mongoPort, Network.localhostIsIPv6()))
                .build();

        mongoProcess = starter.prepare(config).start();
    }

    @After
    public void tearDown() {
        mongoProcess.stop();
    }

    private int findOpenPort() throws IOException {
        try (final ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
