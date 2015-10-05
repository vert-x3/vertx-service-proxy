var eb = require('vertx-js/bus');
var TestService = require('test-js/test_service-proxy');
var testService = new TestService(eb, 'someaddress');

testService.dataObjectType({
  "string": "foo",
  "number":123,
  "bool":true
});


