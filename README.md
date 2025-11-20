Here is a clean, good-looking `README.md` you can use for your terminal-based Checkers game.
It includes your Coveralls badge, instructions, features, screenshots placeholder, and build instructions.

You can copy/paste directly into your repository.

---

# 🏁 Checkers — Terminal Edition

[![Coverage Status](https://coveralls.io/repos/github/Nitegar/Checkers/badge.svg?branch=github-workflow)](https://coveralls.io/github/Nitegar/Checkers?branch=github-workflow)

A fully playable **Checkers / Draughts game** written in **Scala**, designed to run directly in your terminal.
Includes a clean SBT build, automated CI, and full code-coverage reporting via Coveralls.

---

## 🎮 Features

* ✔️ Play Checkers in the terminal
* ✔️ Fully rule-compliant (forced captures, multi-jumps, king promotion, etc.)
* ✔️ Highlighted valid moves
* ✔️ Bot/AI support *(optional)*
* ✔️ Undo / Redo functionality
* ✔️ Well-structured MVC or functional architecture (depending on your design)
* ✔️ Thorough test suite with Scoverage + Coveralls integration
* ✔️ Cross-platform with SBT (Linux, Windows, macOS)

---

## 🛠️ Requirements

* **Java 21** (or Java 17+)
* **SBT**
* A terminal 😄

---

## 🚀 Running the Game

```bash
sbt run
```

This launches the interactive Checkers board inside your terminal.

---

## 🧪 Running Tests + Coverage

```bash
sbt clean coverage test coverageReport
```

Upload coverage (CI only):

```bash
sbt coveralls
```

---

## 📁 Project Structure (Example)

```
Checkers/
│
├─ src/
│  ├─ main/
│  │  ├─ scala/
│  │  │   └─ ... game logic ...
│  └─ test/
│     └─ ... unit tests ...
│
├─ project/
│   └─ plugins.sbt
├─ build.sbt
└─ README.md
```

---

## 🖼️ Screenshots

*(Optional – add your own screenshots or ASCII-art board)*

```
  a b c d e f g h
8 . r . r . r . r  8
7 r . r . r . r .  7
6 . r . r . r . r  6
5 . . . . . . . .  5
4 . . . . . . . .  4
3 b . b . b . b .  3
2 . b . b . b . b  2
1 b . b . b . b .  1
  a b c d e f g h
```

---

## 🤖 Roadmap

* [ ] Optional bot level difficulty
* [ ] Highlight legal moves dynamically
* [ ] Save/Load game state
* [ ] Multiplayer over network
* [ ] Web UI version

---

## 🧑‍💻 Development

To compile:

```bash
sbt compile
```

To format (if using scalafmt):

```bash
sbt scalafmtAll
```

---

## 📜 License

MIT License — free to use, modify, distribute, or study.

---
