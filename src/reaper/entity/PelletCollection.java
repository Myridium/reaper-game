/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper.entity;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import processing.core.PApplet;
import processing.core.PVector;

/**
 *
 * @author murdock
 */
public final class PelletCollection implements IEntity<PelletCollection> {
    
    private List<Pellet> pellets;
    private static final int LAYER = 20;
    
    private PVector spawnDiskCenter;
    private float spawnRadius;
    private float spawnVelMaxMag;
    private float spawnVelMinMag;
    //Setting this to off will also pause the autspawn timer.
    private boolean autoSpawn;
    private long minSpawnTime;
    private long maxSpawnTime;
    private long nextPelletNanoTime;
    
    
    public PelletCollection() {
        pellets = new ArrayList<>();
        spawnDiskCenter = new PVector(0,0);
        spawnRadius = 0;
        spawnVelMaxMag = 0;
        this.setSpawnFreq(1, 10);
        autoSpawn = false;
    }
    
    
    @Override
    public int getLayer() {
        return LAYER;
    }
    @Override
    public void draw(PApplet pApplet) {
        
        PelletCollection.sortPelletsByType(pellets);
        
        Pellet.Type pTypeBeingDrawn = null;
        for (Pellet p : pellets) {
            if (pTypeBeingDrawn != (pTypeBeingDrawn = p.type)) {
                //Update the way we need to draw this new type of pellet.
                p.prepareDraw(pApplet);
            }
            p.draw(pApplet);
        }
        
    }
    @Override
    public void prepareDraw(PApplet pApplet) {
        /*No preparation needs to be done! */
    }
    @Override
    public PelletCollection deepClone() {
        PelletCollection pc = new PelletCollection();
        for (Pellet p : pellets) {
            pc.addPellet(p.deepClone());
        }
        pc.setSpawnCenter(spawnDiskCenter.x, spawnDiskCenter.y);
        pc.setSpawnRadius(spawnRadius);
        pc.setSpawnVelMag(spawnVelMinMag, spawnVelMaxMag);
        pc.setAutoSpawn(autoSpawn);
        
        //The setter method for these two variables takes frequencies, not period
        pc.minSpawnTime = this.minSpawnTime;
        pc.maxSpawnTime = this.maxSpawnTime;
        
        pc.nextPelletNanoTime = this.nextPelletNanoTime;
        return pc;
    }
    @Override
    public void evolve(long nanoTimestep) {
        for (Pellet p : pellets) {
            p.evolve(nanoTimestep);
        }
        prunePellets();
        if (autoSpawn) {
            nextPelletNanoTime -= nanoTimestep;
            if (nextPelletNanoTime <= 0) {
                spawnRandomPellet();
                queueRandomPellet();
            }
        }
    }
    
    public void setSpawnCenter(float x, float y) {
        spawnDiskCenter = new PVector(x,y);
    }
    public void setSpawnRadius(float rad) {
        spawnRadius = rad;
    }
    public void setAutoSpawn(boolean b) {
        autoSpawn = b;
        if (autoSpawn) {
            queueRandomPellet();
        }
    }
    public void setSpawnVelMag(float magMin, float magMax) {
        spawnVelMinMag = magMin;
        spawnVelMaxMag = magMax;
    }
    
    public void setSpawnFreq(float minFreq, float maxFreq) {
        minSpawnTime = Math.round((1000l*1000l*1000l)/maxFreq);
        maxSpawnTime = Math.round((1000l*1000l*1000l)/minFreq);
    }
    
    private void queueRandomPellet() {
        nextPelletNanoTime = Math.round(Math.random()*(maxSpawnTime - minSpawnTime)) + minSpawnTime;
    }
    private void spawnRandomPellet() {
        float angle = (float)(Math.random()*2*Math.PI);
        float xSpawn = spawnDiskCenter.x + (spawnRadius*(float)Math.cos(angle));
        float ySpawn = spawnDiskCenter.y + (spawnRadius*(float)Math.sin(angle));
        
        //The velocity of this new pellet should point in toward the circle.
        //The angle of its velocity should have at least a pi/2 separation from the spawn angle
        float vangle = (float)((angle + Math.PI/2) + (Math.random()*Math.PI));
        float vmag = (float)Math.random()*(spawnVelMaxMag-spawnVelMinMag) + spawnVelMinMag;
        float vxSpawn = vmag * (float)Math.cos(vangle);
        float vySpawn = vmag * (float)Math.sin(vangle);
        
        PVector xySpawn = new PVector(xSpawn,ySpawn);
        PVector vvSpawn = new PVector(vxSpawn,vySpawn);
        Pellet.Type type = Pellet.Type.NORMAL;
        
        Pellet p = new Pellet(type,xySpawn,vvSpawn);
        this.addPellet(p);
    }
    
