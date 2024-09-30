package io.vertx.serviceproxy.tests.testmodel;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;

/**
 * @author <a href="mailto:oreilldf@gmail.com">Dan O'Reilly</a>
 */
public class MyServiceException extends ServiceException {
  private final String extra;
  /**
   * Create a ServiceException.
   *
   * @param failureCode The failure code.
   * @param message     The failure message.
   */
  public MyServiceException(int failureCode, String message, String extra) {
    super(failureCode, message);
    this.extra = extra;
  }

  public MyServiceException(int failureCode, String message, JsonObject debugInfo, String extra) {
    super(failureCode, message, debugInfo);
    this.extra = extra;
  }

  /**
   * Get the extra data
   *
   * @return The extra data
   */
  public String getExtra() {
    return extra;
  }

  /**
   * Wrap and MyServiceException in a failed Future and return it.
   *
   * @param failureCode The failure code.
   * @param message     The failure message.
   * @param extra       The extra data
   * @param <T>         The type returned if the Future succeeds.
   * @return The MyServiceException wrapped in a failed future.
   */
  public static <T> Future<T> fail(int failureCode, String message, String extra) {
    return Future.failedFuture(new MyServiceException(failureCode, message, extra));
  }
}
