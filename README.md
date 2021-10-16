# reactive-lab4 - Managing akka actor persistent state

## Persisting and restoring actor internal state

* To persist actor state, we can use _Event Sourcing_ pattern implemented in [Akka Persistence](https://doc.akka.io/docs/akka/current/typed/persistence.html)
* In this repo you can find examples of akka persistance usage
    * Check `PersistentBankAccount` actor behavior after mutliple project runs. What will change after sending to it `Snap` message?
    * Project uses embedded database _LevelDB_ to store events as event log. Database files are created in folder `target/journal` (for events) and `target/snapshot` (for snapshots).
    * database configuration can be found in [src/main/resources/application.conf](src/main/resources/application.conf) under `akka.persistance` config key.
    * Actor state persistance use case is presented in [PersistentToggle.scala](src/main/scala/reactive4/persistence/PersistentToggle.scala)


## Homework

The template for Lab 3: https://github.com/agh-reactive/reactive-scala-labs-templates/tree/lab-4 
* **be sure that your local lab-4 branch is up to date with remote one**
* **remember about merging solution from lab-3 into this branch**

1. (15 points Basket persistence. Implement basket persistence with event sourcing pattern. Take into consideration correct timer recreation.
2. (15 points) Implement state persistence for `PersistentCheckout` and test the scenario where during the checkout operation, the app is halted (e.g. via `system.terminate`). After the restart, the state should be correctly restored (together with timers).
3. (10 points) Write additional tests for `PersistentCartActor` (in particular, take into account actor state recovery).