package satellitemap;

public class SatellitePosition
 {
  private double year;
  private double day;
  private double dt;
  private double X;
  private double Y;
  private double Z;
  private double currentmotion; //現在の平均運動量

  public double getYear()
   {
    return year;
   }

  public void setYear(double y)
   {
    year = y;
   }

  public double getDay()
   {
    return day;
   }

  public double getDeltaT()
   {
    return dt;
   }

  public void setDeltaT(double val)
   {
    dt = val;
   }

  public void setDay(double d)
   {
    day = d;
   }

  public double getX()
   {
    return X;
   }

  public void setX(double x)
   {
    X = x;
   }

  public double getY()
   {
    return Y;
   }

  public void setY(double y)
   {
    Y = y;
   }

  public double getZ()
   {
    return Z;
   }

  public void setZ(double z)
   {
    Z = z;
   }

  public double getCurrentMotion()
   {
    return currentmotion;
   }

  public void setCurrentMotion(double val)
   {
    currentmotion = val;
   } }
