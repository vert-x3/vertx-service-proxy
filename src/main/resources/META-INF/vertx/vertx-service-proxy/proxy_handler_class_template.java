/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package $Package$;

import examples.SomeDatabaseService;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.HelperUtils;
import io.vertx.serviceproxy.ProxyHandler;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;
import java.lang.Override;

/*
  Generated Proxy code - DO NOT EDIT
  @author Roger the Robot
*/
@SuppressWarnings({"unchecked", "rawtypes"})
public class $TypeName$ extends ProxyHandler {

  public static final long DEFAULT_CONNECTION_TIMEOUT = 300;

  private final Vertx vertx;
  private final $ServiceName$ service;
  private final long timerID;
  private long lastAccessed;
  private final long timeoutSeconds;
  private final boolean includeDebugInfo;

  public $TypeName$(Vertx vertx, $ServiceName$ service) {
    this(vertx, service, DEFAULT_CONNECTION_TIMEOUT);
  }

  public $TypeName$(Vertx vertx, $ServiceName$ service,
    long timeoutInSecond) {
    this(vertx, service, true, timeoutInSecond);
  }

  public $TypeName$(Vertx vertx, $ServiceName$ service,
    boolean topLevel, long timeoutInSecond) {
    this(vertx, service, true, timeoutInSecond, false);
  }

  public $TypeName$(Vertx vertx, $ServiceName$ service,
    boolean topLevel, long timeoutSeconds, boolean includeDebugInfo) {
    this.vertx = vertx;
    this.service = service;
    this.includeDebugInfo = includeDebugInfo;
    this.timeoutSeconds = timeoutSeconds;

    try {
      this.vertx.eventBus().registerDefaultCodec(ServiceException.class, new ServiceExceptionMessageCodec());
    } catch(IllegalStateException ex) { }

    if(timeoutSeconds != -1 && !topLevel) {
      long period = timeoutSeconds * 1000 / 2;
      if (period > 10000) {
        period = 10000;
      }
      this.timerID = vertx.setPeriodic(period,this::checkTimedOut);
    } else {
      this.timerID = -1;
    }

    accessed();
  }

  void checkTimedOut(long id) {
    long now=System.nanoTime();
    if(now - lastAccessed > timeoutSeconds * 1000000000) {
      $CheckedTimedOutImpl$;
    }
  }

  @Override
  public void close() {
    if (timerID != -1) {
      vertx.cancelTimer(timerID);
    }
    super.close();
  }

  private void accessed() {
    this.lastAccessed = System.nanoTime();
  }

  @Override
  public void handle(Message<JsonObject> msg) {
    $HandleImplementation$;
  }

}
