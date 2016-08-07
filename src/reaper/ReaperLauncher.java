/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import input.ControllerReader;

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
    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException, ControllerReader.NoControllerException, ControllerReader.NoSuchAxisException {
        
        // Set the class-path for the LWJGL library stuff to the "natives/" directory
        // in the same directory as this .jar
        // It's the same thing as providing the "-Djava.library.path="natives/"" switch to the "java -jar" command
        // when running this program.
         
        //String s = ClassLoader.getSystemClassLoader().getResource(".").getPath();
        String s = new File(ReaperLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
        s = s + "/lib/native";
        System.out.println("Setting LWJGL library path to:");
        System.out.println(s);
        
        // If a class-path for LWJGL is not specified, then use the path that is included with the .jar file.
        // The path needs to be specified when running and debugging.
        //if(System.getProperty("org.lwjgl.librarypath")==null && System.getProperty("Djava.library.path")==null)
        
        //I do not understand why this works, but if NetBeans provides an argument, then the property is registered as null
        //even though it shouldn't be, and so this line is run anyway, and for some reason this doesn't cause exceptions.
        //It makes no sense.
        if (System.getProperty("Djava.library.path") == null && System.getProperty("org.lwjgl.librarypath") == null ) {
            System.setProperty("org.lwjgl.librarypath", s);
        }
        
        // This line will set the class-path for the LWJGL library stuff to the "natives/" directory
        // in the same directory as this .jar
        // It's the same thing as providing the "-Djava.library.path="natives/"" switch to the "java -jar" command
        // when running this program.
        //System.setProperty("org.lwjgl.librarypath", new File("lib/native").getAbsolutePath());
        //System.setProperty("Djava.library.path", new File("lib/native").getAbsolutePath());
        
        new ReaperLWJGL().run();
        
        
    }
    
}
