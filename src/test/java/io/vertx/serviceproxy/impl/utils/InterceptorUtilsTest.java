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
    ServiceInterceptor authn = AuthenticationInterceptor.create(null);
    assertEquals(InterceptorPriority.AUTHENTICATION, InterceptorUtils.getWeight(authn));
  }

  @Test
  public void testGetWeightAuthz() {
    ServiceInterceptor authz = AuthorizationInterceptor.create(null);
    assertEquals(InterceptorPriority.AUTHORIZATION, InterceptorUtils.getWeight(authz));
  }

  @Test
  public void testGetWeightUser() {
    ServiceInterceptor user = (vertx, context, msg) -> Future.succeededFuture();
    assertEquals(InterceptorPriority.USER, InterceptorUtils.getWeight(user));
  }

  @Test
  public void testCheckInterceptorOrderFromEmptyList() {
    List<InterceptorHolder> interceptorHolders = new ArrayList<>();
    ServiceInterceptor authn = AuthenticationInterceptor.create(null);
    InterceptorUtils.checkInterceptorOrder(interceptorHolders, authn);
    interceptorHolders.clear();
    ServiceInterceptor authz = AuthorizationInterceptor.create(null);
    InterceptorUtils.checkInterceptorOrder(interceptorHolders, authz);
    interceptorHolders.clear();
    ServiceInterceptor user = (vertx, context, msg) -> Future.succeededFuture();
    InterceptorUtils.checkInterceptorOrder(interceptorHolders, user);
  }

  @Test
  public void testCheckInterceptorOrderRightPriority() {
    List<InterceptorHolder> interceptorHolders = new ArrayList<>();
    ServiceInterceptor authn = AuthenticationInterceptor.create(null);
    InterceptorUtils.checkInterceptorOrder(interceptorHolders, authn);
    interceptorHolders.add(new InterceptorHolder(authn));
    ServiceInterceptor authz = AuthorizationInterceptor.create(null);
    InterceptorUtils.checkInterceptorOrder(interceptorHolders, authz);
    interceptorHolders.add(new InterceptorHolder(authz));
    ServiceInterceptor user = (vertx, context, msg) -> Future.succeededFuture();
    InterceptorUtils.checkInterceptorOrder(interceptorHolders, user);
  }

  @Test
  public void testCheckInterceptorOrderWrongPriority() {
    List<InterceptorHolder> interceptorHolders = new ArrayList<>();
    ServiceInterceptor authz = AuthorizationInterceptor.create(null);
    interceptorHolders.add(new InterceptorHolder(authz));
    ServiceInterceptor authn = AuthenticationInterceptor.create(null);
    assertThrows(IllegalStateException.class, () ->
      InterceptorUtils.checkInterceptorOrder(interceptorHolders, authn));
    interceptorHolders.clear();
    ServiceInterceptor user = (vertx, context, msg) -> Future.succeededFuture();
    interceptorHolders.add(new InterceptorHolder(user));
    assertThrows(IllegalStateException.class, () ->
      InterceptorUtils.checkInterceptorOrder(interceptorHolders, authn));
    assertThrows(IllegalStateException.class, () ->
      InterceptorUtils.checkInterceptorOrder(interceptorHolders, authz));
  }
}
