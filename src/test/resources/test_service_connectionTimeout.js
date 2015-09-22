var eb = require('vertx-js/bus');
var TestService = require('test-js/test_service-proxy');
var testService = new TestService(eb, 'someaddress');

testService.createConnection("foo", function(conn, err) {
  if (conn == null || err !== undefined) {
    vertx.eventBus().send("testaddress", "unexpected create connection error");
  } else {
    conn.startTransaction(function(startRes, startErr) {
      if (startErr !== undefined) {
        vertx.eventBus().send("testaddress", "unexpected start transaction error: " + startErr);
      } else if (startRes != "foo") {
        vertx.eventBus().send("testaddress", "unexpected start transaction result: " + startRes);
      } else {
        vertx.eventBus().consumer("closeCalled", function(msg) {
          if (msg.body() != "blah") {
            vertx.eventBus().send("testaddress", "fail");
          } else {
            conn.startTransaction(function(startRes2, startErr2) {
              if (startErr2 === undefined || startErr2.failureType != "NO_HANDLERS") {
                vertx.eventBus().send("testaddress", "was expecting NO_HANDLERS failure instead of " + startErr2.failureType);
              } else {
                vertx.eventBus().send("testaddress", "ok");
              }
            });
          }
        });
      }
    });
  }
});


