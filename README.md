# springboot-ocr-data-extraction
OCR Processing using Asprise Engine

Maven Command to execute the project
mvn clean install

Prerequisites

    JDK
    Maven
    MYSQL

How to Run

Step 1: Clone the project and execute mysql.sql file in your local mysql server to start the application

Step 2: Edit the application.properties(src/main/resource) file.

Step 3: Run SpringBootApplication using main method com.techforwarder.bot.simplebot.SimplebotApplication or mvn spring-boot:run

Step 4: Access the URL http://localhost:8080/login

Step 5: Create OCR Templates and place the sample images in the windows folder. Folder can be modified under ocr.properties file.

Step 6: http://localhost:8080/api/ocr/process/{templateId} to process data extraction from the placed images.
