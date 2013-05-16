// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

// Most testing of error messages can go in autogen/tests/commands.  Some tests are here because
// they go beyond the capabilities of the autogenerated test framework.  (In the long run, perhaps
// that framework should be extended so these tests could be done in it.)  - ST 3/18/08
import org.scalatest.{ FunSuite, BeforeAndAfterEach }
import org.nlogo.{ api, nvm }

class TestErrorMessages extends AbstractTestLanguage with FunSuite with BeforeAndAfterEach {

  override def beforeEach() { init() }
  override def afterEach() { workspace.dispose() }

  test("perspectiveChangeWithOf") {
    testCommand(
      "create-frogs 3 [ set spots turtle ((who + 1) mod count turtles) ]")
    testCommand(
      "ask frog 2 [ die ]")
    val ex = intercept[nvm.EngineException] {
      testCommand(
        "ask turtle 0 [ __ignore [who] of frogs with " +
        "[age = ([age] of [spots] of self)]]")
    }
    // is the error message correct?
    expectResult("That frog is dead.")(ex.getMessage)
    // is the error message attributed to the right agent? frog 2 is dead,
    // but it's frog 1 that actually encountered the error
    expectResult("frog 1")(ex.context.agent.toString)
  }

  test("argumentTypeException") {
    testCommand("set glob1 [1.4]")
    val ex = intercept[nvm.ArgumentTypeException] {
      testCommand("__ignore 0 < position 5 item 0 glob1") }
    val message =
      "POSITION expected input to be a string or list but got the number 1.4 instead."
    expectResult(message)(ex.getMessage)
    expectResult("POSITION")(ex.instruction.token.text.toUpperCase)
  }

  test("breedOwnRedeclaration") {
    val ex = intercept[api.CompilerException] {
      compiler.compileProgram(
        "breed [hunters hunter] hunters-own [fear] hunters-own [loathing]",
        api.Program.empty,
        workspace.getExtensionManager)
    }
    expectResult("Redeclaration of HUNTERS-OWN")(ex.getMessage)
  }

  def testBadProcedureName(name: String, error: String, headerSource: String = "") {
    def compile(source: String) {
      val ex = intercept[api.CompilerException] {
        compiler.compileProgram(
          source, api.Program.empty, workspace.getExtensionManager)
      }
      expectResult(error)(ex.getMessage)
    }
    test("bad procedure name: " + name) {
      compile(headerSource + "\nto " + name + " end")
    }
  }

  testBadProcedureName("",
    "identifier expected")
  testBadProcedureName("3",
    "identifier expected")
  testBadProcedureName("to",
    "identifier expected")
  testBadProcedureName("fd",
    "There is already a primitive command called FD")
  testBadProcedureName("turtles",
    "There is already a primitive reporter called TURTLES")
  testBadProcedureName("???",
    "Names beginning with ? are reserved for use as task inputs")
  testBadProcedureName("kitten",
    "There is already a breed called KITTEN", "breed [kittens kitten]")
  testBadProcedureName("kittens",
    "There is already a breed called KITTENS", "breed [kittens kitten]")
  testBadProcedureName("turtles-at",
    "There is already a primitive reporter called TURTLES-AT")
  testBadProcedureName("shell",
    "There is already a TURTLES-OWN variable called SHELL",
    "turtles-own [shell]")
  testBadProcedureName("silliness",
    "There is already a KITTENS-OWN variable called SILLINESS",
    "breed [kittens kitten] kittens-own [silliness]")
  testBadProcedureName("end1",
    "There is already a link variable called END1")
  testBadProcedureName("size",
    "There is already a turtle variable called SIZE")
  testBadProcedureName("color", // well, is actually both turtle and link variable - ST 5/16/03
    "There is already a turtle variable called COLOR")
  testBadProcedureName("pcolor",
    "There is already a patch variable called PCOLOR")

  // at least we get errors on these, but the messages aren't great
  testBadProcedureName("kittens-at",
    "Cannot use KITTENS-AT as a procedure name.  Conflicts with: _breedat:KITTENS",
    "breed [kittens kitten]")
  testBadProcedureName("array:set",
    "Cannot use ARRAY:SET as a procedure name.  Conflicts with: _extern:+0",
    "extensions [array]")

}
