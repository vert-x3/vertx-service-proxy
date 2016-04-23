package io.vertx.serviceproxy;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;

/**
 * An Exception to be returned from Service implementations.
 *
 * @author <a href="mailto:oreilldf@gmail.com">Dan O'Reilly</a>
 */
public class ServiceException extends ReplyException {

  /**
   * Create a ServiceException.
   *
   * @param failureCode The failure code.
   * @param message The failure message.
   */
  public ServiceException(int failureCode, String message) {
    super(ReplyFailure.RECIPIENT_FAILURE, failureCode, message);
  }

  /**
   * Create a failed Future containing a ServiceException.
   *
   * @param <T> The type of the AsyncResult.
   * @param failureCode The failure code.
   * @param message The failure message.
   * @return A failed Future containing the ServiceException.
   */
  public static <T> AsyncResult<T> fail(int failureCode, String message) {
    return Future.failedFuture(new ServiceException(failureCode, message));
  }

}
