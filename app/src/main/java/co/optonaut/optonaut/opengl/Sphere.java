package co.optonaut.optonaut.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import co.optonaut.optonaut.opengl.deprecated.example.MyGLRenderer;
import co.optonaut.optonaut.util.Maths;

/**
 * @author Nilan Marktanner
 * @date 2015-12-18
 */
// source: www.jimscosmos.com/code/android-open-gl-texture-mapped-spheres/
public class Sphere {
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
            "attribute vec2 a_texCoord;" +
            "varying vec2 v_texCoord;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  v_texCoord = a_texCoord;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "varying vec2 v_texCoord;" +
            "uniform sampler2D s_texture;" +
            "void main() {" +
            "  gl_FragColor = texture2D(s_texture, v_texCoord);" +
            "}";

    private final int numVerticesPerStrip;

    private int program;

    // Handles
    private int positionHandle;
    private int mvpMatrixHandle;
    private int texCoordHandle;
    private int textureSamplerHandle;

    /**
     * Sphere constructor.
     * @param depth integer representing the split of the sphere. Will be clamped to internal variable {@code MAXIMUM_ALLOWED_DEPTH}
     * @param radius The spheres radius.
     */
    public Sphere(final int depth, final float radius) {
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

        }

        initializeProgram();
    }

    public void loadGLTexture(final Bitmap bitmap, boolean recycle) {
        GLES20.glGenTextures(1, this.textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textures[0]);

        // Create nearest filtered texture
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

        // Use Android GLUtils to specify a two-dimensional texture image from our bitmap.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        if (recycle) {
            bitmap.recycle();
        }
    }

    public void draw(float[] mvpMatrix) {
        // bind the previously generated texture.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textures[0]);
        GLES20.glUseProgram(program);

        // get handles
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        texCoordHandle = GLES20.glGetAttribLocation(program, "a_texCoord");
        textureSamplerHandle = GLES20.glGetUniformLocation(program, "s_texture");

        // prepare coordinates
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(texCoordHandle);



        // TODO: check if necessary
        GLES20.glFrontFace(GLES20.GL_CW);

        for (int i = 0; i < this.totalNumOfStrips; ++i) {
            GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer.get(i));
            GLES20.glVertexAttribPointer (texCoordHandle, COORDS_PER_TEXTURE, GLES20.GL_FLOAT, false, 0, textureBuffer.get(i));

            // Pass the projection and view transformation to the shader
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

            // Set the sampler texture unit to 0, where we have saved the texture.
            GLES20.glUniform1i(textureSamplerHandle, 0);

            // Draw the sphere
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, numVerticesPerStrip);
        }
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }

    public boolean hasTexture() {
        return GLES20.glIsTexture(this.textures[0]);
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

    public void clearTexture() {
        if (hasTexture()) {
            GLES20.glDeleteTextures(1, textures, 0);
        }
    }
}
