<div align="center">

# Peixoto Dash 🐟

### A serious game for balance rehabilitation.

**Peixoto Dash** is an endless-runner style game where the player shifts their body weight
left and right to dodge obstacles on the beach. Movement can come from an **Arduino-based
balance controller** or the keyboard, and every session records rehabilitation metrics such
as time spent centered, direction changes and reaction time.

<br/>

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![libGDX](https://img.shields.io/badge/libGDX-1.13.1-E74C3C?style=for-the-badge)](https://libgdx.com/)
[![Arduino](https://img.shields.io/badge/Arduino-00878F?style=for-the-badge&logo=arduino&logoColor=white)](https://www.arduino.cc/)
[![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)

</div>

---

## 📖 About

Balance exercises are repetitive, and repetition is where patients lose motivation. Peixoto
Dash turns weight-shifting exercises into a game: leaning to one side moves the fish, staying
centered keeps it steady, and the game measures how well the player controls those movements.

The project was built for a game development course at **Universidade Presbiteriana Mackenzie**.

---

## 🚀 Features

- **🎮 Three levels** — *Praia Inicial*, *Intermediária* and *Avançada*, each with its own speed ramp, obstacle spawn rate, target score and minimum time centered.
- **🔌 Arduino controller** — reads left/right/center signals over serial (jSerialComm); falls back to the keyboard (A/D or ←/→) when no board is connected.
- **📊 Rehabilitation metrics** — obstacles avoided, collisions, items collected, time centered vs. active, direction changes, and fastest and average reaction time.
- **🏁 Performance score** — each session is scored on score, center ratio, avoidance rate and reaction time, and a level is passed only when the score and the balance goal are both met.
- **🖥️ Full game flow** — menu, level select, game, game over and results screens.

---

## 🛠️ Tech Stack

| Area | Tech |
|------|------|
| Language | Java |
| Game framework | libGDX 1.13.1 (LWJGL3 desktop backend) |
| Hardware | Arduino over serial (jSerialComm) |
| Build | Gradle |

---

## 📂 Project Structure

```
peixoto_dash/
├── core/src/main/java/br/mackenzie/
│   ├── PeixotoDash.java        # Game entry and screen management
│   ├── GameScreen.java         # Main gameplay loop
│   ├── Player.java · Obstacle.java
│   ├── InputController.java    # Arduino + keyboard input, movement tracking
│   ├── ArduinoSerial.java      # Serial port reader
│   ├── LevelConfig.java        # Level difficulty settings
│   ├── RehabMetrics.java       # Session metrics and performance score
│   └── MenuScreen · LevelSelectScreen · GameOverScreen · ResultsScreen
├── lwjgl3/                     # Desktop launcher
└── assets/                     # Sprites and backgrounds
```

---

## 🎯 Getting Started

Requires a JDK (17+ recommended).

```bash
git clone https://github.com/dayoleal/Peixoto-Dash.git
cd Peixoto-Dash/peixoto_dash
./gradlew lwjgl3:run
```

To use the balance controller, connect the Arduino before launching the game. Without it,
play with **A/D** or the **arrow keys**.

---

## 👥 Authors

- [Dayô Araújo](https://github.com/dayoleal) · [LinkedIn](https://www.linkedin.com/in/dayo-araujo/)
- [Enssoo](https://github.com/Enssoo)
