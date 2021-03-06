package com.nearinfinity.examples.zookeeper.lock;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nearinfinity.examples.zookeeper.util.ConnectionHelper;
import com.nearinfinity.examples.zookeeper.util.EmbeddedZooKeeperServer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BlockingWriteLockTest {

    private static EmbeddedZooKeeperServer embeddedServer;
    private ZooKeeper zooKeeper;
    private String testLockPath;
    private BlockingWriteLock writeLock;

    private static final int ZK_PORT = 53181;
    private static final String ZK_CONNECTION_STRING = "localhost:" + ZK_PORT;

    @BeforeClass
    public static void beforeAll() throws IOException, InterruptedException {
        embeddedServer = new EmbeddedZooKeeperServer(ZK_PORT);
        embeddedServer.start();
    }

    @AfterClass
    public static void afterAll() {
        embeddedServer.shutdown();
    }

    @Before
    public void setUp() throws IOException, InterruptedException {
        zooKeeper = new ConnectionHelper().connect(ZK_CONNECTION_STRING);
        testLockPath = "/test-writeLock-" + System.currentTimeMillis();
        writeLock = new BlockingWriteLock("Test Lock", zooKeeper, testLockPath);
    }

    @After
    public void tearDown() throws InterruptedException, KeeperException {
        List<String> children = zooKeeper.getChildren(testLockPath, false);
        for (String child : children) {
            zooKeeper.delete(testLockPath + "/" + child, -1);
        }
        zooKeeper.delete(testLockPath, -1);
    }

    @Test
    public void testLock() throws InterruptedException, KeeperException {
        writeLock.lock();
        assertNumberOfChildren(zooKeeper, testLockPath, 1);
    }

    @Test
    public void testUnlock() throws InterruptedException, KeeperException {
        writeLock.lock();
        writeLock.unlock();
        assertNumberOfChildren(zooKeeper, testLockPath, 0);
    }

    private void assertNumberOfChildren(ZooKeeper zk, String path, int expectedNumber)
            throws InterruptedException, KeeperException {
        List<String> children = zk.getChildren(path, false);
        assertThat(children.size(), is(expectedNumber));
    }
}
