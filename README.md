# springboot-gemfire-jpa-atomikos
Atomikos JTA provider as global transaction manager to coordinate GemFire/Geode cache transactions with JPA/JDBC and/or JMS resources.

[Atomikos](https://github.com/atomikos/transactions-essentials) is light-weight (e.g. out-of-container), embeddable global 
transaction manager. Atomikos is JTA compliant and can be integrated with Gemfire/Geode to perform XA transaction across Geode, 
JPA/JDBC and JMS operations. 

## Atomikos Gemfire Integration:
1. Add the following SpringBoot starters to your POM:
 * spring-boot-starter-data-gemfire (or spring-data-geode)
 * spring-boot-starter-data-jpa
 * spring-boot-starter-jta-atomikos
2. Start an In-Memory JNDI provider (`SimpleNamingContextBuilder`) *before* the application context is initialized.
3. After the Application Context initialization (e.g. in `@PostConstruct`) *bind* the Atomikos UserTransaction manager to the JNDI
using name: `java:comp/UserTransaction`

Now you can use Spring `@Transaction` annotations to start global (manged by Atomikos) transactions. If Gemfire operation (like put/get) is performed within such transaction it will atomatically participate in the global transaction. 

## Geode/Gemfire JTA Background
Out of the box, Gemfire/Geode provides the following [JTA Global Transactions](http://geode.docs.pivotal.io/docs/developing/transactions/JTA_transactions.html) integration options:

1. Have Gemfire/Geode act as JTA transaction manager - Mind that Gemfire JTA manager is **not JTA compliant** and could cause synchronization and transaction coordination problems. In its current state you better not use it as JTA manager!
2. Coordinate with an external JTA transaction manager in a container (such as WebLogic or JBoss). Also GemFire can be set as the "last resource" while using a container. - While this approach provides a reliable JTA capabilities it requires a heavey-weight JEE container. 

The [SpringBoot Atomikos](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-jta.html#_using_an_atomikos_transaction_manager) 
integration extends option (2) by using Atomikos as an external JTA manager without the need of running a J2EE container. 

At startup GemFire looks for a TransactionManager `javax.transaction.TransactionManager` that has been bound to its `JNDI` context. 
When GemFire finds such an external transaction manager, all GemFire region operations (such as get and put) will participate in 
global transactions hosted by this external JTA transaction manager: [Coordinates with External JTA Transactions Managers](http://geode.docs.pivotal.io/docs/developing/transactions/JTA_transactions.html#concept_cp1_zx1_wk)

Because Gemfire/Gedoe require JNDI provider to lookup the global transactions we have build a simple (in-memory) JNDI provider: `io.pivotal.poc.gemfire.gtx.jndi.SimpleNamingContextBuilder`.
Note: `SimpleNamingContextBuilder` re-uses the code from the `spring-test` project. If you know a more elgant way to create in-memory JNDI providers please let me know!

## Build (default with Gemfire)
``` 
mvn clean install
```

#### Build with Apache Geode
```
mvn clean install -Pgeode
```

## Run
```
java -Dgemfire.name=server1 
     -Dgemfire.server.port=40405 
     -Dgemfire.jmx-manager-port=1199 
     -Dgemfire.jmx-manager=true 
     -Dgemfire.jmx-manager-start=true 
     -jar ./target/gemfire-jpa-atomikos2-0.0.1-SNAPSHOT.jar
```
