/*
 * Created on 2004-08-25
 */

package traffix.ui;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Item;
import traffix.Traffix;
import traffix.core.model.Model;
import traffix.core.schedule.Schedule;
import traffix.core.sim.entities.FreezeLink;
import traffix.core.sim.entities.IDetector;
import traffix.core.sim.entities.TransitDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

public class FreezeLinksCheckboxTable extends Composite {
  private Table m_linksTable;
  private CheckboxTableViewer m_linksTableViewer;
  private List<FreezeLink> m_allLinks;
  private String[] m_columnNames = {
    "Detektor", "Tmax"
  };

  class FreezeLabelProvider extends LabelProvider implements ITableLabelProvider {
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }


    public String getColumnText(Object element, int columnIndex) {
      FreezeLink l = (FreezeLink) element;
      if (columnIndex == 0)
        return l.linkedDetector.getName();
      else if (columnIndex == 1)
        return Integer.toString(l.tmax);
      return "";
    }
  }

  class FreezeLinksModifier implements ICellModifier {
    public boolean canModify(Object element, String property) {
      int id = Arrays.asList(m_columnNames).indexOf(property);
      if (id == 1)
        return true;
      return false;
    }

    public Object getValue(Object element, String property) {
      FreezeLink l = (FreezeLink) element;
      int id = Arrays.asList(m_columnNames).indexOf(property);
      if (id == 1)
        return Integer.toString(l.tmax);
      return null;
    }

    public void modify(Object element, String property, Object value) {
      int id = Arrays.asList(m_columnNames).indexOf(property);

      if (element instanceof Item)
        element = ((Item) element).getData();

      FreezeLink l = (FreezeLink) element;
      
      if (id == 1) {
        try {
          l.tmax = Integer.parseInt((String)value);
        } catch (Exception e) {
        }
      }
      m_linksTableViewer.refresh();
    }
  }

  public Table asTable() {
    return m_linksTable;
  }

  public CheckboxTableViewer asViewer() {
    return m_linksTableViewer;
  }

  public List<FreezeLink> getFreezeLinks() {
    Object[] elems = m_linksTableViewer.getCheckedElements();
    List<FreezeLink> links = new ArrayList<FreezeLink>();
    for (Object e : elems)
      links.add((FreezeLink)e);
    return links;
  }

  public void setFreezeLinks(List<FreezeLink> linksToSet) {
    HashMap<String, FreezeLink> setOfLinksToSet = new HashMap<String, FreezeLink>();
    for (FreezeLink l : linksToSet) setOfLinksToSet.put(l.linkedDetector.getName(), l);
    List<FreezeLink> setLinksList = new ArrayList<FreezeLink>();
    for (FreezeLink l : m_allLinks) {
      if (setOfLinksToSet.containsKey(l.linkedDetector.getName())) {
        l.assign(setOfLinksToSet.get(l.linkedDetector.getName()));
        setLinksList.add(l);
      }
    }
    m_linksTableViewer.setCheckedElements(setLinksList.toArray());
  }

  public Point computeSize(int wHint, int hHint, boolean changed) {
    if (wHint == SWT.DEFAULT)
      wHint = 150;
    if (hHint == SWT.DEFAULT)
      hHint = 160;
    return new Point(wHint, hHint);
  }

  public FreezeLinksCheckboxTable(Composite parent, int style) {
    super(parent, style);
    setLayout(new FillLayout());

    List<IDetector> list = Traffix.model().getActiveSimManager().getDetectors();
    m_allLinks = new ArrayList<FreezeLink>();
    for (IDetector d : list) {
      if (!(d instanceof TransitDetector)) {
        FreezeLink newLink = new FreezeLink();
        newLink.linkedDetector = d;//.getName();
        newLink.tmax = 0;
        m_allLinks.add(newLink);
      }
    }

    TableColumn col;
    m_linksTable = new Table(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
      | SWT.CHECK | SWT.FULL_SELECTION);
    m_linksTable.setHeaderVisible(true);
    //m_groupsTable.setLinesVisible(true);
    //m_groupsTable.setSize(200,200);
    col = new TableColumn(m_linksTable, SWT.LEFT);
    col.setText(m_columnNames[0]);
    col.setWidth(95);
    col = new TableColumn(m_linksTable, SWT.LEFT);
    col.setText(m_columnNames[1]);
    col.setWidth(45);
    m_linksTableViewer = new CheckboxTableViewer(m_linksTable);
    m_linksTableViewer.setColumnProperties(m_columnNames);
    m_linksTableViewer.setContentProvider(createContentProvider());
    m_linksTableViewer.setLabelProvider(createLabelProvider());
    m_linksTableViewer.setCellModifier(createCellModifier());
    m_linksTableViewer.setInput(Traffix.model());

    CellEditor[] editors = new CellEditor[2];
    editors[1] = new TextCellEditor(m_linksTable);
    m_linksTableViewer.setCellEditors(editors);
    col.pack();
  }

  private IContentProvider createContentProvider() {
    return new IStructuredContentProvider() {
      public void dispose() {
      }

      public Object[] getElements(Object inputElement) {
        return m_allLinks.toArray();
      }

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      }
    };
  }

  private ILabelProvider createLabelProvider() {
    return new FreezeLabelProvider();
  }

  private ICellModifier createCellModifier() {
    return new FreezeLinksModifier();
  }
}