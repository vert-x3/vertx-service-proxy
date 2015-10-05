var eb = require('vertx-js/bus');
var TestService = require('test-js/test_service-proxy');
var testService = new TestService(eb, 'someaddress');

var s = testService.fluentMethod("foo", function(err, res) {
  if (err !== undefined) {
    vertx.eventBus().send("testaddress", "unexpected failure " + err);
  } else if (res != 'bar') {
    vertx.eventBus().send("testaddress", "unexpected result " + res);
  } else if (s != testService) {
    vertx.eventBus().send("testaddress", "no fluent");
  } else {
    vertx.eventBus().send("testaddress", "ok");
  }
});