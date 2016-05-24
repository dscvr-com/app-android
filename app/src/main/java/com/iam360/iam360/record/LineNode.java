package com.iam360.iam360.record;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.iam360.iam360.util.MyGLUtils;

/**
 * @author Nilan Marktanner
 * @date 2016-02-10
 */
public class LineNode {
    private static final int COORDS_PER_VERTEX = 3;
    private static final int VERTICES_PER_LINE = 2;
    private static float[] VERTICES = new float[VERTICES_PER_LINE * COORDS_PER_VERTEX];

    private FloatBuffer vertexBuffer;

    boolean isProgramInitialized;

    private float[] posA;
    private float[] posB;

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private int program;
    // Handles
    private int positionHandle;
    private int colorHandle;
    private int mvpMatrixHandle;

//    private float colorRed[] = {0.9372f, 0.2823f, 0.2117f, 1.0f };
    private float colorRed[] = {0.996f, 0.812f, 0.365f, 1.0f };// colorYellow 0.996, 0.812, 0.365
    private float colorWhite[] = {1.0f, 1.0f, 1.0f, 1.0f };
    private float color[] = colorWhite;

    public LineNode(float[] posA, float[] posB) {
        this.posA = posA;
        this.posB = posB;
        isProgramInitialized = false;

        initialize();
    }

    private void initialize() {
        buildVertices();
        buildVertexBuffer();
    }

    private void buildVertices() {
        VERTICES[0] = posA[0];
        VERTICES[1] = posA[1];
        VERTICES[2] = posA[2];
        VERTICES[3] = posB[0];
        VERTICES[4] = posB[1];
        VERTICES[5] = posB[2];
    }

    private void buildVertexBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(VERTICES_PER_LINE * COORDS_PER_VERTEX * Float.SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());

        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(VERTICES);
        vertexBuffer.position(0);
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

        isProgramInitialized = true;
    }

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(program);

        // get handles
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        colorHandle = GLES20.glGetUniformLocation(program, "vColor");
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");

        // prepare coordinates
        GLES20.glEnableVertexAttribArray(positionHandle);

        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        // Set color
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the line
        GLES20.glLineWidth(10);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, VERTICES_PER_LINE);

        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    public boolean isProgramInitialized() {
        return isProgramInitialized;
    }

    public void isRecordedEdge(boolean recorded) {
        if(recorded) color = colorRed;
        else color = colorWhite;
    }
}