    private void removePellet(Pellet p) {
        pellets.remove(p);
    }
    private void removePellets(Collection<Pellet> cp) {
        pellets.removeAll(cp);
    }
    private void prunePellets() {
        
        List<Pellet> removeList = new ArrayList<>();
        for (Pellet p : pellets) {
            if(prunePredicate(p))
                removeList.add(p);
        }
        removePellets(removeList);
        
        //NO! This modifies the iterator while it is being used. Results in a ConcurrentModificationException
        /*
        for (Pellet p : pellets) {
            if(prunePredicate(p))
                removePellet(p);
        }
        */
    }
    
    //This is a bit simplistic. If pellets have left a bounding circle.
    //This doesn't stop many pellets from reentering the screen after they leave.
    private boolean prunePredicate(Pellet p) {
        if (p.isDead())
            return true;
        
        float pX = p.pos.x - spawnDiskCenter.x;
        float pY = p.pos.y - spawnDiskCenter.y;
        float dist2 = (pX*pX) + (pY*pY);
        return (dist2 > spawnRadius*spawnRadius*2);
    }
    
    public static class Pellet implements IEntity<Pellet> {
        
        PVector pos;
        PVector vel; //Velocity in pixels per second.
        Type type;
        float health; 

        @Override
        public int getLayer() {
            return LAYER;
        }
        
        @Override
        public void evolve(long nanoTimestep) {
            double secondsPassed = nanoTimestep / (double)(1000*1000*1000);
            
            pos.x += secondsPassed * vel.x;
            pos.y += secondsPassed * vel.y;
            
        }

        @Override
        public Pellet deepClone() {
            Pellet p = new Pellet(type, pos, vel);
            p.health = this.health;
            return p;
        }

        public enum Type {
            NORMAL,
            HEALTH,
            FOCUS,
            SUPER;
            
            /*
            public Type deepClone() {
                for (Type t : Type.values()) {
                    if (t.ordinal() == this.ordinal())
                        return t;
                }
                return null;
            }
            */
        }
        
        public static int colorOf(Type t) {
            Color c;
            switch (t) {
                case NORMAL:
                    c = Color.WHITE;
                    break;
                case HEALTH:
                    c = Color.ORANGE;
                    break;
                case FOCUS:
                    c = Color.BLUE;
                    break;
                case SUPER:
                    c = new Color(0xFFD86DB1,true);
                    break;
                default:
                    c = Color.RED;
            }
            
            /*Color.getRGB() actually returns ARGB*/
            /*Doing this requires overwriting what is normally the leading bit indicating the sign of the integer*/
            return c.getRGB();
            
        }
        public static float strokeWeightOf(Type t) {
            return 1.5f;
        }
        public static float defaultRadiusOf(Type t) {
            return 5.0f;
        }
        public static float defaultHealthOf(Type t) {
            switch (t) {
                case NORMAL:
                    return 10f;
                case HEALTH:
                    return 30f;
                case FOCUS:
                    return 20f;
                case SUPER:
                    return 50f;
                default:
                    return 1.f;
            }
        }
        
        /*This is made protected because the 'draw' method should not be called without first preparing to draw it (i.e. by changing stroke type and whatnot)*/
        @Override
        public void draw(PApplet pApplet) {
            float healthPerc = health / defaultHealthOf(type);
            float drawRad = Math.max(Pellet.defaultRadiusOf(type)*healthPerc, 1);
            pApplet.ellipse(pos.x, pos.y, drawRad, drawRad);
        }
        @Override
        public void prepareDraw(PApplet pApplet) {
            pApplet.noFill();
            pApplet.stroke(Pellet.colorOf(type));
            pApplet.colorMode(PApplet.RGB);
            pApplet.strokeWeight(Pellet.strokeWeightOf(type));
            pApplet.ellipseMode(PApplet.RADIUS);
        }
        
        protected Pellet(Type t, PVector posIn, PVector velIn) {
            type = t;
            pos = new PVector(posIn.x,posIn.y);
            vel = new PVector(velIn.x,velIn.y);    
            health = defaultHealthOf(t);
        }
        
        public boolean isDead() {
            return health <= 0;
        }
    }
    
    public void addPellet(Pellet.Type type, PVector pos, PVector vel) {
        pellets.add(new Pellet(type,pos,vel));
    }
    private void addPellet(Pellet p) {
        pellets.add(p);
    }
    
    public List<Pellet> getPellets() {
        return pellets;
    }
    
    /**
     * Sorts a list of pellets according to their type.
     * @param pList 
     */
    private static void sortPelletsByType(List<Pellet> pList) {
        if (pList.size() > 0) {
            Collections.sort(pList, 
            
                    new Comparator<Pellet>() {
                        @Override
                        public int compare(final Pellet p1, final Pellet p2) {
                            return p1.type.compareTo(p2.type);
                        }
                    }
                    
            );
        }
    }
}
