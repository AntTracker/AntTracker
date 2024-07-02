@JvmInline
value class ReleaseId(
    private val id: String,
) {
    init {
        require(id.length in 1..8) {
            "Release id length must be between 1 and 8 characters"
        }
    }
}
