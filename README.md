# Kotlin FSM

[![Release](https://img.shields.io/github/v/release/Sounddimension/kotlin-fsm?label=latest)](https://github.com/Sounddimension/kotlin-fsm/releases)
[![Release Date](https://img.shields.io/github/release-date/Sounddimension/kotlin-fsm)](https://github.com/Sounddimension/kotlin-fsm/releases)
[![GitHub Package](https://img.shields.io/badge/GitHub%20Packages-available-brightgreen)](https://github.com/Sounddimension/kotlin-fsm/packages)

Generic Finite State Machine (FSM) library for Kotlin/JVM.  
Usable in pure Kotlin projects as well as Android apps.

---

## Installation

Add the GitHub Packages repository and dependency:

```kotlin
repositories {
    maven { url = uri("https://maven.pkg.github.com/Sounddimension/kotlin-fsm") }
}
dependencies {
    implementation("com.sounddimension:kotlin-fsm:0.1.1") // replace with latest
}
```

---

## Usage Example

```kotlin
import fsm.*

enum class S { IDLE, ACTIVE }
enum class E { START, STOP }

fun main() {
    val idle = State<S,E>().apply { on(E.START) { Transition(S.ACTIVE) } }
    val active = State<S,E>().apply { on(E.STOP) { Transition(S.IDLE) } }

    val fsm = FSM(
        initialState = S.IDLE,
        stateObjects = mapOf(S.IDLE to idle, S.ACTIVE to active)
    )

    println(fsm.currentState)    // IDLE
    fsm.handleEvent(Event(E.START))
    println(fsm.currentState)    // ACTIVE
}
```
