//Nicholas Vadivelu
//Mass Flow Calculator
//Started: 19 May 2015
//Finished
//Credit to: http://introcs.cs.princeton.edu/java/97data/PolynomialRegression.java.html for the quadratic regression stuff
//Credit to: http://compsci.ca/v3/viewtopic.php?t=30585 for the integration stuff

import java.util.Scanner;
import java.lang.Math;
import Jama.Matrix;
import Jama.QRDecomposition;

public class MassFlowCalculator
{
  private static PolynomialRegression depthEquation, flowEquation;
  private static double[] depths = new double[5]; //{-0.3, -0.6, -0.8, -1.2, -1.7};//
  private static double[] flowrates = new double[5];//{0.1, 0.2, 0.2, 0.4, 0.4};//
  private static double[] flowY = new double [10];//{0.1, 0.2, 0.2, 0.4, 0.4, 0.4, 0.4, 0.2, 0.2, 0.1};//
  private static double[] flowX = new double [10];//{-2.1, -1.7, -1.3, -0.9, -0.5,  0.5,  0.9,  1.3,  1.7,  2.1};//
  private static double dx, riverWidth, volumeFlow, middle; //delta X
  private static double x[] = new double[12]; //{-2.5, -2.1, -1.7, -1.3, -0.9, -0.5,  0.5,  0.9,  1.3,  1.7,  2.1, 2.5};//
  private static double y[] = new double[12]; //{0   , -0.3, -0.6, -0.8, -1.2, -1.7, -1.7, -1.2, -0.8, -0.6, -0.3, 0};//
  final private static double WATER_DENSITY = 999.97; //kg/meter cubic. we are not acccounting for the temperature because the range was typically between 5 to 30 degrees. At this range, the density is negligible

  private static double getArea (double[] coefficient, double lowLimit, double highLimit) //definite integration
  {
    //DecimalFormat df = new DecimalFormat ("#.####");
    int degree = 2;
    //double coefficient[] = new double [degree + 1];
    double indefinite[] = new double [degree + 1];
    double temp[] = new double [degree + 1];
    double temp2 [] = new double [degree + 1];
    double limit[] = {lowLimit, highLimit};
    double area = 0, area2 = 0;
    int count = degree;
    for (int i = 0 ; i <= degree ; i++)
    {
      indefinite [i] = coefficient [i] / (count + 1);
      count--;
    }
    count = degree + 1;
    for (int i = 0 ; i <= degree ; i++)
    {
      temp [i] = indefinite [i] * Math.pow (limit [1], count);
      temp2 [i] = indefinite [i] * Math.pow (limit [0], count);
      area += temp [i];
      area2 += temp2 [i];
      count--;
    }
    return Math.abs(area - area2);
  }
  
  private static void getInfo() //temporary method to get data for now
  {
    y[0] = 0;
    y[11] = 0;
    Scanner in = new Scanner(System.in);
    System.out.println("What are the 5 depths as a positive decimal number(press enter after each one):");
    for (int i = 0; i < depths.length; i++)
    {
      depths[i] = -(in.nextDouble());
      y[i+1] = depths[i];
      y[10-i] = depths[i];
    }
    
    System.out.println("What are the 5 flowrates as a positive decimal number(press enter after each one):");
    for (int i = 0; i < flowrates.length; i++)
    {
      flowrates[i] = in.nextDouble();
    }
    
    System.out.println("What is the distance between the points as a positive decimal number(delta x)?:");
    dx = in.nextDouble();
    
    System.out.println("What is the width of the river?:");
    riverWidth = in.nextDouble();
    
    x[0] = -(riverWidth/2);
    x[11] = (riverWidth/2);
    for (int i = 1; i < 6; i++)
    {
      x[i] = x[0] + dx*i;
    }
    for (int i = 6, z = 5; i < 11; i++, z--)
    {
      x[i] = x[11] - dx*z;
    }
    for (int i = 0; i < 5; i++)
    {
      flowX[i] =  (dx*(i+1)) - (riverWidth/2);
      flowY[i] = flowrates[i];
    }
    for(int i = 5; i < 10; i++)
    {
      flowY[i] = flowrates[9-i];
      flowX[i] = (riverWidth/2) - (dx*(10-i));
    }
    if (riverWidth < (dx*10))
    {
      System.out.println("This program was not designed to accept the specified river width and delta x. The river width should be greater than ten times the delta x. Nick says sorry he is bad at programming");
    }
    middle = (riverWidth - (dx*10))/2;
    
    in.close();
  }
  
  //private static void getInfo(int asdf) //temporary method to get data for now
  //{
  //  dx = 0.4;
  //  riverWidth = 5.0;
  //  middle = (riverWidth - (dx*10))/2;
  //}
  
