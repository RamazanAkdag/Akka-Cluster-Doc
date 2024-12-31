# ENGLISH
---
# Akka Cluster Configuration Explanation

---

## **Actor Configuration**
```hocon
actor {
    provider = cluster

    serializers {
        jackson-json = "akka.serialization.jackson.JacksonJsonSerializer"
    }

    serialization-bindings {
        "org.example.Command" = jackson-json
    }
}
```


- **`provider = cluster`:**
  Indicates that the Akka system is configured to run as a cluster, enabling distributed actor communication.

- **`serializers`:**
  Specifies which serializer class Akka should use. In this case:
  ```xml
  <dependency>
      <groupId>com.typesafe.akka</groupId>
      <artifactId>akka-serialization-jackson_2.13</artifactId>
  </dependency>
  ```
  The serializer is set to use the `JacksonJsonSerializer` class.

- **`serialization-bindings`:**
  Defines which classes should be serialized with which serializer. For example:
    - `org.example.Command` is serialized with `jackson-json`.
    - This enables seamless data transfer between modules using the `Command` class.

---

## **Cluster Bootstrap Configuration**
```hocon
akka.management {
  cluster.bootstrap {
    contact-point-discovery {
      discovery-method = kubernetes-api
      required-contact-point-nr = 3
    }
  }
}
```


- **`akka.management.cluster.bootstrap`:**
  Configures how Akka will manage cluster bootstrapping. Akka nodes will discover each other automatically using the specified method.

- **`discovery-method = kubernetes-api`:**
  Akka uses the Kubernetes API to find other nodes by querying pod information.

- **`required-contact-point-nr = 3`:**
  Specifies that at least 3 nodes must discover each other before the cluster is considered formed. This ensures cluster stability.

---

## **Discovery Configuration**
```hocon
akka.discovery {
  kubernetes-api {
    pod-label-selector = "app=akka-bootstrap-demo"
    pod-namespace = "default"
  }
}
```


- **`akka.discovery.kubernetes-api`:**
  Configures Akka to use the Kubernetes API for node discovery.

- **`pod-label-selector = "app=akka-bootstrap-demo"`:**
  Specifies that pods with the label `app=akka-bootstrap-demo` will be discovered as Akka cluster nodes.

- **`pod-namespace = "default"`:**
  Specifies that Akka will look for pods in the `default` namespace. If your pods are in a different namespace, update this value accordingly.

---

## **New Cluster Enabling**
```hocon
akka.management.cluster.bootstrap.new-cluster-enabled = on
```

- Automatically enables the creation of a new cluster if one does not already exist.
- New nodes can join the cluster automatically without manual configuration.

---

## **Deployment YAML**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: akka-cluster-node-1
  labels:
    app: akka-bootstrap-demo
spec:
  replicas: 2
  selector:
    matchLabels:
      app: akka-bootstrap-demo
  template:
    metadata:
      labels:
        app: akka-bootstrap-demo
    spec:
      containers:
        - name: akka-cluster-node-1
          image: ramazanakdag/akka-1:latest
          imagePullPolicy: Always
          ports:
            - containerPort: 8558
              protocol: TCP
          env:
            - name: AKKA_CLUSTER_BOOTSTRAP_SERVICE_NAME
              value: akka-cluster-node-1
          livenessProbe:
            httpGet:
              path: /alive
              port: 8558
          readinessProbe:
            httpGet:
              path: /ready
              port: 8558
---
apiVersion: v1
kind: Service
metadata:
  name: akka-cluster-node-1
spec:
  selector:
    app: akka-bootstrap-demo
  ports:
    - protocol: TCP
      port: 8558
      targetPort: 8558
  type: ClusterIP
