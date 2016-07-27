/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper.entity;

import processing.core.PApplet;

/**
 *
 * @author murdock
 */
public interface IDrawable {
    public void prepareDraw();
    public void draw();
    public int getLayer();
}
