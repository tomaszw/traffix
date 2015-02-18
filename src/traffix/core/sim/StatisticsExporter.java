/*
 * Created on 2004-09-15
 */

package traffix.core.sim;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.ole.win32.OleAutomation;

import traffix.Traffix;
import traffix.automation.Excel;
import traffix.core.VehicleGroup;
import traffix.core.VehicleGroupSet;
import traffix.core.Time;
import traffix.core.schedule.Schedule;
import traffix.core.sim.entities.*;
import traffix.core.sim.graph.Graph;
import traffix.core.sim.graph.Node;

public class StatisticsExporter {
  private Excel m_excel;
  private Graph m_graph;
  private int m_nodecount;
  private int m_routecount;
  private int m_row;
  private OleAutomation m_sheet;
  private float m_simTime;

  public void changeSheet(int num) {
    m_sheet = m_excel.getWorksheet(num);
    m_row = 0;
  }
  
  public void exportDatabanks() {
    m_excel = new Excel(Traffix.shell());
    m_excel.setVisible(true);
    m_excel.addWorkbook();
    m_sheet = m_excel.getWorksheet(1);
    m_row = 0;
    m_graph = Traffix.simManager().getGraph();
    m_simTime = Traffix.simManager().getCurrentTime();

    Databanks banks = Traffix.simManager().databanks();
    int active = banks.getActive();

    // for each bank
    for (int i=0; i<Databanks.MAX_DATABANKS; ++i) {
      if (!banks.getComment(i).equals("")) {
        banks.setActive(i);
        exportDatabank();
      }
    }
    banks.setActive(active);
  }

  public void dispose() {
    m_excel.dispose();
  }
  
  public void exportExperiments() {
    m_excel = new Excel(Traffix.shell());
    m_excel.setVisible(true);
    m_excel.addWorkbook();
    m_sheet = m_excel.getWorksheet(1);
    m_row = 0;
    m_graph = Traffix.simManager().getGraph();

    m_simTime = Traffix.simManager().getCurrentTime();
    Time time = new Time();
    time = time.addSecs((int) Traffix.simManager().getCurrentTime());

    setCell(0, m_row, "Czas trwania: " + time);
    ++m_row;

    Databanks banks = Traffix.simManager().databanks();
    int active = banks.getActive();

    setCell(0, m_row, "Wybrany bank: " + banks.getComment(active));
    ++m_row;
    ++m_row;

    exportDatabank();

    exportRouteStats();

    exportGroups();

    exportProgramData();

    exportAccomodationMatrix();

    exportPresenceDetectors();

    exportPedestrianDetectors();

    exportTransitDetectors();

    m_excel.dispose();
  }

  private String cell(int x, int y) {
    int numletters = 'Z' - 'A' + 1;
    String colstr = "" + (char) (x%numletters + 'A');
    if (x >= numletters) {
      x -= numletters;
      colstr = "A" + colstr;
    }

    return colstr + Integer.toString(y + 1);
  }

  private void exportAccomodationMatrix() {
    setCell(0, m_row, "Macierz cyklu akomodacyjnego");
    ++m_row;
    AccomodationMatrix mat = Traffix.simScheduleManager().accMatrix();
    Schedule[] prischs = mat.getSchedules();
    for (int i = 0; i < prischs.length + 1; ++i) {
      for (int j = 0; j < prischs.length + 1; ++j) {
        if (i == 0 && j == 0) {
        } else if (i == 0) {
          setCell(j, m_row, prischs[j - 1].getName());
        } else if (j == 0) {
          setCell(0, m_row, prischs[i - 1].getName());
        } else {
          setCell(j, m_row, Integer.toString(mat.getPriority(i - 1, j - 1)));
        }
      }
      ++m_row;
    }
    ++m_row;
  }

  private void setCell(int x, int y, String txt) {
    m_excel.setCellValue(m_sheet, cell(x, y), txt);
  }

