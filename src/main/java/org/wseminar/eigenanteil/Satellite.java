package org.wseminar.eigenanteil;

public class Satellite {

    double[] pos;
    double pr;

    public Satellite(double x_, double y_, double z_, double pr_) {
        pos = new double[]{x_, y_, z_};
        pr = pr_;
    }
}
