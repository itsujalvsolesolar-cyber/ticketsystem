package com.sujal.itsm.core.exception;

public class NotClockedInException extends RuntimeException {
  public NotClockedInException() {
    super("You are not currently clocked in!");
  }
}
