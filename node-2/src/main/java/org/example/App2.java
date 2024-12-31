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
public class App2
{
    public static void main( String[] args ) throws InterruptedException {
        ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(), "ClusterSystem");
        try {
            AkkaManagement.get(system).start();
            ClusterBootstrap.get(system).start();

            ActorRef<Object> actorRef = system.systemActorOf(ActorNode2.create(), "ActorNode2", Props.empty());
        } catch (Exception e) {
            system.log().error("Terminating due to initialization failure.", e);
            system.terminate();
        }
    }
}
