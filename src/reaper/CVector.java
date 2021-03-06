/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper;

/**
 *
 * @author murdock
 */
public class CVector {
    public float x;
    public float y;
    
    public CVector(float xIn, float yIn) {
        x = xIn;
        y = yIn;
    }
    
    public static CVector normVector(float x1, float y1, float x2, float y2) {
        return normVector(new CVector(x1,y1), new CVector(x2,y2));
    }
    public static CVector normVector(CVector start, CVector end) {
        float relX, relY, length;
        relX = end.x - start.x;
        relY = end.y - start.y;
        length = (float)Math.sqrt((relX*relX) + (relY*relY));
        relX /= length;
        relY /= length;
        return new CVector(relX,relY);
    }
    
    public CVector multiply(float mult) {
        return new CVector(x*mult,y*mult);
    }
    public CVector add(CVector v) {
        return new CVector(x + v.x, y + v.y);
    }
    public CVector subtract(CVector v) {
        return add(v.multiply(-1));
    }
    public float mag() {
        return (float)Math.sqrt((x*x) + (y*y));
    }
    public static float dist(CVector v1, CVector v2) {
        return v1.subtract(v2).mag();
    }
    public CVector deepClone() {
        return new CVector(x,y);
    }
    public float angle() {
        return (float)Math.atan2(y, x);
    }
    public static CVector fromPolar(float angle, float magnitude) {
        CVector v;
        float x,y;
        x = (float)Math.cos(angle)*magnitude;
        y = (float)Math.sin(angle)*magnitude;
        v = new CVector(x,y);
        return v;
    }
    public static float scalarCross(CVector v1, CVector v2) {
        float angle = v2.angle() - v1.angle();
        float mag = v2.mag()*v1.mag();
        return mag*(float)Math.sin(angle);
    }
}
