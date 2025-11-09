package org.wseminar.eigenanteil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Reciever {

    private double[] posCart = new double[3]; //Position in Kartesischen Koordinaten {x, y, z}
    private double[] posPolar = new double[3]; //Position in Polarkoordinaten {phi, lambda, h}
    private final double RADIUS_EARTH = 6_371_000.785; //Erdradius
    private final double SPEED_LIGHT = 299_792_458; //Lichtgeschwindigkeit

    public double[] calc_pos_rec(ArrayList<Satellite> satellites) {
        double[][] sats = new double[4][4];

        //Daten der Satelliten werden ausgelesen
        for (int i = 0; i < 4; i += 1) {
            Satellite sat = satellites.get(i);
            for (int j = 0; j < 4; j += 1) {
                if (j < 3) {
                    sats[i][j] = sat.pos[j];
                } else {
                    sats[i][3] = sat.pr;
                }
            }
        }

        ArrayList<double[]> iterations = new ArrayList<>(); //Iterationsschritte
        ArrayList<Double> delta = new ArrayList<>(); //Abweichung zwischen den beiden letzten Iterationsschritten
        delta.add(1.0);
        double tol = 0.1; //Toleranz

        //Startwert Newton
        iterations.add(new double[]{0.0, 0.0, 0.0, 0.0});
        double[] xn;

        int maxIt = 10;
        while (maxIt > 0 && delta.getLast() > tol) {
            double[] fxn = new double[4]; //Vektor
            double[][] jacobian = new double[4][4]; //Jakobi-Matrix

            xn = iterations.getLast(); //Letzten Iterationsschritt auslesen

            //Funktionsvektor und Jacobi-Matrix berechnen
            for (int i = 0; i < 4; i += 1) {
                double d = Math.sqrt((xn[0] - sats[i][0]) * (xn[0] - sats[i][0])
                        + (xn[1] - sats[i][1]) * (xn[1] - sats[i][1])
                        + (xn[2] - sats[i][2]) * (xn[2] - sats[i][2]));
                fxn[i] = d + SPEED_LIGHT * xn[3] - sats[i][3];

                jacobian[i][0] = (xn[0] - sats[i][0]) / d;
                jacobian[i][1] = (xn[1] - sats[i][1]) / d;
                jacobian[i][2] = (xn[2] - sats[i][2]) / d;
                jacobian[i][3] = SPEED_LIGHT;
            }

            // Inverse Matrix J^-1 berechnen
            double[][] invertedJacobian = invertMatrix4(jacobian);

            //Multiplikation inverse Matrix mit f(xn)
            double[] step = multiplyMatrixVector4(invertedJacobian, fxn);

            //Update x = x - delta
            for (int i = 0; i < 4; i += 1) {
                xn[i] = xn[i] - step[i];
            }
            double[] xnCopy = xn.clone();

            iterations.add(xnCopy);
            delta.add(change(step));

            delta.add(change(step));
        }

        for (int i = 0; i < 3; i += 1) {
            posCart[i] = iterations.getLast()[i];
        }

        //neue output.csv Datei mit höherem Inde, als vorherige
        int counter = 1;
        File file;
        do {
            file = new File("results/output_" + counter + ".txt");
            counter += 1;
        } while (file.exists());

        //output file
        FileWriter writer;
        try {
            writer = new FileWriter(file);
            writer.write("Ergebnis der Positionsberechnung\nStellitendaten:\ns, x, y, z, pr\n");
            for (int i = 0; i < 4; i += 1) {
                writer.write(i + ", " + sats[i][0] + ", " + sats[i][1] + ", " + sats[i][2] + ", " + sats[i][3] + "\n");
            }
            writer.write("\nMit Newton-Verfahren berechnete Werte nach Iterationsschritten:\nn, x, y, z, dt0, dn\n");

            //Iterationsschrite werden ins output.txt File übertragen
            writer.write("0, 0.0, 0.0, 0.0, 0.0, 1.0\n");
            for (int i = 0; i < iterations.size(); i += 1) {
                double[] step_ = iterations.get(i);
                double dn = delta.get(i + 1);
                writer.write(i + 1 + ", " + step_[0] + ", " + step_[1] + ", " + step_[2] + ", " + step_[3] + ", " + dn + "\n");
            }

            writer.write("\nEndgültige berechnete Position (x, y, z): " + Arrays.toString(posCart));
            writer.write("\nEndgültige berechnete Position (phi, lambda, h): " + Arrays.toString(getPolar()));
            writer.write("\nEndgültiger berechneter Fehler Empfängeruhr: " + iterations.getLast()[3]);

            writer.close();

        } catch (IOException ex) {

        }

        return iterations.getLast();
    }

    //Multiplikation von Matrix und Vektor
    private static double[] multiplyMatrixVector4(double[][] matrix, double[] vector) {
        double[] res = new double[4];
        for (int i = 0; i < 4; i += 1) {
            for (int j = 0; j < 4; j += 1) {
                res[i] = res[i] + matrix[i][j] * vector[j];
            }
        }
        return res;
    }

    //Änderung der Ergebnisse
    private static double change(double[] v) {
        double sum = 0;
        for (double val : v) {
            sum += val * val;
        }
        return Math.sqrt(sum);
    }

    //Inverse Matrix wird mit dem Gauss-Jordan Verfahren errechnet
    private static double[][] invertMatrix4(double[][] matrix) {
        int n = 4;
        double[][] M = new double[n][2 * n];

        //Matrix wird erweitert
        for (int i = 0; i < n; i += 1) {
            //Matrix wird kopiert
            System.arraycopy(matrix[i], 0, M[i], 0, n);
            //Einheitsmatrix wird erstellt
            M[i][i + n] = 1.0;
        }

        //Gauß-Jordan-Elimination
        for (int i = 0; i < n; i += 1) {
            double max = Math.abs(M[i][i]);
            int pivot = i;
            for (int k = i + 1; k < n; k += 1) {
                if (Math.abs(M[k][i]) > max) {
                    max = Math.abs(M[k][i]);
                    pivot = k;
                }
            }
            double[] temp = M[i];
            M[i] = M[pivot];
            M[pivot] = temp;

            //Normierung
            double diag = M[i][i];
            for (int j = 0; j < 2 * n; j += 1) {
                M[i][j] = M[i][j] / diag;
            }

            //Elimination
            for (int k = 0; k < n; k += 1) {
                if (k == i) {
                    continue;
                }
                double factor = M[k][i];
                for (int j = 0; j < 2 * n; j++) {
                    M[k][j] = M[k][j] - factor * M[i][j];
                }
            }
        }

        //Inverse Matrix wird extrahiert
        double[][] inverse = new double[n][n];
        for (int i = 0; i < n; i += 1) {
            System.arraycopy(M[i], n, inverse[i], 0, n);
        }

        return inverse;
    }

    public void setCart(double x, double y, double z) {
        posCart = new double[]{x, y, z};
    }

    public void setPolar(double width, double length, double height) {
        posPolar = new double[]{width, length, height};
    }

    public double[] getCart() {
        return posCart;
    }

    public double[] getPolar() {
        return posPolar;
    }

    public void calcPolar() {
        calcPolar(posCart);
    }

    public void calcCart() {
        calcCart(posPolar);
    }

    public void calcPolar(double[] posCart_) {
        //Berechnung von Polarkoordinaten aus Kartesischen Koordinaten (siehe 2.1)
        double length;
        double width;
        double x = posCart_[0];
        double y = posCart_[1];
        double z = posCart_[2];

        //Längengrad
        //Alternativ hätte man auch Math.atan2(y, x) nehmen können, das ist genau das
        if (x > 0) {
            length = Math.toDegrees(Math.atan(y / x));
        } else if (x < 0 && y >= 0) {
            length = Math.toDegrees(Math.atan(y / x)) + 90;
        } else if (x < 0 && y < 0) {
            length = Math.toDegrees(Math.atan(y / x)) - 90;
        } else if (x == 0 && y > 0) {
            length = 90;
        } else if (x == 0 && y < 0) {
            length = -90;
        } else {
            length = 0;
        }

        //Breitengrad
        if (x == 0 && y == 0) {
            width = 0;
        } else {
            width = Math.toDegrees(Math.atan(z / Math.sqrt(x * x + y * y)));
        }

        //Betrag bzw. Höhe berechnen
        double height = Math.sqrt(x * x + y * y + z * z) - RADIUS_EARTH;

        setPolar(width, length, height);
    }

    public void calcCart(double[] pos_pol) {
        //Berechnung von Kartesischen Koordinaten aus Polarkoordinaten (siehe 2.1)
        double width = pos_pol[0]; //Breitengrad
        double length = pos_pol[1]; //Längengrad
        double height = pos_pol[2]; //Höhe

        //Berechnung Karthesische Koordinaten
        double x = (RADIUS_EARTH + height) * Math.cos(Math.toRadians(width)) * Math.cos(Math.toRadians(length)); //x
        double y = (RADIUS_EARTH + height) * Math.cos(Math.toRadians(width)) * Math.sin(Math.toRadians(length)); //y
        double z = (RADIUS_EARTH + height) * Math.sin(Math.toRadians(width)); //z

        setCart(x, y, z);
    }
}
