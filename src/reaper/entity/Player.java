/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper.entity;

import java.awt.Color;
import reaper.CVector;
import reaper.GLDrawHelper;

/**
 *
 * @author murdock
 */
public class Player implements IEntity<Player> {

    private static final int LAYER = 10;
    
    private CVector pos;
    private CVector vel;
    
    private float capturePower;
    
    //These first two determine the maximum focii separation
    private float collideRadius;
    private float captureEffectiveRadius;
    private float fociiSeparationRelative;
    private float captureAngle;
    
    //These variables are calculated from the others.
    private float maxFociiSeparation;
    private float captureFociiSeparation;
    private float captureMajorRadius;
    private float captureMinorRadius;
    private float captureUpperBoundSq;
    
    protected Player(float x, float y) {
        pos = new CVector(x,y);
        vel = new CVector(0,0);
        collideRadius = 30;
        captureEffectiveRadius = 150;
        fociiSeparationRelative = 0;        
        captureAngle=(float)Math.PI/8f;
        refreshMaxFociiSeparation();
        refreshFociiDistance();
        refreshRadii();
        
        capturePower = 5f;
    }
    
    public void setEffectiveCaptureRadius(float rad) {
        captureEffectiveRadius = rad;
        refreshMaxFociiSeparation();
        refreshFociiDistance();
        refreshRadii();
    }
    public void setCollideRadius(float rad) {
        collideRadius = rad;
        refreshMaxFociiSeparation();
        refreshFociiDistance();
        refreshRadii();
    }
    public void setFociiRelativeDistance(float rel) {
        fociiSeparationRelative = rel;
        refreshFociiDistance();
        refreshRadii();
    }
    public void setCaptureAngle(float angle) {
        captureAngle = angle;
    }
    
    public void setVelocity(float xvel, float yvel) {
        vel.x = xvel;
        vel.y = yvel;
    }
    
    private void refreshFociiDistance() {
        captureFociiSeparation = maxFociiSeparation*fociiSeparationRelative;
    }
    private void refreshMaxFociiSeparation() {
        double r = captureEffectiveRadius;
        double R = collideRadius;
        double r4 = Math.pow(r, 4);
        double R2 = Math.pow(R, 2);
        double cache = (54 * r4 * R2) + (R2*R2*R2)+ 
                (
                6*Math.sqrt(3d)*Math.sqrt(r4*R2*R2*(27d*r4 + (R2*R2)))
                )
                ;
        cache = Math.pow(cache, 1d/3d);
        maxFociiSeparation = 
                (float)
                (
                ((-5d*R) + (R*R*R/cache) + (cache/R))/3d
                );
        
    }
    private void refreshRadii() {
        captureMajorRadius = (float)Math.sqrt(
                (Math.pow(captureFociiSeparation/2d, 2)+Math.sqrt(Math.pow(captureFociiSeparation/2d, 4) + (4*Math.pow(captureEffectiveRadius, 4))))/2d
        );
        captureMinorRadius = (float)Math.sqrt(
                (0d-Math.pow(captureFociiSeparation/2d, 2)+Math.sqrt(Math.pow(captureFociiSeparation/2d, 4) + (4*Math.pow(captureEffectiveRadius, 4))))/2d
        );
        captureUpperBoundSq = (float)Math.pow(2*captureMajorRadius-collideRadius, 2);
    }
    
    public float getCapturePower() {
        return capturePower;
    }
    public float getEffectiveRadius() {
        return captureEffectiveRadius;
    }
    public float getFociiSeparation() {
        return captureFociiSeparation;
    }
    public float getCaptureMajorRadius() {
        return captureMajorRadius;
    }
    public float getCaptureMinorRadius() {    
        return captureMinorRadius;
    }
    public CVector getCaptureCenter() {
        //The collision circle is centered on one of the focii.
        CVector pv;
        
        float cx = pos.x, cy = pos.y, fsOn2 = getFociiSeparation()/2;
        cx += fsOn2*Math.cos(captureAngle);
        cy += fsOn2*Math.sin(captureAngle);
        
        pv = new CVector(cx,cy);
        return pv;
    }
    
    @Override
    public int getLayer() {
        return LAYER;
    }
    @Override
    public void prepareDraw() {
        GLDrawHelper.setColor(1, 0, 0);
        GLDrawHelper.setStrokeWidth(1);
    }
    @Override
    public void draw() {
        
        GLDrawHelper.circle(pos.x, pos.y, collideRadius);
        GLDrawHelper.setColor(Color.GRAY);
        CVector pv = getCaptureCenter();
        float cx = pv.x, cy = pv.y;
        GLDrawHelper.ellipse(cx, cy, getCaptureMinorRadius(), getCaptureMajorRadius(), captureAngle);
        
    }
    @Override
    public void evolve(long nanoTimestep) {
        float secondsElapsed = nanoTimestep/(1000f*1000f*1000f);
        pos.x += vel.x * secondsElapsed;
        pos.y += vel.y * secondsElapsed;
    }
    @Override
    public Player deepClone() {
        Player p = new Player(pos.x,pos.y);
        p.setVelocity(vel.x, vel.y);
        p.setCaptureAngle(captureAngle);
        p.setCollideRadius(collideRadius);
        p.setEffectiveCaptureRadius(captureEffectiveRadius);
        p.setFociiRelativeDistance(fociiSeparationRelative);
        p.captureUpperBoundSq = this.captureUpperBoundSq;
        p.capturePower = this.capturePower;
        
        return p;
    }
    
    public boolean inCaptureRange(float x, float y) {
        
        //Quickly answer no if it's obvious
        float dist2 = (float)(Math.pow(x - pos.x, 2) + Math.pow(y - pos.y,2));
        if (dist2 > captureUpperBoundSq)
            return false;
        
        CVector captureCenter = getCaptureCenter();
        float cache,relX, relY;
        cache = x - captureCenter.x;
        relY = (y - captureCenter.y);
        //Rotate relX and relY so their coordinates align with those of the capture ellipse.
        relX = (float)((Math.cos(captureAngle)*cache) + (Math.sin(captureAngle)*relY));
        relY = (float)((-Math.sin(captureAngle)*cache) + (Math.cos(captureAngle)*relY));
        relY = relY;
        return (Math.pow(relY/getCaptureMinorRadius(),2) + Math.pow(relX/getCaptureMajorRadius(),2) <= 1);
    }
    
    public boolean isColliding(float x, float y) {
        float relX = x - pos.x;
        float relY = y - pos.y;
        return ((relX*relX) + (relY*relY)) <= (collideRadius*collideRadius);
    }

    @Override
    public float getBoundingRadius() {
        // dunno lel
        return -1;
    }

    @Override
    public float getX() {
        return pos.x;
    }

    @Override
    public float getY() {
        return pos.y;
    }
}
