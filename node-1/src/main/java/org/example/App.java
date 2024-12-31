package org.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import akka.actor.typed.javadsl.Behaviors;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.management.javadsl.AkkaManagement;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) {
        ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(), "ClusterSystem");
        try {

            AkkaManagement.get(system).start();


            ClusterBootstrap.get(system).start();


            ActorRef<Command> actorNode1 = system.systemActorOf(ActorNode1.create(), "ActorNode1", Props.empty());

        } catch (Exception e) {
            system.log().error("Terminating due to initialization failure.", e);
            system.terminate();
        }
    }
}
