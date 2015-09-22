var eb = require('vertx-js/bus');
var TestService = require('test-js/test_service-proxy');
var testService = new TestService(eb, 'someaddress');

testService.mapParams(
  {"eek":"foo","wob":"bar"},
  {"eek":12,"wob":13},
  {"eek":123,"wob":134},
  {"eek":1234,"wob":1235},
  {"eek":12345,"wob":12356},
  {"eek":{"foo":"bar"},"wob":{"blah":"eek"}},
  {"eek":["foo"],"wob":["blah"]}
);


