res -> {
  if (res.failed()) {
    HelperUtils.manageFailure(msg, res.cause(), includeDebugInfo);
  } else {
    msg.reply($L);
  }
}
