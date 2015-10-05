var eb = require('vertx-js/bus');
var TestService = require('test-js/test_service-proxy');
var testService = new TestService(eb, 'someaddress');

testService.createConnection("foo", function(err, conn) {
  if (conn == null || err !== undefined) {
    vertx.eventBus().send("testaddress", "unexpected create connection error");
  } else {
    conn.startTransaction(function(startErr, startRes) {
      if (startRes != "foo" || startErr !== undefined) {
        vertx.eventBus().send("testaddress", "unexpected start transaction error: " + startRes);
      } else {
        conn.insert("blah", {}, function(insertErr, insertRes) {
          if (insertRes != "foo" || insertErr !== undefined) {
            vertx.eventBus().send("testaddress", "unexpected insert error: " + insertRes);
          } else {
            conn.commit(function(commitErr, commitRes) {
              if (commitRes != "foo" || commitErr !== undefined) {
                vertx.eventBus().send("testaddress", "unexpected commit error: " + commitRes);
              } else {
                conn.close();
                try {
                  conn.startTransaction(function() {});
                } catch(err) {
                  // Expected
                  vertx.eventBus().send("testaddress", "ok");
                }
              }
            });
          }
        });
      }
    });
  }
});


