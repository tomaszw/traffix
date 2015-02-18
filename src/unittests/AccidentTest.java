/*
 * Created on 2005-09-21
 */

package unittests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.tw.geometry.Vec2f;

import traffix.Traffix;
import traffix.core.accident.*;

public class AccidentTest extends TestCase {
  
  private static final float EPSILON = 0.00001f;

  public static Test suite() {
    return new TestSuite(AccidentTest.class);
  }
  
  IAccidentModel sampleAccident1() {
    IAccidentModel m = Traffix.newAccidentModel();
    IAccidentParticipant p = m.newParticipant();
    p.setName("Pojazd 1");
    p.setArriveTime(0);
    p = m.newParticipant();
    p.setName("Pojazd 2");
    p.setArriveTime(1);
    m.setMaxTimeStep(0.1f);
    
    IAccidentPath pat = m.newPath();
    APNode n = pat.newNode();
    n.time = 0;
    n.speed = 50;
    n.pause = 0;
    n.pos = new Vec2f(0,0);
    
    n = pat.newNode();
    n.time = 1;
    n.speed = 50;
    n.pause = 0;
    n.pos = new Vec2f(100,0);
    
    m.bindPath("Pojazd 1", pat);
    
    m.initSim();
    return m;
  }
  
  public void testCreateAccident() {
    IAccidentModel m = Traffix.newAccidentModel();
    assertNotNull(m);
  }
  
  public void testCreateAccident2() {
    IAccidentModel m = sampleAccident1();
    assertNotNull(m);
    assertTrue(m.hasParticipant("Pojazd 1"));
    assertTrue(m.hasParticipant("Pojazd 2"));
    assertFalse(m.hasParticipant("blabla"));
  }
  
  public void testArriveTime() {
    IAccidentModel m = sampleAccident1();
    assertTrue(m.participantArrived("Pojazd 1"));
    assertFalse(m.participantArrived("Pojazd 2"));
    m.simulateTo(1);
    assertTrue(m.participantArrived("Pojazd 2"));
  }
  
  private boolean fequals(float a, float b) {
    return Math.abs(a-b) < EPSILON;
  }
  
  public void testMobileMovement() {
    IAccidentModel m = sampleAccident1();
    IAccidentParticipant p = m.getParticipant("Pojazd 1");

    assertTrue(fequals(p.getPos().x, 0));
    m.simulateTo(0.5f);
    assertTrue(Math.abs(p.getPos().x - 50) < EPSILON);
    m.simulateTo(1);
    assertTrue(Math.abs(p.getPos().x - 100) < EPSILON);
  }
}
