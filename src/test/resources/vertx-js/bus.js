/*
 *   Copyright (c) 2011-2015 The original author or authors
 *   ------------------------------------------------------
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *
 *       The Eclipse Public License is available at
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *       The Apache License v2.0 is available at
 *       http://www.opensource.org/licenses/apache2.0.php
 *
 *   You may elect to redistribute this code under either of these licenses.
 */

var bus = {};

function wrapBody(body) {
  var json = JSON.stringify(body);
  return new Packages.io.vertx.core.json.JsonObject(json);
}

function unwrapMsg(msg) {
  var json = new Packages.io.vertx.core.json.JsonObject();
  var headers = new Packages.io.vertx.core.json.JsonObject();
  var msgHeaders = msg.headers();
  for (var i = msgHeaders.names().iterator();i.hasNext();) {
    var headerName = i.next();
    headers.put(headerName, msgHeaders.get(headerName));
  }
  json.put("body", msg.body());
  json.put("headers", headers);
  return JSON.parse(json.encode());
}

function wrapHeaders(headers) {
  var ret = new Packages.io.vertx.core.eventbus.DeliveryOptions();
  if (typeof headers !== 'undefined') {
    for (var name in headers) {
      if (headers.hasOwnProperty(name)) {
        ret.addHeader(name, headers[name]);
      }
    }
  }
  return ret;
}

function unwrapError(err) {
  return {
    "failureType" : err.failureType().name(),
    "failureCode" : err.failureCode(),
    "message": err.getMessage()
  }
}

bus.send = function (/*address, message, headers, replyHandler, failureHandler*/) {
  var eb = vertx._jdel.eventBus();
  var arity = arguments.length;
  if (arity < 2) {
    throw new Error('At least 2 arguments [address, message] are required!');
  } else if (arity === 2) {
    eb.send(arguments[0], wrapBody(arguments[1]));
  } else if (arity === 3) {
    if (typeof arguments[2] === 'function') {
      var replyHandler = arguments[2];
      eb.send(arguments[0], wrapBody(arguments[1]), function (ar) {
        if (ar.succeeded()) {
          replyHandler(unwrapMsg(ar.result()));
        }
      });
    } else {
      eb.send(arguments[0], wrapBody(arguments[1]), wrapHeaders(arguments[2]));
    }
  } else if (arity === 4) {
    if (typeof arguments[2] === 'function' && typeof arguments[3] === 'function') {
      var replyHandler = arguments[2];
      var failureHandler = arguments[3];
      eb.send(arguments[0], wrapBody(arguments[1]), function (ar) {
        if (ar.succeeded()) {
          replyHandler(unwrapMsg(ar.result()));
        } else {
          failureHandler(unwrapError(ar.cause()));
        }
      });
    } else if (typeof arguments[3] === 'function') {
      var replyHandler = arguments[3];
      eb.send(arguments[0], wrapBody(arguments[1]), wrapHeaders(arguments[2]), function (ar) {
        if (ar.succeeded()) {
          replyHandler(unwrapMsg(ar.result()));
        }
      });
    }
  } else {
    var replyHandler = arguments[3];
    var failureHandler = arguments[4];
    eb.send(arguments[0], wrapBody(arguments[1]), wrapHeaders(arguments[2]), function (ar) {
      if (ar.succeeded()) {
          replyHandler(unwrapMsg(ar.result()));
      } else {
        failureHandler(unwrapError(ar.cause()));
      }
    });
  }
  return bus;
};

module.exports = bus;