# @kilocode/cli bundles a native OpenTUI render library (compiled Bun/opencode
# executable) linked against glibc. It needs the real glibc dynamic loader and
# ucontext (getcontext/swapcontext) symbols — musl/Alpine's `gcompat` shim covers
# neither reliably (ucontext is ABI-incompatible between glibc and musl), so this
# sandbox stays on a glibc (Debian) base unlike ai-sandbox-claude.dockerfile.
# Node must also come from a glibc build (node:22-slim), not node:22-alpine.
FROM node:22-slim AS node-bin

# ---- Builder: install npm globals in a stage that gets discarded ----------
FROM eclipse-temurin:21-jdk-jammy AS builder

ARG USERNAME=developer
ARG USER_UID=1000
ARG USER_GID=1000
ARG MAVEN_VERSION=3.9.9

# Minimal toolchain in case npm postinstall scripts need it; discarded with this stage
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates bash \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Maven via official binary tarball — apt's `maven` package pulls in its own full
# openjdk+jmods+X11/AWT libs (~400 MiB) even though this image already has a JDK
RUN curl -fsSL "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -o /tmp/mvn.tgz \
    && tar -xzf /tmp/mvn.tgz -C /opt \
    && mv /opt/apache-maven-${MAVEN_VERSION} /opt/maven \
    && rm /tmp/mvn.tgz

RUN groupadd --gid ${USER_GID} ${USERNAME} \
    && useradd --uid ${USER_UID} --gid ${USER_GID} --create-home \
        --shell /bin/bash ${USERNAME}

# Node from the official glibc-built image — avoids curl|bash NodeSource script entirely
COPY --from=node-bin /usr/local/bin/node /usr/local/bin/
COPY --from=node-bin /usr/local/lib/node_modules/npm /usr/local/lib/node_modules/npm
RUN ln -s /usr/local/lib/node_modules/npm/bin/npm-cli.js /usr/local/bin/npm

USER ${USERNAME}

ENV NPM_CONFIG_PREFIX=/home/${USERNAME}/.npm-global
ENV PATH=/home/${USERNAME}/.npm-global/bin:${PATH}

# Pin agent version for reproducible sandbox builds; cache is discarded with this stage
RUN npm install -g --no-audit --no-fund \
    @kilocode/cli@7.4.7 \
    && npm cache clean --force

# ---- Runtime: JDK + apt dev/debug tools + copied Node/npm toolchain -------
FROM eclipse-temurin:21-jdk-jammy AS runtime

ARG USERNAME=developer
ARG USER_UID=1000
ARG USER_GID=1000

# System + debug/diagnostic tools; procps/lsof/net-tools/iputils-ping support
# JDWP/Node --inspect debugging via the exposed ports
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        git curl unzip ripgrep jq bash procps lsof net-tools iputils-ping \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd --gid ${USER_GID} ${USERNAME} \
    && useradd --uid ${USER_UID} --gid ${USER_GID} --create-home \
        --shell /bin/bash ${USERNAME}

# Pre-create + own credential dir BEFORE switching user, so Docker seeds
# fresh named volumes with correct ownership on first mount
RUN mkdir -p /home/${USERNAME}/.kilocode \
    && chown -R ${USERNAME}:${USERNAME} /home/${USERNAME}

# Copy pre-built Node/npm toolchain + global npm packages from builder (npm cache excluded)
COPY --from=builder /usr/local/bin/node /usr/local/bin/node
COPY --from=builder /usr/local/lib/node_modules/npm /usr/local/lib/node_modules/npm
COPY --from=builder --chown=${USERNAME}:${USERNAME} /home/${USERNAME}/.npm-global /home/${USERNAME}/.npm-global
COPY --from=builder /opt/maven /opt/maven
RUN ln -s /usr/local/lib/node_modules/npm/bin/npm-cli.js /usr/local/bin/npm

USER ${USERNAME}

# Configure npm global install without root
ENV NPM_CONFIG_PREFIX=/home/${USERNAME}/.npm-global
ENV PATH=/home/${USERNAME}/.npm-global/bin:/opt/maven/bin:${PATH}

WORKDIR /workspace

CMD ["bash"]
