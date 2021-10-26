# Lab 4 - Managing akka actor persistent state

## Persistence and recovery of actor internal state

* To persist actor state, we use the _Event Sourcing_ pattern implemented in [Akka Persistence](https://doc.akka.io/docs/akka/current/typed/persistence.html)
* In this repo you can find examples of akka persistance usage
    * Observe `PersistentBankAccount` actor behavior after multiple project runs. What will happen after sending a `Snap` message?
    * Project uses embedded database _LevelDB_ to store events as an event log. Database files are created in folder `target/journal` (for events) and `target/snapshot` (for snapshots).
    * database configuration can be found in [src/main/resources/application.conf](src/main/resources/application.conf) under the `akka.persistance` config key.
    * Actor state persistance example is presented in [PersistentToggle.scala](src/main/scala/reactive4/persistence/PersistentToggle.scala)


## Assignment

The template for Lab 3: https://github.com/agh-reactive/reactive-scala-labs-templates/tree/lab-4 
* **be sure that your local lab-4 branch is up to date with remote one**
* **remember about merging solution from lab-3 into this branch**

1. (15 points) Cart persistence. Implement cart persistence using the event sourcing pattern. Ensure correct timer recovery.
2. (15 points) Implement state persistence for `PersistentCheckout` and test the scenario where during the checkout operation, the app is stopped (e.g. via `system.terminate`). After the restart, the state should be correctly restored (together with timers).
3. (10 points) Write additional tests for `PersistentCartActor` (in particular, take into account actor state recovery).

## Submission

Use the following steps to submit your solution.

First, before implementing the solution:
1. Merge `lab3-solution -> master`
2. Merge `lab-4 -> master`  (resolve conflicts)

Next:
1. Create branch `lab4-solution` (from `master`)
2. Implement the assignments using `lab4-solution`
3. Create a PR `lab4-solution -> master`
4. Submit the link to the PR as a solution
