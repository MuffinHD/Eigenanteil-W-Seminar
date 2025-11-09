package org.wseminar.eigenanteil;

import java.util.ArrayList;
import java.util.Arrays;

public class Calculation {

    public static void main(String[] args) {
        ArrayList<Satellite> sats = new ArrayList<>();

        Reciever rec = new Reciever();

        //Beispieldaten von vier Satelliten
        sats.add(new Satellite(24579064.322, 8946047.799, 4612095.599, 21947041.935));
        sats.add(new Satellite(-8536219.457, 14785165.803, 20346140.409, 24487215.189));
        sats.add(new Satellite(11500817.362, -19920000.000, -13279999.999, 28452219.402));
        sats.add(new Satellite(-8536219.457, -3106929.795, 24958236.008, 24220675.368));

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
