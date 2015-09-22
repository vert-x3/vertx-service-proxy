var eb = require('vertx-js/bus');
var TestService = require('test-js/test_service-proxy');
var testService = new TestService(eb, 'someaddress');

testService.failingMethod(function(res, err) {
  if (err === undefined || err == null) {
    vertx.eventBus().send("testaddress", "unexpected failure " + err);
  } else if (res != null) {
    vertx.eventBus().send("testaddress", "unexpected result " + res);
  } else if (err.failureType != 'RECIPIENT_FAILURE') {
    vertx.eventBus().send("testaddress", "unexpected err type  " + err.failureType);
  } else if (err.message != 'wibble') {
    vertx.eventBus().send("testaddress", "unexpected err type  " + err.message);
  } else {
    vertx.eventBus().send("testaddress", "ok");
  }
});