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

import java.util.Properties;
import java.util.Map.Entry;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.avro.data.Json;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The Builder for our project. We extend from Builder which implements
 * BuildStep, which has a perform method we will use. Additionally all
 * {@link hudson.tasks.BuildStep}s have a prebuild method to override, which
 * runs before the build step.
 *
 * The main purpose of this example builder is to extract environment
 * information from the executing slaves, and make this data available for the
 * view we wish to present it in.
 *
 * During execution we re-use already added actions, and add the discovered data
 * to the already existing build action.
 *
 * @author Praqma
 */
public class PredictorBuilder extends Builder {

	
	Helper helper;
	String apiUrl;
	public final String fileTag = "File.csv";
	
	/**
	 * Required static constructor. This is used to create 'One Project Builder'
	 * BuildStep in the list-box item on your jobs configuration page.
	 */
	@Extension
	public static class GuessingBuilderImpl extends BuildStepDescriptor<Builder> {

		/**
		 * This is used to determine if this build step is applicable for your
		 * chosen project type. (FreeStyle, MultiConfiguration, Maven) Some
		 * plugin build steps might be made to be only available to
		 * MultiConfiguration projects.
		 *
		 * Required. In our example we require the project to be a free-style
		 * project.
		 *
		 * @param proj
		 *            The current project
		 * @return a boolean indicating whether this build step can be chose
		 *         given the project type
		 */
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> proj) {
			return true;
		}

		/**
		 * Required method.
		 * 
		 * @return The text to be displayed when selecting your BuildStep, in
		 *         the project
		 */
		@Override
		public String getDisplayName() {
			return "Values for defect Predection";
		}

		
		/**
		 * Form validate the upper bound configuration setting
		 * 
		 * @param sourceMPath
		 * @param baseProject
		 * @param pathToJenkins
		 * @return
		 */

	}

	/**
	 * Our builder currently has 2 configuration parameters. Upper and
	 * lowerbound
	 * 
	 * @param sourceMPath
	 * @param baseProject
	 * @param pathToJenkins
	 * @param predictorApiUrl
	 */
	@DataBoundConstructor
	public PredictorBuilder(final String sourceMPath, final String baseProject, final String pathToJenkins,final String predictorApiUrl) {
		this.apiUrl = predictorApiUrl;
		String resultDir = createFileForSourceMResults();
		String projectName = baseProject.replace(" ","_").trim(); //using the same name for baseproject and the folder for sourcemeter data
		String projectBaseDir = pathToJenkins+File.separator+baseProject;
		helper = new Helper( sourceMPath, baseProject,  projectBaseDir,  projectName,  resultDir,  pathToJenkins);
		
	}
	
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {

		listener.getLogger().println(String.format("Calculating matrics by source-meter"));
		Process sourceMeterProcess = helper.executeSourceMeter();
		sourceMeterProcess.waitFor();

		String [][] matricsTable = helper.getMatricsData(fileTag);
		JsonArray matricsTableJson = helper.convert2DArrayToJson(matricsTable);
		listener.getLogger().println(String.format("sourcemeter values calculated ,Calling api for calulating prediction values"));
		String resultFromApi = helper.requestPredictorApi(matricsTableJson.toString(),apiUrl);
		listener.getLogger().println(String.format("analysis result by API: "+resultFromApi));
		
		String[][] finalTableArray = helper.create2DArrayWithBugsValues(matricsTable, resultFromApi);
		
		for (int i = 0; i < finalTableArray.length; i++) {
			for (int j = 0; j < finalTableArray[0].length; j++) {
				listener.getLogger().println(String.format("["+i+","+j+"] = "+finalTableArray[i][j]));
			}
		}
		
		
		//Add the action to jenkins. This way we can reuse the data.
		int currentGuessingBuildActions = build.getActions(PredictorBuildAction.class).size();
		build.addAction(new PredictorBuildAction(currentGuessingBuildActions + 1, -1, 1, 1 == -1, "", finalTableArray));

		// Add a GuessingRecorder if not already done
		AbstractProject<?, ?> project = build.getProject();
		if (project.getPublishersList().getAll(PredictorRecorder.class).isEmpty()) {
			project.getPublishersList().add(new PredictorRecorder());
			project.save();
		}

		// return true (we summarize results in post build)
		return true;
	}

	
	public String createFileForSourceMResults()
	{
		String path = System.getProperty("user.home") + File.separator + "temp_def_predictor";
		File file = new File(path);
		if(!file.exists())
		file.mkdir();
		
		return path;
	}
	
}