  public static void main (String [] args)
  {
    getInfo();
    depthEquation = new PolynomialRegression(x, y, 2);
    flowEquation = new PolynomialRegression(flowX, flowY, 2);
    //System.out.println("Sum of all the things: ");
    volumeFlow += 2*getArea(depthEquation.get(), -(riverWidth/2), flowX[0]+(dx/2)) * flowrates[0];//CORRECT
    //System.out.println("Ready: " + 2*getArea(depthEquation.get(), -(riverWidth/2), flowX[0]+(dx/2)) * flowrates[0]);
    for (int i = 1; i < flowrates.length; i++)
    {
      volumeFlow += 2*(getArea(depthEquation.get(), flowX[i] - (dx/2), flowX[i]+(dx/2)) * flowrates[i]); //x2 to account for both sides of the river
      //System.out.println("Set: " + 2*(getArea(depthEquation.get(), flowX[i] - (dx/2), flowX[i]+(dx/2)) * flowrates[i]));
      
    }
    double[] flowco = flowEquation.get(); //coefficients for flow equation
    int iNeed = (int) Math.round(middle/dx);
    double tester = 0;
    double middler = 0;
    //System.out.println(Double.toString(middle/dx-0.05));
    for (int i = 0; i < iNeed; i++)
    {
      if ((riverWidth/2-dx*5-dx*(i+1)-(dx/2)) < 0)
        tester = 0;
      else
        tester = (riverWidth/2-dx*5-dx*(i+1)-(dx/2));
      middler = (riverWidth/2 - dx*5-dx*(i)-(dx/2) + tester)/2;
      volumeFlow += 2*(getArea(depthEquation.get(), tester, riverWidth/2 - dx*5-dx*(i)-(dx/2)) * (flowco[0]*(middler*middler) + flowco[1]*(middler) + flowco[2]));
      //System.out.println("Integral of " + tester + " to " + (riverWidth/2 - dx*5-dx*(i)-(dx/2)));
      //System.out.println("Go: " + 2*(getArea(depthEquation.get(), tester, riverWidth/2 - dx*5-dx*(i)-(dx/2)) * (flowco[0]*(middler*middler) + flowco[1]*(middler) + flowco[2])));
    }
    //volumeFlow += 2*flowco[2]*getArea(depthEquation.get(), 0, Math.abs(iNeed));
    //System.out.println("Ok: " + 2*flowco[2]*getArea(depthEquation.get(), 0, Math.abs(iNeed)));
    
    //System.out.println("The coordinates for the flow rates are: ");
    for(int i = 0; i < flowX.length; i++)
    {
      //System.out.print("(" + Double.toString(flowX[i]) + ", " + Double.toString(flowY[i]) + ") ");
    }
    //System.out.println("\n\nThe coordinates for the depths are: ");
    for(int i = 0; i < x.length; i++)
    {
      //System.out.print("(" + Double.toString(x[i]) + ", " + Double.toString(y[i]) + ") ");
    }
    //System.out.println("\n\nThe equation for the flow rates is: " + flowEquation);
    //System.out.println("The equation for the depths is " + depthEquation);
    System.out.println("The volumetric flow for this river is: " + volumeFlow + " m^3/s");
    //System.out.println("The mass flow for this river is: " + (volumeFlow*WATER_DENSITY) + " kg/s");
  }
}

//NOT MINE thanks Princeton University for some code:
class PolynomialRegression {
  private final int N;
  private final int degree;
  private final Matrix beta;
  private double SSE;
  private double SST;
  
  public PolynomialRegression(double[] x, double[] y, int degree) {
    this.degree = degree;
    N = x.length;
    
    // build Vandermonde matrix
    double[][] vandermonde = new double[N][degree+1];
    for (int i = 0; i < N; i++) {
      for (int j = 0; j <= degree; j++) {
        vandermonde[i][j] = Math.pow(x[i], j);
      }
    }
    Matrix X = new Matrix(vandermonde);
    
    // create matrix from vector
    Matrix Y = new Matrix(y, N);
    
    // find least squares solution
    QRDecomposition qr = new QRDecomposition(X);
    beta = qr.solve(Y);
    
    
    // mean of y[] values
    double sum = 0.0;
    for (int i = 0; i < N; i++)
      sum += y[i];
    double mean = sum / N;
    
    // total variation to be accounted for
    for (int i = 0; i < N; i++) {
      double dev = y[i] - mean;
      SST += dev*dev;
    }
    
    // variation not accounted for
    Matrix residuals = X.times(beta).minus(Y);
    SSE = residuals.norm2() * residuals.norm2();
    
  }
  
  public double beta(int j) {
    return beta.get(j, 0);
  }
  
  public int degree() {
    return degree;
  }
  
  public double R2() {
    return 1.0 - SSE/SST;
  }
  
  // predicted y value corresponding to x
  public double predict(double x) {
    // horner's method
    double y = 0.0;
    for (int j = degree; j >= 0; j--)
      y = beta(j) + (x * y);
    return y;
  }
  
  public String toString() {
    String s = "";
    int j = degree;
    
    // ignoring leading zero coefficients
    while (Math.abs(beta(j)) < 1E-5)
      j--;
    
    // create remaining terms
    for (; j >= 0; j--) {
      if      (j == 0) s += String.format("%.5f ", beta(j));
      else if (j == 1) s += String.format("%.5f x + ", beta(j));
      else             s += String.format("%.5f x^%d + ", beta(j), j);
    }
    return s + "  (R^2 = " + String.format("%.3f", R2()) + ")";
  }
  
  public double[] get()
  {
    double[] get = {beta(2), beta(1), beta(0)};
    return get;
  }
}


