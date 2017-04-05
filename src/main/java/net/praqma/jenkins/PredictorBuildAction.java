/*
 * The MIT License
 *
 * Copyright 2013 Praqma.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.praqma.jenkins;

import hudson.model.Action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import org.json.JSONObject;
import org.json.JSONArray;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map.Entry;
//for logistic regression 



/**
 * 
 * A class representing an action performed in a build step(It can be used in
 * all parts of the build). These actions are added to the build. These actions
 * can contain business logic, data etc. Builds can have multiple actions of the
 * same type.
 * 
 * This data can the be extracted for use in the various views that Jenkins
 * offers.
 * 
 * In our example we will re-use the same action through the entire build
 * pipeline.
 * 
 * @author Praqma
 */
public class PredictorBuildAction implements Action {

	private int index;
	private int guess;
	private int number;
	private boolean correct;
	String [][] arrayForTableView;
	/*public String projectName;
	public  String dateFileName ;
	public String csvFilePath;
	*///"C:\\Users\\Athar\\Desktop\\DefectPrectionProjectWork\\AnalysisResults\\"+projectName+"\\java\\"+resultedFileName+"\\"+projectName+"-File.csv"; 
	
	

	public PredictorBuildAction() {
		
	}

	public PredictorBuildAction(int index, int guess, int number,
			boolean correct, String dateFileName,String[][] finalTableArray) {
		this.index = index;
		this.guess = guess;
		this.number = number;
		this.correct = correct;
		arrayForTableView = finalTableArray;
		/*this.dateFileName = dateFileName;
		this.projectName = projectName;
		this.csvFilePath = "C:\\Users\\Athar\\Desktop\\DefectPrectionProjectWork\\AnalysisResults\\";
		*///this.csvFilePath =  csvFilePath+""+projectName+"\\java\\"+dateFileName+"\\"+projectName;
	
		// isBug();
		
	}

	
public String[] retriveHeader(String fileTag) throws IOException{
		
		String line = "";
	    String cvsSplitBy = ",";
	    String[] header=null;
	    //BufferedReader br = new BufferedReader(new FileReader(csvFilePath+"-"+fileTag));
	    BufferedReader br =Helper.getFileFromPath(fileTag);
	  
	        if ((line = br.readLine()) != null) {
	        	  header = line.split(cvsSplitBy);
	        }
	       
	        for(int i = 0;i<header.length ;i++)
	        {
	        	header[i] = header[i].replace("\"", "");
	        }
	        
	        return header;
	}
	

	public String[][] retriveColumn()
	{
		return arrayForTableView;
	}
	
	
	
	
	/**
	 * 
	 * @return the path to the icon file to be used by Jenkins. If null, no link
	 *         will be generated
	 */
	@Override
	public String getIconFileName() {
		return "/plugin/bugs-predictor/images/64x64/staricon.png";
			
	}

	@Override
	public String getDisplayName() {
		return "Build Status " + index;
	}

	@Override
	public String getUrlName() {
		return "buildanalysis" + index;
	}

	/**
	 * @return the correct
	 */
	public boolean isCorrect() {
		return correct;
	}

	/**
	 * @param correct
	 *            the correct to set
	 */
	public void setCorrect(boolean correct) {
		this.correct = correct;
	}

	/**
	 * @return the guess
	 */
	public int getGuess() {
		return guess;
	}

	/**
	 * @param guess
	 *            the guess to set
	 */
	public void setGuess(int guess) {
		this.guess = guess;
	}

	/**
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * @param number
	 *            the number to set
	 */
	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	public String toString() {
		// this send data for one build to jelly
		return String.format("%s - %s - %s", guess, number, correct);
	}

// --------------------------------------------------------------------------------
	int loc;
	int cbo;
	int wmc;


	
	
	
	
	
}
