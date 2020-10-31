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
	public int buyerAgreeableness;
	public int buyerExtraPaymentsOffer;
	public String owner = "None";
	public String catalogID = "None";
		
	public void PrintAll() {
		System.out.println(carBrand);
		System.out.println(carModel);
		System.out.println(carBodyType);
		System.out.println(carEngineType);
		System.out.println(carEngineSize);
		System.out.println(carProductionYear);
		System.out.println(carPrice);
		System.out.println(carExtraPayments);
		System.out.println(owner);
		System.out.println(catalogID);
		System.out.println("");
	}
	/*
	public boolean equals(Car obj) {
        return (this.carBrand.equals(obj.carBrand)
                && this.carModel.equals(obj.carModel) 
                && this.carBodyType.equals(obj.carBodyType) 
                && this.carEngineSize.contentEquals(obj.carEngineSize) 
                && this.carEngineType.equals(obj.carEngineType)
                && this.carProductionYear.equals(obj.carProductionYear));
    }
    */
	@Override
	public boolean equals(Object obj) {
		if ( (this.owner != "None"  &&  this.catalogID != "None" ) && ( ((Car) obj).owner != "None" && ((Car) obj).catalogID != "None") ) {
			return (this.carBrand.equals(((Car) obj).carBrand)
					&& this.carModel.equals(((Car) obj).carModel)
					&& this.carBodyType.equals(((Car) obj).carBodyType)
					&& this.carEngineType.equals(((Car) obj).carEngineType)
					&& this.carEngineSize.equals(((Car) obj).carEngineSize)
					&& this.carProductionYear.equals(((Car) obj).carProductionYear)
					&& this.owner.equals(((Car) obj).owner)
					&& this.catalogID.equals(((Car) obj).catalogID));
		}
		else {
			return (this.carBrand.equals(((Car) obj).carBrand)
					&& this.carModel.equals(((Car) obj).carModel)
					&& this.carBodyType.equals(((Car) obj).carBodyType)
					&& this.carEngineType.equals(((Car) obj).carEngineType)
					&& this.carEngineSize.equals(((Car) obj).carEngineSize)
					&& this.carProductionYear.equals(((Car) obj).carProductionYear));
		}
			
    }
	
	
}
