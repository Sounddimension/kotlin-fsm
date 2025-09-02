package fsm

open class State<S : Enum<S>, E : Enum<E>>(
    val timeoutMillis: Long? = null,
    val verbose: Boolean = false
) {
    private val handlers = mutableMapOf<E, (Event<E>) -> Transition<S>?>()

    internal val enterHooks = mutableListOf<(S) -> Unit>()
    internal val exitHooks  = mutableListOf<(S) -> Unit>()

    private val enterHooksWithPayload = mutableListOf<(S, Any?) -> Unit>()
    private val exitHooksWithPayload  = mutableListOf<(S, Any?) -> Unit>()

    fun on(eventType: E, handler: (Event<E>) -> Transition<S>?) {
        handlers[eventType] = handler
    }

    fun addEnterHook(hook: (S) -> Unit) { enterHooks.add(hook) }
    fun addExitHook(hook: (S) -> Unit)  { exitHooks.add(hook) }
    fun addEnterHookWithPayload(hook: (S, Any?) -> Unit) { enterHooksWithPayload.add(hook) }
    fun addExitHookWithPayload(hook: (S, Any?) -> Unit)  { exitHooksWithPayload.add(hook) }

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

    internal fun executeEnterHooks(state: S) {
        enterHooks.forEach { it(state) }
    }
    internal fun executeExitHooks(state: S) {
        exitHooks.forEach { it(state) }
    }

    // Nya exekverare med payload
    internal fun executeEnterHooks(state: S, payload: Any?) {
        executeEnterHooks(state)
        enterHooksWithPayload.forEach { it(state, payload) }
    }
    internal fun executeExitHooks(state: S, payload: Any?) {
        executeExitHooks(state)
        exitHooksWithPayload.forEach { it(state, payload) }
    }
}