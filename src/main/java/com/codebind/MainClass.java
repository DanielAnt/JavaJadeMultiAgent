package com.codebind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
		// Initiates seller every 60 sec, up to 8
		RunnableSeller T1 = new RunnableSeller("Thread-1", containerController, 8, 60000);
		T1.start();
		CreateBuyers(1);
	}
		
	public static void CreateBuyers(int num){
		for(int i = 1 ; i < num + 1 ; i++){
			AgentController Seller;
	           try {
	        	   Seller = containerController.createNewAgent("Buyer"+i, "com.buyers.Buyer", null);
	        	   Seller.start();    
	           } catch (StaleProxyException e) {
	               e.printStackTrace();
	           }
	       }

		
	  }
}
