/*
 * Created on 2004-07-17
 */

package traffix.core.sim.entities;

import java.io.*;
import java.util.List;
import java.util.StringTokenizer;

import org.tw.geometry.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import traffix.Traffix;
import traffix.core.sim.*;
import traffix.core.sim.graph.*;

public class Mobile extends AbstractObstacle implements IMobile {
  public static MoveParams[]    defaultMoveParams         = createDefaultMoveParams();

  public static float           WIDTH                     = 2;
  public float                  arrivalTime               = 0;
  public float                  length;
  public float                  location, rotation;
  public float                  maxAccel;
  public Route                  route;
  public float                  speed, maxSpeed, targetSpeed, limitSpeed;
  public int                    type;

  protected Polygonf            m_bounds;
  protected Obb                 m_obb;

  private float[]               m_boundingEdgeLens        = new float[4];
  private Rectanglef            m_boundingRect            = new Rectanglef();

  private boolean               m_colliding;
  private IObstacle             m_firstBlockingObstacle;

  private IObstacle             m_firstObstacle;
  private GraphLocation         m_graphLocation;
  private ITrafficModelProvider m_modelProvider;
  private Vec2f                 m_pos                     = new Vec2f();
  private MobileState           m_state                   = MobileState.Normal;
  private float                 m_stopDuration;
  private float                 m_stoppedAt;
  private float                 m_toFirstBlockingObstacle = Float.POSITIVE_INFINITY;
  private float                 m_toFirstObstacle         = Float.POSITIVE_INFINITY;

