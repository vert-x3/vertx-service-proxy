/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.serviceproxy.tests.testmodel;

import io.vertx.codegen.annotations.DataObject;

/**
 *
 * The Json properties are used to reproduce https://github.com/vert-x3/vertx-service-proxy/issues/56
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@DataObject
public class StringDataObject {

  private String value;

  public StringDataObject() {
  }


  public StringDataObject(String value) {
    this.value = value;
  }

  public String toJson() {
    return value;
  }

  public String getValue() {
    return value;
  }

  public StringDataObject setValue(String value) {
    this.value = value;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    StringDataObject that = (StringDataObject) o;

    if (!value.equals(that.value)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    String v = this.value;
    return v != null ? v.hashCode() : 0;
  }
}