```


- **`metadata`:**
    - **`name: akka-cluster-node-1`:** Name of the deployment.
    - **`labels`:** Tags to identify the deployment and enable communication between pods and services.

- **`spec`:**
    - **`replicas: 2`:** Specifies that 2 pod instances will be created.
    - **`selector`:** Matches pods with the label `app=akka-bootstrap-demo`.

- **`containers`:**
    - **`image`:** Docker image to be used for the Akka cluster node.
    - **`env`:**
        - **`AKKA_CLUSTER_BOOTSTRAP_SERVICE_NAME`:** Specifies the service name for this node.
    - **`livenessProbe` & `readinessProbe`:** Health checks for the pod.

- **`Service`:**
    - Creates a network endpoint for the pods.
    - **`ClusterIP`:** Makes the service accessible only within the Kubernetes cluster.

---

# TÜRKÇE
---

## **Actor Yapılandırması**
```hocon
actor {
    provider = cluster

    serializers {
        jackson-json = "akka.serialization.jackson.JacksonJsonSerializer"
    }

    serialization-bindings {
        "org.example.Command" = jackson-json
    }
}
```

- **`provider = cluster`:**
  Akka sisteminin bir cluster (küme) olarak çalışacağını belirtir.

- **`serializers`:**
  Akka'nın hangi serileştirme sınıfını kullanacağını tanımlar. Örnek bağımlılık:
  ```xml
  <dependency>
      <groupId>com.typesafe.akka</groupId>
      <artifactId>akka-serialization-jackson_2.13</artifactId>
  </dependency>
  ```

- **`serialization-bindings`:**
  Hangi sınıfların hangi serileştiriciyle serileştirileceğini belirler. Örneğin:
    - `org.example.Command` sınıfı `jackson-json` ile serileştirilecektir.
    - Bu, `Command` sınıfının modüller arasında paylaşılabilmesini sağlar.

---

### **Cluster Bootstrap Yapılandırması**
```hocon
akka.management {
  cluster.bootstrap {
    contact-point-discovery {
      discovery-method = kubernetes-api
      required-contact-point-nr = 3
    }
  }
}
```

- **`akka.management.cluster.bootstrap`:**
  Akka'nın cluster başlatma sürecini nasıl yöneteceğini tanımlar.

- **`discovery-method = kubernetes-api`:**
  Kubernetes API'sini kullanarak düğümleri keşfeder.

- **`required-contact-point-nr = 3`:**
  Kümenin başlatılabilmesi için en az 3 düğümün birbirini bulması gerektiğini belirtir.

---

## **Discovery Yapılandırması**
```hocon
akka.discovery {
  kubernetes-api {
    pod-label-selector = "app=akka-bootstrap-demo"
    pod-namespace = "default"
  }
}
```

- **`pod-label-selector`:**
  `app=akka-bootstrap-demo` etiketine sahip podların keşfedileceğini belirtir.

- **`pod-namespace`:**
  Podların bulunduğu namespace'i belirtir. Varsayılan olarak `default` kullanılır.

---

## **Yeni Küme Etkinleştirme**
```hocon
akka.management.cluster.bootstrap.new-cluster-enabled = on
```

- Eğer mevcut bir cluster yoksa, yeni bir cluster oluşturmayı etkinleştirir.

---

## **Deployment YAML**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: akka-cluster-node-1
  labels:
    app: akka-bootstrap-demo
spec:
  replicas: 2
  selector:
    matchLabels:
      app: akka-bootstrap-demo
  template:
    metadata:
      labels:
        app: akka-bootstrap-demo
    spec:
      containers:
        - name: akka-cluster-node-1
          image: ramazanakdag/akka-1:latest
          imagePullPolicy: Always
          ports:
            - containerPort: 8558
              protocol: TCP
          env:
            - name: AKKA_CLUSTER_BOOTSTRAP_SERVICE_NAME
              value: akka-cluster-node-1
          livenessProbe:
            httpGet:
              path: /alive
              port: 8558
          readinessProbe:
            httpGet:
              path: /ready
              port: 8558
---
apiVersion: v1
kind: Service
metadata:
  name: akka-cluster-node-1
spec:
  selector:
    app: akka-bootstrap-demo
  ports:
    - protocol: TCP
      port: 8558
      targetPort: 8558
  type: ClusterIP
```

- **`metadata`:**
    - **`name: akka-cluster-node-1`:** Deployment kaynağına verilen isim.
    - **`labels`:** Pod ve servislerin birbirleriyle eşleşmesini sağlayan etiketler.

- **`spec`:**
    - **`replicas: 2`:** 2 pod kopyasının oluşturulacağını belirtir.
    - **`selector`:** `app=akka-bootstrap-demo` etiketiyle eşleşen podları seçer.

- **`containers`:**
    - **`image`:** Kullanılacak Docker imajı.
    - **`env`:**
        - **`AKKA_CLUSTER_BOOTSTRAP_SERVICE_NAME`:** Bu düğümün servis adı.
    - **`livenessProbe` & `readinessProbe`:** Pod sağlık kontrolleri.

- **`Service`:**
    - Podlar için bir ağ arayüzü oluşturur.
    - **`ClusterIP`:** Servis sadece Kubernetes ağı içinde erişilebilir olur.

