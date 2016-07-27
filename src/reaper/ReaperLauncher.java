/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper;

import java.io.IOException;
import processing.core.PApplet;

/**
 *
 * @author murdock
 */
public class ReaperLauncher {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        
        new ReaperLWJGL().run();
        //PApplet.main(new String[]{"reaper.ReaperPApplet"});
        
    }
    
}
