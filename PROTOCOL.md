#  Service RPC Protocol

The protocol defines interactions on top of the event-bus transport.

## Service

A service is an event-bus message handler bound to a specific user defined address called
*service address*.

## Client

A client interact by exchanging messages with a service using the service address.

## Interactions

The following interactions are expected

- *send* : the client sends a message to the service
- *request/response* : the client sends a message to the service and expects an asynchronous response

## General message format

### Initiating message

The client will always initiate an interaction with a service by sending a message to the service address following this format:

* `action` message header: a mandatory specific identifier that the service will process, the typical usage is to map to language method or function.
Such action is used by the service implementation to dispatch and route the message to the processing part.
* the message body is a json object, the typical usage is to map each entry to a method argument using the json object key, the mapping
of values depends on the implementation of the processor. The client and service must agree on the json object format
prior exchanging messages, any incorrect message will result in processing errors.

### Reply message

When the client expects a response, it will use the request/response pattern and expect a response.

#### Data response

The service can respond with plain data in JSON format, no message header is used.

The client and service must agree on the json format prior exchanging messages, any incorrect message will result in
processing errors.

#### Failure response

The service can respond with a failure by sending a failure response

* no message headers
* the message body is a reply exception

#### Bind response

The service can bind a new service and respond with this specific service address.

* `proxyaddr` message header: the service address that was bound that the client can use for interacting with this new service
* message body is the `null` value
