/*
 * Created on 2004-07-31
 */

package unittests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class TraffixUnitTests extends TestCase {
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(GeomMathTest.suite());
    suite.addTest(PolygonTest.suite());
    suite.addTest(AccidentTest.suite());
    return suite;
  }
}

