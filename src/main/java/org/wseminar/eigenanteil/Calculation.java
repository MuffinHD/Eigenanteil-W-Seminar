package org.wseminar.eigenanteil;

import java.util.ArrayList;
import java.util.Arrays;

public class Calculation {

    public static void main(String[] args) {
        ArrayList<Satellite> sats = new ArrayList<>();

        Reciever rec = new Reciever();

        //Beispieldaten von vier Satelliten für München
        sats.add(new Satellite(24579064.322, 8946047.799, 4612095.599, 21947041.935));
        sats.add(new Satellite(-8536219.457, 14785165.803, 20346140.409, 24487215.189));
        sats.add(new Satellite(11500817.362, -19920000.000, -13279999.999, 28452219.402));
        sats.add(new Satellite(-8536219.457, -3106929.795, 24958236.008, 24220675.368));

        //alternative Beispieldaten New York City
        //sats.add(new Satellite(7983583.1956, 13305971.992, 21555674.628, 25902131.591));
        //sats.add(new Satellite(-15967166.391, 10644777.594, 18362241.350, 27138869.939));
        //sats.add(new Satellite(23947143.723, -2660793.747, 11175333.737, 23781501.189));
        //sats.add(new Satellite(-5330958.614, -22656574.111, 12794300.674, 21072974.069));

        //Berechnung der Position und ausgabe der Endposition
        double[] pos = rec.calc_pos_rec(sats);
        System.out.println("Position:");
        System.out.printf("x = %.3f m%n", pos[0]);
        System.out.printf("y = %.3f m%n", pos[1]);
        System.out.printf("z = %.3f m%n", pos[2]);
        System.out.printf("dt = %.9f s%n", pos[3]);

        //Umrechnung Koordinaten
        rec.calcPolar();
        System.out.println(Arrays.toString(rec.getPolar()));
        rec.calcCart();
        System.out.println(Arrays.toString(rec.getCart()));

    }
}
