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
import reaper.GLDrawHelper;

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
        pc.setSpawnVelMag(10,100);
        pc.setAutoSpawn(true);
        
        addEntity(player);
        addEntity(pc);
    }
    
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public void evolve(long timestep) {
        
        // World-dependent evolution/physics goes here//////////////////////////
        float secondsElapsed = timestep/(1000f*1000f*1000f);
        
        // This is called both here and in drawAuxilliaries.
        // It's a bit inefficient, but not a bottleneck. I've left it for now.
        List<PelletCollection.Pellet> endP = pc.getEndangeredPellets(player);
        
        List<PelletCollection.Pellet> allP = pc.getPellets();
        for (PelletCollection.Pellet p : allP) {
            if (endP.contains(p)) {
                //Health drop rate of 5 per second
                p.damage(secondsElapsed*player.getCapturePower());
            } else {
                //Automatic health regeneration
                p.heal(secondsElapsed*1f);
            }
        }
        
        player.grow(secondsElapsed*3f);
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

            e.prepareDraw();
            e.draw();
            this.drawAuxilliaries();
        }
    }
    
    // This method draws extra cosmetic things that are not directly tied to an entity,
    // and have no physics. (So need need to call .evolve() on them).
    private void drawAuxilliaries() {
        
        // Draw lines connecting pellets to player
        GLDrawHelper.setStrokeWidth(2);
        GLDrawHelper.setColor(Color.GREEN);
        List<PelletCollection.Pellet> endangeredPellets = pc.getEndangeredPellets(player);
        
        for (PelletCollection.Pellet p : endangeredPellets) {
            float perc = p.getHealthPerc();
            if (perc < 0.99f)
                GLDrawHelper.line(player.getX(), player.getY(), p.getX(), p.getY(), 1 - perc);
        }
        
    }
    
    
    @Override
    public World deepClone() {
        
        List<IEntity<?>> es = new ArrayList<>();
        
        World w = new World(width,height);
        
        for (IEntity e : entities) {
            IEntity cloned = (IEntity)e.deepClone();
            if (e.equals(this.pc)) {
                w.pc = (PelletCollection)e;
            }
            if (e.equals(this.player)) {
                w.player = (Player)e;
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
