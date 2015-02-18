/*
 * Created on 2004-08-10
 */

package unittests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.tw.geometry.Vec2f;
import org.tw.geometry.Polygonf;

public class PolygonTest extends TestCase {
  public static Test suite() {
    return new TestSuite(PolygonTest.class);
  }

  public void testIsInside() {
    Polygonf p = new Polygonf();
    p.addPoint(new Vec2f(0, 0));
    p.addPoint(new Vec2f(5, 0));
    p.addPoint(new Vec2f(4, 5));

    assertTrue(p.isInside(new Vec2f(1, 1)));
    assertTrue(p.isInside(new Vec2f(3, 1)));
    assertTrue(p.isInside(new Vec2f(0, 0)));
    assertTrue(p.isInside(new Vec2f(4, 3)));

    assertFalse(p.isInside(new Vec2f(0, 1)));
    assertFalse(p.isInside(new Vec2f(2, 3)));
    assertFalse(p.isInside(new Vec2f(5, 1)));
    assertFalse(p.isInside(new Vec2f(3, -1)));
  }
}
