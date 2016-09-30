package com.iam360.iam360.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.iam360.iam360.util.Constants;
import com.iam360.iam360.util.MyGLUtils;
import com.iam360.iam360.util.Vector3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import timber.log.Timber;

/**
 * Created by Joven on 9/22/2016.
 */
public class Plane2 {
    public boolean isInitiliazed = false;
    public float[] translation = new float[16];
    public float[] rotation = new float[16];
    public float[] init_rotation = new float[16];


    private static final int COORDS_PER_VERTEX = 3;
    private static final int COORDS_PER_TEXTURE = 2;
    private static final int VERTICES_PER_PLANE = 4;

    private Bitmap texture;


    private static final float[] VERTICES = {
            -0.5f, 0.5f, 0.0f, // left front
            -0.5f, -0.5f, 0.0f, // left back
            0.5f, 0.5f, 0.0f, // right front
            0.5f, -0.5f, 0.0f  // right back
    };

    private static final float[] TEXTURE_COORDS = {
            0, 0,  // bottom left
            0, 1, // top left
            1, 0, // bottom right
            1, 1 // top right

    };

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private final int[] textures = new int[1];

    private boolean hasTexture;
    private boolean textureUpdated;

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
                    "  gl_FragColor = texture2D(s_texture, v_texCoord);" +
                    "}";

    private int program;
    // Handles
    private int positionHandle;
    private int mvpMatrixHandle;
    private int texCoordHandle;
    private int textureSamplerHandle;
    private final Vector3 center = new Vector3();

    public Plane2() {
        initialize();
    }

    private void initialize() {
        buildBuffers();
        resetTexture();
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

    public synchronized void updateTexture(final Bitmap texture) {
        this.texture = texture;
        this.textureUpdated = true;
    };




    private void loadGLTexture() {
        if (texture == null) {
            Timber.w("Loading texture but got no texture in Plane!");
            return;
        }
        GLES20.glGenTextures(1, this.textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textures[0]);

        // Filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // Use Android GLUtils to specify a two-dimensional texture image from our bitmap.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texture, 0);
        this.hasTexture = true;
        this.textureUpdated = false;
    }

    public void draw(float[] mvpMatrix) {
        // Log.d("MARK","Plane mvpMatrix = "+ Arrays.toString(mvpMatrix));
        if (!GLES20.glIsTexture(this.textures[0]) && hasTexture) {
            Timber.v("Rebinding texture, context was probably lost.");
            synchronized (this) {
                loadGLTexture();
            }
        } else if (GLES20.glIsTexture(this.textures[0]) && !hasTexture) {
            Timber.w("Got no texture but is texture!");
        }

        if (textureUpdated) {
            synchronized (this) {
                loadGLTexture();
            }
        }

        if (hasTexture) {
            // bind the previously generated texture.
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textures[0]);
        }
        GLES20.glUseProgram(program);

        // get handles
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");

        if (hasTexture) {
            texCoordHandle = GLES20.glGetAttribLocation(program, "a_texCoord");
            textureSamplerHandle = GLES20.glGetUniformLocation(program, "s_texture");
            GLES20.glEnableVertexAttribArray(texCoordHandle);
        }

        // prepare coordinates
        GLES20.glEnableVertexAttribArray(positionHandle);

        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        if (hasTexture) {
            GLES20.glVertexAttribPointer(texCoordHandle, COORDS_PER_TEXTURE, GLES20.GL_FLOAT, false, 0, textureBuffer);

            // Set the sampler texture unit to 0, where we have saved the texture.
            GLES20.glUniform1i(textureSamplerHandle, 0);
        }

        // Draw the plane
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTICES_PER_PLANE);

        GLES20.glDisableVertexAttribArray(positionHandle);

        if (hasTexture) {
            GLES20.glDisableVertexAttribArray(texCoordHandle);
        }
    }

    public void resetTexture() {
        this.hasTexture = false;
        this.textureUpdated = false;
        updateTexture(Constants.getInstance().getDefaultTexture());
    }

    public void setInitiliazed(boolean initiliazed) {
        isInitiliazed = initiliazed;
    }

    public boolean isInitiliazed() {
        return isInitiliazed;
    }

    public void setInitRotation(float[] rotation) {
        this.init_rotation = rotation;
    }



    public float[] getInitRotation() {
        return init_rotation;
    }



    public void setRotation(float[] rotation) {
        this.rotation = rotation;
    }



    public float[] getRotation() {
        return rotation;
    }

    public void setTranslation(float[] translation) {
        this.translation = translation;
    }

    public float[] getTranslation() {
        return translation;
    }

    public void setCenter(float x, float y, float z){
        this.center.set(x,y,z);
    }

    public Vector3 getCenter() {
        return center;
    }
}