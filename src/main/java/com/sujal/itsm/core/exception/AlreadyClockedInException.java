package com.sujal.itsm.core.exception;

public class AlreadyClockedInException extends RuntimeException {
  public AlreadyClockedInException() {
    super("You are already clocked in!");
  }
}
