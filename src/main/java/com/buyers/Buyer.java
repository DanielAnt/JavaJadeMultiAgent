package com.buyers;

import java.util.Vector;
import java.util.*; 
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Buyer extends Agent {
	
	private int budget = 100000;
	private Vector sellerAgents = new Vector();
	protected void setup() {
		System.out.println("Buyer-agent "+getAID().getName()+" is ready.");
		// GET SELLERS
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Car-selling");
		template.addServices(sd);
		try {
		DFAgentDescription[] result = DFService.search(this, template);
		sellerAgents.clear();
		for (int i = 0; i < result.length; ++i) {
		sellerAgents.addElement(result[i].getName());
		}
		}
		catch (FIPAException fe) {
		fe.printStackTrace();
		}
		
		
		//Updates sellers list
		addBehaviour(new TickerBehaviour(this, 60000) {
			protected void onTick() {
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("Car-selling");
			template.addServices(sd);
			try {
			DFAgentDescription[] result = DFService.search(myAgent, template);
			sellerAgents.clear();
			for (int i = 0; i < result.length; ++i) {
			sellerAgents.addElement(result[i].getName());
			}
			}
			catch (FIPAException fe) {
			fe.printStackTrace();
			}
			}
			} );
		
		
		// LIST SELLERS
		addBehaviour(new TickerBehaviour(this, 10000) {
			protected void onTick() {
			for(Object o: sellerAgents) {
				System.out.println(o.toString());
			}
			}
			} );
		
		
		
			
		}
		
		
	}

