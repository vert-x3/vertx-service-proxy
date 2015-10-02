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

bus.send = function (address, message, headers, callback) {

  var arity = arguments.length;

  if (arity < 2) {
    throw new Error('At least 2 arguments [address, message] are required!');
  }

  var eb = vertx._jdel.eventBus();

  message = wrapBody(message);

  if (typeof headers === 'function') {
    callback = headers;
    headers = null;
  }

  if (headers) {
    // wrap it in a delivery options
    headers = wrapHeaders(headers);
  }

  var handler = function (ar) {
    if (callback) {
      var err, result;
      if (ar.failed()) {
        err = unwrapError(ar.cause());
      }
      if (ar.succeeded()) {
        result = unwrapMsg(ar.result()).body;
      }

      callback(err, result);
    }
  };

  if (headers) {
    eb.send(address, message, headers, handler);
  } else {
    eb.send(address, message, handler);
  }

  return bus;
};

module.exports = bus;