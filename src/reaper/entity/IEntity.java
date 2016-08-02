/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper.entity;

import reaper.CVector;

/**
 *
 * @author murdock
 */
public interface IEntity<T> extends IDrawable, IEvolvable<T> {
    public float getBoundingRadius();
    public default void drawIfInRectangleBound(float x0, float y0, float width, float height) {
        //I want this to be quick, so there's no need for accurate checking.
        float bRad = getBoundingRadius();
        if (getX() < x0-bRad)
            return;
        if (getX() > x0+width+bRad)
            return;
        if (getY() < y0-bRad)
            return;
        if (getY() > y0+height+bRad)
            return;
        this.draw();
    }
    public float getX();
    public float getY();
    default CVector getXY() {
        return new CVector(getX(),getY());
    }
}
