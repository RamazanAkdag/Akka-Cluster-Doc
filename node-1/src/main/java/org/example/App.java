package org.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import akka.actor.typed.javadsl.Behaviors;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) {
        ActorSystem<Void> actorSystem = ActorSystem.create(Behaviors.empty(),"ClusterSystem");

        ActorRef<Command> actorNode1 = actorSystem.systemActorOf(ActorNode1.create(),"ActorNode1",Props.empty());
    }
}
