package com.codebind;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonLoader {

	private ObjectMapper objectMapper;
	
	
	public static Car GenerateCar() throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
		Car[] cars = objectMapper.readValue(new File("src/main/resources/cars.json"), Car[].class);
		Random rd = new Random();
		Car car = cars[rd.nextInt(cars.length)];
		return car;
	}
	
	public static String CarToString(Car aCar) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		String car = objectMapper.writeValueAsString(aCar);
		return car;
	}
		
}
