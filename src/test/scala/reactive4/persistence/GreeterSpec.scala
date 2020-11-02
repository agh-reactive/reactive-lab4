package reactive4.persistence

import java.{util => ju}
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.persistence.typed.PersistenceId
import org.scalatest.flatspec.AnyFlatSpecLike
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit.SerializationSettings
import reactive4.persistence.Greeter.Greet
import reactive4.persistence.Greeter.Greeted
import reactive4.persistence.Greeter.GetAllGreeted
import akka.persistence.testkit.PersistenceTestKitPlugin
import com.typesafe.config.ConfigValueFactory
import akka.persistence.testkit.scaladsl.PersistenceTestKit
import org.scalatest.GivenWhenThen

class GreeterSpec
  extends ScalaTestWithActorTestKit(
    PersistenceTestKitPlugin.config
      .withValue("akka.actor.allow-java-serialization", ConfigValueFactory.fromAnyRef(true))
  )
  with AnyFlatSpecLike with GivenWhenThen {

  private def generateGreeterPersistenceId(): PersistenceId =
    PersistenceId("Greeter", ju.UUID.randomUUID().toString())

  private def createGreeterTestKit(
    greeterPersistenceId: PersistenceId = generateGreeterPersistenceId()
  ): EventSourcedBehaviorTestKit[Greeter.Command, Greeter.Event, List[String]] = {
    EventSourcedBehaviorTestKit(
      system,
      Greeter.apply(greeterPersistenceId),
      SerializationSettings.disabled
    )
  }

  private val persistenceTestKit = PersistenceTestKit(system)

  // testing events generation
  it should "persist the Greeted event on Greet command" in {
    Given("Greeter actor with some persitence id")
    val greeterPersistenceId = generateGreeterPersistenceId()
    val greeterTestKit = createGreeterTestKit(greeterPersistenceId)
    val user1 = "User1"

    When("Sending greeting for User1")
    val greetUser1Result = greeterTestKit.runCommand(Greet(user1))

    Then("Event should be generated and stored in the journal")
    greetUser1Result.event shouldBe Greeted(user1)
    persistenceTestKit.expectNextPersisted(greeterPersistenceId.id, Greeted(user1))
  }

  // EventSourced behavior testing, aka. white box
  it should "correctly update internal state" in {
    Given("Greeter actor with some persitence id")
    val greeterTestKit = createGreeterTestKit()
    val user1 = "User1"

    When("Sending greeting for User1")
    val greetUser1Result = greeterTestKit.runCommand(Greet(user1))

    Then("Event shoudl be generated and state updated")
    greetUser1Result.event shouldBe Greeted(user1)
    greetUser1Result.state shouldBe List(user1)
  }

  // EventSourced behavior testing, testing restart
  it should "keep previously greeted names after restart" in {
    Given("Greeter actor with some persistence id")
    val testProbe = testKit.createTestProbe[List[String]]()
    val greeterPersistenceId = generateGreeterPersistenceId()
    val greeterTestKit = createGreeterTestKit(greeterPersistenceId)
    And("Greeting for User1 and User2 sent")
    val user1 = "User1"
    greeterTestKit.runCommand(Greet(user1))
    val user2 = "User2"
    greeterTestKit.runCommand(Greet(user2))

    When("Actor is restarted")
    greeterTestKit.restart()

    Then("The events in journal should be preserved")
    persistenceTestKit.persistedInStorage(greeterPersistenceId.id) shouldEqual Seq(Greeted(user1), Greeted(user2))

    And("The Actor state should be recovered")
    greeterTestKit.runCommand(GetAllGreeted(testProbe.ref))
    testProbe.expectMessage(List(user1, user2))
  }

  // asynchronous standard akka testing, aka. black box
  it should "respond with all greeted user names" in {
    Given("Greeter actor with some persistence id")
    val testProbe = testKit.createTestProbe[List[String]]()
    val greeter = testKit.spawn(Greeter.apply(generateGreeterPersistenceId()))
    And("Greetings for User1 and User2 sent")
    val user1 = "User1"
    greeter ! Greet(user1)
    val user2 = "User2"
    greeter ! Greet(user2)

    When("Asking for all greeted user list")
    greeter ! GetAllGreeted(testProbe.ref)

    Then("Greeted user list should be returned")
    testProbe.expectMessage(List(user1, user2))
  }

}
