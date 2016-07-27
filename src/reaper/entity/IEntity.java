/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper.entity;

/**
 *
 * @author murdock
 */
public interface IEntity<T> extends IDrawable, IEvolvable<T> {
    public float getBoundingRadius();
    public float getX();
    public float getY();
}
