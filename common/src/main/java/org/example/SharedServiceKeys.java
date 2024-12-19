package org.example;

import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;

public class SharedServiceKeys {


    public static final ServiceKey<Command> ACTOR_NODE1_SERVICE_KEY =
            ServiceKey.create(Command.class, "ActorNode1Service");

    public static final ServiceKey<Receptionist.Listing> ACTOR_NODE2_SERVICE_KEY =
            ServiceKey.create(Receptionist.Listing.class, "ActorNode2Service");
}