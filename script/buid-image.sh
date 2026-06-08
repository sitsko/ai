#!/bin/bash
cd ..
# --- VARIABLES ---
# The name of your Dockerfile
DOCKERFILE="src/main/docker/Dockerfile.jvm"

# The desired name and tag for your Podman image
IMAGE_NAME="ai-image"
IMAGE_TAG="latest"

# --- MAIN SCRIPT ---

# --- PODMAN BUILD COMMAND ---
echo "Building Podman image '${IMAGE_NAME}:${IMAGE_TAG}'..."

# Run the podman build command
# The . at the end specifies the build context (the current directory)
podman build -t "${IMAGE_NAME}:${IMAGE_TAG}" -f "$DOCKERFILE" .

# Check the exit status of the build command
if [ $? -eq 0 ]; then
echo "✅ Podman image build successful!"
else
echo "❌ Podman image build failed."
exit 1
fi

