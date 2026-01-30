# Devcontainer Dockerfile using Eclipse Temurin Java 17
FROM eclipse-temurin:17-jdk

# Install build essentials, git, maven
RUN apt-get update \
  && apt-get install -y --no-install-recommends \
     git curl ca-certificates maven build-essential \
  && rm -rf /var/lib/apt/lists/*

# Create a non-root user 'vscode' for devcontainer
# Use UID 10001 to avoid conflicts with system accounts
ARG USERNAME=vscode
ARG USER_UID=10001
ARG USER_GID=10001

RUN groupadd --gid $USER_GID $USERNAME \
  && useradd -s /bin/bash --uid $USER_UID --gid $USER_GID -m $USERNAME

USER $USERNAME
WORKDIR /workspaces/eclipse-agents
