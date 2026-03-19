// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Holds the information reference (e.g. orderId, orderUpdateId, actionId) as key-value pairs.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfoReference
    implements
      Serializable {

  /**
   * The reference key.
   */
  private String referenceKey;
  /**
   * The reference value.
   */
  private Object referenceValue;

  @JsonCreator
  public InfoReference(
      @Nonnull
      @JsonProperty(required = true, value = "referenceKey")
      String referenceKey,
      @Nonnull
      @JsonProperty(required = true, value = "referenceValue")
      Object referenceValue
  ) {
    this.referenceKey = requireNonNull(referenceKey, "referenceKey");
    this.referenceValue = requireNonNull(referenceValue, "referenceValue");
  }

  public String getReferenceKey() {
    return referenceKey;
  }

  public InfoReference setReferenceKey(
      @Nonnull
      String referenceKey
  ) {
    this.referenceKey = requireNonNull(referenceKey, "referenceKey");
    return this;
  }

  public Object getReferenceValue() {
    return referenceValue;
  }

  public InfoReference setReferenceValue(
      @Nonnull
      Object referenceValue
  ) {
    this.referenceValue = requireNonNull(referenceValue, "referenceValue");
    return this;
  }

  /**
   * Returns the reference value as a string.
   * If the reference value is an array, it will be converted to a comma-separated string.
   *
   * @return The reference value as a string.
   */
  public String getReferenceValueAsString() {
    if (referenceValue == null) {
      return null;
    }
    if (referenceValue instanceof String) {
      return (String) referenceValue;
    }
    if (referenceValue instanceof Object[]) {
      return String.join(
          ", ", java.util.Arrays.stream((Object[]) referenceValue)
              .map(Object::toString)
              .toArray(String[]::new)
      );
    }
    if (referenceValue instanceof java.util.List) {
      return String.join(
          ", ", ((java.util.List<?>) referenceValue)
              .stream()
              .map(Object::toString)
              .toArray(String[]::new)
      );
    }
    return referenceValue.toString();
  }

  @Override
  public String toString() {
    return "InfoReference{" + "referenceKey=" + referenceKey
        + ", referenceValue=" + getReferenceValueAsString()
        + '}';
  }

}
