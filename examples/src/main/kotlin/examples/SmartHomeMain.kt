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
        on(SHEvent.TEMP_HIGH) { Transition(SHState.IDLE) }    // nått måltemp
        on(SHEvent.TIMEOUT)   { Transition(SHState.HEATING) } // heartbeat
    }
    val cooling = State<SHState, SHEvent>(timeoutMillis = 50).apply {
        addEnterHook { println("❄️ Cooling ON") }
        addExitHook  { println("❄️ Cooling OFF") }
        on(SHEvent.TEMP_LOW)  { Transition(SHState.IDLE) }    // nått måltemp
        on(SHEvent.TIMEOUT)   { Transition(SHState.COOLING) } // heartbeat
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

    // IDLE -> HEATING
    fsm.handleEvent(Event(SHEvent.TEMP_LOW))
    delay(20)
    // HEATING -> IDLE
    fsm.handleEvent(Event(SHEvent.TEMP_HIGH))
    // IDLE -> COOLING
    fsm.handleEvent(Event(SHEvent.TEMP_HIGH))
    delay(20)
    // COOLING -> IDLE
    fsm.handleEvent(Event(SHEvent.TEMP_LOW))

    println("Current: ${fsm.currentState}") // IDLE
}