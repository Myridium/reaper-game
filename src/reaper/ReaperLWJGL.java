/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper;

import LWJGLTools.GLDrawing.GLDrawHelper;
import org.lwjgl.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;
import LWJGLTools.input.ControllerReader;


/**
 *
 * @author murdock
 */
public class ReaperLWJGL { 
    
    //The window handle
    private long gameWindow;
    
    private final long minFrameWaitNanoTime = Math.round(1000*1000*1000/120);
    private final int WIDTH = 1600;
    private final int HEIGHT = 1000;
    private final float SCALE = 1.0f;
    
    
    private final boolean FPS_CAPPED = true;
    
    // Stuff related to displaying the FPS
    private final boolean TRACK_FPS = true;
    private final int FPS_DISPLAY_MVA = 50;
    private int[] pastFPS;
    
    private ReaperFrameLooper reaperFrameLoop;
    
    private int displayWidth() {
        return Math.round(WIDTH*SCALE);
    }
    private int displayHeight() {
        return Math.round(HEIGHT*SCALE);
    }
    public void run() throws InterruptedException, ControllerReader.NoControllerException, ControllerReader.NoSuchAxisException, ControllerReader.NotConfiguredException {
        System.out.println("LWJGL Version: " + Version.getVersion());
        
        try {
            init();
            System.out.println();
            System.out.println("Welcome to Reaper.");
            loop();
            
            //Free the window callbacks and destroy the window
            glfwFreeCallbacks(gameWindow);
            glfwDestroyWindow(gameWindow);
        }  finally {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }
    
    private void init() {
        
        if (TRACK_FPS) {
            pastFPS = new int[FPS_DISPLAY_MVA];
            for (int i=0; i < FPS_DISPLAY_MVA; i++) {
                pastFPS[i] = 0;
            }
        }
        
        // Setup an error callback. The default implementation
	// will print the error message in System.err.
	GLFWErrorCallback.createPrint(System.err).set();
        
        // Initialise GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Failed to initialise GLFW.");
        
        // Configure the settings with which a new window will be created.
        // It will later be created and the handle assigned to gameWindow.
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // The window should be invisible until we're ready to display it.
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        
        // Create the window and assign the handle to gameWindow
        // Note that the NULL object is a constant defined in the LWJGL libraries.
        gameWindow = glfwCreateWindow(displayWidth(),displayHeight(), "Reaper", NULL, NULL);
        if (gameWindow == NULL)
            throw new RuntimeException("Failed to create the GLFW window!");
        
        // Setup a key callback. It will be called every time a key is presed, repeated or released.
        glfwSetKeyCallback(gameWindow, (gw, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(gw,true);
        });
        
        // Get the resolution of the primary monitor
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = glfwGetVideoMode(monitor);
        // Get the position of the primary monitor
        int[] xbuff = new int[1], ybuff = new int[1];
        glfwGetMonitorPos(monitor,xbuff,ybuff);
        
        // Center the window
        glfwSetWindowPos(
                gameWindow,
                (vidmode.width() - displayWidth()) / 2 + xbuff[0],
                (vidmode.height() - displayHeight()) / 2 + ybuff[0]
        );
        
        // Make the OpenGL context current
        glfwMakeContextCurrent(gameWindow);
        // Enable v-sync
        glfwSwapInterval(1);
        
        // Finally, make our configured window visible to the user
        glfwShowWindow(gameWindow);
        
        reaperFrameLoop = new ReaperFrameLooper();
        
    }
    
    public void loop() throws InterruptedException, ControllerReader.NoControllerException, ControllerReader.NoSuchAxisException, ControllerReader.NotConfiguredException {
        
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        GL11.glMatrixMode(GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, WIDTH, 0, HEIGHT, -1, 1);
        GL11.glMatrixMode(GL_MODELVIEW);
        
        GL11.glDisable(GL_DEPTH_TEST);
        GL11.glEnable(GL_BLEND);
        GL11.glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);

        reaperFrameLoop.init(WIDTH, HEIGHT);
        
        long currentTime = System.nanoTime();
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(gameWindow) ) {
                
                long elapsedNanoTime;
            
                // Putting on a frame cap (of ~120fps when this comment was made)
                if (FPS_CAPPED) {
                    while( System.nanoTime() - currentTime < minFrameWaitNanoTime) {
                        Thread.sleep(2);
                    }
                }
                
                elapsedNanoTime = System.nanoTime() - currentTime;
                currentTime = System.nanoTime();
            
                glClear(GL_COLOR_BUFFER_BIT); // clear the framebuffer

                // Poll for window events. The key callback above will only be
                // invoked during this call.
                glfwPollEvents();

                // Draw the FPS if that is configured:
                if (TRACK_FPS) {
                    int fps = (int)(1000l*1000l*1000l/elapsedNanoTime);
                    pushFPS(fps);
                GLDrawHelper.drawString(0, HEIGHT, String.valueOf(averagedFPS()),10);
                }
                // Where all the game logic is handled:
                reaperFrameLoop.frame();
                
                
                glfwSwapBuffers(gameWindow); // swap the color buffers
                
                
        }
        
        
        
        
    }
    
    private void pushFPS(int fps) {
        for (int i=0; i < FPS_DISPLAY_MVA - 1; i++) {
            pastFPS[i] = pastFPS[i+1];
        }
        pastFPS[FPS_DISPLAY_MVA-1]=fps;
    }
    
    private int averagedFPS() {
        int sum = 0;
        for (int i=0; i < FPS_DISPLAY_MVA; i++) {
            sum += pastFPS[i];
        }
        return Math.round((float)sum/FPS_DISPLAY_MVA);
    }
    
}
