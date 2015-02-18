/*
 * Created on 2004-08-26
 */

package traffix.core.sim;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.tw.geometry.Vec2f;
import org.tw.geometry.Rectanglef;
import traffix.Traffix;
import traffix.ui.BigImage;
import traffix.ui.Colors;
import traffix.ui.Gc;
import traffix.ui.ListSelectionDialog;
import traffix.ui.sim.CoordinateTransformer;
import traffix.ui.sim.entities.IUiEntity;
import traffix.ui.sim.entities.IUiEntityContextProvider;
import traffix.ui.sim.entities.UiEntityContext;

import javax.media.*;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class MovieMaker
    implements
      IUiEntityContextProvider,
      ControllerListener,
      DataSinkListener {
  public static final String DESCR               = FileTypeDescriptor.MSVIDEO;
  public static final float  MIN_TIME_STEP       = 1.0f / 15.0f;
  int                        m_numFrames;
  IProgressMonitor           m_progressMonitor;
  private BigImage           m_bgImage;
  private Point              m_dstSize;
  private boolean            m_fileDone;
  private boolean            m_fileSuccess       = true;
  private Rectangle          m_srcRc;
  private boolean            m_stateTransitionOk = true;
  private float              m_timeStep          = 1.0f / 25.0f;
  UiEntityContext            m_uiContext;
  private Object             m_waitSync          = new Object();

  class MovieTransformer extends CoordinateTransformer {
    private Rectanglef m_mapWnd;

    public MovieTransformer(Rectanglef mapWnd, Vec2f mapDims, Point wndDims) {
      super(mapDims, wndDims);
      m_mapWnd = mapWnd;
    }

    public float screenToTerrain(int dim) {
      return dim * m_mapWnd.width / getScreenDimension().x;
    }

    public Vec2f screenToTerrain(int x, int y) {
      float tx = x * m_mapWnd.width / getScreenDimension().x + m_mapWnd.x;
      float ty = (-y * m_mapWnd.height / getScreenDimension().y + m_mapWnd.y);
      return new Vec2f(tx, ty);
    }

    public int terrainToScreen(float dim) {
      return (int) (dim * getScreenDimension().x / m_mapWnd.width);
    }

    public Point terrainToScreen(float x, float y) {
      int sx = (int) ((x - m_mapWnd.x) * getScreenDimension().x / m_mapWnd.width);
      int sy = -(int) ((y - m_mapWnd.y) * getScreenDimension().y / m_mapWnd.height);
      return new Point(sx, sy);
    }
  }

  public MovieMaker(Rectanglef sourceRc, Point size) {
    m_dstSize = size;
    Vec2f mapSz = Traffix.simManager().getTerrainDims();
    Rectangle srcImgBounds = Traffix.simManager().getTerrainImage().getBounds();

    CoordinateTransformer sourceCt = new CoordinateTransformer(mapSz, new Point(srcImgBounds.width,
        srcImgBounds.height));

    // m_ctTransformer = new CoordTransformer(mapSz,
    // size);

    Point p = sourceCt.terrainToScreen(sourceRc.x, sourceRc.y);
    m_srcRc = new Rectangle(p.x, p.y, sourceCt.terrainToScreen(sourceRc.width), sourceCt
        .terrainToScreen(sourceRc.height));

    m_uiContext = new UiEntityContext();
    m_uiContext.simRunning = true;
    m_uiContext.setCoordTransformer(new MovieTransformer(sourceRc, mapSz, size));

  }

  public void controllerUpdate(ControllerEvent evt) {
    if (evt instanceof ConfigureCompleteEvent || evt instanceof RealizeCompleteEvent
        || evt instanceof PrefetchCompleteEvent) {
      synchronized (m_waitSync) {
        m_stateTransitionOk = true;
        m_waitSync.notifyAll();
      }
    } else if (evt instanceof ResourceUnavailableEvent) {
      synchronized (m_waitSync) {
        m_stateTransitionOk = false;
        m_waitSync.notifyAll();
      }
    } else if (evt instanceof EndOfMediaEvent) {
      evt.getSourceController().stop();
      evt.getSourceController().close();
    }
  }

  public void createImageSequence(File outputDir, int numFrames) {
    if (!outputDir.isDirectory())
      throw new IllegalArgumentException(outputDir.getName() + " is not a directory.");
    Traffix.simManager().rewindSimulation();
    NumberFormat format = new DecimalFormat("0000000");
    ImageLoader imageLdr = new ImageLoader();
    imageLdr.data = new ImageData[1];

    if (m_progressMonitor != null) {
      m_progressMonitor.beginTask("Generacja sekwencji " + numFrames + " obrazów",
          numFrames);
    }

    for (int i = 0; i < numFrames; ++i) {
      File outFile = new File(outputDir, format.format(i) + ".jpg");
      Image frame = newFrame();
      imageLdr.data[0] = frame.getImageData();
      imageLdr.save(outFile.getAbsolutePath(), SWT.IMAGE_JPEG);
      frame.dispose();
      if (m_progressMonitor != null) {
        m_progressMonitor.worked(1);
        while (Display.getDefault().readAndDispatch()) {
        }
        if (m_progressMonitor.isCanceled())
          return;
      }
    }
    if (m_progressMonitor != null)
      m_progressMonitor.done();
  }

  public void createMovie(File outputFile, int frameRate, int numFrames)
      throws MovieMakerException {
    m_numFrames = numFrames;
    m_timeStep = 1.0f / frameRate * Traffix.simManager().getSpeedFactor();
    // SimulationManager.get().rewindSimulation();

    if (m_progressMonitor != null) {
      m_progressMonitor.beginTask("Generacja filmu (" + numFrames + " ramek)", numFrames);
    }

    ImageDataSource src = new ImageDataSource(m_dstSize.x, m_dstSize.y, frameRate, this);
    Processor p = null;
    try {
      p = Manager.createProcessor(src);
    } catch (Exception e) {
      throw new MovieMakerException("B³ad podczas tworzenia procesora");
    }
    try {
      p.addControllerListener(this);
      p.configure();
      if (!waitForState(p, p.Configured)) {
        throw new MovieMakerException("B³¹d podczas konfigurowania procesora");
      }
      p.setContentDescriptor(new FileTypeDescriptor(DESCR));
      TrackControl tcs[] = p.getTrackControls();
      Format f[] = tcs[0].getSupportedFormats();
      if (f == null || f.length <= 0) {
        throw new MovieMakerException("B³¹d podczas konfigurowania formatu: "
            + tcs[0].getFormat());
      }
      ListSelectionDialog dlg = new ListSelectionDialog(Traffix.shell(), "Wybór formatu",
          "Wybierz format video");
      String[] items = new String[f.length];
      for (int i = 0; i < f.length; ++i) {
        items[i] = Integer.toString(i + 1) + ": " + f[i];
      }
      dlg.setItems(items);
      if (dlg.open() != dlg.OK) {
        return;
      }

      tcs[0].setFormat(f[dlg.getSelectedIndex()]);
      p.realize();
      if (!waitForState(p, p.Realized)) {
        throw new MovieMakerException("B³¹d podczas realizacji procesora");
      }

      // media location
      MediaLocator outML = null;
      try {
        outML = new MediaLocator(outputFile.toURL());
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }

      DataSink sink = null;
      if ((sink = createDataSink(p, outML)) == null) {
        throw new MovieMakerException("B³¹d podczas tworzenia DataSink");
      }
      sink.addDataSinkListener(this);

      try {
        p.start();
        sink.start();
      } catch (IOException e) {
        throw new MovieMakerException("B³¹d IO podczas przetwarzania: " + e.getMessage());
      }
      m_fileDone = false;
      waitForFileDone();

      sink.removeDataSinkListener(this);
      sink.close();

      if (m_progressMonitor != null) {
        m_progressMonitor.done();
      }
    } finally {
      p.removeControllerListener(this);
      p.close();
    }
  }

  public void dataSinkUpdate(DataSinkEvent evt) {
    if (evt instanceof EndOfStreamEvent) {
      m_fileDone = true;
    } else if (evt instanceof DataSinkErrorEvent) {
      m_fileDone = true;
      m_fileSuccess = false;
    }
  }

  public void dispose() {
    if (m_bgImage != null) {
      m_bgImage.dispose();
      m_bgImage = null;
    }
  }

  public UiEntityContext getUiEntityContext() {
    return m_uiContext;
  }

  public Image newFrame() {
    float step = m_timeStep;
    while (step > MIN_TIME_STEP) {
      Traffix.simManager().tick(MIN_TIME_STEP);
      step -= MIN_TIME_STEP;
    }

    Traffix.simManager().tick(step);
    Traffix.simManager().setFps(0);

    BigImage bgImage = Traffix.simManager().getTerrainImage();
    Image bgPortion = bgImage.getImagePortion(m_srcRc, m_dstSize);
    Image frameImage = new Image(Display.getDefault(), m_dstSize.x, m_dstSize.y);
    Gc gc = new Gc(new GC(frameImage));
    m_uiContext.setGc(gc);
    m_uiContext.filming = true;
    gc.drawImage(bgPortion, 0, 0);

    Traffix.simManager().setEntityContextProvider(this);
    List entities = Traffix.simManager().getUiEntities();
    // sort entities
    Collections.sort(entities, new Comparator() {
      public int compare(Object o1, Object o2) {
        IUiEntity e1 = (IUiEntity) o1;
        IUiEntity e2 = (IUiEntity) o2;
        if (e1.getPaintPriority() < e2.getPaintPriority())
          return -1;
        else if (e1.getPaintPriority() > e2.getPaintPriority())
          return 1;
        return 0;
      }
    });
    for (Iterator iter = entities.iterator(); iter.hasNext();) {
      IUiEntity e = (IUiEntity) iter.next();
      e.paint();
      e.dispose();
    }
    String time = Traffix.simManager().getSimulationMessage();
    Point sz = gc.textExtent(time);
    gc.setBackground(Colors.get(new RGB(255, 255, 0)));
    gc.setForeground(Colors.system(SWT.COLOR_BLACK));
    gc.drawText(time, 0, 0);

    gc.dispose();
    return frameImage;
  }

  public void setProgressMonitor(IProgressMonitor progressMonitor) {
    m_progressMonitor = progressMonitor;
  }

  public void setTimeStep(float timeStep) {
    m_timeStep = timeStep;
  }

  private DataSink createDataSink(Processor p, MediaLocator outML) {
    DataSource ds;

    if ((ds = p.getDataOutput()) == null) {
      return null;
    }

    DataSink dsink;

    try {
      dsink = Manager.createDataSink(ds, outML);
      dsink.open();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

    return dsink;
  }

  private boolean waitForFileDone() {
    try {
      while (!m_fileDone) {
        while (Display.getDefault().readAndDispatch()) {
        }
        if (m_progressMonitor != null) {
          if (m_progressMonitor.isCanceled())
            m_fileDone = true;
        }
      }
    } catch (Exception e) {
    }
    return m_fileSuccess;
  }

  private boolean waitForState(Processor p, int state) {
    synchronized (m_waitSync) {
      try {
        while (p.getState() < state && m_stateTransitionOk)
          m_waitSync.wait();
      } catch (Exception e) {
      }
    }
    return m_stateTransitionOk;
  }
}

class ImageDataSource extends PullBufferDataSource {
  private String        m_contentType = ContentDescriptor.RAW;
  private Control[]     m_controls    = new Control[0];
  private Time          m_duration    = DURATION_UNKNOWN;
  private ImageStream   m_stream;
  private ImageStream[] m_streams;

  public ImageDataSource(int w, int h, float frameRate, MovieMaker movieMaker) {
    m_stream = new ImageStream(w, h, frameRate, movieMaker);
    m_streams = new ImageStream[] { m_stream };
  }

  public void connect() throws IOException {
  }

  public void disconnect() {
  }

  public String getContentType() {
    return m_contentType;
  }

  public Object getControl(String controlType) {
    try {
      Class cls = Class.forName(controlType);
      Object cs[] = getControls();
      for (int i = 0; i < cs.length; i++) {
        if (cls.isInstance(cs[i]))
          return cs[i];
      }
      return null;

    } catch (Exception e) { // no such controlType or such control
      return null;
    }
  }

  public Object[] getControls() {
    return m_controls;
  }

  public Time getDuration() {
    return m_duration;
  }

  public PullBufferStream[] getStreams() {
    return m_streams;
  }

  public void start() throws IOException {
  }

  public void stop() throws IOException {
  }
}

class ImageStream implements PullBufferStream {
  private ContentDescriptor     m_contentDesc = new ContentDescriptor(
                                                  ContentDescriptor.RAW);
  private boolean               m_ended       = false;
  private RGBFormat             m_format;
  private float                 m_frameRate;
  private int                   m_maxDataLen;
  private MovieMaker            m_movieMaker;
  private int                   m_numFrame    = 0;
  private BufferTransferHandler m_transferHandler;
  private int                   m_width, m_height;

  public ImageStream(int w, int h, float frameRate, MovieMaker movieMaker) {
    m_width = w;
    m_height = h;
    m_frameRate = frameRate;
    m_movieMaker = movieMaker;

    m_maxDataLen = w * h;
    // m_format = new RGBFormat(new Dimension(w, h), m_maxDataLen, Format.intArray,
    // frameRate, 16, 0xf800, 0x07e0, 0x001f, 1, w, VideoFormat.FALSE,
    // Format.NOT_SPECIFIED);
    m_format = new RGBFormat(new Dimension(w, h), m_maxDataLen, Format.intArray,
        frameRate, 24, 0x0000ff, 0x00ff00, 0xff0000, 1, w, VideoFormat.FALSE,
        Format.NOT_SPECIFIED);
    // m_format = new RGBFormat(new Dimension(w, h), m_maxDataLen, Format.intArray,
    // frameRate, 8, 0xe0, 0x1c, 0x03, 1, w, VideoFormat.FALSE, Format.NOT_SPECIFIED);
  }

  public boolean endOfStream() {
    return m_ended;
  }

  public ContentDescriptor getContentDescriptor() {
    return m_contentDesc;
  }

  public long getContentLength() {
    return 0;
  }

  public Object getControl(String controlType) {
    try {
      Class cls = Class.forName(controlType);
      Object cs[] = getControls();
      for (int i = 0; i < cs.length; i++) {
        if (cls.isInstance(cs[i]))
          return cs[i];
      }
      return null;

    } catch (Exception e) { // no such controlType or such control
      return null;
    }
  }

  public Object[] getControls() {
    return new Control[0];
  }

  public Format getFormat() {
    return m_format;
  }

  private int build565(int r, int g, int b) {
    return ((r >> 3) << 11) | ((g >> 2) << 5) | ((b >> 3));
  }

  private void getPixels(Image img, int[] out) {
    ImageData data = img.getImageData();
    int p = 0;
    if (data.depth == 32) {
      for (int y = 0; y < data.height; ++y) {
        for (int x = 0; x < data.width; ++x) {
          int rgb = data.getPixel(x, y);
          out[p++] = rgb >> 8;
        }
      }
    } else if (data.depth == 24) {
      for (int y = 0; y < data.height; ++y) {
        for (int x = 0; x < data.width; ++x) {
          int rgb = data.getPixel(x, y);
          out[p++] = rgb;
        }
      }
    }
  }

  public void read(Buffer buf) throws IOException {
    if (m_numFrame > m_movieMaker.m_numFrames) {
      buf.setEOM(true);
      buf.setLength(0);
      buf.setOffset(0);
      m_ended = true;
      return;
    } else if (m_numFrame == m_movieMaker.m_numFrames) {
      m_movieMaker.m_uiContext.informationFrame = true;
    }

    Object outData = buf.getData();
    if (outData == null || !(outData.getClass() == Format.intArray)
        || ((int[]) outData).length < m_maxDataLen) {
      outData = new int[m_maxDataLen];
      buf.setData(outData);
    }

    buf.setFormat(m_format);
    buf.setTimeStamp((long) (m_numFrame / m_frameRate * 1000000000));

    final Object finalOutData = outData;
    Display.getDefault().syncExec(new Runnable() {
      public void run() {
        Image frame = m_movieMaker.newFrame();
        getPixels(frame, (int[]) finalOutData);
        frame.dispose();
        if (m_movieMaker.m_progressMonitor != null) {
          m_movieMaker.m_progressMonitor.worked(1);
        }
      }
    });

    buf.setSequenceNumber(m_numFrame);
    buf.setLength(m_maxDataLen);
    buf.setFlags(0);// buf.getFlags() | buf.FLAG_KEY_FRAME);
    buf.setHeader(null);
    buf.setOffset(0);
    ++m_numFrame;
  }

  public boolean willReadBlock() {
    return false;
  }
}