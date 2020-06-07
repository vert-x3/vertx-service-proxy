{
  try {
    JsonObject json = msg.body();
    String action = msg.headers().get("action");
    if (action == null) throw new IllegalStateException("action not specified");
    accessed();
    switch (action) {
  $L
      default: throw new IllegalStateException("Invalid action: " + action);
    }
  } catch (Throwable t) {
      if (includeDebugInfo) msg.reply(new ServiceException(500, t.getMessage(), $T.generateDebugInfo(t)));
      else msg.reply(new ServiceException(500, t.getMessage()));
      throw t;
  }
}
