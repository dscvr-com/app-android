package com.iam360.iam360.opengl;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.iam360.iam360.util.Maths;
import com.iam360.iam360.util.MyGLUtils;

/**
 * @author Nilan Marktanner
 * @date 2016-02-13
 */
// source: www.jimscosmos.com/code/android-open-gl-texture-mapped-spheres/
public class Sphere {
    /** Maximum allowed depth. */
    private static final int MAXIMUM_ALLOWED_DEPTH = 7;

    /** Used in vertex strip calculations, related to properties of a icosahedron. */
    private static final int VERTEX_MAGIC_NUMBER = 5;

    /** Each vertex is a 2D coordinate. */
    private static final int COORDS_PER_VERTEX = 3;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    /** Buffer holding the vertices. */
    private final List<FloatBuffer> vertexBuffer = new ArrayList<>();

    /** The vertices for the sphere. */
    private final List<float[]> vertices = new ArrayList<>();

    /** Total number of strips for the given depth. */
    private final int totalNumOfStrips;


    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "void main() {" +
//            "  gl_FragColor = vec4(0.9372, 0.2823, 0.2117, 1.0);" +
//            "  gl_FragColor = vec4(0.996, 0.812, 0.365, 1.0);" +//yellow
            "  gl_FragColor = vec4(1, 0.459, 0, 1.0);" +//orange
            "}";
    // EF4836

    private final int numVerticesPerStrip;

    private int program;

    // Handles
    private int positionHandle;
    private int mvpMatrixHandle;
    private int colorHandle;
    private float[] transform = new float[16];

    /**
     * Sphere constructor.
     * @param depth integer representing the split of the sphere. Will be clamped to internal variable {@code MAXIMUM_ALLOWED_DEPTH}
     * @param radius The spheres radius.
     */
    public Sphere(final int depth, final float radius) {
        // Clamp depth to the range 1 to MAXIMUM_ALLOWED_DEPTH;
        final int d = Math.max(1, Math.min(MAXIMUM_ALLOWED_DEPTH, depth));


        Matrix.setIdentityM(transform, 0);

        // Calculate basic values for the sphere.
        this.totalNumOfStrips = Maths.power(2, d - 1) * VERTEX_MAGIC_NUMBER;
        this.numVerticesPerStrip = Maths.power(2, d) * 3;
        final double altitudeStepAngle = Maths.ONE_TWENTY_DEGREES / Maths.power(2, d);
        final double azimuthStepAngle = Maths.THREE_SIXTY_DEGREES / this.totalNumOfStrips;
        double x, y, z, h, altitude, azimuth;

        for (int stripNum = 0; stripNum < this.totalNumOfStrips; stripNum++) {
            // Setup arrays to hold the points for this strip.
            final float[] vertices = new float[numVerticesPerStrip * COORDS_PER_VERTEX];
            int vertexPos = 0;

            // Calculate position of the first vertex in this strip.
            altitude = Maths.NINETY_DEGREES;
            azimuth = stripNum * azimuthStepAngle;

            // Draw the rest of this strip.
            for (int vertexNum = 0; vertexNum < numVerticesPerStrip; vertexNum += 2) {
                // First point - Vertex.
                y = radius * Math.sin(altitude);
                h = radius * Math.cos(altitude);
                z = h * Math.sin(azimuth);
                x = h * Math.cos(azimuth);
                vertices[vertexPos++] = (float) x;
                vertices[vertexPos++] = (float) y;
                vertices[vertexPos++] = (float) z;

                // Second point - Vertex.
                altitude -= altitudeStepAngle;
                azimuth -= azimuthStepAngle / 2.0;
                y = radius * Math.sin(altitude);
                h = radius * Math.cos(altitude);
                z = h * Math.sin(azimuth);
                x = h * Math.cos(azimuth);
                vertices[vertexPos++] = (float) x;
                vertices[vertexPos++] = (float) y;
                vertices[vertexPos++] = (float) z;

                azimuth += azimuthStepAngle;
            }

            this.vertices.add(vertices);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(numVerticesPerStrip * COORDS_PER_VERTEX * Float.SIZE);
            byteBuffer.order(ByteOrder.nativeOrder());
            FloatBuffer fb = byteBuffer.asFloatBuffer();
            fb.put(this.vertices.get(stripNum));
            fb.position(0);
            this.vertexBuffer.add(fb);
        }
    }

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(program);

        // get handles
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");

        // prepare coordinates
        GLES20.glEnableVertexAttribArray(positionHandle);

        // TODO: check if necessary
        GLES20.glFrontFace(GLES20.GL_CW);

        float[] matrix = new float[16];
        Matrix.multiplyMM(matrix, 0, mvpMatrix, 0, transform, 0);

        for (int i = 0; i < this.totalNumOfStrips; ++i) {
            GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer.get(i));
            // Pass the projection and view transformation to the shader
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, matrix, 0);

            // Draw the sphere
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, numVerticesPerStrip);
        }
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    public void initializeProgram() {
        int vertexShader = MyGLUtils.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLUtils.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        program = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(program, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(program, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(program);
    }

    public float[] getTransform() {
        return transform;
    }

    public void setTransform(float[] transform) {
        this.transform = transform;
    }
}
