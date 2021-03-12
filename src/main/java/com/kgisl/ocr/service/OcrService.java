package com.kgisl.ocr.service;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asprise.ocr.Ocr;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kgisl.ocr.dao.OcrDao;

@Service
@Transactional
public class OcrService {
	@Autowired
	OcrDao ocrDao;

	@Autowired
	private Environment env;

	public List<?> dataExtractionOutput(Integer ocrId) {
		return ocrDao.dataExtractionOutput(ocrId);
	}

	static String url = "https://api.ocr.space/parse/image"; // OCR API Endpoints

	private static boolean isOverlayRequired = true;

	public Object listAllDataExtractionJobOutput(List<Map<String, String>> ocrCoordinates) throws IOException, JSONException {

		final File folder = new File(env.getProperty("key.folder"));
		final File folder1 = new File(env.getProperty("key.folder.ocr"));
		
		/**
		 * Use below code if Asprise OCR is required
		 * Ocr.setUp();
		 * Ocr ocr = new Ocr();
		 * ocr.startEngine("eng", Ocr.SPEED_SLOW);
		 */
		List<Map<String, String>> listOfMaps = new ArrayList<Map<String, String>>();
		int f = 1;
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isFile()) {
				String fileExtension = FilenameUtils.getExtension(fileEntry.getName());
				if (fileExtension.equals("pdf")) {
					String filename = convertPdfToImage(fileEntry.getPath(), folder1.getPath());

					HashMap<String, String> extractData = new HashMap<String, String>();
					extractData.put("fileName", fileEntry.getName());
					for (Map<String, String> entry : ocrCoordinates) {

						String ocr_field_name = entry.get("ocr_field_name");

						String ocr_field_coordinates = entry.get("ocr_field_coordinates");

						System.out.println(filename.split("/")[1]);
						File imageFile = new File(filename);
						BufferedImage bufferedImage = ImageIO.read(imageFile);
						bufferedImage = cropImage(bufferedImage, Integer.parseInt(ocr_field_coordinates.split(",")[0]),
								Integer.parseInt(ocr_field_coordinates.split(",")[1]),
								Integer.parseInt(ocr_field_coordinates.split(",")[2]),
								Integer.parseInt(ocr_field_coordinates.split(",")[3]));

						File pathFile = new File(folder1 + "\\" + f + "_" + filename.split("/")[1]);
						ImageIO.write(bufferedImage, "jpg", pathFile);

						/**
						 * Use below code if Asprise OCR is required
						 * String value = ocr.recognize(new File[] { pathFile }, Ocr.RECOGNIZE_TYPE_ALL, Ocr.OUTPUT_FORMAT_PLAINTEXT);
						 * System.out.println("Result: " + value); 
						 */
						
						//OCR SPACE API
						String ocrspaceresponse=ocrSpaceAPI(pathFile.toString());
						extractData.put(ocr_field_name, ocrspaceresponse.trim());
						System.out.println("Result: " + ocrspaceresponse.trim());

						bufferedImage.flush();
						f++;
					}
					listOfMaps.add(extractData);

				} else {
					HashMap<String, String> extractData = new HashMap<String, String>();
					extractData.put("fileName", fileEntry.getName());
					for (Map<String, String> entry : ocrCoordinates) {

						String ocr_field_name = entry.get("ocr_field_name");

						String ocr_field_coordinates = entry.get("ocr_field_coordinates");

						System.out.println(fileEntry.getName());
						File imageFile = new File(fileEntry.getPath());
						BufferedImage bufferedImage = ImageIO.read(imageFile);
						bufferedImage = cropImage(bufferedImage, Integer.parseInt(ocr_field_coordinates.split(",")[0]),
								Integer.parseInt(ocr_field_coordinates.split(",")[1]),
								Integer.parseInt(ocr_field_coordinates.split(",")[2]),
								Integer.parseInt(ocr_field_coordinates.split(",")[3]));

						File pathFile = new File(folder1 + "\\" + f + "_" + fileEntry.getName());
						ImageIO.write(bufferedImage, "jpg", pathFile);

//						String value = ocr.recognize(new File[] { pathFile }, Ocr.RECOGNIZE_TYPE_ALL,
//								Ocr.OUTPUT_FORMAT_PLAINTEXT);
//						System.out.println("Result: " + value);
//
//						extractData.put(ocr_field_name, value.trim());
						
						//OCR SPACE API
						String ocrspaceresponse=ocrSpaceAPI(pathFile.toString());
						extractData.put(ocr_field_name, ocrspaceresponse.trim());
						System.out.println("Result: " + ocrspaceresponse.trim());

						bufferedImage.flush();
						f++;
					}
					listOfMaps.add(extractData);
				}

			}

		} // ocr more images here ...
		//ocr.stopEngine();
		return listOfMaps;

	}

	public static BufferedImage cropImage(BufferedImage bufferedImage, int x, int y, int width, int height) {
		BufferedImage croppedImage = bufferedImage.getSubimage(x, y, width, height);
		return croppedImage;
	}

	public String convertPdfToImage(String sourceDir, String destinationDir) {
		File sourceFile = new File(sourceDir);
		File destinationFile = new File(destinationDir);
		File outputfile = null;
		try {

			if (!destinationFile.exists()) {
				destinationFile.mkdir();
				System.out.println("Folder Created -> " + destinationFile.getAbsolutePath());
			}
			if (sourceFile.exists()) {
				System.out.println("Images copied to Folder: " + destinationFile.getName());
				PDDocument document = PDDocument.load(sourceDir);
				@SuppressWarnings("unchecked")
				List<PDPage> list = document.getDocumentCatalog().getAllPages();
				System.out.println("Total files to be converted -> " + list.size());

				String fileName = sourceFile.getName().replace(".pdf", "");
				int pageNumber = 1;
				for (PDPage page : list) {
					BufferedImage image = page.convertToImage();
					outputfile = new File(destinationDir + "/" + fileName + "_" + pageNumber + ".png");
					System.out.println("Image Created -> " + outputfile.getName());
					ImageIO.write(image, "png", outputfile);
					pageNumber++;
					break;
				}
				document.close();
				System.out.println("Converted Images are saved at -> " + destinationFile.getAbsolutePath());
			} else {
				System.err.println(sourceFile.getName() + " File not exists");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return destinationFile.getAbsolutePath() + "/" + outputfile.getName();
	}

	public void saveOcrCoordinates(String templateName, Object obj) {
		// TODO Auto-generated method stub
		try {
			ocrDao.saveOcrCoordinates(templateName, obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// OCR SPACE API
	public String ocrSpaceAPI(String imageUrl) throws JSONException, IOException {
		URL obj = new URL(url); // OCR API Endpoints
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		// add request header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		JSONObject postDataParams = new JSONObject();

		postDataParams.put("apikey", "10f769e49388957");// TODO Add your Registered API key
		postDataParams.put("isOverlayRequired", isOverlayRequired);
		byte[] fileContent = FileUtils.readFileToByteArray(new File(imageUrl));
		String encodedString = Base64.getEncoder().encodeToString(fileContent);
		postDataParams.put("Base64Image", "data:image/png;base64," + encodedString);
		postDataParams.put("language", "eng");

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(getPostDataString(postDataParams));
		wr.flush();
		wr.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {

			response.append(inputLine);
		}
		in.close();

		// return result

		// System.out.println("...................fullresponse" + response);
		JsonObject jsonObject = new JsonParser().parse(response.toString()).getAsJsonObject();
		// System.out.println("*********" + jsonObject.get("ParsedResults"));
		String res = jsonObject.get("ParsedResults").toString();
		JSONArray array = new JSONArray(res);

		JSONObject object = array.getJSONObject(0);
		// System.out.println("TextOverlay...."+object.getString("TextOverlay"));

		JsonObject jsonObject1 = new JsonParser().parse(object.getString("TextOverlay").toString()).getAsJsonObject();
		// System.out.println("*********Lines" + jsonObject1.get("Lines"));
		String res1 = jsonObject1.get("Lines").toString();
		JSONArray array1 = new JSONArray(res1);
		JSONObject object1 = array1.getJSONObject(0);
		System.out.println(object1.getString("LineText"));
		return object1.getString("LineText").toString();
	}

	private String getPostDataString(JSONObject params) throws JSONException, UnsupportedEncodingException {
		// TODO Auto-generated method stub

		StringBuilder result = new StringBuilder();
		boolean first = true;

		Iterator<String> itr = params.keys();

		while (itr.hasNext()) {

			String key = itr.next();
			Object value = params.get(key);

			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(key, "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(value.toString(), "UTF-8"));

		}
		return result.toString();
	}
}
