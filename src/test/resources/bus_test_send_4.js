var bus = require('vertx-js/bus');
bus.send("the_address", {"body": "the_message"}, function (err, res) {
  bus.send("the_address_fail", {"body": "the_message"}, function (err, res) {
    if (err) {
      bus.send("done", {"body": "ok"});
    }
  });
});
