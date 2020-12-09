package jade;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import renderer.Shader;
import renderer.Texture;
import util.Time;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

public class LevelEditorScene extends Scene{

//    private String vertexShaderSrc = "#version 330 core\n" +
//            "\n" +
//            "layout (location=0) in vec3 aPos;\n" +
//            "layout (location=1) in vec4 aColor;\n" +
//            "\n" +
//            "out vec4 fColor;\n" +
//            "\n" +
//            "void main(){\n" +
//            "    fColor = aColor;\n" +
//            "    gl_Position = vec4(aPos, 1.0);\n" +
//            "}";
//
//    private String fragmentShaderSrc = "#version 330 core\n" +
//            "\n" +
//            "in vec4 fColor;\n" +
//            "\n" +
//            "out vec4 color;\n" +
//            "\n" +
//            "void main(){\n" +
//            "    color = fColor;\n" +
//            "}";

    private int vertexID, fragmentID, shaderProgram;

    private float[] vertexArray = {
        //pos                       //color                     //UV coordinates
        100f, 0f, 0.0f,          1.0f, 0.0f, 0.0f, 1.0f,        1, 1,//bottom right   0
        0f, 100f, 0.0f,          0.0f, 1.0f, 0.0f, 1.0f,        0, 0,//Top left       1
        100f, 100f, 0.0f,        1.0f, 0.0f, 1.0f, 1.0f,        1, 0,//Top right      2
        0f, 0f, 0.0f,            1.0f, 1.0f, 0.0f, 1.0f,        0, 1//Bottom left     3

    };

    //Must be counter-clockwise order
    private int[] elementArray = {
            2,1,0,
            0,1,3
    };

    private int vaoID, vboID, eboID;

    private Shader defaultShader;
    private Texture testTexture;

    public LevelEditorScene(){

    }

    @Override
    public void init(){
        this.camera = new Camera(new Vector2f(-200, -300));
        defaultShader = new Shader("assets/shaders/a.glsl");
        defaultShader.compile();
        //make sure image has alpha(opacity) of 1
        this.testTexture = new Texture("assets/images/2474193.jpg"); //images need to be large, like real large//

        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        int positionsSize = 3;
        int colorSize = 4;
        int uvSize = 2;
        int vertexSizeBytes = (positionsSize + colorSize + uvSize) * Float.BYTES;
        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionsSize * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, uvSize, GL_FLOAT, false, vertexSizeBytes, (positionsSize+colorSize) * Float.BYTES);
        glEnableVertexAttribArray(2);
    }

    @Override
    public void update(float dt) {
        //move noise box
        //camera.position.x -= dt * 50.0f;
        //camera.position.y -= dt * 50.0f;
        //bind shader
        defaultShader.use();

        //Upload texture to shader
        defaultShader.uploadTexture("TEX_SAMPLER", 0);
        glActiveTexture(GL_TEXTURE0);
        testTexture.bind();

        defaultShader.uploadMat4f("uProjection",camera.getProjectionMatrix());
        defaultShader.uploadMat4f("uView", camera.getViewMatrix());
        defaultShader.uploadFloat("uTime", Time.getTime());

        glBindVertexArray(vaoID);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        //reiterate using nothing
        glBindVertexArray(0);

        defaultShader.detach();

    }

}