package com.tcs.eas.api.tools.testbuddy.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.tcs.eas.api.tools.testbuddy.constant.Constant;
import com.tcs.eas.api.tools.testbuddy.model.FormData;
import com.tcs.eas.api.tools.testbuddy.model.HttpHeader;
import com.tcs.eas.api.tools.testbuddy.model.QueryParam;
import com.tcs.eas.api.tools.testbuddy.util.Helper;
import com.tcs.eas.api.tools.testbuddy.util.SpecTemplate;

/**
 * 
 * @author 44745
 *
 */
public class Main implements Constant {
	// Logger for logging
	final static Logger logger = Logger.getLogger(Main.class);
	private FormData formData;

	/**
	 * 
	 * @param formData
	 */
	public Main(FormData formData) {
		this.formData = formData;
	}

	/**
	 * 
	 */
	public String run() {
		try {
			String directoryPath = DEST_DIRECTORY_PATH + UUID.randomUUID().toString();
			logger.info("Project directory path => " + directoryPath);
			Path path = Paths.get(directoryPath);
			Files.createDirectories(path);
			logger.info("Project directory created...");
			Helper.copyDirectory(SRC_DIRECTORY_PATH, directoryPath);
			generateDefaultEnvPropFile(directoryPath);
			generateSpecification(directoryPath);
			return getTestReport1(directoryPath);
		} catch (IOException e) {
			logger.error("Caught error: Failed to create directory!" + e.getMessage());
			return "error.html";
		}
	}

	/**
	 * 
	 * @param directoryPath
	 */
	private void generateDefaultEnvPropFile(String directoryPath) {
		try {
			Properties prop = new Properties();
			prop.setProperty(HTTP_SCHEMA, formData.getHttpSchema());
			prop.setProperty(API_HOST, formData.getHost());
			prop.setProperty(API_HOST_PORT,formData.getPort() == null ? "" : formData.getPort());
			prop.store(new FileWriter(directoryPath + File.separatorChar + "env" + File.separatorChar + "default"
					+ File.separatorChar + "default.properties"), "Environment specific properties");
			logger.info("Environment specific properties file created.");
		} catch (Exception e) {
			logger.error("Caught error " + e);
		}
	}

	/**
	 * 
	 * @param directoryPath
	 */
	private void generateSpecification(String directoryPath) {
		new SpecTemplate(formData).writeSpecs(directoryPath);
	}

