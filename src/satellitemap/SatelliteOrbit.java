package satellitemap;

import java.util.Calendar;
import java.util.TimeZone;


public class SatelliteOrbit
 {
  private static final double Second_of_Day = 86400;
  private static final double redimetor = 6378.137;


  //現在の時刻を入力
  public static void setTime(Calendar cal, Satellite sat, SatellitePosition satpos)
   {
    cal.get(Calendar.DAY_OF_YEAR);//setした日付を有効にするためのダミー

    cal.setTimeZone(TimeZone.getTimeZone("UTC")); //世界協定時刻へ変換

    satpos.setYear((double)cal.get(Calendar.YEAR));
    satpos.setDay((double)cal.get(Calendar.DAY_OF_YEAR) + ((double)cal.get(Calendar.HOUR_OF_DAY) * 3600.0 + (double)cal.get(Calendar.MINUTE) * 60.0 + (double)cal.get(Calendar.SECOND)) / Second_of_Day);

    getDeltaT(sat, satpos);
   }


  //元期からの経過日数を調べる
  private static void getDeltaT(Satellite sat, SatellitePosition satpos)
   {
    double year = satpos.getYear();
    double day = satpos.getDay();
    double epochyear = sat.getEpochYear();
    double epochtime = sat.getEpochTime();

    if(year == epochyear)
     {
      satpos.setDeltaT(day - epochtime); //改良の余地あり
     }
    else if(year > epochyear)
     {
      double addition =0.0;
      int y = (int) year; //年をパラメータとして扱うためコピー

      while(y > epochyear)
       {
        y--;

        if( y % 4 == 0) addition += 366.0; //うるう年
        else addition += 365.0;
       }
      satpos.setDeltaT(day - epochtime + addition);
     }

    else if(epochyear > year)
     {
      double addition =0.0;
      int y = (int) epochyear; //年をパラメータとして扱うためコピー

      while(year > y)
       {
        y++;

        if( y % 4 == 0) addition += 366.0; //うるう年
        else addition += 365.0;
       }
      satpos.setDeltaT(-(epochtime + addition - day));
     }
   }


  //現在の平均近点角を算出
  private static double getCurrentMeanAnormaly(Satellite sat, SatellitePosition satpos)
   {
    return sat.getMeanAnormaly() / 360.0 + satpos.getDeltaT() * satpos.getCurrentMotion();//回転数で返す
   }


  //現時刻の平均運動量を計算する
  private static void getCurrentMeanMotion(Satellite sat, SatellitePosition satpos)
   {
    satpos.setCurrentMotion(sat.getMeanMotion() + sat.getDerivative() * satpos.getDeltaT()); //運動量 = 元期における平均運動量 + 変化係数×経過日数
   }


  //離心近点角
  public static double getE(double rev, Satellite sat)
   {
    double M = rev < 0.0 ? (rev - Math.ceil(rev)) * 360.0 : (rev - Math.floor(rev)) * 360.0;
    double Ecurrent = M;

    double Fx = 0.0;
    double Fdashx = 0.0;
    double deltaE = 0.0;
    double eccentricity = sat.getEccentricity();

    for(int i = 0; i < 5 ; i++)
     {
      Fx = M - Ecurrent + eccentricity * Math.sin(Ecurrent / 180.0 * Math.PI);//与えられた関数
      Fdashx = eccentricity * Math.cos(Ecurrent/ 180.0 * Math.PI) - 1.0; //微分した関数
      deltaE = Fx / Fdashx;//
      if ( Math.abs(deltaE) < 1.0E-6) break;

      Ecurrent = Ecurrent - deltaE; //新しい近似値
     }
    return Ecurrent;
   }


  public static double[] getUV(double A,  double E, Satellite sat)
   {
    double Erad = E / 180.0 * Math.PI;
    double eccentricity = sat.getEccentricity();

    return new double[]{ A * Math.cos(Erad) - A * eccentricity, A * Math.sqrt(1.0 - eccentricity * eccentricity) * Math.sin(Erad)};

   }


  //行列の積
  private static void matrixMultiple(double[][] revmatrix, double[] column)
   {
//3×3の行列は左側から縦方向に上から下へ格納されている。
    double result1 = revmatrix[0][0] * column[0] + revmatrix[1][0] * column[1] +revmatrix[2][0] * column[2];
    double result2 = revmatrix[0][1] * column[0] + revmatrix[1][1] * column[1] +revmatrix[2][1] * column[2];
    double result3 = revmatrix[0][2] * column[0] + revmatrix[1][2] * column[1] +revmatrix[2][2] * column[2];

    column[0] = result1;
    column[1] = result2;
    column[2] = result3;
   }


  private static double getGreenidgeTime(Calendar cal)
   {
    //MJDの計算方法
    //グレゴリオ暦（1582年10月15日以降）の西暦年をY、月をM、日をDとする。
    //ただし1月のはM=13、2月はM=14、YはY=Y-1とする。

    //UTCを計算に用いること!

    double Y = (double)cal.get(Calendar.YEAR);
    double M = (double)cal.get(Calendar.MONTH) + 1.0;
    double D = (double)cal.get(Calendar.DAY_OF_MONTH);
    double H = (double)cal.get(Calendar.HOUR_OF_DAY);
    double Mi = (double)cal.get(Calendar.MINUTE);
    double S = (double)cal.get(Calendar.SECOND);

    if (M < 3.0){Y -= 1.0;  M += 12.0;}

    double JD = Math.floor(365.25 * Y) + Math.floor(Y / 400.0) - Math.floor(Y / 100.0) + Math.floor(30.59 * (M - 2.0)) + D +1721088.5 + H/ 24.0 + Mi/1440.0 + S/ 86400.0;
    double TJD = JD -2440000.5;
    double thetaG = (0.671262 + 1.0027379094 * TJD);
    return 360.0 * (thetaG - Math.floor(thetaG));
   }


  public static void getOrbitalPosition(Calendar targettime, Satellite sat, SatellitePosition satpos)
   {
/*
    TimeZone tz = TimeZone.getTimeZone("Asia/Tokyo");
    Calendar targettime = Calendar.getInstance(tz);
    targettime.clear();
    targettime.set(2012, 8, 23,  0, 44, 0);
*/
    setTime(targettime, sat, satpos);

    getCurrentMeanMotion(sat, satpos);

    double[] UV = getUV(sat.getSemiMajorAxis(), getE(getCurrentMeanAnormaly(sat, satpos), sat), sat);
    double altitude = Math.sqrt(UV[0] * UV[0] + UV[1] * UV[1]) - redimetor;

    double[] coordinates = new double[]{ UV[0], UV[1], 0.0};
    double[][] revmatrix = new double[3][3];

    double inclination = sat.getInclination() / 180.0 * Math.PI; //ラジアン変換

    double omega =  180.0 * 0.174 * (2.0 - 2.5 * Math.sin(inclination) * Math.sin(inclination));
    omega = omega / (Math.PI * Math.pow(sat.getSemiMajorAxis() / redimetor, 3.5)) * satpos.getDeltaT() + sat.getArgumentPerigee();
    omega = omega / 180.0 * Math.PI;
    double sinomega = Math.sin(omega);
    double cosomega = Math.cos(omega);

    //第1ステップ
    revmatrix[0][0] = cosomega;
    revmatrix[0][1] = sinomega;
    revmatrix[0][2] = 0.0;
    revmatrix[1][0] = -sinomega;
    revmatrix[1][1] = cosomega;
    revmatrix[1][2] = 0.0;
    revmatrix[2][0] = 0.0;
    revmatrix[2][1] = 0.0;
    revmatrix[2][2] = 1.0;

    matrixMultiple(revmatrix, coordinates);
    //第2ステップ
    double sininc = Math.sin(inclination);
    double cosinc = Math.cos(inclination);

    revmatrix[0][0] = 1.0;
    revmatrix[0][1] = 0.0;
    revmatrix[0][2] = 0.0;
    revmatrix[1][0] = 0.0;
    revmatrix[1][1] = cosinc;
    revmatrix[1][2] = sininc;
    revmatrix[2][0] = 0.0;
    revmatrix[2][1] = -sininc;
    revmatrix[2][2] = cosinc;

    matrixMultiple(revmatrix, coordinates);

    double OMEGA = 180.0 * 0.174 * Math.cos(inclination);
    OMEGA = -(OMEGA / (Math.PI * Math.pow(sat.getSemiMajorAxis() / redimetor, 3.5)) * satpos.getDeltaT()) + sat.getRightAscension();
    OMEGA = OMEGA / 180.0 * Math.PI;
    double sinOMEGA = Math.sin(OMEGA);
    double cosOMEGA = Math.cos(OMEGA);

    //第3ステップ
    revmatrix[0][0] = cosOMEGA;
    revmatrix[0][1] = sinOMEGA;
    revmatrix[0][2] = 0.0;
    revmatrix[1][0] = -sinOMEGA;
    revmatrix[1][1] = cosOMEGA;
    revmatrix[1][2] = 0.0;
    revmatrix[2][0] = 0.0;
    revmatrix[2][1] = 0.0;
    revmatrix[2][2] = 1.0;

    matrixMultiple(revmatrix, coordinates);

    //現時刻の恒星時を算出する
    double thetaG = -getGreenidgeTime(targettime) / 180.0 * Math.PI;
    double sinG = Math.sin(thetaG);
    double cosG = Math.cos(thetaG);

  //緯度経度に変換
    revmatrix[0][0] = cosG;
    revmatrix[0][1] = sinG;
    revmatrix[0][2] = 0.0;
    revmatrix[1][0] = -sinG;
    revmatrix[1][1] = cosG;
    revmatrix[1][2] = 0.0;
    revmatrix[2][0] = 0.0;
    revmatrix[2][1] = 0.0;
    revmatrix[2][2] = 1.0;
    matrixMultiple(revmatrix, coordinates);

    satpos.setX(coordinates[0]);
    satpos.setY(coordinates[1]);
    satpos.setZ(coordinates[2]);
    System.out.println("X= "+ coordinates[0] + " Y=" + coordinates[1] + " Z=" + coordinates[2]);

    double latitude = Math.asin(coordinates[2] / Math.sqrt(coordinates[0] * coordinates[0] + coordinates[1] * coordinates[1] + coordinates[2] * coordinates[2]) ) / Math.PI * 180.0;
    double longitude = Math.atan2(coordinates[1] , coordinates[0] )/Math.PI * 180.0;

    System.out.println("latitude = " + latitude + " longitude = " + longitude);
   }
 }