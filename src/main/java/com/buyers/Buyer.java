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
	
	public List<Car> myCars = new ArrayList<Car>();
	public List<Car> boughtCars = new ArrayList<Car>();
	public List<Car> carOffers = new ArrayList<Car>(); // 
	public List<AID> askedSellers = new ArrayList<AID>(); // sellers already asked for car list
	private int budget = 100000;
	private Vector<AID> sellerAgents = new Vector<AID>();
	int agreedableness;
	int iter;
	final Map<String, List<Car>> sellersCars = new HashMap<String, List<Car>>();
	
	protected void setup() {
		System.out.println("Buyer-agent "+getAID().getName()+" is ready.");
		
		// INIT BUYER
		Random rd = new Random();
		agreedableness = rd.nextInt(10);
		for(iter = 0; iter < 3; iter++) {
			Car car = null;
			try {
				car = JsonLoader.GenerateCar();
				car.carExtraPayments = rd.nextInt(3000);
				myCars.add(car);
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// GET SELLERS AT INIT
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
		
		
		// ASK SELLER FOR CAR LIST
		addBehaviour(new TickerBehaviour(this, 10000) {
			protected void onTick() {
				for(AID agent: sellerAgents) {
					if(!askedSellers.contains(agent)) {
						ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
						msg.addReceiver(agent);
						msg.setContent("show offer");
						send(msg);
					}					
				}
			}
		});
		
		// UPDATE SELLERS LIST
		addBehaviour(new TickerBehaviour(this, 5000) {
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
		
		//  SENDS BUY OFFERS
		addBehaviour(new TickerBehaviour(this, 20000) {
			protected void onTick() {
				boolean first = true;
				for(Car carToBuy: myCars) {
					Car chosenOffert = null;
					if(carOffers.contains(carToBuy)) {
						for(Car offer: carOffers) {
							if(carToBuy == offer && first == true) {
								chosenOffert = offer;
								first = false;
							}
							else if(carToBuy == offer && chosenOffert.carExtraPayments > offer.carExtraPayments) {
								chosenOffert = offer;
							}
						}
					}
					if (chosenOffert != null) {
						ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
						msg.addReceiver(new AID(chosenOffert.owner,true));
						String stringOffer = "";
						try {
							stringOffer = JsonLoader.CarToString(chosenOffert);
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
						msg.setContent(stringOffer);
						myAgent.send(msg);						
					}
				}
			}
		} );
		
		
		
		
		
		// HANDLE RECEVIED MESSAGES
		addBehaviour(new CyclicBehaviour(this){
			public void action() {
				ACLMessage msg = myAgent.receive();
				if(msg != null) {
					
					// HANDLE RECEIVED CAR LIST
					if(ACLMessage.INFORM == msg.getPerformative()){
						askedSellers.add(msg.getSender());
						Car[] sellersCars = null;
						try {
							sellersCars = JsonLoader.StringToList(msg.getContent());
						} catch (JsonMappingException e) {
							e.printStackTrace();
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
						if(sellersCars != null) {
							for(Car aCar: sellersCars) {
								if(!carOffers.contains(aCar) && myCars.contains(aCar)) {
									carOffers.add(aCar);
								}
							}
						}
					}
					
					// HANDLE OFFER ACEPTANCE
					else if(ACLMessage.AGREE == msg.getPerformative()) {
						Car boughtCar = null;
						try {
							boughtCar = JsonLoader.StringToCar(msg.getContent());
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
						budget -= boughtCar.carExtraPayments + boughtCar.carPrice;
						myCars.remove(boughtCar);
						boughtCars.add(boughtCar);
						System.out.println("Bought car, current budget = " + budget);
					}
					
					else if(ACLMessage.FAILURE == msg.getPerformative()) {
						Car unavaiableCar = null;
						try {
							unavaiableCar = JsonLoader.StringToCar(msg.getContent());
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
						carOffers.remove(unavaiableCar);
					}
					
					
					System.out.println("buyer dosta³ wiadomoœæ " + msg.getPerformative());
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
