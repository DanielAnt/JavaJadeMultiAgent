package com.sellers;

import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class RunnableSeller extends  Thread  { 

	private Thread t;
	private String threadName;
	private ContainerController containerController;
	private int agentsNumber;
	private int sleepTime;
	
	public RunnableSeller( String name, ContainerController aContainerController, int aAgentsNumber, int aSleepTime) {
		threadName = name;
		containerController = aContainerController;
		agentsNumber = aAgentsNumber;
		sleepTime = aSleepTime;
		System.out.println("Seller spawner started");
	}
	   
   public void run() {
     try {
         for(int i = 0; i < agentsNumber; i++) {
        	 System.out.println("Seller" + i + " has joined");
        	 AgentController Seller;
        	 try {
        		 Seller = containerController.createNewAgent("Seller"+i, "com.sellers.Seller", null);
        		 Seller.start();
        		 }
        	 catch (StaleProxyException e) {
        		 e.printStackTrace();
        		 }
        	 Thread.sleep(sleepTime);
        	 }
         } catch (InterruptedException e) {
        	 System.out.println("Thread " +  threadName + " interrupted.");
        	 }
     System.out.println("All sellers are active");
     }
   
   public void start () {
	      System.out.println("Starting " +  threadName );
	      if (t == null) {
	         t = new Thread (this, threadName);
	         t.start ();
	      }
	   }
}

	
	

