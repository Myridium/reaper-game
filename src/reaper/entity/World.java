/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import processing.core.PApplet;

/**
 *
 * @author murdock
 */
public class World implements IDrawable, IEvolvable<World> {
    
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
        pc.setSpawnVelMag(10,500);
        pc.setAutoSpawn(true);
        
        addEntity(player);
        addEntity(pc);
    }
    
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public void evolve(long timestep) {
        
        List<PelletCollection.Pellet> pL = pc.getPellets();
        //Health drop rate of 5 per second
        float secondsElapsed = timestep/(1000f*1000f*1000f);
        for (PelletCollection.Pellet p : pL) {
            if (player.inCaptureRange(p.pos.x, p.pos.y))
                p.health -= secondsElapsed*5;
        }
        
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
        this.sortEntitiesByLayer(entities);
        
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
