# Akka Actor Receptionist Guide

## 🌐 English

### Introduction
This guide summarizes what I learned about using Akka’s Receptionist for actor registration and discovery in a cluster. The Receptionist allows actors to be dynamically discovered across the cluster using shared service keys.

---

### Key Concepts

#### Actor Registration in the Receptionist
When an actor is registered with the Receptionist, it becomes available across the entire cluster if Akka clustering is enabled. To achieve this, service keys should be stored in a **common library** to allow access from other applications or nodes in the cluster.

This way, all nodes can access the service keys and discover the desired actor:

```java
public static Behavior<org.example.Command> create(){
    return Behaviors.setup(
        context -> {
            context.getLog().info("Registering the receptionist");
            context.getSystem()
                .receptionist()
                .tell(Receptionist.register(SharedServiceKeys.ACTOR_NODE1_SERVICE_KEY, context.getSelf()));
            return new ActorNode1(context).createReceive();
        }
    );
}
```

- **ActorNode1**: The actor to be discovered by another actor.
- **SharedServiceKeys**: The shared library containing the service keys.

#### Subscribing to Discover an Actor
To discover an actor, subscribe to its service key using the `Receptionist` from another node. Here is how another actor subscribes to discover `ActorNode1`:

```java
public static Behavior<Object> create() {
    return Behaviors.setup(context -> {

        ActorNode2 actorNode2 = new ActorNode2(context);

        context.getSystem().receptionist().tell(
            Receptionist.subscribe(SharedServiceKeys.ACTOR_NODE1_SERVICE_KEY, actorNode2.listingAdapter)
        );

        return actorNode2;
    });
}
```

- **ActorNode2** subscribes to `ActorNode1`'s service key.
- When a change occurs (e.g., a new actor registers), `ActorNode2` will receive a `Receptionist.Listing` message.

#### Handling Listing Messages
The actor must handle the `Receptionist.Listing` message to process the discovered actors:

```java
@Override
public Receive<Object> createReceive() {
    return newReceiveBuilder()
        .onMessage(Receptionist.Listing.class, this::onListing)
        .onMessage(Command.class, this::onCommand)       
        .build();
}
```

The `listingAdapter` is a message adapter that allows the actor to receive `Receptionist.Listing` messages while remaining typed.

---

### Example: Discovering Actors

```java
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
```

Once the actor is discovered, its reference is stored in the actor's state (e.g., `actorNode1`).

#### Sending Messages to the Discovered Actor
When a command message is received, `ActorNode2` forwards it to `ActorNode1`:

```java
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
```

The discovered actor processes the message and sends a reply as needed.

---

## 🇹🇷 Türkçe

### Giriş
Bu rehber, Akka Receptionist kullanımıyla ilgili öğrendiklerimi özetler. Receptionist, aktörlerin dinamik olarak cluster çapında keşedilmesine olanak tanır.

---
Önceki sürümlerinde tip koruması olmayan Akka, yeni sürümlerinde tip koruması ile hataları minimum düzeye indirecek bir yapı kurmuştur.

### Temel Kavramlar

#### Aktörün Receptionist’e Kaydedilmesi
Bir aktör Receptionist'e kaydedildiğinde, Akka cluster çalışıyorsa, bu referanslar tüm clusterda ulaşılabilir olur. Service keyler ile register işlemi yapıldığında, bu service keyleri diğer nodelardaki aktörlerin de bilmesi gereklidir. Bunun için, service key’lerin **ortak bir kütüphanede** saklanması gerekir, böylece cluster’daki diğer uygulamalar da bu keylere erişebilir.

Şu kod, bir aktörün Receptionist'e kaydolmasını sağlar:

```java
public static Behavior<org.example.Command> create(){
    return Behaviors.setup(
        context -> {
            context.getLog().info("Registering the receptionist");
            context.getSystem()
                .receptionist()
                .tell(Receptionist.register(SharedServiceKeys.ACTOR_NODE1_SERVICE_KEY, context.getSelf()));
            return new ActorNode1(context).createReceive();
        }
    );
}
```

- **ActorNode1**: Diğer aktörlerin keşedeceği aktör.
- **SharedServiceKeys**: Service key’leri içeren ortak kütüphane.

#### Aktörün Keşfedilmesi
Bir aktörü bulmak için, ilgili service key’e `Receptionist` üzerinden subscribe olmak gerekir. Şu kod, `ActorNode1`ı keşeder:


Normalde aktörün alacağı mesajlar `Object` tipinde olduğu için sadece o tipte mesaj alabilir.
Service keyi ile aktörü keşfedebilmek için `Receptionist.Listing` tipinde mesaj almamız gerektiğinden, aşağıdaki şekilde bir referans oluşturup onunla subscribe olacağız. 
```java
 private final ActorRef<Receptionist.Listing> listingAdapter;
```
kullanarak aktörün mesaj tipini değiştirmeye gerek kalmadan aktörün alabileceği mesaj 
tiplerini genişletebiliriz. Bu da Akka Framework'ün sağladığı esnekliklerden biridir. 

```java 
public static Behavior<Object> create() {
    return Behaviors.setup(context -> {

        ActorNode2 actorNode2 = new ActorNode2(context);

        context.getSystem().receptionist().tell(
            Receptionist.subscribe(SharedServiceKeys.ACTOR_NODE1_SERVICE_KEY, actorNode2.listingAdapter)
        );

        return actorNode2;
    });
}
```

#### Listing Mesajlarını Dinlemek
`Receptionist.Listing` mesajları dinlenerek keşedilen aktörler işlenebilir:

```java
@Override
public Receive<Object> createReceive() {
    return newReceiveBuilder()
        .onMessage(Receptionist.Listing.class, this::onListing)
        .onMessage(Command.class, this::onCommand)       
        .build();
}
```

`listingAdapter`, aktörün farklı türde mesajlar alabilmesini sağlayan bir tasarım desenidir.

---

### Örnek: Aktörlerin Keşfedilmesi

```java
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
```

Keşedilen aktörün referansı, aktörün state'inde (property) saklanır.

#### Keşedilen Aktöre Mesaj Göndermek
Komut mesajı alındığında `ActorNode2`, mesajı `ActorNode1`'e iletir:

```java
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
```

Keşedilen aktör, mesajı işler ve gerekirse bir cevap döner.

