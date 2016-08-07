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
import reaper.CVector;
import LWJGLTools.GLDrawing.GLDrawHelper;

/**
 *
 * @author murdock
 */
public final class PelletCollection implements IEntity<PelletCollection> {
    
    private List<Pellet> pellets;
    private static final int LAYER = 20;
    
    private CVector spawnDiskCenter;
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
        spawnDiskCenter = new CVector(0,0);
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
    public void draw() {
        
        PelletCollection.sortPelletsByType(pellets);
        
        Pellet.Type pTypeBeingDrawn = null;
        for (Pellet p : pellets) {
            if (pTypeBeingDrawn != (pTypeBeingDrawn = p.type)) {
                //Update the way we need to draw this new type of pellet.
                p.prepareDraw();
            }
            p.draw();
        }
        
    }
    @Override
    public void prepareDraw() {
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
    @Override
    public void drawIfInRectangleBound(float x0, float y0, float width, float height) {
        
        PelletCollection.sortPelletsByType(pellets);
        
        Pellet.Type pTypeBeingDrawn = null;
        for (Pellet p : pellets) {
            if (pTypeBeingDrawn != (pTypeBeingDrawn = p.type)) {
                //Update the way we need to draw this new type of pellet.
                p.prepareDraw();
            }
            p.drawIfInRectangleBound(x0, y0, width, height);
        }
    }
    public boolean isPelletEndangered(Pellet p, Player player) {
        return player.inCaptureRange(p.getX(), p.getY());
    }
    public List<Pellet> getEndangeredPellets(Player player) {
        // Collects pellets in "pellets" matching the criterion.
        // In this case, the criterion is whether or not they are in the player's capture range.
        List<Pellet> pelletsInCaptureRange = new ArrayList<>();
        List<Pellet> pL = pellets;
        int pLength = pL.size();
        Pellet[] pA = new Pellet[pLength];
        pL.toArray(pA);
        
        for (int i=0; i < pLength; i++) {
            Pellet p = pA[i];
            if (isPelletEndangered(p,player))
                pelletsInCaptureRange.add(p);
        }
        
        /*
        This is slow!! Despite being more elegant.
        List<Pellet> pelletsInCaptureRange = pellets.stream()
                .filter(p -> player.inCaptureRange(p.getX(), p.getY())).collect(Collectors.toList());
        */
        return pelletsInCaptureRange;
    }
    public void setSpawnCenter(float x, float y) {
        spawnDiskCenter = new CVector(x,y);
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
    private float randSpawnVel(Pellet.Type pType) {
        switch (pType) {
            case SUPER:
                return (float)Math.random()*(spawnVelMaxMag-spawnVelMinMag)/2f + spawnVelMinMag;
            default:
                return (float)Math.random()*(spawnVelMaxMag-spawnVelMinMag) + spawnVelMinMag;
        }
    }
    private void spawnRandomPellet() {
        Pellet.Type type = randPelletType();
        
        float angle = (float)(Math.random()*2*Math.PI);
        float xSpawn = spawnDiskCenter.x + (spawnRadius*(float)Math.cos(angle));
        float ySpawn = spawnDiskCenter.y + (spawnRadius*(float)Math.sin(angle));
        
        //The velocity of this new pellet should point in toward the circle.
        //The angle of its velocity should have at least a pi/2 separation from the spawn angle
        float vangle = (float)((angle + Math.PI/2) + (Math.random()*Math.PI));
        float vmag = randSpawnVel(type);
        float vxSpawn = vmag * (float)Math.cos(vangle);
        float vySpawn = vmag * (float)Math.sin(vangle);
        
        CVector xySpawn = new CVector(xSpawn,ySpawn);
        CVector vvSpawn = new CVector(vxSpawn,vySpawn);
        
        float rotAngle = 0;
        float rotVel = (float)(2f*Math.random()*Math.PI-Math.PI);
        
        
        
        Pellet p = new Pellet(type,xySpawn,vvSpawn,rotAngle,rotVel);
        this.addPellet(p);
    }
    
    private Pellet.Type randPelletType() {
        double rand = Math.random();
        if (rand < 0.04)
            return Pellet.Type.HEALTH;
        if (rand < 0.08)
            return Pellet.Type.FOCUS;
        if (rand < 0.145)
            return Pellet.Type.HOMING;
        if (rand < 0.1475)
            return Pellet.Type.SUPER;
        return Pellet.Type.NORMAL;
    }
    private void remove(Pellet p) {
        pellets.remove(p);
    }
    private void remove(Collection<Pellet> cp) {
        pellets.removeAll(cp);
    }
    private void prunePellets() {
        
        List<Pellet> removeList = new ArrayList<>();
        for (Pellet p : pellets) {
            if(prunePredicate(p))
                removeList.add(p);
        }
        remove(removeList);
        
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

    @Override
    public float getBoundingRadius() {
        return -1;
    }

    @Override
    public float getX() {
        return -1;
    }
    @Override
    public float getY() {
        return -1;
    }
    
    public static class Pellet implements IEntity<Pellet> {
        
        CVector pos;
        CVector vel; //Velocity in pixels per second.
        Type type;
        float health; 
        float rotAngle;
        float rotVel;
        float[] evolveParams;

        @Override
        public int getLayer() {
            return LAYER;
        }
        
        @Override
        public void evolve(long nanoTimestep) {
            double secondsPassed = nanoTimestep / (double)(1000*1000*1000);
            
            pos.x += secondsPassed * vel.x;
            pos.y += secondsPassed * vel.y;
            
            // The homing pellet homes!
            // This is handled by the World physics though.
            
            rotAngle += secondsPassed*rotVel;
            
        }

        @Override
        public Pellet deepClone() {
            Pellet p = new Pellet(type, pos, vel);
            p.health = this.health;
            p.rotAngle = this.rotAngle;
            p.rotVel = this.rotVel;
            
            //Clone the evolveParams array
            float[] params = new float[this.evolveParams.length];
            for (int i=0; i < params.length; i++) {
                params[i] = this.evolveParams[i];
            }
            p.evolveParams = params;
            return p;
        }

        @Override
        public float getBoundingRadius() {
            return getRadius()*1.2f;
        }

        @Override
        public float getX() {
            return pos.x;
        }

        @Override
        public float getY() {
            return pos.y;
        }

        public enum Type {
            NORMAL,
            HEALTH,
            FOCUS,
            SUPER,
            HOMING;
            
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
        
        public static Color defaultColorOf(Type t) {
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
                case HOMING:
                    c = Color.RED;
                    break;
                default:
                    c = Color.DARK_GRAY;
            }
            
            /*Color.getRGB() actually returns ARGB*/
            /*Doing this requires overwriting what is normally the leading bit indicating the sign of the integer*/
            return c;
            
        }
        public static float strokeWeightOf(Type t) {
            return 1.5f;
        }
        public static float defaultRadiusOf(Type t) {
            switch (t) {
                case NORMAL:
                    return 5f;
                case HEALTH:
                    return 8f;
                case FOCUS:
                    return 3f;
                case SUPER:
                    return 10f;
                case HOMING:
                    return 16f;
                default:
                    return 5f;
            }
        }
        public static float defaultHealthOf(Type t) {
            switch (t) {
                case NORMAL:
                    return 10f;
                case HEALTH:
                    return 20f;
                case FOCUS:
                    return 20f;
                case SUPER:
                    return 30;
                case HOMING:
                    return 5f;
                default:
                    return 1.f;
            }
        }
        public static float defaultDamagePowerOf(Type t) {
            switch (t) {
                case NORMAL:
                    return 30f;
                case HEALTH:
                    return 15f;
                case FOCUS:
                    return 50f;
                case SUPER:
                    return 75f;
                case HOMING:
                    return 100f;
                default:
                    return 0f;
            }
        }
        public static Color defaultAbsorbColor(Type t) {
            Color c = defaultColorOf(t);
            return c;
        }
        public static float[] getDefaultParamArray(Type t) {
            if (t == Type.HOMING) {
                float[] fa = new float[1];
                fa[0] = 3000f;
                return fa;
            }
            return new float[0];
        }
        
        public void setParam(int i, float value) {
            evolveParams[i] = value;
        }
        public float getParam(int i) {
            return evolveParams[i];
        }
        
        public float attackPower() {
            return defaultDamagePowerOf(type);
        }
        public float getHealthPerc() {
            return health / defaultHealthOf(type);
        }
        public float getRadius() {
            return Math.max(Pellet.defaultRadiusOf(type)*getHealthPerc(), 1);
        }
        public Color absorbColor() {
            Color c = defaultAbsorbColor(type);
            float[] cHSB;
            cHSB = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            int cRGB;
            
            float perc = getHealthPerc();
            cRGB = Color.HSBtoRGB(cHSB[0], cHSB[1]*(1-(0.5f*perc)), cHSB[2]*(1f-(0.4f*perc)));
            
            return new Color(cRGB);
        }
        public float absorbLineWidth() {
            switch(type) {
                case NORMAL:
                    return 2f;
                case HEALTH:
                case FOCUS:
                    return 3.5f;
                case SUPER:
                    return 5f;
                case HOMING:
                    return 2f;
                default:
                    return 1f;
            }
        }
        
        public void damage(float amount) {
            heal(-amount);
        }
        public void heal(float amount) {
            if (isDead()) {
                // Do nothing
                return;
            }
            health = Math.min(health + amount, defaultHealthOf(type));
        }
        
        public void addVel(CVector vect) {
            vel = vel.add(vect);
        }
        
        @Override
        public void draw() {
            
            if (type == Type.HOMING) {
                float rad = getRadius();
                GLDrawHelper.urchin(pos.x, pos.y, rad*0.75f, rad, 10, rotAngle);
                return;
            }
            if (type == Type.SUPER) {
                float rad = getRadius();
                GLDrawHelper.urchin(pos.x, pos.y, rad*0.5f, rad, 5, rotAngle);
                return;
            }
                
            GLDrawHelper.circle(pos.x, pos.y, getRadius());
            
        }
        @Override
        public void prepareDraw() {
            
            GLDrawHelper.setStrokeWidth(Pellet.strokeWeightOf(type));
            GLDrawHelper.setColor(Pellet.defaultColorOf(type));
        }
        
        protected Pellet(Type t, CVector posIn, CVector velIn) {
            this(t,posIn,velIn,0f,0f);
        }
        protected Pellet(Type t, CVector posIn, CVector velIn, float initRotationAngle, float initRotationSpeed) {
            type = t;
            pos = new CVector(posIn.x,posIn.y);
            vel = new CVector(velIn.x,velIn.y);    
            health = defaultHealthOf(t);
            rotAngle = initRotationAngle;
            rotVel = initRotationSpeed;
            evolveParams = Pellet.getDefaultParamArray(t);
        }
        
        public boolean isDead() {
            return health <= 0;
        }
    }
    
    public void addPellet(Pellet.Type type, CVector pos, CVector vel) {
        pellets.add(new Pellet(type,pos,vel));
    }
    private void addPellet(Pellet p) {
        pellets.add(p);
    }
    
    public List<Pellet> getPellets() {
        return pellets;
    }
    
    public Pellet[] getPelletsArray() {
        int l = pellets.size();
        Pellet[] pL = new Pellet[l];
        pellets.toArray(pL);
        return pL;
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
