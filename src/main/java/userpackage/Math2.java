package userpackage;

public class Math2 {
    public static float round(float f) {
        double dd = (double)f - Math.floor((double)f);
        float ff = (float)dd;
        float f2 = (float)Math.floor((double)(ff * 10.0F));
        float f3 = f2 / 10.0F;
        float f4 = f3 + 0.05F;
        float f5 = f3 + 0.1F;
        float f34 = f3 + 0.025F;
        float f45 = f4 + 0.025F;
        float f0 = 0.0F;
        if (ff <= f34) {
            f0 = f3;
        }

        if (ff > f34 && ff <= f45) {
            f0 = f4;
        }

        if (ff > f45) {
            f0 = f5;
        }

        int i = (int)f;
        float rsl = (float)i + f0;
        return rsl;
    }
}
