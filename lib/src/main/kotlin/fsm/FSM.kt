package fsm

import kotlinx.coroutines.*

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

    // Befintliga FSM-nivå hooks (behålls)
    private val enterHooks = mutableMapOf<S, MutableList<(S) -> Unit>>()
    private val exitHooks  = mutableMapOf<S, MutableList<(S) -> Unit>>()

    // NYTT: FSM-nivå hooks med payload
    private val enterHooksWithPayload = mutableMapOf<S, MutableList<(S, Any?) -> Unit>>()
    private val exitHooksWithPayload  = mutableMapOf<S, MutableList<(S, Any?) -> Unit>>()

    init {
        require(stateObjects.containsKey(initialState)) { "Initial state $initialState not found" }
        errorState?.let { require(stateObjects.containsKey(it)) { "Error state $it not found" } }
        if (validateTransitions) {
            // (behåll framtida validering här)
        }
        if (timeoutEvent != null && !debugMode) startTimer(initialState)
    }

    // Befintliga API
    fun addEnterHook(state: S, hook: (S) -> Unit) {
        enterHooks.getOrPut(state) { mutableListOf() }.add(hook)
    }
    fun addExitHook(state: S, hook: (S) -> Unit) {
        exitHooks.getOrPut(state) { mutableListOf() }.add(hook)
    }

    // NYTT API
    fun addEnterHookWithPayload(state: S, hook: (S, Any?) -> Unit) {
        enterHooksWithPayload.getOrPut(state) { mutableListOf() }.add(hook)
    }
    fun addExitHookWithPayload(state: S, hook: (S, Any?) -> Unit) {
        exitHooksWithPayload.getOrPut(state) { mutableListOf() }.add(hook)
    }

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

    private fun cancelTimer() { timerJob?.cancel(); timerJob = null }

    fun handleEvent(event: Event<E>) {
        val from = currentState
        val stateObj = stateObjects[from] ?: run {
            if (verbose) println("No state object for $from"); return
        }

        val transition = stateObj.handleEvent(event, from.name) ?: return
        val to = transition.toState
        if (to == from) return

        // EXIT
        cancelTimer()
        stateObj.executeExitHooks(from, transition.payload)
        exitHooks[from]?.forEach { it(from) }
        exitHooksWithPayload[from]?.forEach { it(from, transition.payload) }

        // SWITCH
        val newState = stateObjects[to] ?: run {
            if (verbose) println("No state object for $to"); return
        }
        currentState = to

        // ENTER
        newState.executeEnterHooks(currentState, transition.payload)
        enterHooks[currentState]?.forEach { it(currentState) }
        enterHooksWithPayload[currentState]?.forEach { it(currentState, transition.payload) }

        if (!debugMode) startTimer(currentState)
        if (verbose) println("FSM: $from --(${event.type})--> $to payload=${transition.payload}")
    }

    fun shutdown() {
        cancelTimer()
        scope.cancel()
        if (verbose) println("FSM shutdown complete")
    }

    private fun transitionToErrorState(ex: Throwable) {
        val err = errorState ?: return
        cancelTimer()
        stateObjects[currentState]?.executeExitHooks(currentState)
        val prev = currentState
        currentState = err
        if (verbose) println("Transition $prev -> $err due to ${ex.message}")
        stateObjects[err]?.executeEnterHooks(err)
    }
}