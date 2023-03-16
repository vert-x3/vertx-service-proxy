package examples;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class SomeDatabaseServiceImpl implements SomeDatabaseService {

  @Override
  public Future<Void> save(String collection, JsonObject document) {
    return null;
  }

  @Override
  public Future<Void> foo(String collection, JsonObject document) {
    return null;
  }
}
