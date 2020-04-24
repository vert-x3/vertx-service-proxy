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

## Streaming protocol (WIP)

Point to point bidirectional message channels on top of the event-bus.

### Architecture

* a service binds a channel handler at a service address
* a client open a channel at a service address
* the client and server uses ephemeral event-bus addresses
* an opening handshake will setup the message channel between both parties
* the message channel can be unidirectional or bidirectional

### Terminology

- `local address`
- `remote address`
- ...

### Client to service channel

The service processes messages sent by the client.

#### Opening handshake

- the service creates an event-bus handler at the service address
- the client creates an ephemeral local address for processing server messages (metadata such as flow control)
- the client sends an opening handshake message to the service address setting a reply handler to process the service response
  - setting the `__vertx.stream.address` header to indicate the client local address
  - with an unspecified body
- upon reception of the message the service can accept the request or reject it by sending a failure response
- when the service accepts the request it creates an ephemeral local address for processing client messages
- and then replies to the opening handshake message
  - settings the `__vertx.stream.address` header to indicate the server local address
  - with a unspecified body

#### Communication

- the client setup an initial amount of credits to establish flow control
- the client sends message to the service
- when the client sends a message it decrements the credits
- the service can at any time sends a message to the client to refund credits
  - setting the `_______vertx.stream.credits` header to indicate the amount of credits
  - with an unspecified body

### Service to client channel

Similar to the client to service

The client setup a local address handler and sends the address to the service, that will bind a local address handler to
process flow control messages.

### Duplex channel

A combination of the two above.

### Todo

- closing handshake
- error handling
- timeouts

