package fsm

/**
 * Base class for states in the FSM
 */
open class State<S : Enum<S>, E : Enum<E>>(
    val timeoutMillis: Long? = null,
    val verbose: Boolean = false
) {
    // Event handlers for this state
    private val handlers = mutableMapOf<E, (Event<E>) -> Transition<S>?>()

    // Enter/exit hooks
    internal val enterHooks = mutableListOf<(S) -> Unit>()
    internal val exitHooks  = mutableListOf<(S) -> Unit>()

    /**
     * Add a handler for a specific event type
     */
    fun on(eventType: E, handler: (Event<E>) -> Transition<S>?) {
        handlers[eventType] = handler
    }

    /**
     * Add a hook that runs when entering this state
     */
    fun addEnterHook(hook: (S) -> Unit) {
        enterHooks.add(hook)
    }

    /**
     * Add a hook that runs when exiting this state
     */
    fun addExitHook(hook: (S) -> Unit) {
        exitHooks.add(hook)
    }

    /**
     * Handle an event and return a transition if state should change
     */
    fun handleEvent(event: Event<E>, stateName: String, catchExceptions: Boolean = false): Transition<S>? {
        val handler = handlers[event.type]
        if (handler == null) {
            if (verbose) println("No handler for event ${event.type} in state $stateName")
            return null
        }

        return if (catchExceptions) {
            try {
                handler(event)
            } catch (t: Throwable) {
                if (verbose) println("Error in handler for event ${event.type} in $stateName: ${t.message}")
                null
            }
        } else {
            handler(event)
        }
    }

    /**
     * Execute enter hooks
     */
    internal fun executeEnterHooks(state: S) {
        for (hook in enterHooks) hook(state)
    }

    /**
     * Execute exit hooks
     */
    internal fun executeExitHooks(state: S) {
        for (hook in exitHooks) hook(state)
    }
}