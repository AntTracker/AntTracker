import com.varabyte.kotter.foundation.*
import com.varabyte.kotter.foundation.anim.*
import com.varabyte.kotter.foundation.text.*
import com.varabyte.kotter.foundation.input.*
//
//fun announceTitle(title: String) = session {
//  section {
//    textLine("The issue with title $title has been created")
//    textLine("Press q to return to the main menu")
//    input()
//  }.runUntilInputEntered{
//    onInputEntered { mainMenu() }
//  }
//}
//
//
//fun mkIssueProcess() =
//  session {
//    section {
//      textLine("Please input the issue title")
//      input()
//    }.runUntilInputEntered {
//      onInputEntered { announceTitle(input) }
//    }
//  }
//
//fun mainMenu(): Unit  {
//  session {
//    section {
//      textLine("Welcome!")
//      textLine("Please select one of the options below by entering the number associated with it")
//      textLine("1) Create issue")
//      textLine("2) Edit issue")
//      textLine("3) List issues")
//      textLine("4) Create release")
//      textLine("5) Add person")
//      input()
//    }.runUntilInputEntered{
//      onInputEntered{
//        section {
//          textLine("Please input the issue title")
//          input()
//        }.runUntilInputEntered {
//          onInputEntered { announceTitle(input) }
//        } }
//    }
//  }}
//
//fun mainMenu2(): Unit  {
//  session {
//    section {
//      textLine("Welcome!")
//      textLine("Please select one of the options below by entering the number associated with it")
//      textLine("1) Create issue")
//      textLine("2) Edit issue")
//      textLine("3) List issues")
//      textLine("4) Create release")
//      textLine("5) Add person")
//    }
//  }}
//
val welcomeMessage = listOf("Welcome",
  "Please select one of the options below by entering the number associated with it",
  "1) Create issue",
  "2) Edit issue",
  "3) List issues",
  "4) Create release",
  "5) Add person").joinToString(separator = "\n", postfix = "\n")
//
//
//fun main() = mainMenu()

fun main() =
  session {
    var menuOption by liveVarOf<Int?>(null)
    section {
      text(welcomeMessage)
      when (menuOption) {
        1 ->  section {
          var title: String? = null
          var description: String? = null
          var name: String? = null
          section {
            textLine("Please create a title for your issue")
          }.runUntilInputEntered {
            onInputEntered { title = input }
          }
          section {
            textLine("Please create a description of your issue")
          }.run {
            onInputEntered { description = input }
            rerender()
          }
          section {
            textLine("Please add the name of the reporter")
          }.runUntilInputEntered {
            onInputEntered { name = input }
          }
          textLine("The issue with title $title reported by $name has been created!")
        }

      }
      input()
    }.runUntilInputEntered {
      onInputEntered { menuOption = input.toInt() }
    }
  }


//    section {
//      text(welcomeMessage)
//      input()
//    }.runUntilInputEntered {
//      onInputEntered {menuOption = input.toInt()}
//    }
//    run { section {
//        when (menuOption) {
//          1 -> {var title: String? = null
//              var description: String? = null
//              var name: String? = null
//            section {
//              textLine("Please create a title for your issue")
//            }.runUntilInputEntered {
//              onInputEntered { title = input }
//            }
//            section {
//              textLine("Please create a description of your issue")
//            }.runUntilInputEntered {
//              onInputEntered { description = input }
//            }
//            section {
//              textLine("Please add the name of the reporter")
//            }.runUntilInputEntered {
//              onInputEntered { name = input }
//            }
//            textLine("The issue with title $title reported by $name has been created!")
//          }
//        }
//        }
//    }
//  }