package io.vertx.stream;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.net.NetSocket;
import io.vertx.streams.Consumer;
import io.vertx.streams.Producer;
import io.vertx.streams.impl.EventBusTransport;
import io.vertx.streams.impl.NetTransport;
import io.vertx.streams.impl.Transport;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class StreamTest extends VertxTestBase {

  @Test
  public void testEventBusStream() throws Exception {
    testStream(vertx, new EventBusTransport(vertx.eventBus()));
    await();
  }

  @Test
  public void testNetStream() throws Exception {
    NetTransport transport = new NetTransport(vertx, 1234, "localhost");
    vertx.createNetServer().connectHandler(transport).listen(1234, onSuccess(v -> {
      try {
        testStream(vertx, transport);
      } catch (Exception e) {
        fail(e);
      }
    }));
    await();
  }

  private void testStream(Vertx vertx, Transport transport) throws Exception {

    Producer<String> producer = Producer.producer(vertx.eventBus(), transport);
    producer.readStreamHandler(sub -> {
      sub.write("foo");
      sub.write("bar");
      sub.write("juu");
      sub.end();
    });
    producer.register("the-address");

    AtomicInteger count = new AtomicInteger();
    Consumer<String> consumer = Consumer.consumer(vertx.eventBus(), "the-address", transport);
    consumer.openReadStream(onSuccess(stream -> {
      assertEquals(0, count.getAndIncrement());
      stream.handler(event -> {
        int val = count.getAndIncrement();
        switch (val) {
          case 1:
            assertEquals("foo", event);
            break;
          case 2:
            assertEquals("bar", event);
            break;
          case 3:
            assertEquals("juu", event);
            break;
          default:
            fail("Unexpected " + val);
        }
      });
      stream.endHandler(v -> {
        if (stream instanceof EventBusTransport.MessageReadStream) {
          MessageConsumer mc = ((EventBusTransport.MessageReadStream) stream).consumer();
          assertFalse(mc.isRegistered());
        }
        assertEquals(4, count.getAndIncrement());
        testComplete();
      });
    }));
  }

  @Test
  public void testEventBusReadStream() throws Exception {
    Vertx vertx = Vertx.vertx();
    testWriteStream(vertx, new EventBusTransport(vertx.eventBus()));
    await();
  }

  @Test
  public void testNetReadStream() throws Exception {
    NetTransport transport = new NetTransport(vertx, 1234, "localhost");
    vertx.createNetServer().connectHandler(transport).listen(1234, onSuccess(v -> {
      try {
        testStream(vertx, transport);
      } catch (Exception e) {
        fail(e);
      }
    }));
    await();
  }

  private void testWriteStream(Vertx vertx, Transport transport) throws Exception {

    AtomicInteger count = new AtomicInteger();
    Producer<String> producer = Producer.producer(vertx.eventBus(), transport);
    producer.writeStreamHandler(stream -> {
      assertEquals(0, count.getAndIncrement());
      stream.handler(event -> {
        int val = count.getAndIncrement();
        switch (val) {
          case 1:
            assertEquals("foo", event);
            break;
          case 2:
            assertEquals("bar", event);
            break;
          case 3:
            assertEquals("juu", event);
            break;
          default:
            fail("Unexpected " + val);
        }
      });
      stream.endHandler(v -> {
        assertEquals(4, count.getAndIncrement());
        testComplete();
      });
    });
    producer.register("the-address");

    Consumer<String> consumer = Consumer.consumer(vertx.eventBus(), "the-address", transport);
    consumer.openWriteStream(onSuccess(stream -> {
      stream.write("foo");
      stream.write("bar");
      stream.write("juu");
      stream.end();
    }));
  }

/*
  @Test
  public void testClose() throws Exception {
    Producer<String> producer = Producer.producer(vertx.eventBus());
    producer.readStreamHandler(sub -> {
      sub.closeHandler(v -> {
        testComplete();
      });
    });
    producer.register("the-address");

    Consumer<String> consumer = Consumer.consumer(vertx.eventBus(), "the-address");
    consumer.openReadStream(onSuccess(stream -> {
      stream.handler(event -> {
        fail();
      });
      stream.close();
    }));
    await();
  }
*/
}
