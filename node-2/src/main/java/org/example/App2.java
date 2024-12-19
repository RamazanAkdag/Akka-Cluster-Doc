package org.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import akka.actor.typed.javadsl.Behaviors;

/**
 * Hello world!
 *
 */
public class App2
{
    public static void main( String[] args ) throws InterruptedException {
        ActorSystem<Void> actorSystem = ActorSystem.create(Behaviors.empty(),"ClusterSystem");



        ActorRef<Object> actorRef = actorSystem.systemActorOf(ActorNode2.create(),"ActorNode2", Props.empty());




    }
}
