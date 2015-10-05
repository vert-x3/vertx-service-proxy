var eb = require('vertx-js/bus');
var TestService = require('test-js/test_service-proxy');
var testService = new TestService(eb, 'someaddress');

try {
  testService.noParams("should_not_be_here");
} catch (error) {
  if (error.message == 'function invoked with invalid arguments') {
    vertx.eventBus().send("testaddress", "ok");
  } else {
    vertx.eventBus().send("testaddress", "should have raised an invalid arguments error");
  }
}


