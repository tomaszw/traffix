/*
 * Created on 2004-09-01
 */

package traffix.core.sim;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.graphics.Rectangle;
import org.tw.geometry.Rectanglef;
import org.tw.geometry.Vec2f;
import org.tw.patterns.observer.IUpdateListener;
import org.tw.patterns.observer.IUpdateable;

import traffix.core.model.IPersistent;
import traffix.core.sim.entities.EntityManager;
import traffix.core.sim.entities.IDetector;
import traffix.core.sim.entities.IMobile;
import traffix.core.sim.generation.IGenerationModelProvider;
import traffix.core.sim.graph.Graph;
import traffix.core.sim.graph.Node;
import traffix.ui.BigImage;
import traffix.ui.sim.entities.IUiEntity;
import traffix.ui.sim.entities.IUiEntityContextProvider;

public interface ISimManager
    extends
      IPersistent,
      IGenerationModelProvider,
      IScheduleProvider,
      ITrafficModelProvider,
      IUpdateable {

  public static final int   MAX_MOBILES            = 1000;
  public static final float MAX_SIMULATION_STEP    = 0.2f;
  public static final int   MSG_CHANGED_START_NODE = 0;
  public static final int   MSG_CHANGED_ZOOM       = 1;
  public static final int   MSG_OTHER              = -1;
  public static final int   MSG_TICKED             = 2;

  void addMobile(IMobile m);
  // Don't forget to dispose created entities!
  //List<IUiEntity> createUiEntities(IUiEntityContextProvider provider);
  void setEntityContextProvider(IUiEntityContextProvider provider);
  List<IUiEntity> getUiEntities();
  Databanks databanks();
  void dispose();
  EntityManager entityManager();
  void fireUpdated();
  float getCurrentTime();
  List<IDetector> getDetectors();
  Rectanglef getFilmRectangle();
  Graph getGraph();
  Iterator<IMobile> getMobileIterator();
  String getName();
  IDetector getNamedDetector(String name);
  Set<String> getRouteNames();
  List<Route> getRoutes();
  List<Route> getRoutesFromNode(Node n);
  String getSimulationMessage();
  float getSpeedFactor();
  Vec2f getTerrainDims();
  Rectangle getTerrainDimsZoomedAndOnScreen();
  BigImage getTerrainImage();
  float getZoom();
  void importImage(String path);
  Iterable<IMobile> mobiles();
  void rescaleMap(float w, float h);
  void reset();
  void rewindAndGenerateSimulation();
  void rewindSimulation();
  ScheduleManager scheduleManager();
  void setFilmRectangle(Rectanglef rc);
  void setFps(float fps);
  void setGenerationModel(int generationModel);
  void setName(String name);
  void setSpeedFactor(float speedFactor);
  void setZoom(float zoom);
  SimParams simParams();
  void tick(float delta);
}