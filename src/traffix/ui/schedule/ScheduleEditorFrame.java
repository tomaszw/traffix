/*
 * Created on 2004-06-27
 */

package traffix.ui.schedule;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.tw.patterns.observer.IUpdateListener;
import org.tw.web.XmlKit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import traffix.Traffix;
import traffix.core.actionframework.IActionManager;
import traffix.core.actionframework.IActionManagerListener;
import traffix.core.model.Model;
import traffix.core.schedule.BadFormatException;
import traffix.core.schedule.LightTypes;
import traffix.core.schedule.Schedule;
import traffix.ui.Images;
import traffix.ui.Keymap;
import traffix.ui.misc.SwtKit;
import za.co.quirk.layout.LatticeData;
import za.co.quirk.layout.LatticeLayout;

public class ScheduleEditorFrame extends Window {
  static final int        LIMAGE_H                = 24;
  static final int        LIMAGE_W                = 24;
  Menu                    m_cellWidthMenu;
  Schedule                m_editedSchedule;
  ScheduleEditorWidget          m_editor;

  ToolItem                m_insertItem, m_cellWidthItem;
  Menu                    m_insertMenu;
  Image[]                 m_lightImg;
  ToolBar                 m_toolBar;
  ToolItem                m_undo, m_redo;
  
  private Action          m_help                  = new Help();
  private ToolItem        m_helpItem;
  private Menu            m_helpMenu;

  private Schedule        m_referenceSchedule;
  private IInputValidator m_scheduleNameValidator = new IInputValidator() {
                                                    public String isValid(String newText) {
                                                      if (newText.equals(""))
                                                        return "Nazwa nie mo¿e byæ pusta.";
                                                      if (Traffix.model().getSchedule(
                                                          newText) != null)
                                                        return "Nazwa jest zajêta.";
                                                      return null;
                                                    }
                                                  };

  private class Help extends Action {
    public Help() {
      setText("Pomoc");
      setToolTipText("Poka¿ pomoc");
      setImageDescriptor(Images.getDescriptor("icons/qmark.gif"));
      setMenuCreator(new IMenuCreator() {
        public void dispose() {
        }

        public Menu getMenu(Control parent) {
          MenuManager helpMenu = new MenuManager("P&omoc");
          Traffix.appendFilesToHelpMenu(helpMenu);
          return helpMenu.createContextMenu(parent);
        }

        public Menu getMenu(Menu parent) {
          MenuManager helpMenu = new MenuManager();
          Traffix.appendFilesToHelpMenu(helpMenu);
          Menu menu = new Menu(parent);
          helpMenu.fill(menu, 0);
          return menu;
        }
      });
    }
    public void run() {
    }
  }

  public ScheduleEditorFrame(Shell parent) {
    super(parent);
    setShellStyle(getShellStyle());// | SWT.SYSTEM_MODAL);
  }

  public boolean close() {
    if (m_referenceSchedule.equals(m_editedSchedule))
      return super.close();
    String message = "'" + m_editedSchedule.getName() + "' zosta³ zmodyfikowany! ";
    int coll = m_editedSchedule.getTotalNumOfCollisions(Traffix.model().getMcmMatrix());
    if (coll > 0) {
      message = message + coll + " SKOLIDOWANYCH sekund. ";
    }
    message = message + "Zapamiêtaæ zmiany?";
    String[] buttons = { "Zapisz", "Zapisz pod inn¹ nazw¹", "Odrzuæ", "Anuluj" };
    MessageDialog dlg = new MessageDialog(getShell(), Traffix.NAME, null, message,
        MessageDialog.QUESTION, buttons, 0);
    int ret = dlg.open();
    if (ret == 0) {
      m_referenceSchedule.assign(m_editedSchedule);
      Traffix.model().setModified(true);
      Traffix.model().fireUpdated(Model.EVT_CHANGE_SCHEDULE, null);
      return super.close();
    } else if (ret == 1) {
      String msg = "Wybierz nazwê zapisywanego harmonogramu";
      String init = m_editedSchedule.getName();
      InputDialog dlg2 = new InputDialog(getShell(), Traffix.NAME, msg, init,
          m_scheduleNameValidator);
      if (dlg2.open() == InputDialog.OK) {
        m_editedSchedule.setName(dlg2.getValue());
        Traffix.model().addSchedule(m_editedSchedule);
        return super.close();
      } else
        return false;
    } else if (ret == 2) {
      return super.close();
    }
    return false;
  }

