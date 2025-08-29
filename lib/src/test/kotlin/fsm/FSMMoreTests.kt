package fsm

import kotlin.test.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay

enum class S2 { A, B }
enum class E2 { GO, TIMEOUT }

class FSMMoreTests {

    @Test
    fun debugModeDisablesTimers() = runBlocking {
        val a = State<S2,E2>(timeoutMillis = 10).apply { on(E2.TIMEOUT) { Transition(S2.B) } }
        val b = State<S2,E2>()
        val fsm = FSM(
            initialState = S2.A,
            stateObjects = mapOf(S2.A to a, S2.B to b),
            timeoutEvent = E2.TIMEOUT,
            debugMode = true
        )
        delay(30)
        assertEquals(S2.A, fsm.currentState) // 3)
    }

    @Test
    fun timerResetsOnReenter() = runBlocking {
        val a = State<S2,E2>(timeoutMillis = 20).apply {
            on(E2.GO) { Transition(S2.B) }
            on(E2.TIMEOUT) { Transition(S2.B) }
        }
        val b = State<S2,E2>().apply { on(E2.GO) { Transition(S2.A) } }
        val fsm = FSM(
            initialState = S2.A,
            stateObjects = mapOf(S2.A to a, S2.B to b),
            timeoutEvent = E2.TIMEOUT
        )
        // A -> B (manuellt), B -> A (manuellt) => timer ska starta om i A
        fsm.handleEvent(Event(E2.GO)) // A->B
        fsm.handleEvent(Event(E2.GO)) // B->A
        delay(30)
        assertEquals(S2.B, fsm.currentState) // 5)
    }

    @Test
    fun timerCancelledOnStateChange() = runBlocking {
        val a = State<S2,E2>(timeoutMillis = 50).apply {
            on(E2.TIMEOUT) { Transition(S2.B) }
            on(E2.GO) { Transition(S2.B) }
        }
        val b = State<S2,E2>()
        val fsm = FSM(
            initialState = S2.A,
            stateObjects = mapOf(S2.A to a, S2.B to b),
            timeoutEvent = E2.TIMEOUT
        )
        fsm.handleEvent(Event(E2.GO)) // direkt A->B, A:s timer SKA avbrytas
        delay(70)
        assertEquals(S2.B, fsm.currentState) // fortfarande B (6)
    }

    @Test
    fun noHandlerStaysInState() {
        val a = State<S2,E2>()
        val b = State<S2,E2>()
        val fsm = FSM(initialState = S2.A, stateObjects = mapOf(S2.A to a, S2.B to b))
        fsm.handleEvent(Event(E2.GO))
        assertEquals(S2.A, fsm.currentState) // 7)
    }
}