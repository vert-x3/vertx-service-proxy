var eb = require('vertx-js/bus');
var TestService = require('test-js/test_service-proxy');
var testService = new TestService(eb, 'someaddress');

testService.createConnectionWithCloseFuture(function(conn, err) {
  if (err !== undefined) {
    vertx.eventBus().send("testaddress", "unexpected create connection error");
  } else {
    vertx.eventBus().consumer("closeCalled", function(msg) {
      if (msg.body() != "blah") {
        vertx.eventBus().send("testaddress", "unexpected close called message");
      } else {
        conn.someMethod(function(someMethodRes, someMethodErr) {
          if (someMethodErr === undefined || someMethodErr.failureType != "NO_HANDLERS") {
            vertx.eventBus().send("testaddress", "was expecting NO_HANDLERS failure instead of " + someMethodErr.failureType);
          } else {
            vertx.eventBus().send("testaddress", "ok");
          }
        });
      }
    });
  }
});


