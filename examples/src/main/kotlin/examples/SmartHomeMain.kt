package examples

import fsm.*
import kotlinx.coroutines.*

private enum class SHState { IDLE, HEATING, COOLING }
private enum class SHEvent { TEMP_HIGH, TEMP_LOW, TIMEOUT }

fun main() = runBlocking {
    val idle = State<SHState, SHEvent>(timeoutMillis = 100).apply {
        on(SHEvent.TEMP_LOW)  { Transition(SHState.HEATING) }
        on(SHEvent.TEMP_HIGH) { Transition(SHState.COOLING) }
        on(SHEvent.TIMEOUT)   { Transition(SHState.IDLE) }
    }
    val heating = State<SHState, SHEvent>(timeoutMillis = 50).apply {
        addEnterHook { println("🔥 Heating ON") }
        addExitHook  { println("🔥 Heating OFF") }
        on(SHEvent.TEMP_HIGH) { Transition(SHState.IDLE) }
        on(SHEvent.TIMEOUT)   { Transition(SHState.HEATING) }
    }
    val cooling = State<SHState, SHEvent>(timeoutMillis = 50).apply {
        addEnterHook { println("❄️ Cooling ON") }
        addExitHook  { println("❄️ Cooling OFF") }
        on(SHEvent.TEMP_LOW)  { Transition(SHState.IDLE) }
        on(SHEvent.TIMEOUT)   { Transition(SHState.COOLING) }
    }

    val fsm = FSM(
        initialState = SHState.IDLE,
        stateObjects = mapOf(
            SHState.IDLE to idle,
            SHState.HEATING to heating,
            SHState.COOLING to cooling
        ),
        timeoutEvent = SHEvent.TIMEOUT
    )

    fsm.handleEvent(Event(SHEvent.TEMP_LOW))   // -> HEATING
    delay(10)
    fsm.handleEvent(Event(SHEvent.TEMP_HIGH))  // -> IDLE

    println("Current: ${fsm.currentState}")
}