package co.optonaut.optonaut.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import co.optonaut.optonaut.util.MyGLUtils;

/**
 * @author Nilan Marktanner
 * @date 2016-01-12
 */
public class Plane {
    private static final int COORDS_PER_VERTEX = 3;
    private static final int COORDS_PER_TEXTURE = 2;
    private static final int VERTICES_PER_PLANE = 4;

    private static final float[] VERTICES = {
            -0.5f, 0.0f, 0.5f, // left front
            -0.5f, 0.0f, -0.5f, // left back
            0.5f, 0.0f, 0.5f, // right front
            0.5f, 0.0f, -0.5f  // right back
    };

    private static final float[] TEXTURE_COORDS = {
            0, 0,  // bottom left
            0, 1, // top left
            1, 1, // top right
            1, 0 // bottom right
    };


    private FloatBuffer vertexBuffer;

    private FloatBuffer textureBuffer;

    private final int[] textures = new int[1];


    private final String vertexShaderCode =
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
            //"  gl_FragColor = vec4(abs(v_texCoord.x), 0.0, 0.0, 0.0);" +
            //"  gl_FragColor = vec4(1.0, 0.0, 0.0, 0.0);" +
            "  gl_FragColor = texture2D(s_texture, v_texCoord);" +
            "}";

    private int program;

    // Handles
    private int positionHandle;
    private int mvpMatrixHandle;
    private int texCoordHandle;
    private int textureSamplerHandle;

    public Plane(Bitmap texture) {
        initialize();
        loadGLTexture(texture);
    }

    public Plane() {
        initialize();
    }

    private void initialize() {
        buildBuffers();
        initializeProgram();
    }

    private void buildBuffers() {
        buildVertexBuffer();
        buildTextureBuffer();
    }

    private void buildVertexBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(VERTICES_PER_PLANE * COORDS_PER_VERTEX * Float.SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());

        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(VERTICES);
        vertexBuffer.position(0);
    }


    private void buildTextureBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(VERTICES_PER_PLANE * COORDS_PER_TEXTURE * Float.SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());

        textureBuffer = byteBuffer.asFloatBuffer();
        textureBuffer.put(TEXTURE_COORDS);
        textureBuffer.position(0);
    }


    private void initializeProgram() {
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

    public void loadGLTexture(final Bitmap bitmap) {
        GLES20.glGenTextures(1, this.textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textures[0]);

        // Create nearest filtered texture
        //GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        //GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

        // Use Android GLUtils to specify a two-dimensional texture image from our bitmap.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
    }

    public void draw(float[] mvpMatrix) {
        // bind the previously generated texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textures[0]);
        GLES20.glUseProgram(program);

        // get handles
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        texCoordHandle = GLES20.glGetAttribLocation(program, "a_texCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        textureSamplerHandle = GLES20.glGetUniformLocation(program, "s_texture");

        // prepare coordinates
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(texCoordHandle);

        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glVertexAttribPointer(texCoordHandle, COORDS_PER_TEXTURE, GLES20.GL_FLOAT, false, 0, textureBuffer);

        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i(textureSamplerHandle, 0);

        // Draw the plane
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTICES_PER_PLANE);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }

}
