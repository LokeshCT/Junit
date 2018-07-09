package com.bt.cqm.model;


public class SearchCustomerModel {

  private boolean displayErrors;
  private String userId;
  private String errorMessage;

  public SearchCustomerModel(boolean displayErrors, String userId, String errorMessage) {
    this.displayErrors = displayErrors;
    this.userId = userId;
    this.errorMessage = errorMessage;
  }

  public boolean isDisplayErrors() {
    return displayErrors;
  }

  public String getUserId() {
    return userId;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
