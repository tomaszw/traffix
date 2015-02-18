/*
 * Created on 2005-08-28
 */

package traffix;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.swt.graphics.RGB;

public class Config {
  public static RGB     RGB_TRANSIT_DETECTOR_ACTIVE         = new RGB(160, 160, 160);
  public static RGB     RGB_TRANSIT_DETECTOR_INACTIVE       = new RGB(225, 225, 225);
  public static RGB     RGB_PRESENCE_DETECTOR_ACTIVE        = new RGB(160, 160, 160);
  public static RGB     RGB_PRESENCE_DETECTOR_INACTIVE      = new RGB(225, 225, 225);
  public static RGB     RGB_PEDESTRIAN_DETECTOR_ACTIVE      = new RGB(160, 160, 160);
  public static RGB     RGB_PEDESTRIAN_DETECTOR_INACTIVE    = new RGB(225, 225, 225);
  public static RGB     RGB_CONDCLEAR_DETECTOR_ACTIVE       = new RGB(160, 160, 160);
  public static RGB     RGB_CONDCLEAR_DETECTOR_INACTIVE     = new RGB(225, 225, 225);

  public static boolean SCHEDULE_PAINT_JUNCTION_ZEROMARKERS = true;
  public static boolean SCHEDULE_PAINT_GROUP_ZEROMARKERS    = true;

  public static int     SIM_FAST_SIMULATION_SPEEDFACTOR     = 25;

  public static class MalformedFileException extends Exception {}

  public static void load() throws MalformedFileException {
    Properties p = new Properties();
    try {
      p.load(new FileInputStream("trafficls.properties"));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return;
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    String s;

    s = p.getProperty("Symulacja.DetektorTranzytuAktywnyRgb");
    if (s != null)
      RGB_TRANSIT_DETECTOR_ACTIVE = parseRgb(s);

    s = p.getProperty("Symulacja.DetektorTranzytuNieaktywnyRgb");
    if (s != null)
      RGB_TRANSIT_DETECTOR_INACTIVE = parseRgb(s);

    s = p.getProperty("Symulacja.DetektorObecnosciAktywnyRgb");
    if (s != null)
      RGB_PRESENCE_DETECTOR_ACTIVE = parseRgb(s);

    s = p.getProperty("Symulacja.DetektorObecnosciNieaktywnyRgb");
    if (s != null)
      RGB_PRESENCE_DETECTOR_INACTIVE = parseRgb(s);

    s = p.getProperty("Symulacja.DetektorPieszychAktywnyRgb");
    if (s != null)
      RGB_PEDESTRIAN_DETECTOR_ACTIVE = parseRgb(s);

    s = p.getProperty("Symulacja.DetektorPieszychNieaktywnyRgb");
    if (s != null)
      RGB_PEDESTRIAN_DETECTOR_INACTIVE = parseRgb(s);

    s = p.getProperty("Symulacja.DetektorKasujacyAktywnyRgb");
    if (s != null)
      RGB_CONDCLEAR_DETECTOR_ACTIVE = parseRgb(s);

    s = p.getProperty("Symulacja.DetektorKasujacyNieaktywnyRgb");
    if (s != null)
      RGB_CONDCLEAR_DETECTOR_INACTIVE = parseRgb(s);
    
    s = p.getProperty("Harmonogram.ZnacznikiZerowejSekundySkrzyzowan");
    if (s != null)
      SCHEDULE_PAINT_JUNCTION_ZEROMARKERS = parseBool(s);

    s = p.getProperty("Harmonogram.ZnacznikiZerowejSekundyGrup");
    if (s != null)
      SCHEDULE_PAINT_GROUP_ZEROMARKERS = parseBool(s);

    s = p.getProperty("Symulacja.PrzyspieszenieGdySzybkoDoPrzodu");
    if (s != null)
      SIM_FAST_SIMULATION_SPEEDFACTOR = parseInt(s);
  }

  private static boolean parseBool(String str) throws MalformedFileException {
    if (str.toLowerCase().equals("tak"))
      return true;
    if (str.toLowerCase().equals("nie"))
      return false;
    throw new MalformedFileException();
  }

  private static int parseInt(String str) throws MalformedFileException {
    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException e) {
      throw new MalformedFileException();
    }
  }

  private static RGB parseRgb(String str) throws MalformedFileException {
    StringTokenizer tok = new StringTokenizer(str, " ,");
    if (tok.countTokens() != 3)
      throw new MalformedFileException();
    try {
      int r = Integer.parseInt(tok.nextToken());
      int g = Integer.parseInt(tok.nextToken());
      int b = Integer.parseInt(tok.nextToken());
      RGB rgb = new RGB(r, g, b);
      return rgb;
    } catch (NumberFormatException e) {
      throw new MalformedFileException();
    }
  }
}
