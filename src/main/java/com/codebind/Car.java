package com.codebind;




public class Car {
	public String carBrand;
	public String carModel;
	public String carBodyType;
	public String carEngineType;
	public String carEngineSize;
	public String carProductionYear;
	public int carPrice;
	public int carExtraPayments;
		
	public void PrintAll() {
		System.out.println(carBrand);
		System.out.println(carModel);
		System.out.println(carBodyType);
		System.out.println(carEngineType);
		System.out.println(carEngineSize);
		System.out.println(carProductionYear);
		System.out.println(carPrice);
		System.out.println(carExtraPayments);
	}
}
