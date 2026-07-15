FROM node:22-alpine AS node-bin

# ---- Builder: install npm globals in a stage that gets discarded ----------
FROM eclipse-temurin:21-jdk-alpine AS builder

ARG USERNAME=developer
ARG USER_UID=1000
ARG USER_GID=1000

# Minimal toolchain in case npm postinstall scripts need it; discarded with this stage
RUN apk add --no-cache curl ca-certificates bash

RUN addgroup -g ${USER_GID} ${USERNAME} \
    && adduser -D -u ${USER_UID} -G ${USERNAME} -h /home/${USERNAME} ${USERNAME}

# Node from the official musl-built image — avoids curl|bash NodeSource script entirely
COPY --from=node-bin /usr/local/bin/node /usr/local/bin/
COPY --from=node-bin /usr/local/lib/node_modules/npm /usr/local/lib/node_modules/npm
RUN ln -s /usr/local/lib/node_modules/npm/bin/npm-cli.js /usr/local/bin/npm

USER ${USERNAME}

ENV NPM_CONFIG_PREFIX=/home/${USERNAME}/.npm-global
ENV PATH=/home/${USERNAME}/.npm-global/bin:${PATH}

# Pin agent versions for reproducible sandbox builds; cache is discarded with this stage
RUN npm install -g --no-audit --no-fund \
    @kilocode/cli@7.4.5 \
    @anthropic-ai/claude-code@2.1.198 \
    && npm cache clean --force

# ---- Runtime: lean image = JDK + apk dev tools + copied Node/npm toolchain -
FROM eclipse-temurin:21-jdk-alpine AS runtime

ARG USERNAME=developer
ARG USER_UID=1000
ARG USER_GID=1000

# System tools (musl-compatible; Alpine has no bash by default, unlike Debian)
RUN apk add --no-cache \
    git curl unzip ripgrep jq maven bash

# Non-root user via busybox tools (no `shadow` package needed)
RUN addgroup -g ${USER_GID} ${USERNAME} \
    && adduser -D -u ${USER_UID} -G ${USERNAME} -h /home/${USERNAME} ${USERNAME}

# Pre-create + own credential dirs BEFORE switching user, so Docker seeds
# fresh named volumes with correct ownership on first mount
RUN mkdir -p /home/${USERNAME}/.claude /home/${USERNAME}/.kilocode /home/${USERNAME}/.config/kilo \
    && chown -R ${USERNAME}:${USERNAME} /home/${USERNAME}

# Copy pre-built Node/npm toolchain + global npm packages from builder (npm cache excluded)
COPY --from=builder /usr/local/bin/node /usr/local/bin/node
COPY --from=builder /usr/local/lib/node_modules/npm /usr/local/lib/node_modules/npm
COPY --from=builder --chown=${USERNAME}:${USERNAME} /home/${USERNAME}/.npm-global /home/${USERNAME}/.npm-global
RUN ln -s /usr/local/lib/node_modules/npm/bin/npm-cli.js /usr/local/bin/npm

USER ${USERNAME}

# ~/.claude.json lives directly under $HOME (not inside ~/.claude/), but only
# ~/.claude/ is a persisted volume. Symlink it into place at build time — $HOME
# isn't itself persisted, so this only ever needs to exist once per image, and
# ln -s doesn't require the target to exist yet (it resolves once the volume
# is mounted and Claude Code writes into it on first login).
RUN ln -s "/home/${USERNAME}/.claude/.claude.json" "/home/${USERNAME}/.claude.json"

# Configure npm global install without root
ENV NPM_CONFIG_PREFIX=/home/${USERNAME}/.npm-global
ENV PATH=/home/${USERNAME}/.npm-global/bin:${PATH}

WORKDIR /workspace

CMD ["bash"]
