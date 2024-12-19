package org.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.Signal;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;

import java.util.Set;

public class ActorNode2 extends AbstractBehavior<Object> {

    private ActorRef<Command> actorNode1; 


    private final ActorRef<Receptionist.Listing> listingAdapter;
    private final ActorRef<Command> commandAdapter;


    public ActorNode2(ActorContext<Object> context) {
        super(context);

        this.listingAdapter = context.messageAdapter(Receptionist.Listing.class, listing -> listing);
        this.commandAdapter = context.messageAdapter(Command.class, command -> command);
    }

    public static Behavior<Object> create() {
        return Behaviors.setup(context -> {

            ActorNode2 actorNode2 = new ActorNode2(context);

            context.getSystem().receptionist().tell(
                    Receptionist.subscribe(SharedServiceKeys.ACTOR_NODE1_SERVICE_KEY, actorNode2.listingAdapter)
            );

            return actorNode2;
        });
    }

    @Override
    public Receive<Object> createReceive() {
        return newReceiveBuilder()
                .onMessage(Receptionist.Listing.class, this::onListing) 
                .onMessage(Command.class, this::onCommand)       
                .build();
    }

    private Behavior<Object> onListing(Receptionist.Listing listing) {
        Set<ActorRef<Command>> serviceInstances = listing.getServiceInstances(SharedServiceKeys.ACTOR_NODE1_SERVICE_KEY);
        if (!serviceInstances.isEmpty()) {
            actorNode1 = serviceInstances.iterator().next();
            getContext().getLog().info("Discovered ActorNode1: {}", actorNode1);
        } else {
            getContext().getLog().warn("ActorNode1 not found in the cluster.");
        }
        return this;
    }

    private Behavior<Object> onCommand(Command command) {
        getContext().getLog().info("ActorNode2 received command: {}", command.message);

        command.replyTo = commandAdapter;

        if (actorNode1 != null) {
            actorNode1.tell(command);
            getContext().getLog().info("Forwarded command to ActorNode1: {}", command.message);
        } else {
            getContext().getLog().warn("ActorNode1 not yet discovered, dropping command: {}", command.message);
        }

        return this;
    }



}
