package examples

import fsm.*
import kotlinx.coroutines.*

private enum class TLState { RED, GREEN, YELLOW }
private enum class TLEvent { TIMEOUT }

fun main() = runBlocking {
    // Snabba tider för demo
    val red = State<TLState, TLEvent>(timeoutMillis = 3000).apply {
        addEnterHook { println("🔴 RED") }
        on(TLEvent.TIMEOUT) { Transition(TLState.GREEN) }
    }
    val green = State<TLState, TLEvent>(timeoutMillis = 3000).apply {
        addEnterHook { println("🟢 GREEN") }
        on(TLEvent.TIMEOUT) { Transition(TLState.YELLOW) }
    }
    val yellow = State<TLState, TLEvent>(timeoutMillis = 2000).apply {
        addEnterHook { println("🟡 YELLOW") }
        on(TLEvent.TIMEOUT) { Transition(TLState.RED) }
    }

    val fsm = FSM(
        initialState = TLState.RED,
        stateObjects = mapOf(
            TLState.RED to red,
            TLState.GREEN to green,
            TLState.YELLOW to yellow
        ),
        timeoutEvent = TLEvent.TIMEOUT
    )

    // Låt den snurra några varv
    delay(20000)
    println("Current: ${fsm.currentState}")
    fsm.shutdown()
}