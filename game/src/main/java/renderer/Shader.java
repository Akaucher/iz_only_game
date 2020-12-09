package renderer;

import org.joml.*;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class Shader {

    private int shaderProgramID;

    private String vertexSource;
    private String fragmentSource;
    private String filepath;
    private boolean beingUsed = false;

    public Shader(String filepath){
        this.filepath = filepath;
        try {
            String source = new String(Files.readAllBytes(Paths.get(filepath)));
            String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");

            int index = source.indexOf("#type")+6;
            int eol = source.indexOf("\r\n", index);
            String firstPattern = source.substring(index, eol).trim();

            index = source.indexOf("#type", eol)+ 6;
            eol = source.indexOf("\r\n", index);
            String secondPattern = source.substring(index, eol).trim();

            if (firstPattern.equals("vertex")){
                vertexSource = splitString[1];
            }else if (firstPattern.equals("fragment")){
                fragmentSource = splitString[1];
            }else{
                throw new IOException("Unexpected token '"+ firstPattern + " in "+filepath+"'");
            }

            if (secondPattern.equals("vertex")) {
                vertexSource = splitString[2];//possibly change to 1
            }else if (secondPattern.equals("fragment")){
                fragmentSource = splitString[2];//possibly change to 1
            }else{
                throw new IOException("Unexpected token '"+ secondPattern + "'");
            }
        }catch (IOException e){
            e.printStackTrace();
            assert false : "error: could not open shader file: '" + filepath + "'";
        }
    }

    public void compile(){
        int vertexID, fragmentID;

        vertexID = glCreateShader(GL_VERTEX_SHADER);
        //pass shader source into GPU
        glShaderSource(vertexID, vertexSource);
        glCompileShader(vertexID);

        //check for errors
        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);

        if (success == GL_FALSE){
            int len = glGetShaderi(vertexID,GL_INFO_LOG_LENGTH);
            System.out.println("Error: " + filepath+  "\n\tVertex shader compilation failed.");
            System.out.println(glGetShaderInfoLog(vertexID, len));
            assert false: "";
        }

        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
        //pass shader source into GPU
        glShaderSource(fragmentID, fragmentSource);
        glCompileShader(fragmentID);

        //check for errors
        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if (success == GL_FALSE){
            int len = glGetShaderi(fragmentID,GL_INFO_LOG_LENGTH);
            System.out.println("Error: " + filepath+  "\n\tFragment shader compilation failed.");
            System.out.println(glGetShaderInfoLog(fragmentID, len));
            assert false: "";
        }

        shaderProgramID = glCreateProgram();
        glAttachShader(shaderProgramID, vertexID);
        glAttachShader(shaderProgramID, fragmentID);
        glLinkProgram(shaderProgramID);

        success = glGetProgrami(shaderProgramID, GL_LINK_STATUS);
        if (success == GL_FALSE){
            int len = glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH);
            System.out.println("Error: '"+filepath+"'\n\tLinking of shaders failed.");
            System.out.println(glGetShaderInfoLog(shaderProgramID, len));
            assert false: "";
        }
    }

    public void use(){
        glUseProgram(shaderProgramID);
        if (!beingUsed){
            glUseProgram(shaderProgramID);
            beingUsed = true;
        }
    }

    public void detach(){
        glUseProgram(0);
        beingUsed = false;
    }

    public void uploadMat4f(String varName, Matrix4f mat4){
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
        mat4.get(matBuffer);
        glUniformMatrix4fv(varLocation, false, matBuffer);
    }

    public void uploadMat3f(String varName, Matrix3f mat3){
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(9);
        mat3.get(matBuffer);
        glUniformMatrix3fv(varLocation, false, matBuffer);
    }

    public void uploadVec4f(String varName, Vector4f vec4f){
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform4f(varLocation, vec4f.x, vec4f.y, vec4f.z, vec4f.w);
    }

    public void uploadVec3f(String varName, Vector3f vec3f){
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform3f(varLocation, vec3f.x, vec3f.y, vec3f.z);
    }

    public void uploadVec2f(String varName, Vector2f vec2f){
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform2f(varLocation, vec2f.x, vec2f.y);
    }

    public void uploadFloat(String varName, float val){
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform1f(varLocation, val);
    }

    public void uploadInt (String varName, int val){
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform1i(varLocation, val);
    }

    public void uploadTexture (String varName, int slot){
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform1i(varLocation, slot);
    }
}
