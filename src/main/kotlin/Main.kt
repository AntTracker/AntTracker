import com.varabyte.kotter.foundation.*
import com.varabyte.kotter.foundation.anim.*
import com.varabyte.kotter.foundation.text.*
import com.varabyte.kotter.foundation.input.*

fun announceTitle(title: String) = session {
  section {
    textLine("The issue with title $title has been created")
    textLine("Press q to return to the main menu")
    input()
  }.runUntilInputEntered{
    onInputEntered { mainMenu() }
  }
}


fun mkIssueProcess() = session{
  section {
   textLine("Please input the issue title")
    input()
  }.runUntilInputEntered{
    onInputEntered { announceTitle(input) }
  }
}

fun mainMenu(): Unit  { session {
  section {
    textLine("Welcome!")
    textLine("Please select one of the options below by entering the number associated with it")
    textLine("1) Create issue")
    textLine("2) Edit issue")
    textLine("3) List issues")
    textLine("4) Create release")
    textLine("5) Add person")
    input()
  }.runUntilInputEntered{
    onInputEntered{mkIssueProcess()}
  }
}}

fun main() = mainMenu()