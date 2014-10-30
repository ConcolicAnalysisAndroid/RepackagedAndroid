package com.rit.AndroidAnalysisEngine.engine;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.SystemUtils;

import com.rit.AndroidAnalysisEngine.engine.impl.ActivityFinder;

import android.app.Activity;

public class WrapperCompiler {
	
	private final String indexToken = "{{INDEX}}";
	private final String importsToken = "{{CLASS_IMPORTS}}";
	private final String functionsToken = "{{FUNCTION_CALLS}}";
	private String classPathDelimiter;
	
	public WrapperCompiler(){
		classPathDelimiter = SystemUtils.IS_OS_WINDOWS ? " ; " : " : ";
	}
	
	private String readFile(File src, Charset encoding) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(src.getPath()));
			  return new String(encoded, encoding);
			}
	
	
	public void spawnFiles(int index, File targetJar) throws IOException{
		ActivityFinder activityFinder = new ActivityFinder();
		spawnWrapperFile(index, activityFinder.getClassInfo(targetJar, new HashSet<Class<? extends Activity>>()));
		spawnManifestFile(index, targetJar);
	}
	
	private String getClassPathString(Path targetJarPath) throws IOException{
		String classpath = "";
		File dir = new File("./lib/");
        File [] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        for (File file : files) {
        	classpath+= "  "+new File("./spawn/").toPath().relativize(file.toPath()).toString()+"\n";
        }
        String relativePath = new File(".").toPath().relativize(targetJarPath).toString();
		return relativePath + classpath;
	}
	
	public void spawnManifestFile(int index, File targetJar) throws IOException{
		File sourceFile = new File("./spawn/manifest.mf.template");
		
		String rawSource = readFile(sourceFile, Charset.defaultCharset());
		String editedSource = rawSource.replace(indexToken, new Integer(index).toString());
		editedSource = editedSource.replace("{{CLASSPATH}}", getClassPathString(targetJar.toPath()));

		PrintWriter outToFile = new PrintWriter("./spawn/manifest"+index+".mf");
		outToFile.println(editedSource);
		outToFile.close();
	}
	
	public void spawnWrapperFile(int index, Set<Class<? extends Activity>> activityClasses) throws IOException{
		File sourceFile = new File("./spawn/Wrapper.template");
		SourceWriter manipulator = new SourceWriter(activityClasses);
		
		String rawSource = readFile(sourceFile, Charset.defaultCharset());
		String editedSource = rawSource.replace(indexToken, new Integer(index).toString());
		editedSource = editedSource.replace(importsToken, manipulator.getImportStrings());
		editedSource = editedSource.replace(functionsToken, manipulator.getFunctionCalls());
		
		//TODO crazy stuff here
		PrintWriter outToFile = new PrintWriter("./spawn/Wrapper"+index+".java");
		outToFile.println(editedSource);
		outToFile.close();
		
	}
	
	public File doCompile(int index, File targetJar) throws IOException{
		spawnFiles(index, targetJar);
			
		//compile
		
		Process compileProc = Runtime.getRuntime().exec("javac -classpath ./lib/*:"+targetJar.toPath().toString()+" ./spawn/Wrapper"+index+".java");
		try {
			compileProc.waitFor(); //what could go wrong? Oh, right, infinite hang . . .
		} catch (InterruptedException e) {
			//HERESY WILL BE SUPPRESSED
			e.printStackTrace();
		} 
		
		File classFile = new File("./spawn/Wrapper"+index+".class");
		if(!classFile.exists()){
			return null;
		}
		
		//make the damn jar
		Process jarProc =  Runtime.getRuntime().exec("jar cmf ./spawn/manifest"+index+".mf ./spawn/Wrapper"+index+".jar ./spawn/Wrapper"+index+".class");
        
		try {
			jarProc.waitFor(); //what could go wrong? Oh, right, infinite hang . . .
		} catch (InterruptedException e) {
			//HERESY WILL BE SUPP - wait, didn't I already yell this?
			e.printStackTrace();
		} 
		//clean up
		//cleanOutJavaFiles();
        //cleanOutClassFiles();
        return new File("./spawn/Wrapper"+index+".jar");
        /*WARNING: if you got major.minor versions, I feel bad for you son
        			I got 99 problems, but too many JDKs ain't one 
        Real Answer: get your default user JDK and JRE in sync.  No really. 
        	The inanity of how I'm doing this (who uses exec?) means I can't control 
        	which java is on your path.*/
	}
	
	public void cleanOutJarFiles() throws IOException{
		cleanOutFilesByPattern("*.jar");
	}
	
	private void cleanOutClassFiles() throws IOException{
		cleanOutFilesByPattern("*.class");
	}
	
	private void cleanOutJavaFiles() throws IOException{
		cleanOutFilesByPattern("*.java");
	}
	
	private void cleanOutFilesByPattern(String pattern) throws IOException{
		File dir = new File("./spawn");
        File [] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".class");
            }
        });
        for (File file : files) {
            file.delete();
        }
	}
}
