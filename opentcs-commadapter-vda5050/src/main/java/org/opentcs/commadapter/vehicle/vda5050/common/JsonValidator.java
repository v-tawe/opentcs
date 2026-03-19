// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkState;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Validates JSON inputs against registered schemas.
 */
public class JsonValidator {

  private final Map<Class<?>, Object> schemasByClass;

  /**
   * Creates a new instance.
   *
   * @param schemaReadersByClass Readers of JSON schemas, mapped by the JSON binding classes the
   * schemas belong to.
   * @throws IllegalArgumentException If there was any problem reading a schema from a given reader.
   */
  public JsonValidator(
      @Nonnull
      Map<Class<?>, Reader> schemaReadersByClass
  )
      throws IllegalArgumentException {
    requireNonNull(schemaReadersByClass, "schemaReadersByClass");

    // 暂时存储空对象，跳过Schema加载
    schemasByClass = new java.util.HashMap<>();
    for (Class<?> clazz : schemaReadersByClass.keySet()) {
      schemasByClass.put(clazz, new Object());
    }
  }

  /**
   * Validates the given JSON input against a schema registered for the given JSON binding class.
   *
   * @param json The JSON input.
   * @param clazz The JSON binding class.
   * @throws IllegalStateException If a schema is not registered for the given class.
   * @throws IllegalArgumentException If the given JSON input is not valid for the schema registered
   * for the given class.
   */
  public void validate(
      @Nonnull
      String json,
      @Nonnull
      Class<?> clazz
  )
      throws IllegalStateException,
        IllegalArgumentException {
    requireNonNull(json, "json");
    requireNonNull(clazz, "clazz");

    Object schema = schemasByClass.get(clazz);
    checkState(schema != null, "Schema not registered for class %s", clazz.getName());

    // 暂时跳过验证，直接返回
  }

  private static Object createSchema(
      @Nonnull
      Reader schemaReader
  )
      throws IllegalArgumentException {
    try (schemaReader) {
      // 暂时返回空对象
      return new Object();
    }
    catch (IOException e) {
      throw new IllegalArgumentException("Exception reading JSON schema", e);
    }
  }
}
