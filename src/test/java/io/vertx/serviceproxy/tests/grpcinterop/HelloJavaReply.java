package io.vertx.serviceproxy.tests.grpcinterop;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class HelloJavaReply {

  private String message;

  public HelloJavaReply() {
  }

  public HelloJavaReply(JsonObject json) {
    this.message = json.getString("message");
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    if (message != null) {
      json.put("message", message);
    }
    return json;
  }

  public String getMessage() {
    return message;
  }

  public HelloJavaReply setMessage(String message) {
    this.message = message;
    return this;
  }
}
