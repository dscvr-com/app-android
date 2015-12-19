package co.optonaut.optonaut.opengl.ES2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import co.optonaut.optonaut.opengl.ES1.Maths;
import co.optonaut.optonaut.opengl.MyGLRenderer;

/**
 * @author Nilan Marktanner
 * @date 2015-12-18
 */

// source: www.jimscosmos.com/code/android-open-gl-texture-mapped-spheres/
public class GL2Sphere {
    /** Maximum allowed depth. */
    private static final int MAXIMUM_ALLOWED_DEPTH = 7;

    /** Used in vertex strip calculations, related to properties of a icosahedron. */
    private static final int VERTEX_MAGIC_NUMBER = 5;

    /** Each vertex is a 2D coordinate. */
    private static final int COORDS_PER_VERTEX = 3;

    /** Each texture is a 2D coordinate. */
    private static final int COORDS_PER_TEXTURE = 2;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    /** Buffer holding the vertices. */
    private final List<FloatBuffer> vertexBuffer = new ArrayList<>();

    /** The vertices for the sphere. */
    private final List<float[]> vertices = new ArrayList<>();

    /** Buffer holding the texture coordinates. */
    private final List<FloatBuffer> textureBuffer = new ArrayList<>();

    /** The texture pointer. */
    private final int[] textures = new int[1];

    /** Total number of strips for the given depth. */
    private final int totalNumOfStrips;


    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            // the matrix must be included as a modifier of gl_Position
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private final int numVerticesPerStrip;

    private int program;

    // Handles
    private int positionHandle;
    private int colorHandle;
    private int mvpMatrixHandle;

    // Set color with red, green, blue and alpha (opacity) values
    private float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    /**
     * GL2Sphere constructor.
     * @param depth integer representing the split of the sphere.
     * @param radius The spheres radius.
     */
    public GL2Sphere(final int depth, final float radius) {
        // Clamp depth to the range 1 to MAXIMUM_ALLOWED_DEPTH;
        final int d = Math.max(1, Math.min(MAXIMUM_ALLOWED_DEPTH, depth));

        // Calculate basic values for the sphere.
        this.totalNumOfStrips = Maths.power(2, d - 1) * VERTEX_MAGIC_NUMBER;
        this.numVerticesPerStrip = Maths.power(2, d) * 3;
        final double altitudeStepAngle = Maths.ONE_TWENTY_DEGREES / Maths.power(2, d);
        final double azimuthStepAngle = Maths.THREE_SIXTY_DEGREES / this.totalNumOfStrips;
        double x, y, z, h, altitude, azimuth;
        final List<float[]> texture = new ArrayList<>();


        for (int stripNum = 0; stripNum < this.totalNumOfStrips; stripNum++) {
            // Setup arrays to hold the points for this strip.
            final float[] vertices = new float[numVerticesPerStrip * COORDS_PER_VERTEX];
            final float[] texturePoints = new float[numVerticesPerStrip * COORDS_PER_TEXTURE];
            int vertexPos = 0;
            int texturePos = 0;

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

                // First point - Texture.
                texturePoints[texturePos++] = (float) (1 - azimuth / Maths.THREE_SIXTY_DEGREES);
                texturePoints[texturePos++] = (float) (1 - (altitude + Maths.NINETY_DEGREES) / Maths.ONE_EIGHTY_DEGREES);

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

                // Second point - Texture.
                texturePoints[texturePos++] = (float) (1 - azimuth / Maths.THREE_SIXTY_DEGREES);
                texturePoints[texturePos++] = (float) (1 - (altitude + Maths.NINETY_DEGREES) / Maths.ONE_EIGHTY_DEGREES);

                azimuth += azimuthStepAngle;
            }

            this.vertices.add(vertices);
            texture.add(texturePoints);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(numVerticesPerStrip * COORDS_PER_VERTEX * Float.SIZE);
            byteBuffer.order(ByteOrder.nativeOrder());
            FloatBuffer fb = byteBuffer.asFloatBuffer();
            fb.put(this.vertices.get(stripNum));
            fb.position(0);
            this.vertexBuffer.add(fb);

            // Setup texture.
            byteBuffer = ByteBuffer.allocateDirect(numVerticesPerStrip * COORDS_PER_TEXTURE * Float.SIZE);
            byteBuffer.order(ByteOrder.nativeOrder());
            fb = byteBuffer.asFloatBuffer();
            fb.put(texture.get(stripNum));
            fb.position(0);
            this.textureBuffer.add(fb);

            initializeProgram();
        }
    }

    /**
     * Load the texture for the square.
     *
     * @param gl Handle.
     * @param context Handle.
     * @param texture Texture map for the sphere.
     */
    public void loadGLTexture(final GL10 gl, final Context context, final int texture) {
        // Generate one texture pointer, and bind it to the texture array.
        gl.glGenTextures(1, this.textures, 0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, this.textures[0]);

        // Create nearest filtered texture.
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        // Use Android GLUtils to specify a two-dimensional texture image from our bitmap.
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), texture);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
    }

    public void loadGLTexture(final Context context, final int texture) {
        GLES20.glGenTextures(1, this.textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        // Create nearest filtered texture
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

        // Use Android GLUtils to specify a two-dimensional texture image from our bitmap.
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), texture);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
    }

    /**
     * The draw method for the square with the GL context.
     *
     * @param gl Graphics handle.
     */
    public void draw(float[] mvpMatrix) {
        // bind the previously generated texture.
        //gl.glBindTexture(GL10.GL_TEXTURE_2D, this.textures[0]);

        GLES20.glUseProgram(program);

        // get handles
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        colorHandle = GLES20.glGetUniformLocation(program, "vColor");
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");

        // prepare coordinates
        GLES20.glEnableVertexAttribArray(positionHandle);

        for (int i = 0; i < this.totalNumOfStrips; ++i) {
            GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer.get(i));

            // get handle to fragment shader's vColor member
            colorHandle = GLES20.glGetUniformLocation(program, "vColor");

            // Set color for drawing the triangle
            GLES20.glUniform4fv(colorHandle, 1, color, 0);

            // get handle to shape's transformation matrix
            mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");

            // Pass the projection and view transformation to the shader
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

            // Draw the triangle
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, numVerticesPerStrip);

        }


        /*
        // Point to our buffers.
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        // Set the face rotation, clockwise in this case.
        gl.glFrontFace(GL10.GL_CW);

        // Point to our vertex buffer.
        for (int i = 0; i < this.totalNumOfStrips; i++) {
            gl.glVertexPointer(COORDS_PER_VERTEX, GL10.GL_FLOAT, 0, this.vertexBuffer.get(i));
            gl.glTexCoordPointer(COORDS_PER_TEXTURE, GL10.GL_FLOAT, 0, this.textureBuffer.get(i));

            // Draw the vertices as triangle strip.
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, this.vertices.get(i).length / COORDS_PER_VERTEX);
        }

        // Disable the client state before leaving.
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        */
    }

    private void initializeProgram() {
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
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

}
