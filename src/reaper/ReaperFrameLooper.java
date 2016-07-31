/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper;

import reaper.entity.Player;
import reaper.entity.World;
import reaper.input.ControllerReader;
import reaper.input.ControllerReader.JoystickFilteredState;

/**
 *
 * @author murdock
 */
public class ReaperFrameLooper {
    
    final long dt = 1000*1000*1000 / 1000;
    final long MAX_FRAME_TIME = (1000*1000*1000) / 10;
    ControllerReader cr;
    
    long currentTime;
    long accumulator;
    
    World world;
    Player player;
    
    int framesToNextPellet;
    
    
    
    public void init(int width, int height) {
     
        world = new World(width,height);
        player = world.getPlayer();
        
        currentTime = System.nanoTime();
        accumulator = 0;
        
        cr = new ControllerReader(ControllerReader.ControllerID.ONE);
        
    }
    
    public void frame() throws ControllerReader.NoControllerException, ControllerReader.NoSuchAxisException {
        long elapsedTime = 0 - (currentTime) + (currentTime = System.nanoTime());
        /*In case the simulation can't keep up with the frame advancement, we should put an upper limit on how much the physics will advance per frame*/
        elapsedTime = Math.min(elapsedTime, MAX_FRAME_TIME);
        accumulator += elapsedTime;
        
        ///////////////////////*Handle player input here*///////////////////////
        JoystickFilteredState js;
        js = cr.getJoystickState(ControllerReader.Joystick.LEFT);
        
        float mag = js.getMag();
        float angle = js.getAngle();
        
        mag*=800;
        player.setVelocity(mag*(float)Math.cos(angle),mag*(float)Math.sin(-angle));
        
        
        js = cr.getJoystickState(ControllerReader.Joystick.RIGHT);
        mag = js.getMag();
        angle = js.getAngle();
        player.setFociiRelativeDistance(mag);
        player.setCaptureAngle(-angle);
        
        float value = cr.getTriggerState(ControllerReader.Trigger.LEFT).getValue();
        player.setCaptureBoost(100f*value);
        
        
        
        
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
