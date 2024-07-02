data class Request(
    val affectedRelease: ReleaseId,
    val issue: Issue,
    val anticipatedRelease: ReleaseId,
    val contact: Contact,
)
