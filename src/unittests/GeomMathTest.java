/*
 * Created on 2004-07-31
 */

package unittests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.tw.geometry.GeometryKit;


public class GeomMathTest extends TestCase {
  public static Test suite() {
    return new TestSuite(GeomMathTest.class);
  }

  public void testDistanceOfLineToPoint() {
    float d = GeometryKit.distanceOfLineToPoint(0, 0, 1, 0, 0, 1);
    assertTrue(Float.toString(d), Math.abs(d - 1) < 0.001);

    d = GeometryKit.distanceOfLineToPoint(0, 0, 0, 1, 1, 0);
    assertTrue(Float.toString(d), Math.abs(d - 1) < 0.001);

    d = GeometryKit.distanceOfLineToPoint(-1, -1, -1, 1, 0, 0);
    assertTrue(Float.toString(d), Math.abs(d - 1) < 0.001);

    d = GeometryKit.distanceOfLineToPoint(-1, -1, 1, 8, 2, 4);
    assertTrue(Float.toString(d), Math.abs(d - 1.8439) < 0.001);

    d = GeometryKit.distanceOfLineToPoint(-4, -4, 4, 4, 0, 0);
    assertTrue(Float.toString(d), Math.abs(d) < 0.001);

    d = GeometryKit.distanceOfLineToPoint(-3, -2, 4, 5, -3, -2);
    assertTrue(Float.toString(d), Math.abs(d) < 0.001);
  }
}
