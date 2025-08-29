package fsm

import kotlin.test.*

enum class TestState { A, B }
enum class TestEvent { GO, FAIL }

class StateTests {

    @Test
    fun handlerShouldReturnTransition() {
        val state = State<TestState, TestEvent>(verbose = true)
        state.on(TestEvent.GO) { Transition(TestState.B) }

        val result = state.handleEvent(Event(TestEvent.GO), "A")
        assertEquals(TestState.B, result?.toState)
    }

    @Test
    fun handlerShouldBeNullIfNotRegistered() {
        val state = State<TestState, TestEvent>(verbose = true)
        val result = state.handleEvent(Event(TestEvent.GO), "A")
        assertNull(result)
    }

    @Test
    fun enterAndExitHooksShouldRun() {
        val state = State<TestState, TestEvent>()
        var entered: TestState? = null
        var exited: TestState? = null

        state.addEnterHook { entered = it }
        state.addExitHook { exited = it }

        state.executeEnterHooks(TestState.A)
        state.executeExitHooks(TestState.A)

        assertEquals(TestState.A, entered)
        assertEquals(TestState.A, exited)
    }

    @Test
    fun handlerExceptionIsCaughtWhenCatchExceptionsTrue() {
        val state = State<TestState, TestEvent>(verbose = true)
        state.on(TestEvent.FAIL) { throw RuntimeException("boom") }

        val result = state.handleEvent(Event(TestEvent.FAIL), "A", catchExceptions = true)
        assertNull(result) // should not crash
    }
}