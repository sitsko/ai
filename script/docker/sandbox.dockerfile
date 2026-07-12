FROM eclipse-temurin:21-jdk-jammy

ARG USERNAME=developer
ARG USER_UID=1000
ARG USER_GID=1000

# System tools
RUN apt-get update && apt-get install -y \
    git \
    curl \
    wget \
    unzip \
    ripgrep \
    jq \
    bash \
    maven \
    && rm -rf /var/lib/apt/lists/*

# Install Node.js
RUN curl -fsSL https://deb.nodesource.com/setup_22.x | bash - \
    && apt-get install -y nodejs \
    && npm cache clean --force

# Create non-root developer user
RUN groupadd --gid ${USER_GID} ${USERNAME} \
    && useradd --uid ${USER_UID} \
       --gid ${USER_GID} \
       --create-home \
       ${USERNAME}

USER ${USERNAME}

# Configure npm global install without root
ENV NPM_CONFIG_PREFIX=/home/${USERNAME}/.npm-global
ENV PATH=/home/${USERNAME}/.npm-global/bin:${PATH}

# Install AI coding agents
RUN npm install -g \
    @kilocode/cli \
    @anthropic-ai/claude-code

WORKDIR /workspace

CMD ["bash"]