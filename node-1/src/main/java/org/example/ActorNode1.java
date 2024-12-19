package org.example;


import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import org.example.Command;

public class ActorNode1 extends AbstractBehavior<Command> {



    int count = 0;


    public static Behavior<org.example.Command> create(){
        return Behaviors.setup(
                context ->{
                    context.getLog().info("registering the receptionist");
                    context.getSystem()
                            .receptionist()
                            .tell(Receptionist.register(SharedServiceKeys.ACTOR_NODE1_SERVICE_KEY, context.getSelf()));
                    return new ActorNode1(context).createReceive();
                }
        );
    }

    public ActorNode1(ActorContext<Command> context) {
        super(context);

    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(org.example.Command.class, this::onMessage)
                .build();
    }

    private Behavior<Command> onMessage(Command command) {
        getContext().getLog().info("Message Received by me : " + command.message);
        command.replyTo.tell(new Command("Your message is received by me, How are you",getContext().getSelf()));

        if(this.count > 1){
            return Behaviors.empty();
        }
        this.count++;
        return this;
    }
}
