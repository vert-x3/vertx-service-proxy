package io.vertx.serviceproxy.impl.utils;

import io.vertx.core.Future;
import io.vertx.serviceproxy.AuthenticationInterceptor;
import io.vertx.serviceproxy.AuthorizationInterceptor;
import io.vertx.serviceproxy.InterceptorHolder;
import io.vertx.serviceproxy.ServiceInterceptor;
import io.vertx.serviceproxy.impl.InterceptorPriority;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class InterceptorUtilsTest {

  @Test
  public void testGetWeightAuthn() {
    ServiceInterceptor authn = new AuthenticationInterceptor();
    assertEquals(InterceptorPriority.AUTHN, InterceptorUtils.getWeight(authn));
  }

  @Test
  public void testGetWeightAuthz() {
    ServiceInterceptor authz = new AuthorizationInterceptor();
    assertEquals(InterceptorPriority.AUTHZ, InterceptorUtils.getWeight(authz));
  }

  @Test
  public void testGetWeightUser() {
    ServiceInterceptor user = (context, msg) -> Future.succeededFuture();
    assertEquals(InterceptorPriority.USER, InterceptorUtils.getWeight(user));
  }

  @Test
  public void testCheckInterceptorOrderFromEmptyList() {
    List<InterceptorHolder> interceptorHolders = new ArrayList<>();
    ServiceInterceptor authn = new AuthenticationInterceptor();
    InterceptorUtils.checkInterceptorOrder(interceptorHolders, authn);
    interceptorHolders.clear();
    ServiceInterceptor authz = new AuthorizationInterceptor();
    InterceptorUtils.checkInterceptorOrder(interceptorHolders, authz);
    interceptorHolders.clear();
    ServiceInterceptor user = (context, msg) -> Future.succeededFuture();
    InterceptorUtils.checkInterceptorOrder(interceptorHolders, user);
  }

  @Test
  public void testCheckInterceptorOrderRightPriority() {
    List<InterceptorHolder> interceptorHolders = new ArrayList<>();
    ServiceInterceptor authn = new AuthenticationInterceptor();
    InterceptorUtils.checkInterceptorOrder(interceptorHolders, authn);
    interceptorHolders.add(new InterceptorHolder(authn));
    ServiceInterceptor authz = new AuthorizationInterceptor();
    InterceptorUtils.checkInterceptorOrder(interceptorHolders, authz);
    interceptorHolders.add(new InterceptorHolder(authz));
    ServiceInterceptor user = (context, msg) -> Future.succeededFuture();
    InterceptorUtils.checkInterceptorOrder(interceptorHolders, user);
  }

  @Test
  public void testCheckInterceptorOrderWrongPriority() {
    List<InterceptorHolder> interceptorHolders = new ArrayList<>();
    ServiceInterceptor authz = new AuthorizationInterceptor();
    interceptorHolders.add(new InterceptorHolder(authz));
    ServiceInterceptor authn = new AuthenticationInterceptor();
    assertThrows(IllegalStateException.class, () ->
      InterceptorUtils.checkInterceptorOrder(interceptorHolders, authn));
    interceptorHolders.clear();
    ServiceInterceptor user = (context, msg) -> Future.succeededFuture();
    interceptorHolders.add(new InterceptorHolder(user));
    assertThrows(IllegalStateException.class, () ->
      InterceptorUtils.checkInterceptorOrder(interceptorHolders, authn));
    assertThrows(IllegalStateException.class, () ->
      InterceptorUtils.checkInterceptorOrder(interceptorHolders, authz));
  }
}
