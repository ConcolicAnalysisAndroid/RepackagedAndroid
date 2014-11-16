package com.rit.AndroidAnalysisEngine.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import com.rit.AndroidAnalysisEngine.engine.impl.Dex2JarWrapper;

public class Engine {
	
	public static void processApk(Path path){
		
		ApkToJarConverter converter = new Dex2JarWrapper();
		System.out.println("Converting!");
		String jarToTarget  = converter.convertApkToJar(path, null);
		System.out.println("Compiling!");
        WrapperCompiler wrComp = new WrapperCompiler();
        File compiledJar = null;
        try {
        	compiledJar = wrComp.doCompile(99, new File(jarToTarget));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
        System.out.println("Running JPF!");
        File output =null;
        try {
        	JpfRunner jpfRunner = new JpfRunner();
            output = jpfRunner.runAgainstFile(path.getFileName().toString(), compiledJar);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
        
        System.out.println("Program complete!");
        if ((null==output) || (!output.exists())){
        	System.out.println("There was a problem generating the output file");
        }else{
        	System.out.println("Output file located at: "+output.getAbsolutePath().toString());
        }
	}
	
}
