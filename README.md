# Checkers Game

[![Coverage Status](https://coveralls.io/repos/github/Nitegar/Checkers/badge.svg?branch=github-workflow)](https://coveralls.io/github/Nitegar/Checkers?branch=github-workflow)

This is a **Checkers game** implemented in **Scala 3**.  
The project uses **sbt** for building and **JavaFX** for the GUI. It supports both **TUI (text-based UI)** and **GUI** modes.

---

## Usage

This is a standard **sbt project**.

### Build & Run
* **Compile the code:**
    ```bash
    sbt compile
    ```
* **Run the application:**
    ```bash
    sbt run
    ```
* **Start a Scala 3 REPL:**
    ```bash
    sbt console
    ```

**Main Class:** `de.htwg.CheckersApp`

---

## Docker

The project can be built and run in Docker. GUI support is included using JavaFX.

### Prerequisites
* **Docker Buildx** installed.
* **macOS:** XQuartz installed for GUI support.
* **Linux:** X11 server available.

### Build Docker Image
From the root of the project:
```bash
docker buildx build --platform linux/arm64 -t checkers .

```

> **Note:** On Apple Silicon (M1/M2), use `--platform linux/arm64`. On Intel Mac or Linux x86_64, use `--platform linux/amd64`.

### Run Docker Container

#### Linux

```bash
# Allow Docker to access your X server
xhost +local:docker

# Run container
docker run -it --rm \
    -e DISPLAY=$DISPLAY \
    -v /tmp/.X11-unix:/tmp/.X11-unix \
    checkers

```

#### macOS (Apple Silicon)

1. **Setup XQuartz:**
* `brew install --cask xquartz`
* Open XQuartz.
* Go to **Preferences → Security** and check **"Allow connections from network clients"**.


2. **Configure Environment:**
```bash
export DISPLAY=$(ipconfig getifaddr en0):0
xhost + $(ipconfig getifaddr en0)

```


3. **Run Container:**
```bash
docker run -it --rm -e DISPLAY=$DISPLAY checkers

```



---

## Notes

* **TUI:** The TUI works independently of the GUI, allowing you to play directly in the terminal.
* **Architecture:** JavaFX requires the container architecture to match your host (ARM64 for Apple Silicon, AMD64 for Intel).
* **Java 21:** Requires enabling native access for JavaFX via `--enable-native-access=javafx.graphics`. This is pre-configured in the SBT `javaOptions`.

---

## License

MIT License

---