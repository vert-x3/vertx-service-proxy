var bus = require('vertx-js/bus');
bus.send("the_address", {"body":"the_message"},
  function() {
    bus.send("the_address_fail", {"body":"the_message"}, function() {}, function() {
      bus.send("done", {"body":"ok"});
    });
  },
  function() {});
