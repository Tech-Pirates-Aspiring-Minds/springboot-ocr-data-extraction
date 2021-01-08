package com.kgisl.ocr.web;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.kgisl.ocr.model.DocumentStorageProperty;
import com.kgisl.ocr.model.ResponseMessage;
import com.kgisl.ocr.service.OcrService;

@RestController
@RequestMapping("/api/ocr/")
public class OcrController {
	@Autowired
	private OcrService imageService;
	@Autowired
	private Environment env;
	private final Path docStorageLocation;

	@Autowired
	public OcrController(DocumentStorageProperty docStorageProperty) throws IOException {
		// TODO Auto-generated constructor stub
		this.docStorageLocation = Paths.get(docStorageProperty.getUploadDirectory()).toAbsolutePath().normalize();
		Files.createDirectories(this.docStorageLocation);
	}

	/**
	 * The processFiles method Rest API implements an application that gives JSON
	 * response as output using Asprise OCR Engine.
	 *
	 * @author Kiruba Shankar
	 * @version 1.0
	 */
	@GetMapping("/process/{ocrId}")
	public Object processFiles(@PathVariable("ocrId") Integer ocrId) throws Exception {

		@SuppressWarnings("rawtypes")
		List result = imageService.dataExtractionOutput(ocrId);
		Object dataExtractionResults = null;
		try {
			if (!result.isEmpty()) {
				dataExtractionResults = imageService.listAllDataExtractionJobOutput(result);
				return dataExtractionResults;
			} else {
				String message = "Invalid Template ID found in the API request";
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
			}
		} catch (Exception e) {
			e.printStackTrace();
			String message = e.getMessage();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message));
		}
	}

	/**
	 * The save method Rest API implements an application that creates an ocr
	 * templates in the MYSQL db.
	 *
	 * @author Kiruba Shankar
	 * @version 1.0
	 */
	@PostMapping("/save/{templateName}")
	public String save(@PathVariable("templateName") String templateName, @RequestBody Object obj) throws Exception {
		String response = null;

		try {
			imageService.saveOcrCoordinates(templateName, obj);
			response = "success";
		} catch (Exception e) {
			e.printStackTrace();
			response = e.getMessage();
		}
		return response;
	}

	/**
	 * The uploadFile method Rest API implements an application that converts pdf to
	 * Image templates using PDFBOX library.
	 *
	 * @author Kiruba Shankar
	 * @version 1.0
	 */
	@PostMapping("/upload")
	public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {
		String message = "";
		final File folder1 = new File(env.getProperty("key.folder.ocr"));
		try {
			Path filepath = Paths.get(folder1.toString(), file.getOriginalFilename());
			Path targetLocation = this.docStorageLocation;

			try (OutputStream os = Files.newOutputStream(filepath)) {
				os.write(file.getBytes());
				os.close();
			}
			imageService.convertPdfToImage(filepath.toString(), targetLocation.toString());
			String encodedString = Base64.getEncoder()
					.encodeToString(FileUtils.readFileToByteArray(new File(targetLocation.toString() + "/"
							+ FilenameUtils.removeExtension(file.getOriginalFilename()) + "_1.png")));

			message = "Uploaded the file successfully: " + file.getOriginalFilename();
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(encodedString));
		} catch (Exception e) {
			message = "Could not upload the file: " + file.getOriginalFilename() + "!";
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
		}

	}
}
