# ============================================================================
# ai-sandbox-kile.dockerfile
# Multi-stage dev sandbox for running, compiling, and debugging Java/Node apps.
# Keep the full JDK in runtime (compile + JDWP debug require it).
# Version pinning makes builds reproducible.
# ============================================================================

# ---- Builder: install toolchain + AI agents, warm caches -------------------
FROM eclipse-temurin:21.0.7_6-jdk-jammy AS builder

ARG NODE_VERSION=22.14.0
ARG MAVEN_VERSION=3.9.9
ARG USERNAME=developer
ARG USER_UID=1000
ARG USER_GID=1000

# Minimal fetch tools only (no recommends); cleaned in the same layer
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        curl wget ca-certificates bash \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Node via official binary tarball (avoids NodeSource apt repo + GPG bloat)
RUN ARCH=$(uname -m | sed 's/x86_64/x64/;s/aarch64/arm64/') \
    && wget -q "https://nodejs.org/dist/v${NODE_VERSION}/node-v${NODE_VERSION}-linux-${ARCH}.tar.xz" -O /tmp/node.tar.xz \
    && tar -xJf /tmp/node.tar.xz -C /opt \
    && mv /opt/node-v${NODE_VERSION}-linux-${ARCH} /opt/node \
    && rm /tmp/node.tar.xz

# Maven via official binary tarball (avoids apt maven + its dependencies)
RUN wget -q "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -O /tmp/mvn.tgz \
    && tar -xzf /tmp/mvn.tgz -C /opt \
    && rm /tmp/mvn.tgz

RUN groupadd --gid ${USER_GID} ${USERNAME} \
    && useradd --uid ${USER_UID} --gid ${USER_GID} --create-home \
        --shell /bin/bash ${USERNAME}

USER ${USERNAME}
ENV PATH="/opt/node/bin:/opt/maven/bin:${PATH}"
ENV NPM_CONFIG_PREFIX=/home/${USERNAME}/.npm-global
ENV PATH="/home/${USERNAME}/.npm-global/bin:${PATH}"

# Pin agent versions for reproducible, stable sandbox builds
RUN npm install -g --no-audit --no-fund \
        @kilocode/cli@0.1.0 @anthropic-ai/claude-code@1.0.0 \
    && npm cache clean --force

# ---- Runtime: lean image = JDK + copied toolchain + debug tools -----------
FROM eclipse-temurin:21.0.7_6-jdk-jammy AS runtime

ARG USERNAME=developer
ARG USER_UID=1000
ARG USER_GID=1000

# Debug/diagnostic helpers: jps/jstack (procps), lsof, net tools, ping
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        git ripgrep jq bash procps lsof net-tools iputils-ping \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid ${USER_GID} ${USERNAME} \
    && useradd --uid ${USER_UID} --gid ${USER_GID} --create-home \
        --shell /bin/bash ${USERNAME}

# Copy pre-built toolchain + global npm packages from builder
COPY --from=builder /opt/node  /opt/node
COPY --from=builder /opt/maven /opt/maven
COPY --from=builder /home/${USERNAME}/.npm-global /home/${USERNAME}/.npm-global

USER ${USERNAME}
ENV PATH="/opt/node/bin:/opt/maven/bin:/home/${USERNAME}/.npm-global/bin:${PATH}"
ENV NPM_CONFIG_PREFIX=/home/${USERNAME}/.npm-global

WORKDIR /workspace

LABEL org.opencontainers.image.title="ai-sandbox" \
      org.opencontainers.image.description="AI coding-agent sandbox (run/compile/debug)"

CMD ["bash"]
