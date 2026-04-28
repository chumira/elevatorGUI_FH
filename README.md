# Elevator GUI

## Description
Graphical user interface for the simulation and control of an elevator system with serial communication to an external control unit.

Developed as part of a bachelor's thesis.

---

## Technologies
- Java
- JavaFX
- Serial communication

## Features
- Visualization of elevators and floors
- Real-time interaction with a control unit
- Simulation of elevator states

### Requirements
- Java (version 21 or higher)
- JavaFX
- Serial interface

### Run
1. Run the main class
2. Select the correct serial port in the GUI
3. Send commands via the serial port

---

## Usage (Command Interface)

The application communicates with a control unit via a serial interface.  
Commands can either be received (to control the GUI) or sent (as events).

### Initialization (required before start)
- `INIT_BASE <F> <E> <L> <H> <M>` – initialize simulation  
- `INIT_STATE <E> <F>` – set initial elevator position (optional)  
- `INIT_DONE` – start simulation  

---

### Commands (received from control unit)
- `OPEN <E>` – open elevator door  
- `CLOSE <E>` – close elevator door  
- `MOVE_UP <E>` – move elevator up  
- `MOVE_DOWN <E>` – move elevator down  
- `STOP <E>` – stop elevator  
- `LIGHT <ON|OFF> <F|E> <N> <M>` – control lights  

---

### Events (sent to control unit)
- `ARRIVE <E> <F>` – elevator has reached a floor  
- `LEAVE <E> <F>` – elevator has left a floor  
- `REQUEST <E> <F>` – request from elevator  
- `BUTTON_PUSH <F>` – floor button pressed  

---

### Modes (optional)
- `MODE UPDOWN` → adds:
  - `BUTTON_UP <F>`
  - `BUTTON_DOWN <F>`

- `MODE EMERGENCY` → adds:
  - `EMERGENCY_STOP_FLOOR <F>`
  - `EMERGENCY_STOP_ELEVATOR <E>`

---

## Notes
- Requires a compatible control unit or simulation environment
- Intended for demonstration and educational purposes
