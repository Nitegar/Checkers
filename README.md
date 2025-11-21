# 🏁 Checkers — Terminal Based

[![Coverage Status](https://coveralls.io/repos/github/Nitegar/Checkers/badge.svg?branch=github-workflow)](https://coveralls.io/github/Nitegar/Checkers?branch=github-workflow)

A fully playable **Checkers / Draughts game** written in **Scala**, designed to run directly in your terminal.
Includes a clean SBT build, automated CI, and full code-coverage reporting via Coveralls.

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

---

---

## 🖼️ Rules

- Regular pieces move diagonally forward
- Kings move diagonally in any direction
- You must jump when available
- Reach the opposite end to become a King

## Turn example
```sh
                                                                                                                                                                                                                          
╦═╗╔═╗╔╦╗  ╔╦╗╦ ╦╦═╗╔╗╔                                                                                                                                                                                                   
╠╦╝║╣  ║║   ║ ║ ║╠╦╝║║║                                                                                                                                                                                                   
╩╚═╚═╝═╩╝   ╩ ╚═╝╩╚═╝╚╝                                                                                                                                                                                                   
                                                                                                                                                                                                                          
      ○ RED'S TURN! ○                                                                                                                                                                                                     

    a  b  c  d  e  f  g  h 
  +--+--+--+--+--+--+--+--+
1 |  |● |  |● |  |● |  |● | 1
  +--+--+--+--+--+--+--+--+
2 |● |  |● |  |● |  |● |  | 2
  +--+--+--+--+--+--+--+--+
3 |  |● |  |● |  |● |  |● | 3
  +--+--+--+--+--+--+--+--+
4 |  |  |  |  |  |  |  |  | 4
  +--+--+--+--+--+--+--+--+
5 |  |  |  |  |  |  |  |  | 5
  +--+--+--+--+--+--+--+--+
6 |○ |  |○ |  |○ |  |○ |  | 6
  +--+--+--+--+--+--+--+--+
7 |  |○ |  |○ |  |○ |  |○ | 7
  +--+--+--+--+--+--+--+--+
8 |○ |  |○ |  |○ |  |○ |  | 8
  +--+--+--+--+--+--+--+--+
    a  b  c  d  e  f  g  h 

Pieces: ○/◎ = Red, ●/◉ = Black (Ring = King)


RED (○)'s turn (Red: 12, Black: 12)
Enter move (e.g., 'b3 c4') or 'quit'/'q': 
```

## 🧑‍💻 Development

To compile:

```bash
sbt compile
```

---