  public static MoveParams[] createDefaultMoveParams() {
    MoveParams[] mp = new MoveParams[IMobile.NumTypes];
    for (int i = 0; i < IMobile.NumTypes; ++i)
      mp[i] = new MoveParams();

    mp[NormalVehicle].speed = 60.0f / 3.6f;
    mp[NormalVehicle].acceleration = 3.5f;
    mp[NormalVehicle].length = 5;

    mp[HeavyVehicle].speed = 60.0f / 3.6f;
    mp[HeavyVehicle].acceleration = 3.0f;
    mp[HeavyVehicle].length = 10;

    mp[Bus].speed = 35.0f / 3.6f;
    mp[Bus].acceleration = 3.0f;
    mp[Bus].length = 10;

    mp[Trolley].speed = 35.0f / 3.6f;
    mp[Trolley].acceleration = 2.5f;
    mp[Trolley].length = 13;

    mp[Pedestrian].speed = 1.5f;
    mp[Pedestrian].acceleration = 6;
    mp[Pedestrian].length = 1;
    
    mp[Cyclist].length = 2.0f;
    mp[Cyclist].speed = 2.8f;
    mp[Cyclist].acceleration = 6;
    
    BufferedReader r = null;
    try {
      r = new BufferedReader(new FileReader("parametry_ruchu.txt"));
      for (int i = 0; i < IMobile.NumTypes; ++i) {
        String line = r.readLine();
        if (line == null)
          break;
        StringTokenizer tok = new StringTokenizer(line);
        String token = tok.nextToken();
        mp[i].speed = Float.parseFloat(token) / 3.6f;
        token = tok.nextToken();
        mp[i].acceleration = Float.parseFloat(token);
        token = tok.nextToken();
        mp[i].vdelta = Float.parseFloat(token) / 3.6f;
        token = tok.nextToken();
        mp[i].length = Float.parseFloat(token);
      }
      r.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return mp;
  }

  public Mobile(ITrafficModelProvider tmp) {
    m_modelProvider = tmp;
    m_bounds = new Polygonf();
    for (int i = 0; i < 4; ++i)
      m_bounds.addPoint(new Vec2f());
    limitSpeed = Float.POSITIVE_INFINITY;
  }

  public float approxNodeImpactTime(Node node) {
    float nodePos = route.path().getNodeLocation(node);
    if (nodePos == Float.NaN)
      return Float.POSITIVE_INFINITY;
    float d = nodePos - location;
    float fakeSpeed = Math.max(speed, 1.0f);
    if (d <= 0)
      return Float.POSITIVE_INFINITY;

    return d / fakeSpeed;
  }

  public float approxTravelTime(float dist) {
    float fakeSpeed = Math.max(speed, 1.0f);
    if (dist <= 0)
      return Float.POSITIVE_INFINITY;

    return dist / fakeSpeed;
  }

  public boolean bumpedIntoObstacle() {
    return m_colliding;
  }

  public boolean countsAsSingle() {
    return true;
  }

  public void dispose() {
    super.dispose();
    if (m_firstObstacle != null)
      m_firstObstacle.notifyPass(this);
  }

  public float getArrivalTime() {
    return arrivalTime;
  }

  public float[] getBoundingEdgeLens() {
    return m_boundingEdgeLens;
  }

  public Rectanglef getBoundingRect() {
    return m_boundingRect;
  }

  public Polygonf getBounds() {
    return m_bounds;
  }

  public Vec2f getCenter() {
    throw new UnsupportedOperationException();
  }

  public Vec2f getDirection() {
    return new Vec2f((float) Math.cos(rotation), (float) Math.sin(rotation));
  }

  public float getDistanceToObstacle() {
    // return m_toFirstBlockingObstacle;
    return Math.max(0, m_toFirstBlockingObstacle);
  }

  public IObstacle getFirstBlockingObstacle() {
    return m_firstBlockingObstacle;
  }

  public float getLength() {
    return length;
  }

  public float getLocation() {
    return location;
  }

  public GraphLocation getMobileGraphLocation() {
    return m_graphLocation;
  }

  public Obb getObb() {
    if (m_obb == null)
      updateObb();
    return m_obb;
  }

  public float getObstacleRadius() {
    return 1;
  }

  public Vec2f getPosition() {
    return m_pos;
  }

  public Route getRoute() {
    return route;
  }

  public float getSpeed() {
    return speed;
  }

  public int getType() {
    return type;
  }

  // public float getTurnSharpness() {
  // GraphLocation loc = getMobileGraphLocation();
  // float c = loc.edge.getDirection().dot(getDirection());
  // float a = (float) Math.acos(c);
  // a = (float) Math.min(a, Math.PI / 2);
  // return (float) (a / (Math.PI / 2));
  // }

  public int getVirtualVehiclesWeight() {
    switch (type) {
      case NormalVehicle :
        return 1;
      case HeavyVehicle :
        return 2;
      case Bus :
        return 3;
      case Trolley :
        return 5;
    }
    return 0;
  }

  public String getXmlTagName() {
    return null;
  }

  public void init() {
    if (route == null || route.getNumNodes() < 2)
      return;
    m_graphLocation = route.traverseDistanceEx(location);
    m_pos = m_graphLocation.getPoint();
    updateBounds();
    updateObb();
    updateGraphLocations();
    updateOrientation();
    updateCollisions();
  }

  public boolean isBlocked(IObstacle except) {
    if (m_colliding)
      return true;
    if (m_firstBlockingObstacle == null || m_firstBlockingObstacle == except)
      return false;
    if (m_firstBlockingObstacle instanceof Light)
      return true;
    if (m_firstBlockingObstacle instanceof Intersection)
      return false;
    if (m_toFirstBlockingObstacle > 8)
      return false;
    return getSpeed() < 8;// && m_firstBlockingObstacle.shouldBlock(this);
  }

  public boolean isBlockedByLight() {
    boolean gotLight = m_firstBlockingObstacle != null
        ? m_firstBlockingObstacle instanceof Light
        : false;
    return gotLight && getSpeed() < 4;
  }

  public void limitSpeed(float maxspeed) {
    limitSpeed = maxspeed;
  }

  public void reset() {
  }

  public boolean shouldBlock(IMobile mobile) {
    return true;
  }

  public boolean shouldDisintegrate() {
    return route == null || location >= route.getLength();
  }

  public void stopFor(float time) {
    m_stoppedAt = Traffix.simScheduleManager().getAbsoluteTime();
    m_state = MobileState.Stopped;
    m_stopDuration = time;
  }

  public void tick(float t0, float delta) {
    if (route == null)
      return;
    if (!checkStopCondition(t0)) {

      m_graphLocation = route.traverseDistanceEx(location);
      m_pos = m_graphLocation.getPoint();

      boolean isMoving = getSpeed() > 0;// || targetSpeed>0;

      m_modelProvider.getActiveTrafficModel().move(this, delta);

      // isMoving = isMoving || (getSpeed()>0);

      if (isMoving) {
        m_graphLocation = route.traverseDistanceEx(location);
        m_pos = m_graphLocation.getPoint();
        updateOrientation();
        updateBounds();
        updateObb();
        updateGraphLocations();
      }
      updateCollisions();
    }
    updateStats(delta);
    // clear speed limit
    limitSpeed = Float.POSITIVE_INFINITY;
  }

  public void update() {
  }

  public void updateCollisions() {
    m_colliding = false;

    float onEdge = m_graphLocation.getDistanceOverEdge();

    IObstacle prevFirst = m_firstObstacle;

    m_firstBlockingObstacle = null;
    m_firstObstacle = null;
    m_toFirstBlockingObstacle = Float.POSITIVE_INFINITY;
    m_toFirstObstacle = Float.POSITIVE_INFINITY;

    IGraphPath path = route.trim(location, location + 100);
    if (path.getNumNodes() < 2) {
      return;
    }

    IGraphPath.ObstacleEntry first = path.getFirstObstacle(onEdge, this);
    if (first != null) {
      m_firstObstacle = first.obstacle;
      m_toFirstObstacle = first.pathLocation.getDistance() - onEdge
          - first.obstacle.getObstacleRadius();
    }

    // notify first that we passed (if we did)
    if (prevFirst != null && m_firstObstacle != prevFirst) {
      prevFirst.notifyPass(this);
    }

    // notify first that we are approaching
    if (m_firstObstacle != null) {
      path.notifyObstaclesOnApproach(onEdge, this);
      // m_firstObstacle.notifyApproach(this, m_toFirstObstacle);
    }

    IGraphPath.ObstacleEntry firstBlocking = null;
    if (first != null && first.obstacle.shouldBlock(this))
      firstBlocking = first;
    else
      firstBlocking = path.getFirstBlockingObstacle(onEdge, this);

    if (firstBlocking != null) {
      m_firstBlockingObstacle = firstBlocking.obstacle;
      m_toFirstBlockingObstacle = firstBlocking.pathLocation.getDistance() - onEdge
          - firstBlocking.obstacle.getObstacleRadius();
    }

    // check if we crashed
    if (m_firstBlockingObstacle != null) {
      Polygonf bounds = m_firstBlockingObstacle.getBounds();
      if (bounds != null && bounds.isInside(getPosition()))
        m_colliding = true;
    }
  }

  public boolean xmlLoad(Document document, Element element) {
    return false;
  }

  public Element xmlSave(Document document) {
    return null;
  }

  protected List<GraphLocation> computeObstacleLocations() {
    return Traffix.simManager().getGraph().intersectMobileBounds(this);
  }

  protected void updateBounds() {
    if (m_bounds == null) {
      m_bounds = new Polygonf();
      for (int i = 0; i < 4; ++i) {
        m_bounds.addPoint(new Vec2f());
      }
    }

    float w = WIDTH;
    if (type == Cyclist)
      w = 1;
    
    m_bounds.getPoint(0).assign(-length, -w/ 2);
    m_bounds.getPoint(1).assign(0, -w / 2);
    m_bounds.getPoint(2).assign(0, w / 2);
    m_bounds.getPoint(3).assign(-length, w / 2);
    m_boundingEdgeLens[0] = length;
    m_boundingEdgeLens[1] = w;
    m_boundingEdgeLens[2] = length;
    m_boundingEdgeLens[3] = w;

    Vec2f frontPos = getPosition();
    Vec2f zero = new Vec2f(0, 0);
    float minx = Float.POSITIVE_INFINITY;
    float maxx = Float.NEGATIVE_INFINITY;
    float miny = Float.POSITIVE_INFINITY;
    float maxy = Float.NEGATIVE_INFINITY;
    for (int i = 0; i < m_bounds.getNumSides(); ++i) {
      Vec2f p = m_bounds.getPoint(i);
      p = p.rotate(zero, rotation).add(frontPos);
      m_bounds.setPoint(i, p);
      if (p.x < minx)
        minx = p.x;
      else if (p.x > maxx)
        maxx = p.x;
      if (p.y < miny)
        miny = p.y;
      else if (p.y > maxy)
        maxy = p.y;
    }

    m_boundingRect.x = minx;
    m_boundingRect.y = miny;
    m_boundingRect.width = maxx - minx;
    m_boundingRect.height = maxy - miny;
  }

  protected void updateObb() {
    Vec2f[] pts = new Vec2f[4];
    Polygonf bounds = getBounds();
    for (int i = 0; i < 4; ++i)
      pts[i] = bounds.getPoint(i);
    m_obb = new Obb(pts);
  }

  private boolean checkStopCondition(float curTime) {
    if (m_state != MobileState.Stopped)
      return false;

    if (curTime - m_stoppedAt >= m_stopDuration) {
      m_state = MobileState.Normal;
      return false;
    }
    speed = 0;
    return true;
  }

  private void setDirection(Vec2f dir) {
    rotation = (float) Math.atan2(dir.y, dir.x);
    if (rotation < 0)
      rotation += 2 * Math.PI;
  }

  private void updateOrientation() {
    GraphLocation loc = getMobileGraphLocation();
    if (loc.getDistanceOverEdge() >= length) {
      setDirection(loc.getEdge().getDirection());
    } else {
      IGraphPath path = route.path();
      int i = path.getNodeIndex(loc.getEdge().A);
      while (i >= 1) {
        Node n1 = path.getNode(i - 1);
        Node n2 = path.getNode(i);
        Vec2f dir = n2.getPos().sub(n1.getPos());
        float[] ts = GeometryKit.intersectCircleVector(loc.getPoint(), length, n1.getPos(), dir);
        if (ts.length >= 1) {
          float t = ts[0];
          if (ts.length == 2) {
            if (ts[1] >= 0 && ts[1] <= 1)
              t = ts[1];
          }
          if (t >= 0 && t <= 1) {
            Vec2f inter = n1.getPos().add(dir.mul(t));
            Vec2f mobDir = loc.getPoint().sub(inter).normalize();
            setDirection(mobDir);
            return;
          }
        }
        --i;
      }
      if (path.getNumNodes() >= 2) {
        GraphEdge e = Traffix.simManager().getGraph().findEdge(route.path().getNode(0),
            route.path().getNode(1));
        setDirection(e.getDirection());
      } else
        setDirection(loc.getEdge().getDirection());
    }
  }

  private void updateStats(float dt) {
    if (getSpeed() < 1)
      route.getInfo().summedStopTime += dt;
  }
}