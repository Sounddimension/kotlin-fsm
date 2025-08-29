package examples

import fsm.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

private enum class S { OFF, IDLE, ON }
private enum class E { TURN_ON, TURN_OFF, TIMEOUT }

private class Hardware {
    fun ledOff() = println("...LED OFF")
    fun ampsOff() = println("...Amps OFF")
    fun screenOff() = println("...Screen OFF")
    fun screenDim() = println("...Screen DIM")
    fun ampsIdle() = println("...Amps IDLE")
    fun powerUp() = println("...Powering UP")
    fun allOff() { ledOff(); ampsOff(); screenOff() }
}

fun main() = runBlocking {
    val hw = Hardware()

    // Gemensamma handlers
    val powerDown: (Event<E>) -> Transition<S>? = {
        hw.allOff(); Transition(S.OFF)
    }
    val powerUp: (Event<E>) -> Transition<S>? = {
        hw.powerUp(); Transition(S.ON)
    }

    val off = State<S,E>().apply {
        on(E.TURN_ON){
            println("TURN ON in state OFF!"); powerUp(it) // OFF -> ON
        }            
    }
    val idle = State<S,E>().apply {
        on(E.TURN_ON){
            println("TURN ON in state IDLE!"); powerUp(it) // IDLE -> ON
        }          
        on(E.TURN_OFF, powerDown)         // IDLE -> OFF
    }
    val on = State<S,E>(timeoutMillis = 5000).apply {
        on(E.TURN_OFF){
            println("TURN OFF in state ON!"); powerDown(it) // ON -> OFF
        } 
        on(E.TIMEOUT) {                   // ON -> IDLE med dim/idle
            println("Timeout in state ON!");hw.screenDim(); hw.ampsIdle();
            Transition(S.IDLE)
        }
    }

    val fsm = FSM(
        initialState = S.OFF,
        stateObjects = mapOf(S.OFF to off, S.IDLE to idle, S.ON to on),
        timeoutEvent = E.TIMEOUT
    )

    // Demo
    fsm.handleEvent(Event(E.TURN_ON))   // IDLE -> ON
    // ...wait 7 sec so TIMEOUT triggers -> IDLE (dim/idle runs)
    delay(7000)
    fsm.handleEvent(Event(E.TURN_ON))   // IDLE -> ON
    fsm.handleEvent(Event(E.TURN_OFF)) // ON -> OFF 
}