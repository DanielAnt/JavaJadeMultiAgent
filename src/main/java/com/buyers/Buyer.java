package com.buyers;

import java.io.IOException;
import java.util.*;

import com.codebind.Car;
import com.codebind.JsonLoader;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class Buyer extends Agent {
	
	public List<Car> Cars = new ArrayList<Car>();
	private int budget = 100000;
	private Vector<AID> sellerAgents = new Vector<AID>();
	int agreedableness;
	int iter;
	
	protected void setup() {
		System.out.println("Buyer-agent "+getAID().getName()+" is ready.");
		
		// Buyers characteristics
		Random rd = new Random();
		agreedableness = rd.nextInt(10);
		for(iter = 0; iter < 15; iter++) {
			Car car = null;
			try {
				car = JsonLoader.GenerateCar();
				//car.carExtraPayments = rd.nextInt(3000);
				car.carExtraPayments = 0;
				Cars.add(car);
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
				
		
		// GET SELLERS
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Car-selling");
		template.addServices(sd);
		try {
		DFAgentDescription[] result = DFService.search(this, template);
		sellerAgents.clear();
		for (iter = 0; iter < result.length; ++iter) {
		sellerAgents.add(result[iter].getName());
		}
		}
		catch (FIPAException fe) {
		fe.printStackTrace();
		}
		
		// test message
		addBehaviour(new TickerBehaviour(this, 10000) {
			protected void onTick() {
				for(AID agent: sellerAgents) {
					ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
					msg.addReceiver(agent);
					msg.setContent("show offer");
					send(msg);
				}
			}
		});
		
		//Updates sellers list
		addBehaviour(new TickerBehaviour(this, 100) {
			protected void onTick() {
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("Car-selling");
			template.addServices(sd);
			try {
			DFAgentDescription[] result = DFService.search(myAgent, template);
			sellerAgents.clear();
			for (iter = 0; iter < result.length; iter++) {
			sellerAgents.add(result[iter].getName());
			}
			}
			catch (FIPAException fe) {
			fe.printStackTrace();
			}
			}
			} );
		
		
		
		// message receive
		addBehaviour(new CyclicBehaviour(this){
			public void action() {
				ACLMessage msg = myAgent.receive();
				if(msg != null) {
					Car[] testCars = null;
					try {
						testCars = JsonLoader.StringToList(msg.getContent());
					} catch (JsonMappingException e) {
						e.printStackTrace();
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
					if(testCars != null) {
						for(Car aCar: testCars) {
							if(Cars.contains(aCar)) {
								System.out.println("JEST");
							}
						}
					}
				}
				else {
					block();
				}
			}
		});
		
		/*
		// LIST SELLERS
		addBehaviour(new TickerBehaviour(this, 10000) {
			protected void onTick() {
				for(Object o: sellerAgents) {
					System.out.println(o.toString());
				}
			}
		} );
		*/
		
	}
}
