package fsm

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay

enum class S { A, B }
enum class E { GO, TIMEOUT }

class FSMTests {

    @Test
    fun simpleTransition() {
        val a = State<S,E>(verbose = true).apply {
            on(E.GO) { Transition(S.B) }
        }
        val b = State<S,E>()
        val fsm = FSM(
            initialState = S.A,
            stateObjects = mapOf(S.A to a, S.B to b),
            verbose = true,
            debugMode = true // inga timers i detta test
        )
        fsm.handleEvent(Event(E.GO))
        assertEquals(S.B, fsm.currentState)
    }

    @Test
    fun timeoutTriggersEventAndMovesState() = runBlocking {
        val a = State<S,E>(timeoutMillis = 10, verbose = true).apply {
            on(E.TIMEOUT) { Transition(S.B) }   // 👈 hantera timeout-event
        }
        val b = State<S,E>()
        val fsm = FSM(
            initialState = S.A,
            stateObjects = mapOf(S.A to a, S.B to b),
            timeoutEvent = E.TIMEOUT,
            verbose = true,
            debugMode = false
        )
        delay(30) // vänta tills timeout hunnit ske
        assertEquals(S.B, fsm.currentState, "Timeout ska flytta A -> B")
    }

    @Test
    fun hooksRunOnExitAndEnter() {
        val calls = mutableListOf<String>()
        val a = State<S,E>().apply {
            addExitHook { calls += "exitA" }
            on(E.GO) { Transition(S.B) }
        }
        val b = State<S,E>().apply {
            addEnterHook { calls += "enterB" }
        }
        val fsm = FSM(
            initialState = S.A,
            stateObjects = mapOf(S.A to a, S.B to b),
            debugMode = true
        )
        fsm.addExitHook(S.A) { calls += "fsmExitA" }
        fsm.addEnterHook(S.B) { calls += "fsmEnterB" }

        fsm.handleEvent(Event(E.GO))
        assertEquals(listOf("exitA","fsmExitA","enterB","fsmEnterB"), calls)
    }

    @Test
    fun noHandlerReturnsNoTransition() {
        val a = State<S,E>(verbose = true)
        val fsm = FSM(
            initialState = S.A,
            stateObjects = mapOf(S.A to a, S.B to State())
        )
        // inget händer
        fsm.handleEvent(Event(E.GO))
        assertEquals(S.A, fsm.currentState)
    }
}