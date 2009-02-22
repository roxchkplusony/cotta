package net.sf.cotta.test.assertion;

public class BooleanAssert extends BaseAssert<Boolean> {
  public BooleanAssert(Boolean value) {
    super(value);
  }

  public void isFalse() {
    eq(false);
  }

  public void isTrue() {
    eq(true);
  }
}
