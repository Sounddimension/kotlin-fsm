package fsm

import kotlinx.coroutines.*

/**
 * Generic Finite State Machine with optional timeouts and hooks
 */
class FSM<S : Enum<S>, E : Enum<E>>(
    initialState: S,
    private val stateObjects: Map<S, State<S, E>>,
    private val timeoutEvent: E? = null,
    private val errorState: S? = null,
    private val validateTransitions: Boolean = true,
    private val verbose: Boolean = false,
    private val debugMode: Boolean = false,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    var currentState: S = initialState
        private set

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private var timerJob: Job? = null

    // Extra hooks defined at FSM level
    private val enterHooks = mutableMapOf<S, MutableList<(S) -> Unit>>()
    private val exitHooks  = mutableMapOf<S, MutableList<(S) -> Unit>>()

    init {
        require(stateObjects.containsKey(initialState)) { "Initial state $initialState not found" }
        errorState?.let { require(stateObjects.containsKey(it)) { "Error state $it not found" } }

        if (validateTransitions) {
            // TODO: We can’t fully validate closures in Kotlin, same som i Swift
        }

        if (timeoutEvent != null && !debugMode) {
            startTimer(initialState)
        }
    }

    /**
     * Add a hook that runs when entering a state
     */
    fun addEnterHook(state: S, hook: (S) -> Unit) {
        enterHooks.getOrPut(state) { mutableListOf() }.add(hook)
    }

    /**
     * Add a hook that runs when exiting a state
     */
    fun addExitHook(state: S, hook: (S) -> Unit) {
        exitHooks.getOrPut(state) { mutableListOf() }.add(hook)
    }

    /**
     * Start a timeout timer for given state (if timeoutMillis and timeoutEvent are set)
     */
    private fun startTimer(state: S) {
        if (debugMode) {
            if (verbose) println("Debug mode: skipping timer for $state")
            return
        }
        cancelTimer()

        val timeout = stateObjects[state]?.timeoutMillis ?: return
        val event = timeoutEvent ?: return

        timerJob = scope.launch {
            delay(timeout)
            handleEvent(Event(event))
        }
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    /**
     * Handle an incoming event
     */
    fun handleEvent(event: Event<E>) {
        val current = currentState
        val stateObj = stateObjects[current] ?: run {
            if (verbose) println("No state object for $current")
            return
        }

        val transition = stateObj.handleEvent(event, current.name) ?: return
        if (transition.toState == current) return // no change

        // Exit current
        cancelTimer()
        stateObj.executeExitHooks(current)
        exitHooks[current]?.forEach { it(current) }

        // Switch
        val newState = stateObjects[transition.toState] ?: run {
            if (verbose) println("No state object for ${transition.toState}")
            return
        }
        currentState = transition.toState

        // Enter new
        newState.executeEnterHooks(currentState)
        enterHooks[currentState]?.forEach { it(currentState) }

        if (!debugMode) startTimer(currentState)
    }

    /**
     * Shutdown the FSM
     */
    fun shutdown() {
        cancelTimer()
        scope.cancel()
        if (verbose) println("FSM shutdown complete")
    }

    /**
     * Transition to error state due to exception
     */
    private fun transitionToErrorState(ex: Throwable) {
        val err = errorState ?: return
        cancelTimer()

        val currentObj = stateObjects[currentState]
        currentObj?.executeExitHooks(currentState)

        val prev = currentState
        currentState = err
        val errObj = stateObjects[err]
        if (verbose) println("Transition $prev -> $err due to ${ex.message}")
        errObj?.executeEnterHooks(err)
    }
}