/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper;

import java.awt.Color;
import java.awt.Font;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author murdock
 */
public class GLDrawHelper {
    
    private static final double TAU = 2.0d * Math.PI;
    
    //Number of segments per pixel size of the major axis
    private static final float ELLIPSE_ACCURACY = 1.8f;
    
    /*
    public static void writeText(float x, float y, String str, Color c) {
        // Can't work out how to do this :( 
    }
    */
    public static void setColor(float red, float green, float blue) {
        glColor3f(red,green,blue);
    }
    public static void setColor(float red, float green, float blue, float alpha) {
        glColor4f(red,green,blue,alpha);
    }
    public static void setColor(Color c) {
        glColor4f(c.getRed()/255f,c.getGreen()/255f,c.getBlue()/255f,c.getAlpha()/255f);
    }
    public static void setStrokeWidth(float width) {
        glLineWidth(width);
    }
    
    public static void line(float startX, float startY, float endX, float endY) {
        glBegin(GL_LINES);
            glVertex2f(startX,startY);
            glVertex2f(endX,endY);
        glEnd();
    }
    public static void line(float startX, float startY, float endX, float endY, float stretch) {
        float aendX = (endX - startX)*stretch + startX;
        float aendY = (endY - startY)*stretch + startY;
        line(startX,startY,aendX,aendY);
    }
    public static void lineByAngle(float startX, float startY, float angle, float length) {
        float endX = length*(float)Math.cos(angle) + startX;
        float endY = length*(float)Math.sin(angle) + startY;
        line(startX,startY,endX,endY);
    }
    public static void disk(float x, float y, float radius) {
        ellipseFill(x,y,radius,radius,0);
    }
    public static void circle(float x, float y, float radius) {
        ellipse(x,y,radius,radius,0);
    }
    public static void ellipseFill(float x, float y, float mrad, float Mrad, float angle) {
       
        int sliceCount = (int)Math.ceil(ELLIPSE_ACCURACY*Mrad);
        double cache,relX,relY;

        glBegin(GL_TRIANGLE_FAN);
            glVertex2d(x, y); // center of circle
            for(int i = 0; i <= sliceCount ; i++) { 
                cache = Mrad*Math.cos(i * TAU / sliceCount);
                relY = mrad*Math.sin(i * TAU / sliceCount);
                relX = (Math.cos(-angle)*cache) + (Math.sin(-angle)*relY);
                relY = (-Math.sin(-angle)*cache) + (Math.cos(-angle)*relY);
                
                glVertex2d(
                    x + relX,
                    y + relY
                );
            }
        glEnd();
    }
    public static void ellipse(float x, float y, float mrad, float Mrad, float angle) {
       
        int sliceCount = (int)Math.ceil(ELLIPSE_ACCURACY*Mrad);
        double cache,relX,relY;

        glBegin(GL_LINE_LOOP);
            for(int i = 0; i <= sliceCount ; i++) { 
                cache = Mrad*Math.cos(i * TAU / sliceCount);
                relY = mrad*Math.sin(i * TAU / sliceCount);
                relX = (Math.cos(-angle)*cache) + (Math.sin(-angle)*relY);
                relY = (-Math.sin(-angle)*cache) + (Math.cos(-angle)*relY);
                
                glVertex2d(
                    x + relX,
                    y + relY
                );
            }
        glEnd();
   }
    
}
