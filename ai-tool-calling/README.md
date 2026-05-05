# ai-tool-calling 

Demo project to show how to work with the AI Tool Calling feature in OpenAI API using Quarkus.
Provided examples related high level interaction between application and AI by LangChain4J.
Some examples of structured, unstructured and functional calling.

## Setup

Create a `.env` file in the root of the project with the following content:
```properties
AI_APP_PORT=8080

# Provider: openai | ollama | anthropic
AI_MODEL_PROVIDER=openai
OPEN_AI_TOKEN=<your-openai-token>

# Required when AI_MODEL_PROVIDER=anthropic
# ANTHROPIC_API_KEY=<your-anthropic-key>

# Required when AI_MODEL_PROVIDER=ollama
# OLLAMA_BASE_URL=http://localhost:11434
```

make sure that you have `Java 21` JDK.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
# OpenAI (default)
./mvnw quarkus:dev

# Ollama
./mvnw quarkus:dev -P ollama

# Anthropic Claude
./mvnw quarkus:dev -P anthropic
```


## REST

Available endpoints 
```plaintext
/api/vessels/forecast
/api/vessels/{owner}/count
/api/vessels/{owner}/heavy
/api/lists/{listId}
/api/lists/{listId}/customers/{id}
```

some examples of `GET` requests placed in `script/http/*`.
