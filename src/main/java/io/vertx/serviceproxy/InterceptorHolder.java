package io.vertx.serviceproxy;

public class InterceptorHolder {

  private final String action;

  private final ServiceInterceptor interceptor;

  public InterceptorHolder(String action, ServiceInterceptor interceptor) {
    this.action = action;
    this.interceptor = interceptor;
  }

  public InterceptorHolder(ServiceInterceptor interceptor) {
    this.action = null;
    this.interceptor = interceptor;
  }

  public String action() {
    return action;
  }

  public ServiceInterceptor interceptor() {
    return interceptor;
  }
}
