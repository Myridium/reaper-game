/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper;

import java.io.File;
import java.io.IOException;

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
        
        // This line will set the class-path for the LWJGL library stuff to the "natives/" directory
        // in the same directory as this .jar
        // It's the same thing as providing the "-Djava.library.path="natives/"" switch to the "java -jar" command
        // when running this program.
        System.setProperty("org.lwjgl.librarypath", new File("lib/native").getAbsolutePath());
        //System.setProperty("Djava.library.path", new File("lib/native").getAbsolutePath());
        
        new ReaperLWJGL().run();
        
        
    }
    
}