  private void exportDatabank() {
    m_nodecount = 1;
    m_routecount = 1;

    setCell(0, m_row, "Bank");
    Databanks banks = Traffix.simManager().databanks();
    setCell(1, m_row, banks.getComment(banks.getActive()));
    ++m_row;

    setCell(0, m_row, "Tor");
    setCell(1, m_row, "Wêze³");
    setCell(2, m_row, "Komentarz");
    setCell(3, m_row, "Normalne");
    setCell(4, m_row, "Ciê¿kie");
    setCell(5, m_row, "Autobusy");
    setCell(6, m_row, "Tramwaje");

    setCell(7, m_row, "a_max (normalne)");
    setCell(8, m_row, "v_max (normalne)");
    setCell(9, m_row, "v_delta (normalne)");
    setCell(10, m_row, "a_max (ciê¿kie)");
    setCell(11, m_row, "v_max (ciê¿kie)");
    setCell(12, m_row, "v_delta (ciê¿kie)");
    setCell(13, m_row, "a_max (autobusy)");
    setCell(14, m_row, "v_max (autobusy)");
    setCell(15, m_row, "v_delta (autobusy)");
    setCell(16, m_row, "a_max (tramwaje)");
    setCell(17, m_row, "v_max (tramwaje)");
    setCell(18, m_row, "v_delta (tramwaje)");
    setCell(19, m_row, "Grupy steruj¹ce");
    
    ++m_row;
    for (Iterator<Node> iter = m_graph.getNodeIterator(); iter.hasNext();) {
      Node n = iter.next();
      if (n.isBeginningNode()) {
        List<Route> routes = n.getRoutesFromNode();
        for (Iterator<Route> iter2 = routes.iterator(); iter2.hasNext();) {
          Route route = iter2.next();
          RouteInfo inf = route.getInfo();
          if (inf.hidden)
            continue;
          setCell(0, m_row, Integer.toString(m_routecount++) + ":" + inf.name);
          setCell(1, m_row, Integer.toString(m_nodecount));
          setCell(2, m_row, route.getInfo().comment);
          setCell(3, m_row, Integer
            .toString(inf.getMeasure().nPh));
          setCell(4, m_row, Integer
            .toString(inf.getMeasure().hPh));
          setCell(5, m_row, Integer
            .toString(inf.getMeasure().busPh));
          setCell(6, m_row, Integer
            .toString(inf.getMeasure().trolleyPh));
              
          int col = 7;
          for (int i = 0; i < 4; ++i) {
            setCell(col++, m_row, Float.toString((int)(inf
              .getMoveParams()[i].acceleration)));
            setCell(col++, m_row, Float.toString((int)(inf
              .getMoveParams()[i].speed*3.6f)));
            setCell(col++, m_row, Float.toString((int)(inf
              .getMoveParams()[i].vdelta*3.6f)));
          }

          VehicleGroupSet set = inf.getControllingGroups();
          String gpsstr = "";
          for (Iterator<VehicleGroup> iterator = set.iterator(); iterator.hasNext();) {
            VehicleGroup g = iterator.next();
            gpsstr += g.getElectricName();
            if (iterator.hasNext())
              gpsstr += " ";
          }
          setCell(col++, m_row, gpsstr);
          ++m_row;

        }
        ++m_nodecount;
      }
    }

    ++m_row;
  }
  private void exportGroups() {
    setCell(0, m_row, "Grupa");
    setCell(1, m_row, "Sygnalizator");
    setCell(2, m_row, "Ve");
    setCell(3, m_row, "Vd");
    setCell(4, m_row, "D³ugoœæ");
    ++m_row;
    for (int i = 0; i < Traffix.model().getNumGroups(); ++i) {
      VehicleGroup g = Traffix.model().getGroupByIndex(i);
      setCell(0, m_row, g.getElectricName());
      setCell(1, m_row, g.getName());
      setCell(2, m_row, Integer.toString(g.getEvacuationSpeed()));
      setCell(3, m_row, Integer.toString(g.getApproachSpeed()));
      setCell(4, m_row, Integer.toString((int) g.getLengthInMeters()));
      ++m_row;
    }

    ++m_row;
  }

