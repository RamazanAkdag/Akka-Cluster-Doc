package org.example;

import akka.actor.typed.ActorRef;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Command{
    public String message;
    public ActorRef<Command> replyTo;

    @JsonCreator
    public Command(@JsonProperty("message") String message,
                   @JsonProperty("replyTo") ActorRef<Command> replyTo) {
        this.message = message;
        this.replyTo = replyTo;
    }
}
