res -> {
  if (res.failed()) {
    HelperUtils.manageFailure(msg, res.cause(), includeDebugInfo);
  } else {
    msg.reply(new $T(res.result().stream().map(v -> $L).collect($T.toList())));}
}
