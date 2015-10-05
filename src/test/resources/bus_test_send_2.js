var bus = require('vertx-js/bus');
bus.send("the_address", {"body":"the_message"}, { "the_header_name": "the_header_value" });
