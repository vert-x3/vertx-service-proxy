package io.vertx.serviceproxy.generator.model;

/*
 * Copyright 2014 Red Hat, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

import io.vertx.codegen.processor.MethodInfo;
import io.vertx.codegen.processor.ParamInfo;
import io.vertx.codegen.processor.TypeParamInfo;
import io.vertx.codegen.processor.doc.Doc;
import io.vertx.codegen.processor.doc.Text;
import io.vertx.codegen.processor.type.ClassTypeInfo;
import io.vertx.codegen.processor.type.TypeInfo;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ProxyMethodInfo extends MethodInfo {

  private final boolean proxyIgnore;
  private final boolean proxyClose;

  public ProxyMethodInfo(Set<ClassTypeInfo> ownerTypes, String name, TypeInfo returnType, Text returnDescription, boolean fluent,
                         boolean cacheReturn, List<ParamInfo> params, String comment, Doc doc, boolean staticMethod, boolean defaultMethod,
                         List<TypeParamInfo.Method> typeParams, boolean proxyIgnore, boolean proxyClose, boolean deprecated, Text deprecatedDesc, boolean methodOverride) {


    super(ownerTypes, name, returnType, returnDescription, fluent, cacheReturn, params, comment, doc, staticMethod, defaultMethod, typeParams, deprecated, deprecatedDesc, methodOverride);
    this.proxyIgnore = proxyIgnore;
    this.proxyClose = proxyClose;
  }

  public boolean isProxyIgnore() {
    return proxyIgnore;
  }

  public boolean isProxyClose() {
    return proxyClose;
  }
}
