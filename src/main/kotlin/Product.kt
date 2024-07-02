fun validateProductName(name: String): Boolean = true

@JvmInline
value class Product(
    private val name: String,
) {
    init {
        require(name.length in 1..30) {
            "Name length must be between 1 and 30 characters"
        }
    }
}
