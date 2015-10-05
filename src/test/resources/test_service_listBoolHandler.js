var eb = require('vertx-js/bus');
var TestService = require('test-js/test_service-proxy');
var testService = new TestService(eb, 'someaddress');

testService.listBoolHandler(function(err, res) {
  if (err !== undefined) {
    vertx.eventBus().send("testaddress", "unexpected failure " + err);
  } else if (res[0] != true && res[1] != false && res[2] != true) {
    vertx.eventBus().send("testaddress", "unexpected result " + res);
  } else {
    vertx.eventBus().send("testaddress", "ok");
  }
});


