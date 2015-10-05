var eb = require('vertx-js/bus');
var TestService = require('test-js/test_service-proxy');
var testService = new TestService(eb, 'someaddress');

testService.jsonObjectHandler(function(err, res) {
  if (err !== undefined) {
    vertx.eventBus().send("testaddress", "unexpected failure " + err);
  } else if (res.blah != 'wibble') {
    vertx.eventBus().send("testaddress", "unexpected result " + res);
  } else {
    vertx.eventBus().send("testaddress", "ok");
  }
});


