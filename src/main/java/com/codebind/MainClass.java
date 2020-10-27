package com.codebind;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sellers.RunnableSeller;
import com.sellers.Seller;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class MainClass {
	
	private static ContainerController containerController;
	
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException{
		jade.core.Runtime runtime = jade.core.Runtime.instance();
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		profile.setParameter(Profile.GUI, "true");
		containerController = runtime.createMainContainer(profile);
		RunnableSeller T1 = new RunnableSeller("Thread-1", containerController, 3);
		T1.start();
	}
		
	public static void CreateSellers(int num){
		for(int i=1; i<num; i++){
			AgentController Seller;
	           try {
	        	   Seller = containerController.createNewAgent("Seller"+i, "com.sellers.Seller", null);
	        	   Seller.start();    
	           } catch (StaleProxyException e) {
	               e.printStackTrace();
	           }
	       }

		
	  }
}