  public void exportPedestrianDetectors() {
    setCell(0, m_row, "Detektor pieszych");
    setCell(1, m_row, "Aktywowane programy");
    setCell(2, m_row, "Odstêp");
    setCell(3, m_row, "Kasowany przez");
    ++m_row;

    int peded = 1;
    for (Iterator<IEntity> iter = Traffix.simEntityManager().iterator(); iter.hasNext();) {
      IEntity e = iter.next();
      if (e instanceof PedestrianDetector) {
        PedestrianDetector d = (PedestrianDetector) e;

        setCell(0, m_row, Integer.toString(peded) + ": " + d.getName());
        java.util.List acts = d.getSchedules();
        setCell(1, m_row, formatNames(acts));
        setCell(2, m_row, Integer.toString((int) d
          .getInterval()));
        VehicleGroup cl = d.getClearedBy();
        setCell(3, m_row, cl != null ? cl.getElectricName() : "");

        ++m_row;
        ++peded;
      }
    }

    ++m_row;
  }

  public void exportPresenceDetectors() {
    setCell(0, m_row, "Detektor obecnoœci");
    setCell(1, m_row, "Aktywowane programy");
    setCell(2, m_row, "Czas aktywacji");
    setCell(3, m_row, "Kasowany przez");
    ++m_row;

    int pd = 1;
    for (Iterator<IEntity> iter = Traffix.simEntityManager().iterator(); iter.hasNext();) {
      IEntity e = iter.next();
      if (e instanceof PresenceDetector) {
        PresenceDetector d = (PresenceDetector) e;

        setCell(0, m_row, Integer.toString(pd) + ": " + d.getName());

        java.util.List acts = d.getSchedules();
        setCell(1, m_row, formatNames(acts));
        setCell(2, m_row, Integer.toString((int) d
          .getActivationTime()));
        VehicleGroup cl = d.getClearedBy();
        setCell(3, m_row, cl != null ? cl.getElectricName() : "");
        ++m_row;
        ++pd;
      }
    }

    ++m_row;
  }

  private void exportProgramData() {
    setCell(0, m_row, "Zdefiniowane programy");
    ++m_row;
    setCell(0, m_row, "Nazwa");
    setCell(1, m_row, "D³ugoœæ");
    setCell(2, m_row, "Iloœæ rozpoczêtych cykli");
    ++m_row;
    Schedule[] schedules = Traffix.scheduleBank().getSchedules();
    for (int i = 0; i < schedules.length; i++) {
      setCell(0, m_row, schedules[i].getName());
      setCell(1, m_row, Integer.toString(schedules[i]
        .getProgramLength()));
      setCell(2, m_row, Integer
        .toString(schedules[i].numSimCycles));
      ++m_row;
    }
    ++m_row;
  }

