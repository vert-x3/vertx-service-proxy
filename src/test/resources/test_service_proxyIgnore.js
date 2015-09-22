var eb = require('vertx-js/bus');
var TestService = require('test-js/test_service-proxy');
var testService = new TestService(eb, 'someaddress');

if (testService.proxyIgnore !== undefined) {
  vertx.eventBus().send("testaddress", "proxy ignore method not ignored");
} else {
  vertx.eventBus().send("testaddress", "ok");
}



