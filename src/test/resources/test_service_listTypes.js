var eb = require('vertx-js/bus');
var TestService = require('test-js/test_service-proxy');
var testService = new TestService(eb, 'someaddress');

testService.listParams(
  ["foo","bar"],
  [12,13],
  [123, 134],
  [1234, 1235],
  [12345, 12346],
  [{"foo":"bar"},{"blah":"eek"}],
  [["foo"],["blah"]],
  [{"number":1,"string":"String 1","bool":false},{"number":2,"string":"String 2","bool":true}]
);


