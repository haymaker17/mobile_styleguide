//
//  SampleData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/20/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import "SampleData.h"


@implementation SampleData

-(void) initTrips
{
	self.trip1 = @{@"Name": @"Seattle to Boston", @"Start": @"Thu Nov 12", @"End": @"Mon Nov 16", @"Locator": @"JNZVXA", @"TripRecloc": @"A9ZX56", @"AirRecloc": @"QZ845736, QD866055, RW175774", @"CarConfirm": @"AV73037", @"HotelConfirm": @"MRT3309"};
	self.trip2 = @{@"Name": @"Seattle to Orlando", @"Start": @"Thu Nov 26", @"End": @"Mon Nov 30", @"Locator": @"PZLET", @"TripRecloc": @"T5YU67", @"AirRecloc": @"GG445331", @"CarConfirm": @"AV34536", @"HotelConfirm": @"MRT3309"};
    self.trip3 = @{@"Name": @"Seattle to Frankfurt to Bangalore", @"Start": @"Sun Dec 13", @"End": @"Tue Dec 29", @"Locator": @"KSMURF", @"TripRecloc": @"FR54345", @"AirRecloc": @"QZ463646", @"CarConfirm": @"AV73037", @"HotelConfirm": @"MRT3309"};
    
    self.trips = @[self.trip1, self.trip2, self.trip3];
	
	self.tripKeys = [[NSMutableArray alloc] initWithObjects:@"A", @"B", @"C", nil];

	//Trip Details
	 self.flight1 = @{@"Header": @"", @"ImageHeader": @"24 flights.png", @"Section": @"Departing",
							 @"Label1": @"6:33 AM PST", @"Label2": @"SeaTac International Airport",
							 @"Vendor": @"United", @"VendorDetail": @"512", @"VendorImage": @"united.png"};
	 self.flight2 = @{@"Header": @"", @"ImageHeader": @"24 flights.png", @"Section": @"Connecting",
							 @"Label1": @"2:30 PM CST", @"Label2": @"Minot Airport",
							 @"Vendor": @"United", @"VendorDetail": @"261", @"VendorImage": @"united.png"};
	 self.flight3 = @{@"Header": @"", @"ImageHeader": @"24 flights.png", @"Section": @"Departing",
							 @"Label1": @"6:00 AM EST", @"Label2": @"Logan International Airport",
							 @"Vendor": @"United", @"VendorDetail": @"640", @"VendorImage": @"united.png"};
	
	 self.car1 = @{@"Header": @"", @"ImageHeader": @"24 rental-car.png", @"Section": @"Pick-Up",
						  @"Label1": @"7:00 PM EST", @"Label2": @"Logan International Airport",
						  @"Vendor": @"Avis", @"VendorDetail": @"Action:Call", @"VendorImage": @"avis.png", @"VendorAction": @"Call"};
	 self.car2 = @{@"Header": @"", @"ImageHeader": @"24 rental-car.png", @"Section": @"Return By",
						  @"Label1": @"7:00 PM EST", @"Label2": @"Logan International Airport",
						  @"Vendor": @"Avis", @"VendorDetail": @"Action:Call", @"VendorImage": @"avis.png", @"VendorAction": @"Call"};
	
	 self.hotel1 = @{@"Header": @"", @"ImageHeader": @"24 hotel.png", @"Section": @"Check In",
							@"Label1": @"Thu Nov 12, 2009", @"Label2": @"1234 Stay St, Watertown, MA",
							@"Vendor": @"Marriot", @"VendorDetail": @"Action:Direction", @"VendorImage": @"marriott.png", @"VendorAction": @"Dir"};
	 self.hotel2 = @{@"Header": @"", @"ImageHeader": @"24 hotel.png", @"Section": @"Check Out",
							@"Label1": @"Sun Nov 15, 2009", @"Label2": @"1234 Stay St, Watertown, MA",
							@"Vendor": @"Marriot", @"VendorDetail": @"Action:Direction", @"VendorImage": @"marriott.png", @"VendorAction": @"Dir"};
	
	 self.itinDetails1 = @{@"Name": @"", @"Section1": @"Pick-Up", @"label1Section1": @"4:00 PM EST, Thu Nov 12, 2009", @"label2Section1": @"Logan International Airport"
						  , @"Section2": @"Return", @"label1Section2": @"3:29 PM EST, Mon Nov 16, 2009", @"label2Section2": @"Logan International Airport"
						  , @"Section3": @"Rate and Miles", @"label1Section3": @"Daily Rate $105.99 Unlimited Miles", @"label2Section3": @"Extra Days $105.99 Unlimited Miles"
						  , @"label3Section3": @"Extra Hours $79.50 Unlimited Miles"
						  , @"Section4": @"Status", @"label1Section4": @"Confirmed"};
	
	 self.itinDetailsAir = @{@"Name": @"", @"FlightName": @"Seattle To Boston", @"FlightNum": @"107", @"ServiceClass": @"Family Class"
					, @"Departure": @"Tue Jan 23rd 10:00 am PST", @"Arrival": @"Tue Jan 23rd 4:00 pm EST", @"FlightType": @"NOS/I"
					, @"PlaneType": @"Airbus 380", @"Restrictions": @"Credited web fare", @"CarrierDetails": @"Booking Information"
					, @"CarrierURL": @"http://www.southwest.com/flight/select-flight.html?companyName=&cid=&memberName=&disc=0%3A8%3A1260570090.269000%3A5889%4067815EE06B4B49EF1D387B80C7FC39E2C4378B15&ss=0&dest="};

}

//-(void) getLogs:(NSArray *)array
-(void) initLogs
{
	self.logArray = @[@"10.20.2009 3:55 pm PST", @"10.20.2009 4:02 pm PST",
                      @"10.20.2009 4:42 pm PST", @"10.31.2009 6:00 am PST", @"10.31.2009 6:20 am PST", @"10.31.2009 6:45 am PST", 
					  @"10.31.2009 7:00 am PST", @"10.31.2009 7:01 am PST",
                      @"10.31.2009 7:05 am PST", @"10.31.2009 8:40 am PST", @"10.31.2009 9:13 am PST", @"10.31.2009 10:00 am PST"];
}


@end
