var eb = require('vertx-js/bus');
var TestService = require('test-js/test_service-proxy');
var testService = new TestService(eb, 'someaddress');

testService.longDeliveryFailed(function(res, err) {
  if (res != null) {
    vertx.eventBus().send("testaddress", "was expecting null result");
  } else if (err.message != 'Timed out waiting for reply') {
    vertx.eventBus().send("testaddress", "incorrect failure");
  } else {
    vertx.eventBus().send("testaddress", "ok");
  }
});


