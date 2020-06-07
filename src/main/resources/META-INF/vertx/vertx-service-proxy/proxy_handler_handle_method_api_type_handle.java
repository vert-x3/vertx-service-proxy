res -> {
  if (res.failed()) {
    HelperUtils.manageFailure(msg,res.cause(),includeDebugInfo);
  } else {
    String proxyAddress= $T.randomUUID().toString();
    new $T(vertx).setAddress(proxyAddress).setTopLevel(false).setTimeoutSeconds(timeoutSeconds).register($L.class, res.result());
    msg.reply(null, new $T().addHeader("proxyaddr", proxyAddress));
  }
}
