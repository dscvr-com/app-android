package co.optonaut.optonaut.util;

/**
 * @author Nilan Marktanner
 * @date 2015-12-18
 */
// source: http://www.jimscosmos.com/code/android-open-gl-texture-mapped-spheres/
public class Maths {
    /**
     * 180 in radians.
     */
    public static final double ONE_EIGHTY_DEGREES = Math.PI;

    /**
     * 360 in radians.
     */
    public static final double THREE_SIXTY_DEGREES = ONE_EIGHTY_DEGREES * 2;

    /**
     * 120 in radians.
     */
    public static final double ONE_TWENTY_DEGREES = THREE_SIXTY_DEGREES / 3;

    /**
     * 90 degrees, North pole.
     */
    public static final double NINETY_DEGREES = Math.PI / 2;

    /**
     * Used by power.
     */
    private static final long POWER_CLAMP = 0x00000000ffffffffL;

    /**
     * Constructor, although not used at the moment.
     */
    private Maths() {
    }

    /**
     * Quick integer power function.
     *
     * @param base  number to raise.
     * @param raise to this power.
     * @return base ^ raise.
     */
    public static int power(final int base, final int raise) {
        int p = 1;
        long b = raise & POWER_CLAMP;

        // bits in b correspond to values of powerN
        // so start with p=1, and for each set bit in b, multiply corresponding
        // table entry
        long powerN = base;

        while (b != 0) {
            if ((b & 1) != 0) {
                p *= powerN;
            }
            b >>>= 1;
            powerN = powerN * powerN;
        }

        return p;
    }

    public static float[] multiplyThreeByThreeMatrices(float[] A, float[] B) {
        if (A.length != 9 || B.length != 9) {
            throw new RuntimeException();
        }

        float[] result = new float[9];
        result[0] = A[0]*B[0] + A[1]*B[3] + A[2]*B[6];
        result[1] = A[0]*B[1] + A[1]*B[4] + A[2]*B[7];
        result[2] = A[0]*B[2] + A[1]*B[5] + A[2]*B[8];

        result[3] = A[3]*B[0] + A[4]*B[3] + A[5]*B[6];
        result[4] = A[3]*B[1] + A[4]*B[4] + A[5]*B[7];
        result[5] = A[3]*B[2] + A[4]*B[5] + A[5]*B[8];

        result[6] = A[6]*B[0] + A[7]*B[3] + A[8]*B[6];
        result[7] = A[6]*B[1] + A[7]*B[4] + A[8]*B[7];
        result[8] = A[6]*B[2] + A[7]*B[5] + A[8]*B[8];

        return result;
    }

    public static float[] getIdentity(int rank) {
        if (rank <= 0) {
            throw new RuntimeException();
        }

        float[] result = new float[rank*rank];

        for (int i = 0; i<rank*rank; i++) {
            if (i % (rank+1) == 0) {
                result[i] = 1;
            } else {
                result[i] = 0;
            }
        }

        return result;
    }
}