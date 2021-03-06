/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper.entity;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import reaper.CVector;
import LWJGLTools.GLDrawing.GLDrawHelper;

/**
 *
 * @author murdock
 */
public final class World implements IDrawable, IEvolvable<World> {
    
    List<IEntity<?>> entities;
    Player player;
    PelletCollection pc;
    private static final int LAYER = 0;
    private int width, height;
    
    public World(int w, int h) {
        
        width = w;
        height = h;
        
        entities = new ArrayList<>();    
        
        player = new Player(width/2,height/2);
        pc = new PelletCollection();
        pc.setSpawnCenter(width/2, height/2);
        pc.setSpawnFreq(10, 100);
        pc.setSpawnRadius(Math.max(width, height));
        pc.setSpawnVelMag(10,300);
        pc.setAutoSpawn(true);
        
        addEntity(player);
        addEntity(pc);
    }
    
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public void evolve(long timestep) {
        
        // World-dependent evolution/physics goes here//////////////////////////
        float secondsElapsed = timestep/(1000f*1000f*1000f);
        
        CVector playerVect = player.getXY();
        
        // This is called both here and in drawAuxilliaries.
        // It's a bit inefficient, but not a bottleneck. I've left it for now.
        
        PelletCollection.Pellet[] allP = pc.getPelletsArray();
        for (PelletCollection.Pellet p : allP) {
            
            float distFromPlayer = player.distanceFrom(p.getX(), p.getY()) - p.getRadius();
            
            if (p.type == PelletCollection.Pellet.Type.HOMING) {
                // The homing pellet's velocity should be updated
                // The homing pellet has a single evolve parameter which is the closest distance
                // it has ever been to the player.
                p.setParam(0, Math.min(distFromPlayer,p.getParam(0)));
                
                float accValue = p.getParam(0);
                accValue = accValue*(float)Math.pow(distFromPlayer,1) * (float)Math.exp(-distFromPlayer/50f);
                
                CVector toPlayerVect = CVector.normVector(p.getXY(), playerVect);
                
                CVector dir = toPlayerVect.multiply(accValue*secondsElapsed);
                p.addVel(dir);
                
                // For aesthetic appeal, also add spin to the pellet.
                float extraRotVel = CVector.scalarCross(p.vel,toPlayerVect.multiply(accValue))*secondsElapsed/100000f;
                p.addRotVel(extraRotVel);
                
            }
            // At the moment, this could just be done for pellets that are in capture range
            // as they can't harm the player otherwise, but this could change.
            if (distFromPlayer < 0)
                player.damage(secondsElapsed*p.attackPower());
            
            
            
            if (pc.isPelletEndangered(p, player)) {
                //Health drop rate of 5 per second
                p.damage(secondsElapsed*player.getCapturePower());
                
                //The PelletCollection will prune dead pellets when it is evolved.
                //Before that happens, we'll check if the pellet is dead and if so, add health to the player.
                if (p.isDead()) {
                    switch (p.type) {
                        case HEALTH:
                            player.heal(20f);
                            break;
                        case FOCUS:
                            player.addFocus(180f);
                            break;
                        case SUPER:
                            player.addSupertime(20f);
                        default:
                            player.addFocus(15f);
                            player.shrink(4f);
                            break;
                    }
                }
            } else {
                //Automatic health regeneration
                p.heal(secondsElapsed*1f);
            }
        }
        
        //player.heal(secondsElapsed*1f);
        player.grow(secondsElapsed*2f);
        ////////////////////////////////////////////////////////////////////////
        
        // Let the entities evolve as is specified in their own classes
        for (IEntity e : entities) {
            e.evolve(timestep);
        }
    }
    
    @Override
    public void prepareDraw() {
        //Nothing needs to be done!
    }
    @Override
    public void draw() {
        World.sortEntitiesByLayer(entities);
        
        for (IEntity e : entities) {
            
            /*
            float bRad = e.getBoundingRadius();
            // If the entity is out of frame, then don't bother drawing it.
            if (    bRad != -1 && (
                    (e.getX() < 0 - bRad)
                    ||
                    (e.getX() > this.width - bRad)
                    ||
                    (e.getY() < 0 - bRad)
                    ||
                    (e.getX() > this.height - bRad)
                    )
                ) {
                continue;
            }
            */

            e.prepareDraw();
            e.drawIfInRectangleBound(0,0,width,height);
        }
        
        this.drawAuxilliaries();
    }
    
    // This method draws extra cosmetic things that are not directly tied to an entity,
    // and have no physics. (So need need to call .evolve() on them).
    private void drawAuxilliaries() {
        
        // Draw lines connecting pellets to player
        GLDrawHelper.setStrokeWidth(2);
        List<PelletCollection.Pellet> endangeredPellets = pc.getEndangeredPellets(player);
        
        for (PelletCollection.Pellet p : endangeredPellets) {
            
            float perc = p.getHealthPerc();
            
            GLDrawHelper.setColor(Color.GRAY);
            GLDrawHelper.setStrokeWidth(1);
            GLDrawHelper.line(player.getX(), player.getY(), p.getX(), p.getY());
            
            if (perc < 0.99f) {
                Color c  = p.absorbColor();
                GLDrawHelper.setColor(c);
                GLDrawHelper.setStrokeWidth(p.absorbLineWidth());
                GLDrawHelper.line(player.getX(), player.getY(), p.getX(), p.getY(), 1 - perc);
            }
        }
        
    }
    
    
    @Override
    public World deepClone() {
        
        List<IEntity<?>> es = new ArrayList<>();
        
        World w = new World(width,height);
        
        for (IEntity e : entities) {
            IEntity cloned = (IEntity)e.deepClone();
            if (e.equals(this.pc)) {
                w.pc = (PelletCollection)cloned;
            }
            if (e.equals(this.player)) {
                w.player = (Player)cloned;
            }
            es.add(cloned);
        }
        
        w.entities = es;
        
        return w;
    }
    
    public void addEntity(IEntity<?> e) {
        entities.add(e);
        this.sortEntitiesByLayer(entities);
    }
    
    private World(List<IEntity<?>> es) {
        entities = es;
        sortEntitiesByLayer(entities);
    }

    @Override
    public int getLayer() {
        return LAYER;
    }
    
    private static void sortEntitiesByLayer(List<IEntity<?>> eList) {
        if (eList.size() > 0) {
            Collections.sort(eList, 
            
                    new Comparator<IEntity<?>>() {
                        @Override
                        public int compare(final IEntity<?> e1, final IEntity<?> e2) {
                            return Integer.compare(e1.getLayer(), e2.getLayer());
                        }
                    }
                    
            );
        }
    }
}
