var bus = require('vertx-js/bus');
bus.send("the_address", {"body":"the_message"});
