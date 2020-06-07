res -> {
  if (res.failed()) {
    if (res.cause() instanceof ServiceException) {
      msg.reply(res.cause());
    } else {
      msg.reply(new ServiceException(-1, res.cause().getMessage()));
    }
  } else {
    msg.reply(new JsonObject(res.result().entrySet().stream().collect($T.toMap($T.Entry::getKey,e->$L))));
  }
}
