package com.undefinedlabs.scope.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Serializable;

public class PingPongAkkaActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("pingPongActorSystem");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    public static class Start implements Serializable {
        private final int rebounds;

        public Start(int rebounds) {
            this.rebounds = rebounds;
        }

        public int getRebounds() {
            return rebounds;
        }
    }

    public static class Ping implements Serializable {
        private final int rebounds;

        public Ping(int rebounds) {
            this.rebounds = rebounds;
        }

        public int getRebounds() {
            return rebounds;
        }
    }

    public static class Pong implements Serializable {
        private final int rebounds;

        public Pong(int rebounds) {
            this.rebounds = rebounds;
        }

        public int getRebounds() {
            return rebounds;
        }
    }

    public static class Peer implements Serializable {
        private final ActorRef peer;

        public Peer(ActorRef peer) {
            this.peer = peer;
        }

        public ActorRef getPeer() {
            return peer;
        }
    }

    public static class PingActor extends AbstractActor {

        private ActorRef peer;

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(Peer.class, (message) -> {
                        this.peer = message.getPeer();
                    })
                    .match(Start.class, (message) -> {
                        Thread.sleep(100);
                        this.peer.tell(new Ping(message.getRebounds()), getSelf());
                    })
                    .match(Pong.class, (message) -> {
                        Thread.sleep(200);
                        if(message.getRebounds() != 0) {
                            this.peer.tell(new Ping(message.getRebounds() - 1), getSelf());
                        }
                    })
                    .build();
        }
    }

    public static class PongActor extends AbstractActor {

        private ActorRef peer;

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(Peer.class, (message) -> {
                        this.peer = message.getPeer();
                    })
                    .match(Ping.class, (message) -> {
                        Thread.sleep(200);
                        if(message.getRebounds() != 0) {
                            this.peer.tell(new Pong(message.getRebounds()), getSelf());
                        }
                    })
                    .build();
        }
    }

    @Test
    public void pingPong() throws InterruptedException {
        final ActorRef pingActor = system.actorOf(Props.create(PingActor.class), "PingActor");
        final ActorRef pongActor = system.actorOf(Props.create(PongActor.class), "PongActor");

        pingActor.tell(new Peer(pongActor), ActorRef.noSender());
        pongActor.tell(new Peer(pingActor), ActorRef.noSender());

        pingActor.tell(new Start(5), ActorRef.noSender());

        Thread.sleep(5000);
    }
}
