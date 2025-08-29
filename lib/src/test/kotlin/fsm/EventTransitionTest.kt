package fsm

import kotlin.test.Test
import kotlin.test.assertEquals

enum class MyState { A, B }
enum class MyEvent { GO }

class EventTransitionTests {
    @Test
    fun eventAndTransitionWork() {
        val e = Event(MyEvent.GO, args = listOf(42), kwargs = mapOf("msg" to "hi"))
        assertEquals(MyEvent.GO, e.type)
        assertEquals(42, e.args.first())
        assertEquals("hi", e.kwargs["msg"])

        val t = Transition(MyState.B)
        assertEquals(MyState.B, t.toState)
    }
}