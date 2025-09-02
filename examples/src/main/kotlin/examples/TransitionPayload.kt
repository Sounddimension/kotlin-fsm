package examples

import fsm.*
import kotlinx.coroutines.*

enum class MyState { IDLE, WORKING }
enum class MyEvent { Start, Done }

fun main() {
    val idle = State<MyState, MyEvent>().apply {
        on(MyEvent.Start) { e ->
            val jobName = e.args.first() as String
            println("Idle: received Start for $jobName")
            Transition(MyState.WORKING, jobName) // skickar med payload
        }
    }

    val working = State<MyState, MyEvent>().apply {
        addEnterHookWithPayload { s, payload ->
            println("Entering $s with payload=$payload")
        }
        on(MyEvent.Done) { Transition(MyState.IDLE) }
    }

    val fsm = FSM(
        initialState = MyState.IDLE,
        stateObjects = mapOf(MyState.IDLE to idle, MyState.WORKING to working),
        verbose = true
    )

    fsm.handleEvent(Event(MyEvent.Start, args = listOf("Job42")))
    fsm.handleEvent(Event(MyEvent.Done))
}