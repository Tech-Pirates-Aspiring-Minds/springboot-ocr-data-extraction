package com.kgisl.ocr.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asprise.ocr.Ocr;
import com.kgisl.ocr.dao.ImageDao;

@Service
@Transactional
public class ImageService {
	@Autowired
	ImageDao imageDao;

	@Autowired
	private Environment env;

	public List<?> dataExtractionOutput(Integer ocrId) {
		return imageDao.dataExtractionOutput(ocrId);
	}

	public Object listAllDataExtractionJobOutput(List<Map<String, String>> ocrCoordinates) throws IOException {

		final File folder = new File(env.getProperty("key.folder"));
		final File folder1 = new File(env.getProperty("key.folder.ocr"));
		Ocr.setUp(); // one time setup
		Ocr ocr = new Ocr(); // create a new OCR engine
		ocr.startEngine("eng", Ocr.SPEED_SLOW); // English

		List<Map<String, String>> listOfMaps = new ArrayList<Map<String, String>>();
		int f = 1;
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isFile()) {
				String fileExtension = FilenameUtils.getExtension(fileEntry.getName());
				if (fileExtension.equals("pdf")) {
					convertPdfToImage(fileEntry.getPath(), folder1.getPath());
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

						String value = ocr.recognize(new File[] { pathFile }, Ocr.RECOGNIZE_TYPE_ALL,
								Ocr.OUTPUT_FORMAT_PLAINTEXT);
						System.out.println("Result: " + value);

						extractData.put(ocr_field_name, value.trim());

						bufferedImage.flush();
						f++;
					}
					listOfMaps.add(extractData);
				}

			}

		}
		// ocr more images here ...
		ocr.stopEngine();
		return listOfMaps;

	}

	public static BufferedImage cropImage(BufferedImage bufferedImage, int x, int y, int width, int height) {
		BufferedImage croppedImage = bufferedImage.getSubimage(x, y, width, height);
		return croppedImage;
	}

	public void convertPdfToImage(String sourceDir, String destinationDir) {
		try {

			File sourceFile = new File(sourceDir);
			File destinationFile = new File(destinationDir);
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
					File outputfile = new File(destinationDir + "/" + fileName + "_" + pageNumber + ".png");
					System.out.println("Image Created -> " + outputfile.getName());
					ImageIO.write(image, "png", outputfile);
					pageNumber++;
				}
				document.close();
				System.out.println("Converted Images are saved at -> " + destinationFile.getAbsolutePath());
			} else {
				System.err.println(sourceFile.getName() + " File not exists");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveOcrCoordinates(String templateName, Object obj) {
		// TODO Auto-generated method stub
		try {
			imageDao.saveOcrCoordinates(templateName, obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
