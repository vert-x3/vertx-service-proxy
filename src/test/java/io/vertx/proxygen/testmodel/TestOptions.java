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

package io.vertx.proxygen.testmodel;

import io.vertx.codegen.annotations.Options;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@Options
public class TestOptions {

  private int number;
  private String string;
  private boolean bool;

  public TestOptions() {
  }

  public TestOptions(TestOptions other) {
    this.number = other.number;
    this.string = other.string;
    this.bool = other.bool;
  }

  public TestOptions(JsonObject json) {
    this.number = json.getInteger("number");
    this.string = json.getString("string");
    this.bool = json.getBoolean("bool");
  }

  public JsonObject toJson() {
    return new JsonObject()
      .putNumber("number", number)
      .putString("string", string)
      .putBoolean("bool", bool);
  }

  public int getNumber() {
    return number;
  }

  public TestOptions setNumber(int number) {
    this.number = number;
    return this;
  }

  public String getString() {
    return string;
  }

  public TestOptions setString(String string) {
    this.string = string;
    return this;
  }

  public boolean isBool() {
    return bool;
  }

  public TestOptions setBool(boolean bool) {
    this.bool = bool;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TestOptions that = (TestOptions) o;

    if (bool != that.bool) return false;
    if (number != that.number) return false;
    if (!string.equals(that.string)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = number;
    result = 31 * result + string.hashCode();
    result = 31 * result + (bool ? 1 : 0);
    return result;
  }
}
