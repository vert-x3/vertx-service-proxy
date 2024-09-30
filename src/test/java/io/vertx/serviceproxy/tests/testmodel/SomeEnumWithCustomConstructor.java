package io.vertx.serviceproxy.tests.testmodel;

public enum SomeEnumWithCustomConstructor {
  DEV("dev", "development"), ITEST("itest", "integration-test");

  public static SomeEnumWithCustomConstructor of(String pName) {
    for (SomeEnumWithCustomConstructor item : SomeEnumWithCustomConstructor.values()) {
      if (item.names[0].equalsIgnoreCase(pName) || item.names[1].equalsIgnoreCase(pName)
          || pName.equalsIgnoreCase(item.name())) {
        return item;
      }
    }
    return DEV;
  }

  private String[] names = new String[2];

  SomeEnumWithCustomConstructor(String pShortName, String pLongName) {
    names[0] = pShortName;
    names[1] = pLongName;
  }

  public String getLongName() {
    return names[1];
  }

  public String getShortName() {
    return names[0];
  }

}