  private void exportRouteStats() {
    setCell(0, m_row, "Statystyka eksperymentu");
    ++m_row;
    setCell(2, m_row, "Przyby³o");
    setCell(6, m_row, "Przejecha³o");
    ++m_row;
    setCell(0, m_row, "Tor");
    setCell(1, m_row, "Wêze³");
    setCell(2, m_row, "Lekkich");
    setCell(3, m_row, "Ciê¿kich");
    setCell(4, m_row, "Autobusów");
    setCell(5, m_row, "Tramwajów");

    setCell(6, m_row, "Lekkich");
    setCell(7, m_row, "Ciê¿kich");
    setCell(8, m_row, "Autobusów");
    setCell(9, m_row, "Tramwajów");

    setCell(10, m_row, "Umowne (które przejecha³y)");
    setCell(11, m_row, "Umowne/godzinê");

    setCell(12, m_row, "Czas stracony na postój (s)");
    setCell(13, m_row, "Œredni czas postoju (s)");
    setCell(14, m_row, "Œredni czas przejazdu (s)");
    setCell(15, m_row, "% postoju");

    ++m_row;

    m_nodecount = 1;
    m_routecount = 1;
    for (Iterator<Node> iter = m_graph.getNodeIterator(); iter.hasNext();) {
      Node n = iter.next();
      if (n.isBeginningNode()) {
        List<Route> routes = n.getRoutesFromNode();
        for (Iterator<Route> iter2 = routes.iterator(); iter2.hasNext();) {
          Route route = iter2.next();
          RouteInfo inf = route.getInfo();
          
          if (inf.hidden) continue;
          
          setCell(0, m_row, Integer.toString(m_routecount++) + ":" + inf.comment);
          setCell(1, m_row, Integer.toString(m_nodecount));


          int sumArr = 0, sumPass = 0;
          for (int i = 0; i < 4; ++i) {
            setCell(2 + i, m_row, Integer
              .toString(inf.numArrivedVehicles[i]));
            setCell(6 + i, m_row, Integer
              .toString(inf.numPassedVehicles[i]));
            sumArr += inf.numArrivedVehicles[i];
            sumPass += inf.numPassedVehicles[i];
          }
          int col = 10;
          setCell(col++, m_row, Integer
            .toString(inf.numPassedVirtualVehicles));
          setCell(col++, m_row, m_simTime != 0 ? Integer
            .toString((int) (inf.numPassedVirtualVehicles/m_simTime*3600.0f)) : "");
          setCell(col++, m_row, Integer
            .toString((int) inf.summedStopTime));
          float avgStop = inf.summedStopTime/sumArr;
          float avgStop2 = inf.summedStopTime/sumArr;
          float avgTravel = inf.summedTravelTime/sumPass;
          float percStop = avgStop2/avgTravel*100;
          setCell(col++, m_row, normalFloat(avgStop) ? Integer
            .toString((int) avgStop) : "");
          setCell(col++, m_row, normalFloat(avgTravel) ? Integer
            .toString((int) avgTravel) : "");
          setCell(col++, m_row, normalFloat(percStop) ? Integer
            .toString((int) percStop)
            + "%" : "");
          ++m_row;
        }
        ++m_nodecount;
      }
    }
    ++m_row;
  }

  public void exportTransitDetectors() {
    int td = 1;
    for (Iterator<IEntity> iter = Traffix.simEntityManager().iterator(); iter.hasNext();) {
      IEntity e = iter.next();
      if (e instanceof TransitDetector) {
        TransitDetector d = (TransitDetector) e;

        setCell(0, m_row, "Detektor tranzytu " + td + ": " + d.getName());
        ++m_row;
        setCell(0, m_row, "Program");
        setCell(1, m_row, "Sekunda");
        setCell(2, m_row, "Wyd³u¿enie");
        setCell(3, m_row, "Max. wyd³u¿enie");
        setCell(4, m_row, "Sprzê¿one");
        ++m_row;

        List<TransitDetector.Entry> ents = d.getEntries();
        int i = 0;
        for (Iterator<TransitDetector.Entry> iterator = ents.iterator(); iterator.hasNext();) {
          TransitDetector.Entry ent = iterator.next();
          setCell(0, m_row, ent.schedule.getName());
          setCell(1, m_row, Integer.toString(ent.sec));
          setCell(2, m_row, Integer.toString((int) d
            .getFreezeTime()));
          setCell(3, m_row, Integer.toString(ent.tmax));
          
          String linkedDets = "";
          for (FreezeLink l : ent.freezeLinks) {
            linkedDets += l.linkedDetector.getName() + " ";
          }
          setCell(4, m_row, linkedDets);
          ++m_row;
        }
        ++td;

        m_row += 1;//Math.max(1, ents.size()) + 1;
      }
    }

    ++m_row;
  }

  private String formatNames(java.util.List schedules) {
    String r = "";
    for (int i = 0; i < schedules.size(); ++i) {
      r += ((Schedule) schedules.get(i)).getName();
      if (i != schedules.size() - 1)
        r += ",";
    }
    return r;
  }

  private boolean normalFloat(float f) {
    return !(Float.isInfinite(f) || Float.isNaN(f));
  }
}