package com.undefinedlabs.scope.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.Creator;
import akka.testkit.javadsl.TestKit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class SimpleAkkaActorTest {

    public static class PrinterActor extends AbstractActor {

        private static final Logger LOGGER = LoggerFactory.getLogger(PrinterActor.class);

        public static Props props(final ActorRef nextActor) {
            return Props.create(PrinterActor.class, (Creator<PrinterActor>) () -> new PrinterActor(nextActor));
        }

        private final ActorRef next;

        public PrinterActor(final ActorRef next) {
            this.next = next;
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(String.class, (message) -> {
                        System.out.println(message);
                        Thread.sleep(new Random(System.currentTimeMillis()).nextInt(500));

                        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                        final Request.Builder reqBuilder = new Request.Builder().url("http://www.google.com");
                        LOGGER.info("Executing HttpClient request to http://www.google.com");
                        final Response execute = okHttpClient.newCall(reqBuilder.build()).execute();

                        Thread.sleep(new Random(System.currentTimeMillis()).nextInt(500));
                        if(next != null) {
                            next.tell(message, getSelf());
                        }
                    })
                    .build();
        }
    }

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("sampleActorSystem");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void simpleAkkaActorTestPipeline() throws InterruptedException {
        final Props lastActorProps = PrinterActor.props(ActorRef.noSender());
        final ActorRef lastActor = system.actorOf(lastActorProps, "Actor_C");

        final Props middleActorProps = PrinterActor.props(lastActor);
        final ActorRef middleActor = system.actorOf(middleActorProps, "Actor_B");

        final Props initialActorProps = PrinterActor.props(middleActor);
        final ActorRef initialActor = system.actorOf(initialActorProps, "Actor_A");

        initialActor.tell("Hello World", ActorRef.noSender());
        initialActor.tell("Another Hello World", ActorRef.noSender());
        Thread.sleep(3000);
    }
}
