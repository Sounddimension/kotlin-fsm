package fsm

/**
 * Represents a state transition
 */
data class Transition<S : Enum<S>>(
    val toState: S
)