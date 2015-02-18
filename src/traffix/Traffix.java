/*
 * Created on 2004-07-14
 */

package traffix;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.tw.persistence.xml.ClassidBindings;
import org.tw.persistence.xml.SessionFactory;

import traffix.Config.MalformedFileException;
import traffix.core.accident.AccidentModel;
import traffix.core.accident.IAccidentModel;
import traffix.core.actionframework.ActionManager;
import traffix.core.actionframework.IActionManager;
import traffix.core.model.Model;
import traffix.core.schedule.ScheduleBank;
import traffix.core.sim.Databanks;
import traffix.core.sim.ISimManager;
import traffix.core.sim.ScheduleManager;
import traffix.core.sim.entities.EntityManager;
import traffix.ui.Cursors;
import traffix.ui.HelpPictureFrame;
import traffix.ui.TraffixFrame;

public class Traffix {
  public static final String AUTHOR = "Tomasz Wr�blewski";
  public static final String NAME = "TrafficLS";
  public static final String VERSION = "1.0";
  private static IActionManager s_actionManager;
  private static TraffixFrame s_mainFrame;
  private static Model s_model;
  private static SessionFactory s_sesfac;

  private static Traffix s_instance;

  public static IActionManager actionManager() {
    return s_actionManager;
  }

  public static void appendFilesToHelpMenu(MenuManager helpMenu) {
    File helpDir = new File("help");
    File[] files = helpDir.listFiles();
    for (final File f : files) {
      String name = f.getName();
      int p = name.lastIndexOf('.');
      if (p != -1) {
        String ext = name.substring(p + 1).toLowerCase();
        if (ext.equals("png")) {
          Action a = new Action() {
            public void run() {
              openHelpPicture(f);
            }
          };
          a.setText(name.substring(0, p));
          helpMenu.add(a);
        }
      }
    }
  }

  public static Databanks databanks() {
    return simManager().databanks();
  }

  public static void error(String msg) {
    MessageBox dlg = new MessageBox(shell(), SWT.ICON_ERROR | SWT.OK);
    dlg.setText(NAME);
    dlg.setMessage(msg);
    dlg.open();
  }

  public static void inform(String msg) {
    MessageBox dlg = new MessageBox(shell(), SWT.ICON_INFORMATION | SWT.OK);
    dlg.setText(NAME);
    dlg.setMessage(msg);
    dlg.open();
  }

  public static void main(String[] args) {
    get().run();
  }

  public static Model model() {
    return s_model;
  }

  public static IAccidentModel newAccidentModel() {
    return new AccidentModel();
  }

  public static void openHelpPicture(File f) {
    HelpPictureFrame fr = new HelpPictureFrame(null, f);
    fr.setBlockOnOpen(false);
    fr.open();
  }

  public static SessionFactory persistenceSessionFactory() {
    if (s_sesfac != null) {
      return s_sesfac;
    }
    s_sesfac = new SessionFactory();
    ClassidBindings b = new ClassidBindings();
    s_sesfac.addBindings(b);
    return s_sesfac;
  }

  public static void reportException(Throwable e) {
    e.printStackTrace();
    String title = "B��d!";
    String msg = "Nast�pi� wyj�tek programu " + NAME + "!\n" + e;
    msg = msg + "\n\nNast�pi� w:\n";
    StackTraceElement st[] = e.getStackTrace();
    for (int i = 0; i < st.length; ++i)
      msg = msg + st[i] + "\n";
    MessageBox dlg = new MessageBox(shell(), SWT.ICON_ERROR);

    dlg.setText(title);
    dlg.setMessage(msg);
    dlg.open();
  }

  public static ScheduleBank scheduleBank() {
    return model().getScheduleBank();
  }

  public static Shell shell() {
    return s_mainFrame.getShell();
  }

  public static EntityManager simEntityManager() {
    return simManager().entityManager();
  }

  public static ISimManager simManager() {
    return model().getActiveSimManager();
  }

  public static ScheduleManager simScheduleManager() {
    return simManager().scheduleManager();
  }

  public static Traffix get() {
    if (s_instance == null) {
      s_instance = new Traffix();
    }
    return s_instance;
  }

  private void run() {
    try {
      Config.load();
    } catch (MalformedFileException e) {
      e.printStackTrace();
    }

    // init cursors
    try {
      Cursors.class.getClassLoader().loadClass(Cursors.class.getName());
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    s_actionManager = new ActionManager();
    s_model = new Model();
    s_model.reset();
    s_mainFrame = new TraffixFrame(null);

    s_mainFrame.setBlockOnOpen(true);
    s_mainFrame.open();
    Display.getDefault().dispose();
  }
}