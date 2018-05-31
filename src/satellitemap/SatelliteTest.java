package satellitemap;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SatelliteTest extends JPanel implements WindowListener
 {
//ISS(ZARYA)
  String first =  "1 25544U 98067A   18150.92127221  .00001226  00000-0  25793-4 0  9992";
  String second = "2 25544  51.6391 104.7815 0003970 137.3663 325.3733 15.54111493115839";


  private static final double camangle  = 45.0 / 180.0 * Math.PI;
  private static final double[][] camera = new double[][]{
   {1.0, 0.0, 0.0},
   { 0.0, Math.cos(camangle), -Math.sin(camangle)},
   { 0.0, Math.sin(camangle), Math.cos(camangle)},
  };

  //静止衛星用のカメラベクトル
//  private static final double[][] camera = new double[][]{
//   {0.0, 1.0, 0.0},
//   {0.0, 0.0,  -1.0},
//   {-1.0, 0.0, 0.0},
//  };
  private static final double[][] E = new double[][]{
   { 1.0, 0.0, 0.0},
   { 0.0, 1.0, 0.0},
   { 0.0, 0.0, 1.0},
  };

  private static final double R = 6378.137;
  private static final double scrAngle = 100;//Max 110
  private static final int width = 1000;
  private static final int height = 600;

  private JFrame window;
  private Satellite sat;
  private SatellitePosition position;
  private SatellitePosition after5sec;

  private BufferedImage earth;
  private int imageWidth;
  private int imageHeight;
  private double longitudeleft = -29.6;

  public SatelliteTest() throws IOException
   {
    window = new JFrame("View From ISS");
    window.getContentPane().add(this);
    window.addWindowListener(this);
    window.setSize(600, 600);
    window.setVisible(true);

    sat = new Satellite(first, second);
    position = new SatellitePosition();
    after5sec = new SatellitePosition();

    earth = ImageIO.read(new File("C:\\Users\\Kentaro\\Documents\\java\\satellitemap\\map.png"));
    imageWidth = earth.getWidth();
    imageHeight = earth.getHeight();
   }

  public void windowActivated(WindowEvent w){}
  public void windowDeactivated(WindowEvent w){}
  public void windowIconified(WindowEvent w){}
  public void windowDeiconified(WindowEvent w){}
  public void windowOpened(WindowEvent w){}
  public void windowClosed(WindowEvent w){}

  public void windowClosing(WindowEvent w)
   {
    System.exit(0);
   }

  public void paint(Graphics g)
   {
    getSatellitePosition();

    double[][] transVector = getXYZVector();
    double[][] scrVector = new double[3][3];

    double[] eyevector = new double[3];
    double[] transed = new double[3];
    double[] latlon = new double[2];
    double[] result1 = new double[3];
    double[] result2 = new double[3];

    double distance = 0.5 / Math.tan(scrAngle / 360.0 * Math.PI); //2分割のため360度にしてある
    double xlength = 0.5;
    double ylength = (double)height / (double)width / 2.0;

    int centerx = width / 2 - 1;
    int centery = height / 2 - 1;
    int rgb = 0;

    matrixMultiple33Type1(camera , transVector,  scrVector); //変換対象は左から掛ける

    //Z軸を天頂方向に修正する（静止衛星のみ推奨）
    //revice(scrVector);

    for(int i = 0; i < width; i++)
     {
      double screenx = ((double)(i - centerx) / (double)centerx) * xlength;

      for(int j = 0; j < height; j++)
       {
        double screeny = ((double)(centery - j) / (double)centery) * ylength;
        eyevector[0] = screenx;
        eyevector[1] = distance;
        eyevector[2] = screeny;

        double norm = Math.sqrt(eyevector[0] * eyevector[0] + eyevector[1] * eyevector[1] + eyevector[2] * eyevector[2]);
        eyevector[0] /= norm;
        eyevector[1] /= norm;
        eyevector[2] /= norm;

        vectorTransfrom(scrVector, eyevector, position, latlon);

        if (latlon[0] != 1000.0)
         {
          rgb = getRGB(latlon );
         }
        else if(latlon[0] == 2000.0)
         {
          rgb = 0xffffffff;
         }
        else
         {
          rgb = 0;
         }
//ここは将来的にはイメージにする
          g.setColor(new Color(rgb));
          g.drawLine(i, j, i, j);
       }
     }
   }


  private int getRGB(double[] location)
   {
    if (earth == null) return 0;
    double y = (90.0 - location[0]) / 180.0 * imageHeight;
    double x = location[1] - longitudeleft ;
    if (x < 0) x += 360.0;
    x = x / 360.0 * imageWidth;

    if(x >= imageWidth | y >= imageHeight | x < 0 | y < 0 ) { return 0; }

    return earth.getRGB((int)x, (int)y);
   }


  private void getSatellitePosition()
   {
    TimeZone tz = TimeZone.getTimeZone("Asia/Tokyo");
    Calendar targettime = Calendar.getInstance(tz);

    targettime.clear();
    targettime.setTimeZone(tz);
    targettime.set(2016, 0, 16,  23, 30 , 0);
    SatelliteOrbit.getOrbitalPosition(targettime, sat, position);

    targettime.clear();
    targettime.setTimeZone(tz);
    targettime.set(2012, 0, 16,  23, 30 , 10);
    SatelliteOrbit.getOrbitalPosition(targettime, sat, after5sec);
   }

  private void revice(double[][] mat)
   {
    double[] vertical = new double[3];

    getVector(mat[1][0], mat[1][1], mat[1][2], 0.0, 0.0, 1.0, vertical);

    double angle = Math.acos(mat[0][0] * vertical[0] + mat[0][1] * vertical[1] + mat[0][2] * vertical[2]) ;

    double[][] rotate = new double[][]{
     { Math.cos(angle), 0.0, Math.sin(angle)},
     { 0.0, 1.0, 0.0},
     { -Math.sin(angle), 0.0, Math.cos(angle)},
    };
    double[][] result = new double[3][3];
    matrixMultiple33Type1(rotate , mat, result);

    mat[0][0] = result[0][0];
    mat[0][1] = result[0][1];
    mat[0][2] = result[0][2];
    mat[1][0] = result[1][0];
    mat[1][1] = result[1][1];
    mat[1][2] = result[1][2];
    mat[2][0] = result[2][0];
    mat[2][1] = result[2][1];
    mat[2][2] = result[2][2];
   }


  public double[][] getXYZVector()
   {
    double[] Zaxis = new double[3];
    Zaxis[0] = position.getX();
    Zaxis[1] = position.getY();
    Zaxis[2] = position.getZ();

    double altitude = Math.sqrt(Zaxis[0] * Zaxis[0] + Zaxis[1] * Zaxis[1] + Zaxis[2] * Zaxis[2]);
    Zaxis[0] /= altitude;
    Zaxis[1] /= altitude;
    Zaxis[2] /= altitude;

    double x = after5sec.getX() -  position.getX();
    double y = after5sec.getY() -  position.getY();
    double z = after5sec.getZ() -  position.getZ();
    double norm = Math.sqrt(x * x + y * y + z * z);
    x /= norm;
    y /= norm;
    z /= norm;

    double[] Xaxis = new double[3];
    getVector(x, y, z, Zaxis[0], Zaxis[1], Zaxis[2], Xaxis);

    double[] Yaxis = new double[3];
    getVector(Zaxis[0], Zaxis[1], Zaxis[2], Xaxis[0], Xaxis[1], Xaxis[2],  Yaxis);

    return new double[][]{ Xaxis, Yaxis, Zaxis };
   }


  public void vectorTransfrom(double[][] matrix, double[] vec, SatellitePosition satpos, double[] result)
   {
    double[] center = new double[] { satpos.getX(), satpos.getY(), satpos.getZ() }; //地球中心へ向かうベクトル
    double norm = Math.sqrt(center[0] * center[0] + center[1] * center[1] + center[2] * center[2]);
    center[0] /= norm;
    center[1] /= norm;
    center[2] /= norm;

    double[] bearing = new double[3]; //視線の方向ベクトル

    matrixMultiple31type2(matrix, vec, bearing);

    double theta = Math.acos(-center[0] * bearing[0] - center[1] * bearing[1] - center[2] * bearing[2]);

    double altitude = Math.sqrt(satpos.getX() * satpos.getX() + satpos.getY() * satpos.getY() + satpos.getZ() * satpos.getZ());

    double b2 = 2.0 * altitude * Math.cos(theta);
    double D = b2 * b2 - 4.0 * (altitude * altitude - R * R);
    if (D >= 0)
     {
      double root = 0.5 * Math.sqrt(D);

      double x1 = altitude * Math.cos(theta) - root;
      if (x1 < 0) { /* System.out.println("負の実数解");*/result[0] = 2000.0;  return;  }

      //x1は交点までの距離。方向ベクトルに距離を掛けると位置が出る
      double pointX = position.getX() + bearing[0] * x1;
      double pointY = position.getY() + bearing[1] * x1;
      double pointZ = position.getZ() + bearing[2] * x1;

      result[0] = Math.asin(pointZ / R) / Math.PI * 180.0;
      result[1] = Math.acos(pointX / Math.sqrt(pointX * pointX + pointY * pointY))/ Math.PI * 180.0;
      if (pointY < 0) result[1] = -result[1]; //象限を考慮する

//      System.out.println("実数解あり " + pointlat + " :" + pointlon);
     }
    else
     {
      result[0] = 1000.0;

     }//System.out.println("実数解なし");
   }


  private static void getVector(double ax, double ay, double az, double bx, double by, double bz, double[] result)
   {
    if (result.length != 3) return;

    result[0] = ay * bz - az * by;
    result[1] = az * bx - ax * bz;
    result[2] = ax * by - ay * bx;
    double norm = Math.sqrt(result[0] * result[0] + result[1] * result[1]  + result[2] * result[2]);

    result[0] /= norm;
    result[1] /= norm;
    result[2] /= norm;
   }


  //配列が横向きの時の3行3列の掛け算
  private static void matrixMultiple33Type1(double[][] left, double[][] right, double[][] result)
   {
    for(int i = 0; i < 3; i++)
     {
      for(int j = 0; j < 3;j++)
       {
        result[i][j] = left[i][0] * right[0][j] + left[i][1] * right[1][j] + left[i][2] * right[2][j];
       }
     }
   }

  //配列が縦向きの時の3行3列の掛け算
  private static void matrixMultiple33Type2(double[][] left, double[][] right, double[][] result)
   {
    for(int i = 0; i < 3; i++)
     {
      for(int j = 0; j < 3; j++)
       {
        result[i][j] = left[0][i] * right[j][0] + left[1][i] * right[j][1] + left[2][i] * right[j][2];
       }
     }
   }

  //3行3列の行列と列ベクトルの掛け算
  private static void matrixMultiple31type1(double[][] left, double[] right, double[] result)
   {
    for(int i = 0; i < 3; i++)
     {
      result[i] = left[i][0] * right[0] + left[i][1] * right[1] + left[i][2] * right[2];
     }

    double norm = Math.sqrt(result[0] * result[0] + result[1] * result[1] + result[2] * result[2]);
    result[0] /= norm;
    result[1] /= norm;
    result[2] /= norm;
   }


  //3行3列の行列(縦配置)と列ベクトルの掛け算
  private static void matrixMultiple31type2(double[][] left, double[] right, double[] result)
   {
    for(int i = 0; i < 3; i++)
     {
      result[i] = left[0][i] * right[0] + left[1][i] * right[1] + left[2][i] * right[2];
     }

    double norm = Math.sqrt(result[0] * result[0] + result[1] * result[1] + result[2] * result[2]);
    result[0] /= norm;
    result[1] /= norm;
    result[2] /= norm;
   }


  public static void main(String args[]) throws IOException
   {
    new SatelliteTest();
   }
 }