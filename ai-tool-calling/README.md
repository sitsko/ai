# ai-tool-calling 

Demo project to show how to work with the AI Tool Calling feature in OpenAI API using Quarkus.
Provided examples related high level interaction between application and AI by LangChain4J.
Some examples of structured, unstructured and functional calling.

## Setup

Create a `.env` file in the root of the project with the following content:
```properties
AI_APP_PORT=8080

AI_MODEL_PROVIDER=openai
OPEN_AI_TOKEN=<your-openai-token>
```

make sure that you have `Java 21` JDK.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
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
