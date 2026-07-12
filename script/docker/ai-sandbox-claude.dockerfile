FROM node:22-alpine AS node-bin

FROM eclipse-temurin:21-jdk-alpine

ARG USERNAME=developer
ARG USER_UID=1000
ARG USER_GID=1000

# System tools (musl-compatible; Alpine has no bash by default, unlike Debian)
RUN apk add --no-cache \
    git curl unzip ripgrep jq maven bash

# Node from the official musl-built image — avoids curl|bash NodeSource script entirely
COPY --from=node-bin /usr/local/bin/node /usr/local/bin/
COPY --from=node-bin /usr/local/lib/node_modules/npm /usr/local/lib/node_modules/npm
RUN ln -s /usr/local/lib/node_modules/npm/bin/npm-cli.js /usr/local/bin/npm

# Non-root user via busybox tools (no `shadow` package needed)
RUN addgroup -g ${USER_GID} ${USERNAME} \
    && adduser -D -u ${USER_UID} -G ${USERNAME} -h /home/${USERNAME} ${USERNAME}

# Pre-create + own credential dirs BEFORE switching user, so Docker seeds
# fresh named volumes with correct ownership on first mount
RUN mkdir -p /home/${USERNAME}/.claude /home/${USERNAME}/.kilocode /home/${USERNAME}/.config/kilo \
    && chown -R ${USERNAME}:${USERNAME} /home/${USERNAME}

COPY --chown=${USERNAME}:${USERNAME} ai-sandbox-claude-entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

USER ${USERNAME}

# Configure npm global install without root
ENV NPM_CONFIG_PREFIX=/home/${USERNAME}/.npm-global
ENV PATH=/home/${USERNAME}/.npm-global/bin:${PATH}

# Install AI coding agents (pinned for reproducibility)
RUN npm install -g \
    @kilocode/cli@7.4.5 \
    @anthropic-ai/claude-code@2.1.198

WORKDIR /workspace

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["bash"]
