package fsm

/**
 * Represents an event with optional arguments
 */
data class Event<E : Enum<E>>(
    val type: E,
    val args: List<Any> = emptyList(),
    val kwargs: Map<String, Any> = emptyMap()
)