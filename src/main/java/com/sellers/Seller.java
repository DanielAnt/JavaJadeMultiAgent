package com.sellers;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.codebind.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
public class Seller extends Agent {
	
	public List<Car> Cars = new ArrayList<Car>();
	
	protected void setup() {
		
		System.out.println("Seller-agent "+getAID().getName()+" is ready.");
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Car-selling");
		sd.setName(getLocalName()+"-Car-selling");
		dfd.addServices(sd);
		try {
		DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
		fe.printStackTrace();
		}
		
		
		for(int i = 0; i < 8; i++) {
			Car car = null;
			try {
				car = JsonLoader.GenerateCar();
				Random rd = new Random();
				car.carExtraPayments = rd.nextInt(4000);
				Cars.add(car);
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    /*
		addBehaviour(new TickerBehaviour(this, 10000) {
			protected void onTick() {
				System.out.println(this.toString());
			for(Car o: Cars) {
				System.out.println(o.carBrand);
			}
			}
			} );
		*/
				
			
		
			
		}
		
		
	}

}
