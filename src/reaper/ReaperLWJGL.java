/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper;

import org.lwjgl.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;


/**
 *
 * @author murdock
 */
public class ReaperLWJGL { 
    
    //The window handle
    private long gameWindow;
    private final long minFrameWaitNanoTime = Math.round(1000*1000*1000/120);
    
    private final int WIDTH = 1200;
    private final int HEIGHT = 900;
    
    private ReaperFrameLooper reaperFrameLoop;
    
    public void run() throws InterruptedException {
        System.out.println("LWJGL Version: " + Version.getVersion());
        
        try {
            init();
            loop();
            
            //Free the window callbacks and destroy the window
            glfwFreeCallbacks(gameWindow);
            glfwDestroyWindow(gameWindow);
        } catch (ReaperFrameLooper.NoControllerException e) {
            return;
        } finally {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }
    
    private void init() {
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
        gameWindow = glfwCreateWindow(WIDTH,HEIGHT, "Reaper", NULL, NULL);
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
                (vidmode.width() - WIDTH) / 2 + xbuff[0],
                (vidmode.height() - HEIGHT) / 2 + ybuff[0]
        );
        
        // Make the OpenGL context current
        glfwMakeContextCurrent(gameWindow);
        // Enable v-sync
        glfwSwapInterval(1);
        
        // Finally, make our configured window visible to the user
        glfwShowWindow(gameWindow);
        
        reaperFrameLoop = new ReaperFrameLooper();
        
    }
    
    public void loop() throws InterruptedException, ReaperFrameLooper.NoControllerException {
        
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
            
                // Putting on a frame cap (of ~120fps when this comment was made)
                while( System.nanoTime() - currentTime < minFrameWaitNanoTime) {
                    Thread.sleep(10);
                }
                
            
                currentTime = System.nanoTime();
            
                glClear(GL_COLOR_BUFFER_BIT); // clear the framebuffer

                // Poll for window events. The key callback above will only be
                // invoked during this call.
                glfwPollEvents();

                
                // Where all the game logic is handled:
                reaperFrameLoop.frame();
                
                glfwSwapBuffers(gameWindow); // swap the color buffers
                
                
        }
        
        
        
        
    }
    
}
