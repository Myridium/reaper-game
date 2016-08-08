/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper;

import LWJGLTools.GLDrawing.GLDrawHelper;
import reaper.entity.Player;
import reaper.entity.World;
import LWJGLTools.input.ControllerReader;
import LWJGLTools.input.ControllerReader.*;
import java.awt.Color;
import java.io.File;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author murdock
 */
public class ReaperFrameLooper {
    
    final long dt = 1000*1000*1000 / 300;
    final long MAX_FRAME_TIME = (1000*1000*1000) / 10;
    ControllerReader cr;
    
    long currentTime;
    long accumulator;
    
    World world;
    Player player;
    
    boolean lastAButtonState;
    boolean paused;
    
    
    public void init(int width, int height) {
     
        world = new World(width,height);
        player = world.getPlayer();
        
        currentTime = System.nanoTime();
        accumulator = 0;
        
        // Controller reader configuration:
        cr = new ControllerReader();
        String dir = "";
        try {
            dir = new File(ReaperFrameLooper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
            dir = dir + "/controllerConfig.xml";
        } catch (URISyntaxException ex) {
            Logger.getLogger(ReaperFrameLooper.class.getName()).log(Level.SEVERE, null, ex);
            assert(false);
        }
        
        if (new File(dir).exists()) {
            System.out.println("Controller configuration XML found. Will read from this file instead of loading defaults.");
            cr.readConfig(dir);
        } else {
            System.out.println("No controller configuration XML found. Will load defaults.");
            Axis xAxis, yAxis, trigAxis;

            xAxis = new Axis(ControllerID.ONE, AxisID.ZERO,-0.7f,0.7f);
            yAxis = new Axis(ControllerID.ONE, AxisID.ONE,0.7f,-0.7f);
            cr.setJoystickAxes(Joystick.LEFT, xAxis, yAxis);
            cr.setJoystickDeadzone(Joystick.LEFT, 0.4f);

            xAxis = new Axis(ControllerID.ONE, AxisID.THREE,-0.7f,0.7f);
            yAxis = new Axis(ControllerID.ONE, AxisID.FOUR,0.7f,-0.7f);
            cr.setJoystickAxes(Joystick.RIGHT, xAxis, yAxis);
            cr.setJoystickDeadzone(Joystick.RIGHT, 0.4f);

            trigAxis = new Axis(ControllerID.ONE, AxisID.TWO, -0.7f, 0.7f);
            cr.setTriggerAxis(Trigger.LEFT, trigAxis);

            trigAxis = new Axis(ControllerID.ONE, AxisID.FIVE, -0.7f, 0.7f);
            cr.setTriggerAxis(Trigger.RIGHT, trigAxis);
            
            cr.setButton(Button.A, new ButtonContainer(ControllerID.ONE, ButtonID.ZERO));
        }
        
        lastAButtonState = false;
        paused = false;
        
    }
    
    public void frame() throws ControllerReader.NoControllerException, ControllerReader.NoSuchAxisException, ControllerReader.NotConfiguredException, NoSuchButtonException {
        long elapsedTime = 0 - (currentTime) + (currentTime = System.nanoTime());
        /*In case the simulation can't keep up with the frame advancement, we should put an upper limit on how much the physics will advance per frame*/
        elapsedTime = Math.min(elapsedTime, MAX_FRAME_TIME);
        
        if (!lastAButtonState) {
            lastAButtonState = updateAButtonState();
            if (lastAButtonState) {
                paused = !paused;
            }
        } else {
            lastAButtonState = updateAButtonState();
        }
        
        if (!paused) {
            accumulator += elapsedTime;

            ///////////////////////*Handle player input here*///////////////////////
            JoystickFilteredState js;
            js = cr.getJoystickState(ControllerReader.Joystick.LEFT);

            float mag = js.getMag();
            float angle = js.getAngle();

            mag*=800;
            player.setVelocity(mag*(float)Math.cos(angle),mag*(float)Math.sin(angle));


            js = cr.getJoystickState(ControllerReader.Joystick.RIGHT);
            mag = js.getMag();
            angle = js.getAngle();
            player.setFociiRelativeDistance(mag);
            player.setCaptureAngle(angle);

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
        } else {
            GLDrawHelper.setColor(Color.CYAN);
            GLDrawHelper.drawString(world.getWidth()/2, world.getHeight()/2+20, "PAUSED", 10, GLDrawHelper.TextAlignment.MIDDLE_TOP);
        }
        
        
    }
    
    private boolean updateAButtonState() throws NoControllerException, NoSuchButtonException {
        boolean b;
        try {
            b = cr.isButtonPressed(Button.A);
        } catch (NotConfiguredException e) {
            System.err.println("Something went wrong. The A button is not configured, though I expected it to be.");
            b = false;
            assert(false);
        }
        
        return b;
    }
    
}
