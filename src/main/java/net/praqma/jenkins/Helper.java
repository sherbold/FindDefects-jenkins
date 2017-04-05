package net.praqma.jenkins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Helper {

	public String sourceMPath, baseProject;
	static String resultDir;
	static String projectName;// source meter project name
	static String projectBaseDir;
	static String analysisResultJson;
	static String pathToJenkins;
	static String[][] matricsTable;
	static String pathToMatricsResultsFile;
	static String timeStampFolderName; 

	public Helper(String sourceMPath, String baseProject, String projectBaseDir, String projectName, String resultDir,
			String pathToJenkins) {

		this.baseProject = baseProject;
		this.sourceMPath = sourceMPath;
		Helper.projectBaseDir = projectBaseDir;
		Helper.projectName = projectName;
		Helper.resultDir = resultDir;
		Helper.pathToJenkins = pathToJenkins;

	}

	public Process executeSourceMeter() throws IOException {

		char $ = '"';
		String command = "SourceMeterJava"
				.concat(" -projectName=").concat($ + projectName + $)
				.concat(" -projectBaseDir=").concat($ + projectBaseDir + $)
				.concat(" -resultsDir=").concat($ + resultDir + $)
				.concat(" -cleanProject=false").concat(" -runFB=true")
				.concat(" -FBFileList=filelist.txt");

		List<String> cmdAndArgs = Arrays.asList("cmd", "/c", command);
		File dir = new File(sourceMPath.toString());
		ProcessBuilder pb = new ProcessBuilder(cmdAndArgs);
		pb.directory(dir);
		Process process = pb.start();

		return process;
	}

	public String[][] getMatricsData(String fileTag) throws IOException {

		String line = "";
		String cvsSplitBy = ",";

		int size = getSizeOfTable(fileTag);
		String[][] table = new String[size][];
		BufferedReader br = getFileFromPath(fileTag);

		int x = 0;
		while ((line = br.readLine()) != null) {
			line = line.replace("\"", "");
			table[x] = line.split(cvsSplitBy);
			x++;
		}

		return table;
	}

	// take the file with the name specified by user and -File.csv tag created
	// from the base dir
	public static BufferedReader getFileFromPath(String fileTag) throws FileNotFoundException {
		// take recently created dir by date
		timeStampFolderName = getTheLastBuildFile(resultDir, projectName);
		String matricsCsvFilePath = resultDir + "\\" + projectName + "\\java\\" + timeStampFolderName + "\\"+ projectName + "-" + fileTag;
		BufferedReader br = new BufferedReader(new FileReader(matricsCsvFilePath));
		return br;
	}

	
	public int getSizeOfTable(String fileTag) throws IOException {

		BufferedReader br = getFileFromPath(fileTag);
		int size = 0;
		while ((br.readLine()) != null) {
			size++;
		}
		return size;
	}

	/*
	 * On the path of the directory where source meter stores its result. a new
	 * folder is created for each build named as its timestamp which contains
	 * all the csv files for matrics this function returns the folder with the
	 * last time stamp.
	 */
	public static String getTheLastBuildFile(String csvFilePath, String projectName) {
		File file = new File(csvFilePath + "\\" + projectName + "\\java\\");
		String[] directories = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		Arrays.sort(directories);
		return directories[directories.length - 1];
	}

	public JsonArray convert2DArrayToJson(String multiDArray[][]) {
		JsonArray parentJsonArray = new JsonArray();
		// loop through your elements

		for (int i = 1; i < multiDArray.length; i++) {
			JsonObject childJsonObject = new JsonObject();
			JsonObject jsonObjectForMetrics = new JsonObject();
			for (int j = 0; j < multiDArray[0].length; j++) {
				String temp = "" + multiDArray[i][j].charAt(0);
				if (temp.matches(".*\\d+.*"))
					jsonObjectForMetrics.addProperty(multiDArray[0][j], Integer.parseInt(multiDArray[i][j]));
				else
					childJsonObject.addProperty(multiDArray[0][j], multiDArray[i][j]);

			}
			childJsonObject.add("matrics", jsonObjectForMetrics);
			parentJsonArray.add(childJsonObject);
		}
		return parentJsonArray;
	}

	// request the api for analysis
	public String requestPredictorApi(String jsonArray, String apiUrl) {
		String responseJson = "";
		try {
			URL url = new URL(apiUrl);
			URLConnection connection = url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setConnectTimeout(5000);
			// connection.setReadTimeout(20000);
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write(jsonArray);
			out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			responseJson = response.toString();

		} catch (Exception e) {
			System.out.println("\nError while calling Crunchify REST Service");
			System.out.println(e);
		}

		return responseJson;

	}

	public String[][] create2DArrayWithBugsValues(String[][] table, String resultsFromApi) {
		JsonParser parser = new JsonParser();
		JsonArray resultsMainArray = (JsonArray) parser.parse(resultsFromApi);

		String[][] finalTable = new String[table.length - 1][];
		// adding another column for bugs or not bugs to the created array
		for (int i = 1; i < table.length; i++) {
			ArrayList<String> list = new ArrayList<String>(Arrays.asList(table[i]));
			JsonObject anaylsisJsonObj = (JsonObject) resultsMainArray.get(i - 1);
			double predictionValue = anaylsisJsonObj.get("prediction").getAsDouble();
			if (predictionValue == 0.0)
				list.add("No Bugs");
			else
				list.add("Bugs");
			
			finalTable[i - 1] = list.toArray(new String[list.size()]);
		}

		return sortArrayByBugs(finalTable, finalTable[1].length - 1);

	}

	public String[][] sortArrayByBugs(final String[][] arrayTemp, final int numofColums) {

		Arrays.sort(arrayTemp, new Comparator<String[]>() {
			@Override
			public int compare(final String[] entry1, final String[] entry2) {
				final String bugsCol1 = entry1[numofColums];
				final String bugsCol2 = entry2[numofColums];
				return bugsCol1.compareTo(bugsCol2);
			}
		});

		return arrayTemp;

	}

}
