/*
 * Created on 2004-07-04
 */

package traffix.core;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import traffix.core.model.IPersistent;

public class McmMatrix implements IPersistent, Cloneable {

  private Cell[][] m_cells;

  static class Cell implements Cloneable {
    boolean colliding;
    int     mcmTime;

    @Override
    public Cell clone() {
      try {
        Cell cloned = (Cell) super.clone();
        return cloned;
      } catch (CloneNotSupportedException e) {
        e.printStackTrace();
        return null;
      }
    }
  }

  public static McmMatrix createEmptyMatrix(int dim) {
    McmMatrix m = new McmMatrix();
    m.m_cells = new Cell[dim][dim];
    for (int i = 0; i < dim; i++) {
      for (int j = 0; j < dim; j++) {
        m.m_cells[i][j] = new Cell();
      }
    }
    return m;
  }

  public static McmMatrix importFromMcmXml(Element root) {
    try {
      NodeList cellNodes = root.getElementsByTagName("cell");
      // find matrix dimensions
      int dimi = 0, dimj = 0;
      for (int i = 0; i < cellNodes.getLength(); ++i) {
        Element cellElem = (Element) cellNodes.item(i);
        int cellI = Integer.parseInt(cellElem.getAttribute("i"));
        int cellJ = Integer.parseInt(cellElem.getAttribute("j"));
        dimi = Math.max(dimi, cellI);
        dimj = Math.max(dimj, cellJ);
      }
      if (dimi == 0 || dimj == 0) {
        // error
        return null;
      }
      ++dimi;
      ++dimj;

      McmMatrix mat = new McmMatrix(dimi, dimj);
      for (int i = 0; i < cellNodes.getLength(); ++i) {
        Element cellElem = (Element) cellNodes.item(i);
        int cellI = Integer.parseInt(cellElem.getAttribute("i"));
        int cellJ = Integer.parseInt(cellElem.getAttribute("j"));

        boolean colliding = !cellElem.getAttribute("colliding").equals("0");
        int mcmTime = Integer.parseInt(cellElem.getAttribute("mcmTime"));

        mat.m_cells[cellI][cellJ].colliding = colliding;
        mat.m_cells[cellI][cellJ].mcmTime = mcmTime;
      }

      return mat;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public McmMatrix() {
  }

  public McmMatrix(int dimi, int dimj) {
    init(dimi, dimj);
  }

  public boolean areColliding(int i, int j) {
    return m_cells[i][j].colliding;
  }

  @Override
  public McmMatrix clone() {
    try {
      McmMatrix cloned = (McmMatrix) super.clone();
      cloned.m_cells = new Cell[m_cells.length][];
      for (int i = 0; i < m_cells.length; ++i) {
        cloned.m_cells[i] = new Cell[m_cells[i].length];
        for (int j = 0; j < m_cells[i].length; ++j)
          cloned.m_cells[i][j] = m_cells[i][j].clone();
      }
      return cloned;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  }

  public int getDim() {
    return m_cells.length;
  }

  public int getMcmTime(int i, int j) {
    return m_cells[i][j].mcmTime;
  }

  public String getXmlTagName() {
    return "mcm-matrix";
  }

  public void init(int dimi, int dimj) {
    m_cells = new Cell[dimi][dimj];
    for (int i = 0; i < dimi; ++i)
      for (int j = 0; j < dimj; ++j)
        m_cells[i][j] = new Cell();
  }

  public McmMatrix merge(McmMatrix other) {
    int dim1 = getDim();
    int dim2 = other.getDim();
    McmMatrix m = createEmptyMatrix(dim1 + dim2);
    for (int i = 0; i < dim1; ++i)
      for (int j = 0; j < dim1; ++j)
        m.m_cells[i][j] = m_cells[i][j].clone();
    for (int i = 0; i < dim2; ++i)
      for (int j = 0; j < dim2; ++j)
        m.m_cells[i + dim1][j + dim1] = other.m_cells[i][j].clone();
    return m;
  }

  public void swapCols(int a, int b) {
    for (int i = 0; i < getDim(); ++i) {
      Cell tmp = m_cells[i][a];
      m_cells[i][a] = m_cells[i][b];
      m_cells[i][b] = tmp;
    }
  }

  public void swapRows(int a, int b) {
    Cell[] tmp = m_cells[a];
    m_cells[a] = m_cells[b];
    m_cells[b] = tmp;
  }

  public boolean xmlLoad(Document document, Element root) {
    if (!root.getTagName().equals("mcm-matrix"))
      return false;

    NodeList cellNodes = root.getElementsByTagName("cell");
    // find matrix dimensions
    int dimi = 0, dimj = 0;
    for (int i = 0; i < cellNodes.getLength(); ++i) {
      Element cellElem = (Element) cellNodes.item(i);
      int cellI = Integer.parseInt(cellElem.getAttribute("i"));
      int cellJ = Integer.parseInt(cellElem.getAttribute("j"));
      dimi = Math.max(dimi, cellI);
      dimj = Math.max(dimj, cellJ);
    }
    if (dimi == 0 || dimj == 0) {
      // error
      return false;
    }
    ++dimi;
    ++dimj;

    Cell[][] cells = new Cell[dimi][dimj];
    for (int i = 0; i < cellNodes.getLength(); ++i) {
      Element cellElem = (Element) cellNodes.item(i);
      int cellI = Integer.parseInt(cellElem.getAttribute("i"));
      int cellJ = Integer.parseInt(cellElem.getAttribute("j"));

      boolean colliding = !cellElem.getAttribute("colliding").equals("0");
      int mcmTime = Integer.parseInt(cellElem.getAttribute("mcmTime"));

      cells[cellI][cellJ] = new Cell();
      cells[cellI][cellJ].colliding = colliding;
      cells[cellI][cellJ].mcmTime = mcmTime;
    }

    m_cells = cells;
    return true;
  }

  public Element xmlSave(Document document) {
    Element root = document.createElement("mcm-matrix");
    for (int i = 0; i < m_cells.length; ++i) {
      for (int j = 0; j < m_cells[i].length; ++j) {
        Cell cell = m_cells[i][j];
        Element cellElem = document.createElement("cell");
        root.appendChild(cellElem);
        cellElem.setAttribute("i", Integer.toString(i));
        cellElem.setAttribute("j", Integer.toString(j));
        String colliding = cell.colliding ? "1" : "0";
        cellElem.setAttribute("colliding", colliding);
        cellElem.setAttribute("mcmTime", Integer.toString(cell.mcmTime));
      }
    }
    return root;
  }
}
