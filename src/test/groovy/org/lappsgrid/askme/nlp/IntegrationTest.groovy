package org.lappsgrid.askme.nlp

import org.junit.*
import org.lappsgrid.askme.nlp.Main
//import org.lappsgrid.eager.mining.core.json.Serializer
import org.lappsgrid.rabbitmq.Message
import org.lappsgrid.rabbitmq.topic.MailBox
import org.lappsgrid.rabbitmq.topic.PostOffice
import org.lappsgrid.serialization.DataContainer
import org.lappsgrid.serialization.Serializer

import static org.lappsgrid.discriminator.Discriminators.*

import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.lif.Container

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 *
 */
@Ignore
class IntegrationTest {
    public static final String TEXT = "Karen flew to New York. Nancy flew to Bloomington."
//    static final Configuration c = new Configuration()
    static final String SELF = 'askme-test-return'

    PostOffice po

    @Before
    void setup() {
        po = new PostOffice(Main.POSTOFFICE, Main.HOST)
    }

    @After
    void teardown() {
        po.close()
        po = null
    }

    @Test
    void test() {
        String json
        CountDownLatch latch = new CountDownLatch(1)
        MailBox box = new MailBox(Main.POSTOFFICE, SELF, Main.HOST) {
            @Override
            void recv(String message) {
                json = message
                latch.countDown()
            }
        }

        Container container = new Container()
        container.text = TEXT
        Data data = new Data(Uri.LIF, container)
        Message message = new Message()
                .body(data.asJson())
                .command(Main.NER)
                .route(Main.MAILBOX, SELF)

        assert message.route.size() == 2
        po.send(message)

        if (!latch.await(60, TimeUnit.SECONDS)) {
            println 'No response from nlp service'
        }

        assert json != null
        Message msg = Serializer.parse(json, Message)
        data = Serializer.parse(msg.body, DataContainer)
        assert Uri.LIF == data.discriminator
        assert 3 == data.payload.views.size()

        println Serializer.toPrettyJson(data.payload)

        box.close()
        po.close()
    }

    @Test
    void shutdown() {
        Message message = new Message()
                .command("EXIT")
                .route(Main.MAILBOX)

        assert message.route.size() == 1
        po.send(message)
        po.close()
    }
}
