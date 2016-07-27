/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper;

import java.nio.FloatBuffer;
import static org.lwjgl.glfw.GLFW.*;
import reaper.entity.Player;
import reaper.entity.World;

/**
 *
 * @author murdock
 */
public class ReaperFrameLooper {
    
    final long dt = 1000*1000*1000 / 1000;
    final long MAX_FRAME_TIME = (1000*1000*1000) / 10;
    
    long currentTime;
    long accumulator;
    
    World world;
    Player player;
    
    int framesToNextPellet;
    
    public void init() {
     
        world = new World();
        player = world.getPlayer();
        
        currentTime = System.nanoTime();
        accumulator = 0;
        
    }
    
    public void frame() {
        long elapsedTime = 0 - (currentTime) + (currentTime = System.nanoTime());
        /*In case the simulation can't keep up with the frame advancement, we should put an upper limit on how much the physics will advance per frame*/
        elapsedTime = Math.min(elapsedTime, MAX_FRAME_TIME);
        accumulator += elapsedTime;
        
        /*Handle player input here*/
        
        //Through testing I have determined that on my XBOX 360 controller:
        //Axis 0 is the left stick x axis. Left = -1, Right  = 1
        //Axis 1 is the left stick y axis. Top  = -1, Bottom = 1
        //Axis 2 is the left trigger.      Untouched = -1, Fully pressed = 1
        //Axis 3 is the right stick x axis. Left = -1, Right  = 1
        //Axis 4 is the right stick y axis. Top = -1, Bottom = 1
        //Axis 5 is the right trigger.     Untouched = -1, Fully pressed = 1
        //Axis 6 - unused
        //Axis 7 - unused
        
        //glfwPollEvents();
        FloatBuffer fb = glfwGetJoystickAxes(GLFW_JOYSTICK_1);
        
        //Need a deadzone, etc
        float xstick, ystick, mag, angle;
        xstick = fb.get(0);
        ystick = -fb.get(1);
        
        mag = (float)Math.sqrt((xstick*xstick) + (ystick*ystick));
        mag = (float)Math.min(Math.max(mag-0.3, 0),0.6);
        angle = (float)Math.atan2(ystick, xstick);
        mag*=1000;
        player.setVelocity(mag*(float)Math.cos(angle),mag*(float)Math.sin(angle));
        
        xstick = fb.get(3);
        ystick = fb.get(4);
        mag = (float)Math.sqrt((xstick*xstick) + (ystick*ystick));
        mag = (float)Math.min(Math.max(mag-0.3, 0)*1.6,1);
        angle = (float)Math.atan2(-ystick, xstick);
        player.setFociiRelativeDistance(mag);
        player.setCaptureAngle(angle);
        
        
        
        
        
        ///////////////////////////
        
        while (accumulator >= dt) {
            /*Evolve everything by one step*/
            
            world.evolve(dt);
            
            accumulator -= dt;
        }
        //With the time left in the accumulator, spoof a state to be drawn
        World w = world.spoofEvolve(accumulator);
        
        
        w.draw();
    }
    
}
