package com.sujal.itsm.core.exception;

public class DuplicateResourceException extends RuntimeException {
  public DuplicateResourceException(String resourceType, String value) {
    super(resourceType + " already exists: " + value);
  }
}
