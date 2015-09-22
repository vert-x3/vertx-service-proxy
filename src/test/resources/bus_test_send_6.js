var bus = require('vertx-js/bus');
bus.send("the_address", {"body":"the_message"}, { "headers": { "the_header_name": "the_header_value" } },
  function() {
    bus.send("the_address_fail", {"body":"the_message"}, { "headers": { "the_header_name": "the_header_value_fail" } }, function() {}, function() {
      bus.send("done", {"body":"ok"});
    });
  }, function() {});
