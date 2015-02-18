/*
 * Created on 2004-07-10
 */

package traffix.automation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleClientSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Composite;

public class Excel {

  OleAutomation m_excel;
  OleFrame m_frame;
  OleClientSite m_site;

  public Excel(Composite parent) {
    m_frame = new OleFrame(parent, SWT.NONE);
    m_site = new OleClientSite(m_frame, SWT.NONE, "Excel.Application");
    m_excel = new OleAutomation(m_site);
  }

  public void addWorkbook() {
    OleAutomation workbooksAuto = getWorkbooks();
    workbooksAuto.invoke(getDispId(workbooksAuto, "Add"));
  }

  public void dispose() {
    m_excel.dispose();
    m_site.dispose();
    m_frame.dispose();
  }

  public String getCellName(int x, int y) {
    int numletters = 'Z' - 'A' + 1;
    String colstr = "" + (char) (x%numletters + 'A');
    if (x >= numletters) {
      x -= numletters;
      colstr = "A" + colstr;
    }

    return colstr + Integer.toString(y + 1);
  }

  public OleAutomation getWorksheet(int id) {
    OleAutomation ws = getWorksheets();
    Variant worksheet = ws.getProperty(getDispId(ws, "Item"), new Variant[]{new Variant(id)});
    return worksheet.getAutomation();
  }

  public void setCellBold(OleAutomation sheet, String cell, boolean bold) {
    OleAutomation cellAuto = sheet.getProperty(getDispId(sheet, "Range"),
      new Variant[]{new Variant(cell)}).getAutomation();
    OleAutomation font = cellAuto.getProperty(getDispId(cellAuto, "Font"))
      .getAutomation();
    font.setProperty(getDispId(font, "Bold"), new Variant(bold));
  }

  public void setCellColor(OleAutomation sheet, String cell, String color) {
    OleAutomation cellAuto = sheet.getProperty(getDispId(sheet, "Range"),
      new Variant[]{new Variant(cell)}).getAutomation();
    OleAutomation interior = cellAuto.getProperty(getDispId(cellAuto, "Interior"))
      .getAutomation();
    interior.setProperty(getDispId(interior, "Color"), new Variant(color));
  }

  public void setCellValue(OleAutomation sheet, String cell, String value) {
    OleAutomation cellAuto = sheet.getProperty(getDispId(sheet, "Range"),
      new Variant[]{new Variant(cell)}).getAutomation();
    cellAuto.setProperty(getDispId(cellAuto, "Value"), new Variant(value));
  }

  public void setVisible(boolean visible) {
    int id = getDispId("Visible");
    m_excel.setProperty(id, new Variant(visible));
  }

  private int getDispId(OleAutomation automation, String name) {
    int[] ids = automation.getIDsOfNames(new String[]{name});
    return ids[0];
  }

  private int getDispId(String name) {
    return getDispId(m_excel, name);
  }

  private OleAutomation getWorkbooks() {
    Variant workbooks = m_excel.getProperty(getDispId("Workbooks"));
    OleAutomation workbooksAuto = workbooks.getAutomation();
    return workbooksAuto;
  }

  private OleAutomation getWorksheets() {
    Variant worksheets = m_excel.getProperty(getDispId("Worksheets"));
    OleAutomation worksheetsAuto = worksheets.getAutomation();
    return worksheetsAuto;
  }

  public void alignRight(OleAutomation sheet, String cell) {
    Automation sheetAuto = Automation.from(sheet);
    Automation cellAuto = Automation.from(sheetAuto.getProperty("Range", new Variant(cell)));
    cellAuto.setProperty("horizontalAlignment", new Variant(-4152));
  }
}