  public void setEditedSchedule(Schedule editedSchedule) {
    m_editedSchedule = editedSchedule;
    if (m_editor != null)
      m_editor.setSchedule(editedSchedule);
  }

  public void setReferenceSchedule(Schedule schedule) {
    m_referenceSchedule = schedule;
  }

  protected void configureShell(final Shell shell) {
    super.configureShell(shell);
    allocResources(shell);
    shell.setImage(Images.get("icons/schedule.gif"));
    shell.setLayout(new FillLayout());
    shell.setSize(640, 480);
    updateTitle(shell);

    final IActionManagerListener amListener = new IActionManagerListener() {
      public void actionPerformed() {
        updateUndoRedoButtons();
      }

      public void redoPerformed() {
        updateUndoRedoButtons();
      }

      public void undoPerformed() {
        updateUndoRedoButtons();
      }
    };

    Traffix.actionManager().addListener(amListener);

    final IUpdateListener tmListener = new IUpdateListener() {
      public void onUpdate(int hint, Object data) {
        updateTitle(shell);
      }
    };
    Traffix.model().addUpdateListener(tmListener);

    shell.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        Traffix.actionManager().removeListener(amListener);
        Traffix.actionManager().clearUndoHistory();
        Traffix.model().removeUpdateListener(tmListener);
      }
    });
  }

  protected Control createContents(Composite parent) {
    Composite contents = new Composite(parent, SWT.NONE);
    double[][] size = { { LatticeLayout.FILL },
      { LatticeLayout.PREFERRED, LatticeLayout.FILL } };
    LatticeLayout layout = new LatticeLayout(size);
    contents.setLayout(layout);

    m_toolBar = new ToolBar(contents, SWT.FLAT | SWT.WRAP);
    m_toolBar.setLayoutData(new LatticeData("0,0"));

    m_editor = new ScheduleEditorWidget(contents, SWT.BORDER);
    m_editor.setLayoutData(new LatticeData("0,1"));
    m_editor.setSchedule(m_editedSchedule);
    m_editor.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if (e.keyCode == Keymap.SCREENSHOT_KEY)
          onScreenshot();
      }

      public void keyReleased(KeyEvent e) {
      }
    });

    //
    LightPainter painter = new LightPainter();
    m_lightImg = painter.buildLightImages(getShell().getDisplay(), LIMAGE_W, LIMAGE_H);

    getShell().addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        for (int i = 0; i < m_lightImg.length; ++i)
          m_lightImg[i].dispose();
      }
    });

    fillToolBar();

    m_editor.setFocus();
    return contents;
  }

  private void fillToolBar() {
    ToolItem item = new ToolItem(m_toolBar, SWT.NONE);
    item.setText("Zrzut");
    item.setToolTipText("Kopiuj obraz harmonogramu do schowka");
    item.setImage(Images.get("icons/clipboard.gif"));
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        onScreenshot();
      }
    });

    item = new ToolItem(m_toolBar, SWT.NONE);
    item.setText("Kolory");
    item.setToolTipText("Zmieñ kolorystykê diagramu");
    item.setImage(Images.get("icons/colors.gif"));
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        onChangeColors();
      }
    });

    item = new ToolItem(m_toolBar, SWT.NONE);
    item.setText("Eksport");
    item.setToolTipText("Eksportuj harmonogram do zewnêtrznego pliku");
    item.setImage(Images.get("icons/export.gif"));
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        onScheduleExport();
      }
    });

    item = new ToolItem(m_toolBar, SWT.NONE);
    item.setText("Do³¹cz");
    item.setToolTipText("Do³¹cz zewnêtrzny harmonogram do aktualnego");
    item.setImage(Images.get("icons/import.gif"));
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        onScheduleAppend();
      }
    });

    new ToolItem(m_toolBar, SWT.SEPARATOR);

    item = new ToolItem(m_toolBar, SWT.DROP_DOWN);
    item.setText("Wstaw");
    item.setToolTipText("Wstaw œwiat³o");
    item.setImage(Images.get("icons/insert.gif"));
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        onInsert();
      }
    });
    m_insertItem = item;

    // build insert popup menu
    m_insertMenu = new Menu(m_toolBar);

    for (int i = 0; i < LightTypes.NUM_LIGHTS; ++i) {
      MenuItem mi = new MenuItem(m_insertMenu, SWT.NONE);
      mi.setText(LightTypes.names[LightTypes.s_lightTypesMenuOrder[i]]);
      mi.setImage(m_lightImg[LightTypes.s_lightTypesMenuOrder[i]]);
      final int light = i;
      mi.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          selectInsert(LightTypes.s_lightTypesMenuOrder[light]);
        }
      });
    }

    new ToolItem(m_toolBar, SWT.SEPARATOR);
    item = new ToolItem(m_toolBar, SWT.NONE);
    item.setText("Cykl");
    item.setImage(Images.get("icons/cycleLen.gif"));
    item.setToolTipText("Zmieñ d³ugoœæ cyklu");
    item.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        onChangeCycleLen();
      }
    });

    ToolItem c1 = new ToolItem(m_toolBar, SWT.NONE);
    c1.setText("1");
    c1.setToolTipText("Poka¿ 1 cykl");
    ToolItem c2 = new ToolItem(m_toolBar, SWT.NONE);
    c2.setText("2");
    c2.setToolTipText("Poka¿ 2 cykle");

    c1.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        m_editor.setNumCycles(1);
      }
    });
    c2.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        m_editor.setNumCycles(2);
      }
    });

    new ToolItem(m_toolBar, SWT.SEPARATOR);
    m_undo = new ToolItem(m_toolBar, SWT.NONE);
    m_undo.setText("Cofnij");
    m_undo.setToolTipText("Cofnij ostatni¹ akcjê");
    m_undo.setImage(Images.get("icons/undo.gif"));
    m_undo.setEnabled(false);
    m_undo.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        onUndo();
      }
    });

    // undo.setEnabled(false);
    m_redo = new ToolItem(m_toolBar, SWT.NONE);
    m_redo.setText("Przywróæ");
    m_redo.setToolTipText("Przywróæ ostatni¹ akcjê");
    m_redo.setImage(Images.get("icons/redo.gif"));
    m_redo.setEnabled(false);
    m_redo.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        onRedo();
      }
    });
    // redo.setEnabled(false);
    new ToolItem(m_toolBar, SWT.SEPARATOR);
    m_cellWidthItem = new ToolItem(m_toolBar, SWT.DROP_DOWN);
    m_cellWidthItem.setText("Szerokoœæ");
    m_cellWidthItem.setToolTipText("Ustal szerokoœæ pojedynczej komórki");
    m_cellWidthItem.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        onCellWidth();
      }
    });
    // width popup menu
    m_cellWidthMenu = new Menu(m_toolBar);
    final int[] dims = { 10, 12, 14, 8, 6 };
    for (int i = 0; i < dims.length; ++i) {
      final MenuItem mi = new MenuItem(m_cellWidthMenu, SWT.NONE);
      mi.setText(dims[i] + " pikseli");
      final int index = i;
      mi.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          m_editor.setDefaultCellSize(dims[index]);
        }
      });
    }

    new ToolItem(m_toolBar, SWT.SEPARATOR);
    m_helpItem = new ToolItem(m_toolBar, SWT.DROP_DOWN);
    m_helpItem.setText("Pomoc");
    m_helpItem.setToolTipText("Pomoc");
    m_helpItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onHelp();
      }
    });
    m_helpItem.setImage(Images.get("icons/qmark.gif"));
    m_helpMenu = new Menu(m_toolBar);
    File helpDir = new File("help");
    File[] files = helpDir.listFiles();
    for (final File f : files) {
      String name = f.getName();
      int p = name.lastIndexOf('.');
      if (p != -1) {
        String ext = name.substring(p + 1).toLowerCase();
        if (ext.equals("png")) {
          final MenuItem mi = new MenuItem(m_helpMenu, SWT.NONE);
          mi.setText(name.substring(0, p));
          mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
              Traffix.openHelpPicture(f);
            }
          });
        }
      }
    }
  }

  void selectInsert(int num) {
    m_editor.insertBegin(num);
  }

  private void allocResources(Shell shell) {
  }

  private void onCellWidth() {
    Rectangle bounds = m_cellWidthItem.getBounds();
    Point p = m_toolBar.toDisplay(bounds.x, bounds.y);
    p.y += bounds.height;
    m_cellWidthMenu.setLocation(p.x, p.y);
    m_cellWidthMenu.setVisible(true);
  }

  private void onChangeColors() {
    if (m_editor != null)
      m_editor.changeColors();
  }

  private void onChangeCycleLen() {
    IInputValidator validator = new IInputValidator() {
      public String isValid(String newText) {
        int len = -1;
        try {
          len = new Integer(newText).intValue();
          if (len < 1)
            len = -1;
        } catch (NumberFormatException e) {
          len = -1;
        }
        if (len != -1)
          return null;
        return "B³êdna d³ugoœæ cyklu";
      }
    };

    Schedule sch = m_editedSchedule;
    InputDialog dlg = new InputDialog(getShell(), "Zmiana d³ugoœci cyklu",
        "D³ugoœæ cyklu w sekundach", Integer.toString(sch.getProgramLength()), validator);
    dlg.setBlockOnOpen(true);
    dlg.open();
    if (dlg.getReturnCode() == InputDialog.OK) {
      int val = new Integer(dlg.getValue()).intValue();
      sch.setProgramLength(val);
      // Traffix.getModel().setModified(true);
      Traffix.model().fireUpdated(Model.EVT_CHANGE_SCHEDULE, null);
    }

  }

  private void onHelp() {
    Rectangle bounds = m_helpItem.getBounds();
    Point p = m_toolBar.toDisplay(bounds.x, bounds.y);
    p.y += bounds.height;
    m_helpMenu.setLocation(p.x, p.y);
    m_helpMenu.setVisible(true);
  }

  private void onInsert() {
    Rectangle bounds = m_insertItem.getBounds();
    Point p = m_toolBar.toDisplay(bounds.x, bounds.y);
    p.y += bounds.height;
    m_insertMenu.setLocation(p.x, p.y);
    m_insertMenu.setVisible(true);
  }

  private void onRedo() {
    Traffix.actionManager().redo();
    updateUndoRedoButtons();
  }

  private void onScheduleAppend() {
    if (m_editedSchedule != null) {
      FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
      dlg.setText("Import harmonogramu");
      dlg.setFilterExtensions(new String[] { "*.har", "*.*" });
      String filename = dlg.open();
      if (filename != null) {
        Document doc = XmlKit.loadDoc(filename);
        if (doc != null) {
          Schedule other = new Schedule();
          if (!other.xmlLoad(doc, doc.getDocumentElement())) {
            Traffix.error("B³êdny format do³¹czanego harmonogramu");
            return;
          }
          try {
            m_editedSchedule.append(other);
          } catch (BadFormatException e) {
            Traffix.error("B³êdna iloœæ grup w do³¹czanym harmonogramie");
            return;
          }
          // Traffix.getModel().setModified(true);
          Traffix.model().fireUpdated(Model.EVT_CHANGE_SCHEDULE, null);
        }
      }
    }
  }

  private void onScheduleExport() {
    if (m_editedSchedule != null) {
      FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
      dlg.setText("Eksport harmonogramu");
      dlg.setFilterExtensions(new String[] { "*.har", "*.*" });
      dlg.setFileName(m_editedSchedule.getName() + ".har");
      String filename = dlg.open();
      if (filename != null) {
        Document doc = XmlKit.createDoc();
        Element e = m_editedSchedule.xmlSave(doc);
        doc.appendChild(e);
        XmlKit.saveDoc(filename, doc);
      }
    }
  }

  private void onScreenshot() {
    if (m_editor != null) {
      Image image = m_editor.createScreenshot();
      SwtKit.swtImageToClipboard(getShell(), image);
    }
  }

  private void onUndo() {
    Traffix.actionManager().undo();
    updateUndoRedoButtons();
  }

  private void updateTitle(Shell shell) {
    String txt = "Edycja harmonogramu ";
    if (m_editedSchedule != null) {
      txt = txt + m_editedSchedule.getName() + " : cykl ";
      txt = txt + m_editedSchedule.getProgramLength() + "s";
      shell.setText(txt);
    }
  }

  private void updateUndoRedoButtons() {
    IActionManager am = Traffix.actionManager();
    m_undo.setEnabled(am.canUndo());
    if (am.canUndo())
      m_undo.setToolTipText("Cofnij '" + am.getTopUndoAction().getName() + "'");
    else
      m_undo.setToolTipText("Cofnij");
    m_redo.setEnabled(am.canRedo());
    if (am.canRedo())
      m_redo.setToolTipText("Przywróæ '" + am.getTopRedoAction().getName() + "'");
    else
      m_redo.setToolTipText("Przywróæ");
  }
}