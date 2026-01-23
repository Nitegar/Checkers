# Use Eclipse Temurin with Java 25 (Based on Ubuntu 24.04 Noble)
FROM --platform=linux/arm64 eclipse-temurin:21-jdk-noble

RUN apt-get update && apt-get install -y \
    curl \
    gnupg \
    libgl1 \
    libglx-mesa0 \
    libgtk-3-0 \
    libasound2t64 \
    libxtst6 \
    libxi6 \
    && rm -rf /var/lib/apt/lists/*

# Install sbt
RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add && \
    apt-get update && \
    apt-get install -y sbt

WORKDIR /app

# Copy build definition files first to leverage Docker cache
COPY build.sbt ./
COPY project ./project


RUN sbt update

COPY . .

RUN sbt compile

CMD ["sbt", "run"]