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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
public class Seller extends Agent {
	
	public List<Car> Cars = new ArrayList<Car>();
	public List<Car> lockedTrades = new ArrayList<Car>();
	int agreeableness;
	int iter;
	int timeDiscount = 0;
	int earnings = 0;
	
	protected void setup() {
		
		// Sellers characteristics, generates cars
		Random rd = new Random();
		agreeableness = rd.nextInt(20);
		for(iter = 0; iter < 8; iter++) {
			Car car = null;
			try {
				car = JsonLoader.GenerateCar();
				car.carExtraPayments = 2000 + rd.nextInt(3000);
				car.owner = this.getName();
				car.catalogID = String.format("%d", iter);
				Cars.add(car);
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
			
		System.out.println("Seller-agent "+getAID().getName()+" is ready.");
		
		
		//Adds itself to car-selling service
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
		
		//checks for new messages
		addBehaviour(new CyclicBehaviour(this){
			public void action() {
				ACLMessage msg = myAgent.receive();
				if(msg != null) {
					
					// handles requests for it cars 
					if(ACLMessage.REQUEST == msg.getPerformative()){
						ACLMessage reply = msg.createReply();
						if("show offer".equals(msg.getContent())) {
							reply.setPerformative(ACLMessage.INFORM);
							String carString = "";
							try {
								carString = JsonLoader.CarListToString(Cars);
							} catch (JsonProcessingException e) {
								e.printStackTrace();
							}
							reply.setContent(carString);
							myAgent.send(reply);
						}
					}
					
					// handles buy offers
					else if(ACLMessage.PROPOSE == msg.getPerformative()) {
						if(msg.getContent() != "") {
							Car carPropose =  null;
							try {
								carPropose = JsonLoader.StringToCar(msg.getContent());
							} catch (JsonProcessingException e) {
								e.printStackTrace();
							}
							ACLMessage reply = msg.createReply();
							if(Cars.contains(carPropose)) {
								if(!lockedTrades.contains(carPropose)) {
									Cars.remove(carPropose);
									earnings += carPropose.carPrice + carPropose.carExtraPayments;
									reply.setPerformative(ACLMessage.AGREE);
									reply.setContent(msg.getContent());
									myAgent.send(reply);
								}
							}
							else {
								reply.setPerformative(ACLMessage.DISCONFIRM);
								reply.setContent(msg.getContent());
								myAgent.send(reply);
							}
						}
					}
					
					// BARGAINING
					else if(ACLMessage.QUERY_IF == msg.getPerformative()) {
						if(msg.getContent() != "") {
							Car carPropose =  null;
							try {
								carPropose = JsonLoader.StringToCar(msg.getContent());
							} catch (JsonProcessingException e) {
								e.printStackTrace();
							}
							
							int sellerMinimum = carPropose.carExtraPayments * (100 - agreeableness - timeDiscount)/100;
							int buyerMaximum = carPropose.buyerExtraPaymentsOffer * (100 + carPropose.buyerAgreeableness)/100;
							//System.out.println("BARGING");
							//System.out.println(sellerMinimum);
							//System.out.println(buyerMaximum);
							if(sellerMinimum < buyerMaximum) {
								if(agreeableness > carPropose.buyerAgreeableness) {
									carPropose.carExtraPayments = buyerMaximum;
								}
								else {
									carPropose.carExtraPayments = sellerMinimum;
								}
								ACLMessage reply = msg.createReply();
								String stringCar = "";
								try {
									stringCar = JsonLoader.CarToString(carPropose);
								} catch (JsonProcessingException e) {
									e.printStackTrace();
								}
								if(Cars.contains(carPropose)) {
									if(!lockedTrades.contains(carPropose)) {
										lockedTrades.add(carPropose);
										//System.out.println(myAgent.getName() + " locksDeal");
										reply.setPerformative(ACLMessage.QUERY_IF);
										reply.setContent(stringCar);
										myAgent.send(reply);
									}
									else {
										System.out.print("something is locked");
									}
								}
								else {
									reply.setPerformative(ACLMessage.DISCONFIRM);
									reply.setContent(msg.getContent());
									myAgent.send(reply);
								}
							}
						}
					}
					
					// HANDLE BARGAINING ACEPTANCE
					else if(ACLMessage.AGREE == msg.getPerformative()) {
						Car carPropose =  null;
						try {
							carPropose = JsonLoader.StringToCar(msg.getContent());
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
						if( carPropose != null) {
							earnings += carPropose.carPrice + carPropose.carExtraPayments;
							System.out.println(myAgent.getName() + " REMOVED A CAR FROM LOCKED LIST ");
							Cars.remove(carPropose);
							lockedTrades.remove(carPropose);							
						}
						
					}
					
					// HANDLE BARGAINING REFUSAL
					else if(ACLMessage.REFUSE == msg.getPerformative()) {
						Car carPropose =  null;
						try {
							carPropose = JsonLoader.StringToCar(msg.getContent());
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
						if( carPropose != null) {
							System.out.println(myAgent.getName() + " REMOVED A CAR FROM LOCKED LIST ");
							lockedTrades.remove(carPropose);							
						}
						
					}
					//System.out.println(getAID().getName() + " dosta³em wiadomoœæ " + msg.getPerformative());
				}
				else {
					block();
				}
			}
		});
		
		addBehaviour(new TickerBehaviour(this, 3000) {
			protected void onTick() {
				timeDiscount++;
			}
			} );
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
