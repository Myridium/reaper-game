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
    private static final float minCollideRadius = 15f;
    private static final float maxCollideRadius = 50f;
    
    private CVector pos;
    private CVector vel;
    
    private float capturePower;
    private float captureBoost;
    private float health;
    private float maxHealth;
    private float focus;
    private float maxFocus;
    
    //These first two determine the maximum focii separation
    private float collideRadius;
    private float captureEffectiveRadius;
    private float fociiSeparationRelative;
    private float captureAngle;
    
    private float cachedMajorRadius;
    private float cachedMinorRadius;
    private float cachedFociiSeparation;
    private float cachedMaxFociiSeparation;
    
    
    protected Player(float x, float y) {
        pos = new CVector(x,y);
        vel = new CVector(0,0);
        collideRadius = minCollideRadius;
        captureEffectiveRadius = 150;
        fociiSeparationRelative = 0;        
        captureAngle=(float)Math.PI/8f;
        maxHealth = 100;
        health = 100;
        focus = 0;
        maxFocus = 1000;
        
        refreshCachedVariables();
        
        capturePower = 6f;
    }
    
    private void refreshCachedVariables() {
        
        // The order here is important! Each relies on the last.
        cachedMaxFociiSeparation = calculateMaxFociiSeparation();
        cachedFociiSeparation = calculateFociiDistance();
        cachedMajorRadius = calculateCaptureMajorRadius();
        cachedMinorRadius = calculateCaptureMinorRadius();
        
    }
    public void setEffectiveCaptureRadius(float rad) {
        captureEffectiveRadius = rad;
    }
    public void setCollideRadius(float rad) {
        collideRadius = rad;
    }
    public float getCollideRadius() {
        return collideRadius;
    }
    public void setFociiRelativeDistance(float rel) {
        fociiSeparationRelative = rel;
    }
    public void setCaptureAngle(float angle) {
        captureAngle = angle;
    }
    public void setCaptureBoost(float boost) {
        captureBoost = boost;
    }
    
    public void setVelocity(float xvel, float yvel) {
        vel.x = xvel;
        vel.y = yvel;
    }
    public void shrink(float amount) {
        setCollideRadius(
                Math.min(
                        Math.max(minCollideRadius, collideRadius-(amount/collideRadius*maxCollideRadius))
                ,
                captureEffectiveRadius)
        );
    }
    public void grow(float amount) {
        shrink(-amount);
    }
    public void heal(float amount) {
        // Health is from 0 to 100
        health = Math.max(
                0,
                Math.min(health + amount, maxHealth)
        );
    }
    public void damage(float amount) {
        heal(-amount);
    }
    public void addFocus(float amount) {
        focus = Math.max(0, 
                Math.min(amount + focus, maxFocus)
        );
    }
    
    private float boostMultiplier() {
        return (float)(1 - Math.exp(-5f*focus/maxFocus));
    }
    public float getCaptureEffectiveRadius() {
        // The capture boost only takes effect if the player has remaining focus.
        // For aesthetic appeal, a low Focus will diminish the boost amount.
        // I guess this also rewards saving up Focus and using it when the player
        // has a lot.
        float cb = captureBoost;
        cb *= boostMultiplier();
        return captureEffectiveRadius + cb;
    }
    public float getFociiDistance() {
        return cachedFociiSeparation;
    }
    public float calculateFociiDistance() {
        return getMaxFociiSeparation()*fociiSeparationRelative;
    }
    public float getMaxFociiSeparation() {
        return cachedMaxFociiSeparation;
    }
    public float calculateMaxFociiSeparation() {
        double r = getCaptureEffectiveRadius();
        double R = collideRadius;
        double r4 = Math.pow(r, 4);
        double R2 = Math.pow(R, 2);
        double cache = (54 * r4 * R2) + (R2*R2*R2)+ 
                (
                6*Math.sqrt(3d)*Math.sqrt(r4*R2*R2*(27d*r4 + (R2*R2)))
                )
                ;
        cache = Math.pow(cache, 1d/3d);
        float mfs = 
                (float)
                (
                ((-5d*R) + (R*R*R/cache) + (cache/R))/3d
                );
        return mfs;
    }
    public float getCaptureMajorRadius() {
        return cachedMajorRadius;
    }
    public float calculateCaptureMajorRadius() {
        return (float)Math.sqrt(
                (Math.pow(getFociiDistance()/2d, 2)+Math.sqrt(Math.pow(getFociiDistance()/2d, 4) + (4*Math.pow(getCaptureEffectiveRadius(), 4))))/2d
        );
    }
    public float getCaptureMinorRadius() {
        return cachedMinorRadius;
    }
    public float calculateCaptureMinorRadius() {
        return (float)Math.sqrt(
                (0d-Math.pow(getFociiDistance()/2d, 2)+Math.sqrt(Math.pow(getFociiDistance()/2d, 4) + (4*Math.pow(getCaptureEffectiveRadius(), 4))))/2d
        );
    }
    //This method should be QUICK. It will just give a rough upper bound.
    public float getCaptureUpperBoundSq() {
        return (float)Math.pow(2f*getCaptureMajorRadius() - getCollideRadius(),2);
    }
    
    public float getCapturePower() {
        return capturePower;
    }
    public float getEffectiveRadius() {
        return captureEffectiveRadius;
    }
    public CVector getCaptureCenter() {
        //The collision circle is centered on one of the focii.
        CVector pv;
        
        float cx = pos.x, cy = pos.y, fsOn2 = getFociiDistance()/2;
        cx += fsOn2*Math.cos(captureAngle);
        cy += fsOn2*Math.sin(captureAngle);
        
        pv = new CVector(cx,cy);
        return pv;
    }
    
    // From implementing IDrawable
    @Override
    public int getLayer() {
        return LAYER;
    }
    @Override
    public void prepareDraw() {
        // Nothing yet
    }
    @Override
    public void draw() {
        
        GLDrawHelper.setColor(1, 0.5f, 0);
        
        if (true) {
            float angle = 2f * (float)Math.PI * (health / maxHealth);
            GLDrawHelper.diskSector(pos.x, pos.y, collideRadius+1,(float)Math.PI/2,-angle);
        }
        
        GLDrawHelper.setStrokeWidth(3);
        GLDrawHelper.circle(pos.x, pos.y, collideRadius);
        
        GLDrawHelper.setStrokeWidth(1);
        GLDrawHelper.setColor(Color.GRAY);
        CVector pv = getCaptureCenter();
        
        float cx = pv.x, cy = pv.y;
        GLDrawHelper.ellipse(cx, cy, getCaptureMinorRadius(), getCaptureMajorRadius(), captureAngle);
        
        if (focus > 0) {
            float angle = 2f * (float)Math.PI * (focus / maxFocus);
            GLDrawHelper.setColor(0.4f, 0.4f, 1f,1f);
            GLDrawHelper.setStrokeWidth(5);
            GLDrawHelper.circleSector(pos.x, pos.y, collideRadius, (float)Math.PI/2, -angle);
        }
    }
    
    // From implementing IEvolvable
    @Override
    public void evolve(long nanoTimestep) {
        float secondsElapsed = nanoTimestep/(1000f*1000f*1000f);
        pos.x += vel.x * secondsElapsed;
        pos.y += vel.y * secondsElapsed;
        addFocus(-secondsElapsed*captureBoost);
        
        refreshCachedVariables();
    }
    @Override
    public Player deepClone() {
        Player p = new Player(pos.x,pos.y);
        p.setVelocity(vel.x, vel.y);
        p.setCaptureAngle(captureAngle);
        p.setCollideRadius(collideRadius);
        p.setEffectiveCaptureRadius(captureEffectiveRadius);
        p.setFociiRelativeDistance(fociiSeparationRelative);
        p.capturePower = this.capturePower;
        p.health = this.health;
        p.maxHealth = this.maxHealth;
        p.focus = this.focus;
        p.maxFocus = this.maxFocus;
        p.captureBoost = this.captureBoost;
        p.cachedFociiSeparation = this.cachedFociiSeparation;
        p.cachedMajorRadius = this.cachedMajorRadius;
        p.cachedMinorRadius = this.cachedMinorRadius;
        p.cachedMaxFociiSeparation = this.cachedMaxFociiSeparation;
        
        return p;
    }

    // From implementing IEntity
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
    
    public boolean inCaptureRange(float x, float y) {
        
        //Quickly answer no if it's obvious
        float dist2 = (float)(Math.pow(x - pos.x, 2) + Math.pow(y - pos.y,2));
        if (dist2 > getCaptureUpperBoundSq())
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
    public float distanceFrom(float x, float y) {
        float dist = CVector.dist(pos, new CVector(x,y));
        dist = Math.max(0, dist-collideRadius);
        return dist;
    }
}
