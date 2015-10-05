var eb = require('vertx-js/bus');
var TestService = require('test-js/test_service-proxy');
var testService = new TestService(eb, 'someaddress');

testService.basicTypes("foo", 123, 1234, 12345, 123456, 12.34, 12.3456, 'X', true);


