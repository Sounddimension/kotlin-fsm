package examples

import fsm.*
import kotlinx.coroutines.*

private enum class SpkState { IDLE, PAIRING, PLAYING, ERROR }
private enum class SpkEvent { BTN, CONNECTED, PLAY, STOP, TIMEOUT, FAIL }

fun main() = runBlocking {
    val idle = State<SpkState, SpkEvent>().apply {
        on(SpkEvent.BTN) { Transition(SpkState.PAIRING) }
    }

    val pairing = State<SpkState, SpkEvent>(timeoutMillis = 200).apply {
        addEnterHook { println("🔗 Pairing…") }
        addExitHook  { println("🔗 Pairing done") }
        on(SpkEvent.CONNECTED) { Transition(SpkState.PLAYING) }
        on(SpkEvent.TIMEOUT)   { Transition(SpkState.IDLE) }
        on(SpkEvent.FAIL)      { Transition(SpkState.ERROR) }
    }

    val playing = State<SpkState, SpkEvent>().apply {
        addEnterHook { println("🎵 Playing") }
        addExitHook  { println("⏹️ Stopped") }
        on(SpkEvent.STOP) { Transition(SpkState.IDLE) }
    }

    val error = State<SpkState, SpkEvent>().apply {
        addEnterHook { println("💥 Error") }
        on(SpkEvent.BTN) { Transition(SpkState.IDLE) }
    }

    val fsm = FSM(
        initialState = SpkState.IDLE,
        stateObjects = mapOf(
            SpkState.IDLE to idle,
            SpkState.PAIRING to pairing,
            SpkState.PLAYING to playing,
            SpkState.ERROR to error
        ),
        timeoutEvent = SpkEvent.TIMEOUT
    )

    fsm.handleEvent(Event(SpkEvent.BTN))        // -> PAIRING
    fsm.handleEvent(Event(SpkEvent.CONNECTED))  // -> PLAYING
    fsm.handleEvent(Event(SpkEvent.STOP))       // -> IDLE

    println("Current: ${fsm.currentState}")
}