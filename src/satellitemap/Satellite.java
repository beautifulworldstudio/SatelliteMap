package satellitemap;

public class Satellite
 {
  private static final double PIsquare = 4.0 * Math.PI * Math.PI;
  private static final double GM = 2.975537 * Math.pow(10.0, 15.0);

  private double a;		// 軌道長半径
  private double epochyear;	// 元期の年
  private double epochtime; 	// 元期(UT)
  private double meanmotion;	// 元期における平均運動量
  private double meananormaly;	// 元期における平均近点角
  private double inclination;	// 軌道傾斜角
  private double rightascension;//
  private double eccentricity;	// 離心率
  private double argumentperigee;//
  private double derivative;	// 平均運動量変化係数

  private double year;		//現在の年
  private double day;		//現在の日
  private double dt;		//元期からの差分

  //コンストラクタ。two-line elements 渡して生成する場合
  public Satellite(String firstline,  String secondline)
   {
    updateTLE(firstline, secondline);
   }


  public void updateTLE(String firstline, String secondline)
   {
    double targetyear = 2000.0 + Double.parseDouble(firstline.substring(18, 20));
    double targetday = Double.parseDouble(firstline.substring(20, 32));

    if(targetyear < epochyear || targetday < epochtime) return;

    epochyear = targetyear;
    epochtime = targetday;

    decodeTLE(firstline, secondline);
   }


  private void decodeTLE(String firstline, String secondline)
   {
//    epochyear = 2000.0 + Double.parseDouble(firstline.substring(18, 20));
//    epochtime = Double.parseDouble(firstline.substring(20, 32));

    derivative = Double.parseDouble(firstline.substring(33, 43));
    inclination = Double.parseDouble(secondline.substring(8, 16));
    rightascension = Double.parseDouble(secondline.substring(17, 25));
    eccentricity = Double.parseDouble("0." + secondline.substring(26, 33));
    argumentperigee =  Double.parseDouble(secondline.substring(34, 42));
    meananormaly = Double.parseDouble(secondline.substring(43, 51));
    meanmotion = Double.parseDouble(secondline.substring(52, 63));

    setSemiMajorAxis(); //軌道長半径を計算する
   }


  //軌道長半径を計算する
  private void setSemiMajorAxis()
   {
    a = Math.pow(GM /(PIsquare * meanmotion * meanmotion), 1.0 / 3.0);
   }

  public double getSemiMajorAxis()
   {
    return a;
   }

  public double getEpochYear()
   {
    return epochyear;	// 元期の年
  }

  public double getEpochTime()
   {
    return epochtime; 	// 元期(UT)

   }

  public double getMeanMotion()
   {
    return meanmotion;	// 元期における平均運動量
   }

  public double getMeanAnormaly()
   {
    return meananormaly;	// 元期における平均近点角
   }

  public double getInclination()
   {
    return inclination;	// 軌道傾斜角
   }

  public double getRightAscension()
   {
    return rightascension;//
   }

  public double getEccentricity()
   {
    return eccentricity;	// 離心率
   }

  public double getArgumentPerigee()
   {
    return argumentperigee;//
   }

  public double getDerivative()
   {
    return derivative;	// 平均運動量変化係数
   }
 }
