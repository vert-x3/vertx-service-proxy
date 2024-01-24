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

package io.vertx.serviceproxy.generator.model;

import io.vertx.codegen.*;
import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.doc.Doc;
import io.vertx.codegen.doc.Text;
import io.vertx.codegen.type.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ProxyModel extends ClassModel {

  public ProxyModel(ProcessingEnvironment env, TypeMirrorFactory typeFactory, TypeElement modelElt) {
    super(env, typeFactory, modelElt);
  }

  @Override
  public String getKind() {
    return "proxy";
  }

  @Override
  protected void checkParamType(ExecutableElement elem, TypeInfo typeInfo, int pos, int numParams, boolean allowAnyJavaType) {
    // Basic types, int, long, String etc
    // JsonObject or JsonArray
    if (typeInfo.getKind().basic || typeInfo.getKind().json) {
      return;
    }
    // We also allow enums as parameter types
    if (typeInfo.getKind() == ClassKind.ENUM) {
      return;
    }
    if (isLegalContainerParam(typeInfo)) {
      return;
    }
    // We also allow data object as parameter types if they have a complete codec
    if (typeInfo.isDataObjectHolder()) {
      if (typeInfo.getDataObject().isSerializable() && typeInfo.getDataObject().isDeserializable()){
        return;
      }
      throw new GenException(elem, "Data Object " + typeInfo + " must have a valid serializer and deserializer");
    }
    if (elem.getModifiers().contains(Modifier.STATIC)) {
      // Ignore static methods - we won't use them anyway
      return;
    }
    throw new GenException(elem, "type " + typeInfo + " is not legal for use for a parameter in proxy");
  }

  @Override
  protected void checkReturnType(ExecutableElement elem, TypeInfo type, boolean allowAnyJavaType) {
    if (elem.getModifiers().contains(Modifier.STATIC)) {
      // Ignore static methods - we won't use them anyway
      return;
    }
    if (type.isVoid() || type.getName().startsWith("io.vertx.core.Future")) {
      return;
    }
    throw new GenException(elem, "Proxy methods must return Future<T>");
  }

  @Override
  protected void checkMethod(MethodInfo methodInfo) {
    // We don't allow overloaded methods in proxies
    List<MethodInfo> methodsByName = methodMap.get(methodInfo.getName());
    if (methodsByName != null && methodsByName.size() > 1) {
      throw new GenException(this.modelElt, "Overloaded methods are not allowed in ProxyGen interfaces " + methodInfo.getName());
    }
  }

  @Override
  protected MethodInfo createMethodInfo(Set<ClassTypeInfo> ownerTypes, String methodName, String comment, Doc doc, TypeInfo returnType, Text returnDescription, boolean isFluent, boolean isCacheReturn, List<ParamInfo> mParams, ExecutableElement methodElt, boolean isStatic, boolean isDefault, ArrayList<TypeParamInfo.Method> typeParams, TypeElement declaringElt, boolean methodDeprecated, Text methodDeprecatedDesc, boolean methodOverride) {
    AnnotationMirror proxyIgnoreAnnotation = Helper.resolveMethodAnnotation(ProxyIgnore.class, elementUtils, typeUtils, declaringElt, methodElt);
    boolean isProxyIgnore = proxyIgnoreAnnotation != null;
    AnnotationMirror proxyCloseAnnotation = Helper.resolveMethodAnnotation(ProxyClose.class, elementUtils, typeUtils, declaringElt, methodElt);
    boolean isProxyClose = proxyCloseAnnotation != null;
    ProxyMethodInfo proxyMeth = new ProxyMethodInfo(ownerTypes, methodName, returnType, returnDescription,
      isFluent, isCacheReturn, mParams, comment, doc, isStatic, isDefault, typeParams, isProxyIgnore,
      isProxyClose, methodDeprecated, methodDeprecatedDesc, methodOverride);
    if (isProxyClose) {
      if (mParams.size() > 0) {
        throw new GenException(this.modelElt, "@ProxyClose methods can't have parameters");
      }
      if (proxyMeth.getReturnType().getKind() == ClassKind.FUTURE) {
        TypeInfo type = proxyMeth.getReturnType();
        TypeInfo arg = (((ParameterizedTypeInfo) type).getArgs().get(0));
        if (arg.getKind() != ClassKind.VOID) {
          throw new GenException(this.modelElt, "@ProxyClose  must return Future<Void> instead of " + type);
        }
      }
    }
    return proxyMeth;
  }

  private boolean isLegalAsyncResultType(TypeInfo resultType) {
    if (resultType.getKind().json || resultType.getKind().basic ||
      isLegalContainerParam(resultType) || resultType.getKind() == ClassKind.VOID ||
      resultType.getKind() == ClassKind.ENUM || resultType.isDataObjectHolder()) {
      return true;
    }
    if (resultType.getKind() == ClassKind.API) {
      ApiTypeInfo cla = (ApiTypeInfo)resultType;
      if (cla.isProxyGen()) {
        return true;
      }
    }
    return false;
  }

  private boolean isLegalHandlerAsyncResultType(TypeInfo type) {
    if (type.getErased().getKind() == ClassKind.HANDLER) {
      TypeInfo eventType = ((ParameterizedTypeInfo) type).getArgs().get(0);
      if (eventType.getErased().getKind() == ClassKind.ASYNC_RESULT) {
        TypeInfo resultType = ((ParameterizedTypeInfo) eventType).getArgs().get(0);
        return isLegalAsyncResultType(resultType);
      }
    }
    return false;
  }

  private boolean isLegalContainerParam(TypeInfo type) {
    TypeInfo raw = type.getRaw();
    if (raw.getName().equals(List.class.getName()) || raw.getName().equals(Set.class.getName())) {
      TypeInfo argument = ((ParameterizedTypeInfo) type).getArgs().get(0);
      if (argument.getKind().basic || argument.getKind().json || isValidDataObject(argument)) {
        return true;
      }
    } else if (raw.getName().equals(Map.class.getName())) {
      TypeInfo argument0 = ((ParameterizedTypeInfo) type).getArgs().get(0);
      if (!argument0.getName().equals(String.class.getName())) {
        return false;
      }
      TypeInfo argument1 = ((ParameterizedTypeInfo) type).getArgs().get(1);
      if (argument1.getKind().basic || argument1.getKind().json || isValidDataObject(argument1)) {
        return true;
      }
    }
    return false;
  }

  private boolean isValidDataObject(TypeInfo typeInfo) {
    return typeInfo.isDataObjectHolder() && typeInfo.getDataObject().isSerializable() && typeInfo.getDataObject().isDeserializable();
  }
}
