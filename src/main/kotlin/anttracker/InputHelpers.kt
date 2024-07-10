package anttracker

fun displayMenu(
    options: Map<String, Screen>,
    title: String? = null,
): Array<Map.Entry<String, Screen>> {
    val byIndex = options.entries.toTypedArray()

    title?.run { println("== $title ==") }
    byIndex.forEachIndexed { index, entry ->
        val nbr = "${index + 1}".padStart(2, ' ')
        print(nbr)
        println(". ${entry.key}")
    }

    return byIndex
}

fun promptWithOptions(
    message: String,
    choices: List<String>,
): String {
    println("$message: ${choices.joinToString(", ")} ")
    val choice = readln()
    if (choices.contains(choice)) {
        return choice
    }
    return promptWithOptions(message, choices)
}

fun menuUserInput(options: Array<Map.Entry<String, Screen>>): Screen? {
    val choices = (1..options.size).map(Integer::toString) + "0" + "*"
    // The user needs to choose from the choices that are 0, *, 1, 2, 3, 4...
    val response = promptWithOptions("Please choose an option", choices = choices)
    println("You chose: $response")
    return when (response) {
        "*" -> mainMenu
        "0" -> null
        else -> options[response.toInt() - 1].value
    }
}
