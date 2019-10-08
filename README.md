# Authorizer 

An application which reads a stream of transaction events for a specific account from stdin, validates a set of predefined
rules and write a corresponding stream of events to stdout consisting of the current account state after processing each transaction.

The application is written using Kotlin language and tries to be as much functional as possible. Kotlin was chosen cause it is a modern
OOP and Functional language which improves upon the rich JVM ecosystem with a concise, more intuitive and less verbose syntax as well as a safer 
concurrency model based on coroutines and channels.

# Design 

The application is divided in 3 main modules.

## Model

This package contains the business logic of the application which we are building to satisfy a business goal. It consists of: 
* Data classes used to represent the data which the system operates on.
* Interfaces defining repositories for an account and transactions processed.
* A Transaction processor which handles transaction requests.
* A set of predefined rules which must be checked in order to accept or reject a transaction request.

## Infrastucture

This package contains the repositories implementation and would also contain the Kafka producer and consumer in a real word
scenario. The application modeled the tasks of producing and consuming from a topic using Kotlin Coroutines and Channels.

## App

Here we can find the main function which creates the repositories, the operation and result channels and 3 coroutines. 
One coroutines is responsible for listening to request events on stdin, parsing and putting them in the unbounded operation channel.
The main coroutine then process requests received from the operation channel and send the result to the unbounded result channel.
Anothen coroutine is then responsible for reading the response from the result channel, serialize and write it to stdout.

The above behavior simulates three independent processes where one writes requests to a request topic, scond one consumes 
from this topic, processes it and then write the result to a result topic. Lastly the other consumer receives events from 
this topic and notify clients about the result.

In order to ensure account data consistency we have to guarantee there is only one consumer processing requests for a given account
at any time. In Kakfa it can be done using a key to always send requests for a given account to the same partition. As there is only one
consumer reading from a topic partition in a consumer group, concurrency issues won't happen.

## Prerequisites

* Docker

## Launching the application

In the project root folder execute the following commands

1) `./gradlew clean shadowJar`
2) `docker build -t authorizer .`
3) `cat operations | docker run -i --rm authorizer`

## Running the tests

The test can be run executing the commmand `$ ./gradlew clean test`.

There are unit tests for all specified rules and some integration tests for the whole service.

## Improvements

1) Rewrite the application in Clojure and using more pure functions 
2) A e2e test for the whole application 
