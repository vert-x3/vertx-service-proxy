var eb = require('vertx-js/bus');
var TestService = require('test-js/test_service-proxy');
var testService = new TestService(eb, 'someaddress');

var s = null;
vertx.eventBus().consumer("fluentReceived").handler(function(res, err) {
  if (s == testService) {
    vertx.eventBus().send("testaddress", "ok");
  } else {
    vertx.eventBus().send("testaddress", "not fluent");
  }
});
s = testService.fluentNoParams();
