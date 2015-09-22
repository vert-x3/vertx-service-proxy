var eb = require('vertx-js/bus');
var TestService = require('test-js/test_service-proxy');
var testService = new TestService(eb, 'someaddress');

testService.listDoubleHandler(function(res, err) {
  if (err !== undefined) {
    vertx.eventBus().send("testaddress", "unexpected failure " + err);
  } else if (Math.abs(res[0] - 1.11) > 0.01 && Math.abs(res[1] - 1.12) > 0.01 && Math.abs(res[2] - 1.13) > 0.01) {
    vertx.eventBus().send("testaddress", "unexpected result " + res);
  } else {
    vertx.eventBus().send("testaddress", "ok");
  }
});


