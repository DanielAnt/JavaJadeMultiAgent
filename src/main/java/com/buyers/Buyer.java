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
	int agreeableness;
	int iter;
	final Map<String, List<Car>> sellersCars = new HashMap<String, List<Car>>();
	
	protected void setup() {
		System.out.println("Buyer-agent "+getAID().getName()+" is ready.");
		
		// INIT BUYER
		Random rd = new Random();
		agreeableness = rd.nextInt(20);
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
				if(myCars.isEmpty()) {
					System.out.println(myAgent.getName() + " have bought all wanted cars");
					for(Car aCar: boughtCars) {
						aCar.PrintAll();
					}
					myAgent.doDelete();
				}
				else {
					boolean first = true;
					for(Car carToBuy: myCars) {
						Car chosenOffert = null;
						first = true;
						if(carOffers.contains(carToBuy)) {
							for(Car offer: carOffers) {
								if(carToBuy.equals(offer) && first == true) {
									chosenOffert = offer;
									first = false;
								}
								else if(carToBuy.equals(offer) && first == false) {
									if(chosenOffert.carExtraPayments > offer.carExtraPayments) {
										chosenOffert = offer;
									}
									
								}
							}
						}
						if (chosenOffert != null) {
							if(carToBuy.carPrice + carToBuy.carExtraPayments < budget || chosenOffert.carPrice + chosenOffert.carExtraPayments < budget) {
								
								if(chosenOffert.carExtraPayments < carToBuy.carExtraPayments) {
									//System.out.println("test1");
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
								else {
									//System.out.println("test2");
									chosenOffert.buyerAgreeableness = agreeableness;
									chosenOffert.buyerExtraPaymentsOffer = carToBuy.carExtraPayments;
									ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
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
							else {
								System.out.println(myAgent.getName() + " DOSNT HAVE ENOUGH MONEY FOR:");
								chosenOffert.PrintAll();
							}
													
						}
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
						//System.out.println(myAgent.getName() + " have bought a car:");
						//boughtCar.PrintAll();
						//System.out.println("Current budget = " + budget);
					}
					
					// HANDLE OFFER TERMINATED
					else if(ACLMessage.DISCONFIRM == msg.getPerformative()) {
						System.out.println(myAgent.getName() + " COULDNT GET A CAR BECASUE IT WAS BOUGHT BEFORE");
						Car unavaiableCar = null;
						try {
							unavaiableCar = JsonLoader.StringToCar(msg.getContent());
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
						carOffers.remove(unavaiableCar);
					}
					
					// HANDLE BARGAINING OFFER
					else if(ACLMessage.QUERY_IF == msg.getPerformative()) {
						Car carOffer = null;
						if(msg.getContent() != "") {
							try {
								carOffer = JsonLoader.StringToCar(msg.getContent());
							} catch (JsonProcessingException e) {
								e.printStackTrace();
							}
							if(carOffer.carPrice + carOffer.carExtraPayments < budget) {
								budget -= carOffer.carExtraPayments + carOffer.carPrice;
								myCars.remove(carOffer);
								boughtCars.add(carOffer);
								//System.out.println(myAgent.getName() + " have bought a car after a bargaining:");
								//carOffer.PrintAll();
								//System.out.println("Current budget = " + budget);
								ACLMessage reply = msg.createReply();
								reply.setPerformative(ACLMessage.AGREE);
								reply.setContent(msg.getContent());
								myAgent.send(reply);
							}
							else {
								ACLMessage reply = msg.createReply();
								reply.setPerformative(ACLMessage.REFUSE);
								reply.setContent(msg.getContent());
								myAgent.send(reply);
							}
							
							
						}
					}
					//System.out.println("buyer dosta³ wiadomoœæ " + msg.getPerformative());
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
