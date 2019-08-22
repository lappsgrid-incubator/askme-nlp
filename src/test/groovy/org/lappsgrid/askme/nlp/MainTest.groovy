package org.lappsgrid.askme.nlp

import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.lappsgrid.askme.core.Configuration
import org.lappsgrid.rabbitmq.Message
import org.lappsgrid.rabbitmq.RabbitMQ
import org.lappsgrid.rabbitmq.topic.MessageBox
import org.lappsgrid.rabbitmq.topic.PostOffice

/**
 *
 */
class MainTest {

    static Configuration config
    Main app
    final String MAILBOX = "main-test-mailbox"

    @BeforeClass
    static void init() {
        config = new Configuration()
    }

    @Before
    void setup() {
        app = new Main()
        app.start()
    }

    @After
    void teardown() {
        app.stop()
        app = null
    }

    @Test
    void ping() {
        Object lock = new Object()
        boolean passed = false
        MessageBox box = new MessageBox(config.EXCHANGE, MAILBOX, config.HOST) {

            @Override
            void recv(Message message) {
                passed = message.command == "PONG"
                synchronized (lock) {
                    lock.notifyAll()
                }
            }
        }

        PostOffice po = new PostOffice(config.EXCHANGE, config.HOST)
        Message message = new Message()
                .command("PING")
                .route(Main.MAILBOX)
                .route(MAILBOX)

        po.send(message)
        synchronized (lock) {
            lock.wait(1000)
        }
        po.close()
        box.close()
        assert passed
    }
}