	/**
	 * 
	 * @param directoryPath
	 * @return
	 */
	private String getTestReport(String directoryPath) {
		File projectDirectory = new File(directoryPath);
		String projectDirectoryName = projectDirectory.getName();
		ProcessBuilder processBuilder = new ProcessBuilder();
		// Windows
		// processBuilder.command("cmd.exe", "/c", "cd " + directoryPath, " && gauge run
		// specs");
		processBuilder.command("cmd.exe", "/c", "cd " + directoryPath, " && mvn gauge:execute -DspecsDir=specs");
		try {
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				logger.info(line);
			}
			int exitCode = process.waitFor();
			logger.info("\nExited with error code : " + exitCode);
		} catch (IOException e) {
			logger.error("Caught error " + e);
		} catch (InterruptedException e) {
			logger.error("Caught error " + e);
		}
		Helper.copyDirectory(directoryPath + "\\reports\\html-report", WINDOWS_WEB_SERVER_PATH + projectDirectoryName);
		return projectDirectoryName;
	}

	private String getTestReport1(String directoryPath) {
		logger.info("Executing tests and generates report.");
		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
		File projectDirectory = new File(directoryPath);
		String projectDirectoryName = projectDirectory.getName();
		ProcessBuilder processBuilder = new ProcessBuilder();
		Process process = null;
		// Windows
		// processBuilder.command("cmd.exe", "/c", "cd " + directoryPath, " && gauge run
		// specs");
		if (isWindows) {
			logger.info("Executing tests on Windows.");
			processBuilder.command("cmd.exe", "/c", "cd " + directoryPath, " && mvn gauge:execute -DspecsDir=specs");
			try {
				process = processBuilder.start();
			}catch (Exception e) {
				// TODO: handle exception
			}
		} else {
			logger.info("Executing tests on Linux.");
			Runtime rt = Runtime.getRuntime();
			try {
				process  = rt.exec(new String[] { "/bin/sh", "-c",
				"cd "+directoryPath+" ; mvn gauge:execute -DspecsDir=specs" });
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//processBuilder.command("/bin/sh", "-c", "cd " + directoryPath, " && mvn gauge:execute -DspecsDir=specs");
		}
		try {
			//Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				logger.info(line);
			}
			int exitCode = process.waitFor();
			logger.info("\nExited with error code : " + exitCode);
		} catch (IOException e) {
			logger.error("Caught error " + e);
		} catch (InterruptedException e) {
			logger.error("Caught error " + e);
		}
		if(isWindows) {
			Helper.copyDirectory(directoryPath + File.separatorChar+"reports"+File.separatorChar+"html-report", WINDOWS_WEB_SERVER_PATH + projectDirectoryName);
		}else {
			Helper.copyDirectory(directoryPath + File.separatorChar+"reports"+File.separatorChar+"html-report", LINUX_WEB_SERVER_PATH + projectDirectoryName);
		}
		return projectDirectoryName;
	}

	public static void main(String[] args) {
		//https://laptop-c8hbrms1:8443/apis/v1/employees
			

			FormData formData = new FormData();
			formData.setResourceTitle("Testing of Swagger Petstore APIs");
			formData.setHttpSchema("https");
			formData.setHost("laptop-c8hbrms1");
			formData.setPort("8443");
			// formData.setBasePath("/get");
			formData.setBasePath("/apis/v1");
			formData.setResourcePath("/employees");
			QueryParam q = new QueryParam();
			q.setParameterName("financialOptions");
			q.setParameterValue("All");
			List<QueryParam> list = new ArrayList<QueryParam>();
			list.add(q);
			q = new QueryParam();
			q.setParameterName("city");
			q.setParameterValue("ggn");
			// list.add(q);
			// formData.setQueryParams(list);

			List<HttpHeader> headers = new ArrayList<>();

			HttpHeader header = new HttpHeader();
			header.setHeaderName("accept");
			header.setHeaderValue("application/json");
			headers.add(header);
			header = new HttpHeader();
			header.setHeaderName("content-type");
			header.setHeaderValue("application/json");
			//headers.add(header);
			header = new HttpHeader();
			header.setHeaderName("authorization");
			header.setHeaderValue("ff9nksdw8fsdfdsfkfskj");
			//headers.add(header);
			formData.setHttpHeaders(headers);
			formData.setMethod("get");
			formData.setShowRequest(true);
			formData.setShowResponse(true);
			String body = "\"test123\"";
			String body1 = "{\r\n" + "    \"suppliedIndividuals\": [\r\n" + "        {\r\n"
					+ "            \"dateOfBirth\": \"1989-04-17\",\r\n" + "            \"firstName\": \"John\",\r\n"
					+ "            \"houseNameNumber\": \"1\",\r\n" + "            \"postcode\": \"LS1 4AP\",\r\n"
					+ "            \"surname\": \"Doe\"\r\n" + "        }\r\n" + "    ]\r\n" + "}";
			String body2 = "{\r\n" + "  \"id\": 1,\r\n" + "  \"category\": {\r\n" + "    \"id\": 1,\r\n"
					+ "    \"name\": \"Gun dog\"\r\n" + "  },\r\n" + "  \"name\": \"doggie\",\r\n"
					+ "  \"photoUrls\": [\r\n" + "    \"http://photo.com\"\r\n" + "  ],\r\n" + "  \"tags\": [\r\n"
					+ "    {\r\n" + "      \"id\": 1,\r\n" + "      \"name\": \"black\"\r\n" + "    }\r\n" + "  ],\r\n"
					+ "  \"status\": \"available\"\r\n" + "}";
			formData.setBody(body2);
			Main m = new Main(formData);
			System.out.println(m.run());
			
	}
	/**
	 * 
	 * @param args
	 */
	public static void main1(String[] args) {
		FormData formData = new FormData();
		formData.setResourceTitle("Testing of Swagger Petstore APIs");
		formData.setHttpSchema("https");
		formData.setHost("petstore.swagger.io");
		// formData.setBasePath("/get");
		formData.setBasePath("/v2");
		formData.setResourcePath("");
		QueryParam q = new QueryParam();
		q.setParameterName("financialOptions");
		q.setParameterValue("All");
		List<QueryParam> list = new ArrayList<QueryParam>();
		list.add(q);
		q = new QueryParam();
		q.setParameterName("city");
		q.setParameterValue("ggn");
		// list.add(q);
		// formData.setQueryParams(list);

		List<HttpHeader> headers = new ArrayList<>();

		HttpHeader header = new HttpHeader();
		header.setHeaderName("accept");
		header.setHeaderValue("application/json");
		headers.add(header);
		header = new HttpHeader();
		header.setHeaderName("content-type");
		header.setHeaderValue("application/json");
		headers.add(header);
		header = new HttpHeader();
		header.setHeaderName("authorization");
		header.setHeaderValue("ff9nksdw8fsdfdsfkfskj");
		headers.add(header);
		formData.setHttpHeaders(headers);
		formData.setMethod("post");
		formData.setShowRequest(true);
		formData.setShowResponse(true);
		String body = "\"test123\"";
		String body1 = "{\r\n" + "    \"suppliedIndividuals\": [\r\n" + "        {\r\n"
				+ "            \"dateOfBirth\": \"1989-04-17\",\r\n" + "            \"firstName\": \"John\",\r\n"
				+ "            \"houseNameNumber\": \"1\",\r\n" + "            \"postcode\": \"LS1 4AP\",\r\n"
				+ "            \"surname\": \"Doe\"\r\n" + "        }\r\n" + "    ]\r\n" + "}";
		String body2 = "{\r\n" + "  \"id\": 1,\r\n" + "  \"category\": {\r\n" + "    \"id\": 1,\r\n"
				+ "    \"name\": \"Gun dog\"\r\n" + "  },\r\n" + "  \"name\": \"doggie\",\r\n"
				+ "  \"photoUrls\": [\r\n" + "    \"http://photo.com\"\r\n" + "  ],\r\n" + "  \"tags\": [\r\n"
				+ "    {\r\n" + "      \"id\": 1,\r\n" + "      \"name\": \"black\"\r\n" + "    }\r\n" + "  ],\r\n"
				+ "  \"status\": \"available\"\r\n" + "}";
		formData.setBody(body2);
		Main m = new Main(formData);
		System.out.println(m.run());
	}
}
