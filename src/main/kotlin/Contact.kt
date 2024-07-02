@JvmInline
value class Name(
    val name: String,
) {
    init {
        require(name.length in 1..30) {
            "Name must be in between 1 and 30 characters"
        }
    }
}

@JvmInline
value class Email(
    val email: String,
) {
    init {
        require(email.contains("@") && email.length in 5..24) {
            "Email does not contain @ or is not within 1 to 30 characters"
        }
    }
}

@JvmInline
value class PhoneNumber(
    val number: String,
) {
    init {
        require(
            number.all { c -> c.isDigit() } &&
                (
                    (number.length == 11 && number.first() == '1') ||
                        (number.length == 10)
                ),
        ) {
            "The phone number is not callable within BC"
        }
    }
}

data class Contact(
    val name: Name,
    val email: Email,
    val phoneNumber: PhoneNumber,
)
