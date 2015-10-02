var eb = require('vertx-js/bus');
var TestService = require('test-js/test_service-proxy');
var testService = new TestService(eb, 'someaddress');

testService.longDeliverySuccess(function(err, res) {
  if (err !== undefined) {
    vertx.eventBus().send("testaddress", "no err expected");
  } else if (res != 'blah') {
    vertx.eventBus().send("testaddress", "bad result blah != " + res);
  } else {
    vertx.eventBus().send("testaddress", "ok");
  }
});


