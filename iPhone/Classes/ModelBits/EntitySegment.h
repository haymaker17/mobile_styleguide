//
//  EntitySegment.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 02/10/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityBooking, EntityFlightStats, EntitySegmentLocation, EntityTrip;

@interface EntitySegment : NSManagedObject

@property (nonatomic, retain) NSString * descriptionSeg;
@property (nonatomic, retain) NSString * meals;
@property (nonatomic, retain) NSString * operatedByFlightNumber;
@property (nonatomic, retain) NSString * statusLocalized;
@property (nonatomic, retain) NSString * eReceiptStatus;
@property (nonatomic, retain) NSNumber * checkedBaggage;
@property (nonatomic, retain) NSString * segmentDescription;
@property (nonatomic, retain) NSString * bodyType;
@property (nonatomic, retain) NSString * wagonNumber;
@property (nonatomic, retain) NSString * cabin;
@property (nonatomic, retain) NSString * flightNumber;
@property (nonatomic, retain) NSString * parkingLocationId;
@property (nonatomic, retain) NSString * cancellationPolicy;
@property (nonatomic, retain) NSString * eTicket;
@property (nonatomic, retain) NSString * operatedByVendor;
@property (nonatomic, retain) NSString * cliqbookId;
@property (nonatomic, retain) NSNumber * numStops;
@property (nonatomic, retain) NSString * currency;
@property (nonatomic, retain) NSString * amenities;
@property (nonatomic, retain) NSNumber * numRooms;
@property (nonatomic, retain) NSString * status;
@property (nonatomic, retain) NSString * airCond;
@property (nonatomic, retain) NSString * pin;
@property (nonatomic, retain) NSString * trainTypeCode;
@property (nonatomic, retain) NSString * dropoffInstructions;
@property (nonatomic, retain) NSString * type;
@property (nonatomic, retain) NSString * phoneNumber;
@property (nonatomic, retain) NSNumber * miles;
@property (nonatomic, retain) NSString * bodyTypeName;
@property (nonatomic, retain) NSString * operatedByTrainNumber;
@property (nonatomic, retain) NSString * specialEquipment;
@property (nonatomic, retain) NSString * bookSource;
@property (nonatomic, retain) NSString * idKey;
@property (nonatomic, retain) NSString * imageVendorURI;
@property (nonatomic, retain) NSString * aircraftCode;
@property (nonatomic, retain) NSString * aircraftName;
@property (nonatomic, retain) NSString * vendorName;
@property (nonatomic, retain) NSNumber * numPersons;
@property (nonatomic, retain) NSString * transmission;
@property (nonatomic, retain) NSNumber * numCars;
@property (nonatomic, retain) NSString * discountCode;
@property (nonatomic, retain) NSString * trainNumber;
@property (nonatomic, retain) NSString * confirmationNumber;
@property (nonatomic, retain) NSNumber * isExpensed;
@property (nonatomic, retain) NSString * imageCarURI;
@property (nonatomic, retain) NSString * classOfServiceLocalized;
@property (nonatomic, retain) NSString * classOfCar;
@property (nonatomic, retain) NSNumber * dailyRate;
@property (nonatomic, retain) NSString * hotelName;
@property (nonatomic, retain) NSNumber * sentToAirPlus;
@property (nonatomic, retain) NSString * parkingName;
@property (nonatomic, retain) NSString * gdsId;
@property (nonatomic, retain) NSString * bookingKey;
@property (nonatomic, retain) NSNumber * duration;
@property (nonatomic, retain) NSString * roomDescription;
@property (nonatomic, retain) NSString * vendor;
@property (nonatomic, retain) NSString * classOfCarLocalized;
@property (nonatomic, retain) NSString * classOfService;
@property (nonatomic, retain) NSString * operatedBy;
@property (nonatomic, retain) NSString * seatNumber;
@property (nonatomic, retain) NSString * legId;
@property (nonatomic, retain) NSString * reservationId;
@property (nonatomic, retain) NSString * meetingInstructions;
@property (nonatomic, retain) NSNumber * totalRate;
@property (nonatomic, retain) NSString * propertyId;
@property (nonatomic, retain) NSString * rateDescription;
@property (nonatomic, retain) NSString * fareBasisCode;
@property (nonatomic, retain) NSString * rateType;
@property (nonatomic, retain) NSString * specialInstructions;
@property (nonatomic, retain) NSString * segmentName;
@property (nonatomic, retain) NSString * pickupInstructions;
@property (nonatomic, retain) NSString * frequentTravelerId;
@property (nonatomic, retain) NSString * travelPointsPosted;
@property (nonatomic, retain) NSString * travelPointsPending;
@property (nonatomic, retain) NSString * travelPointsBenchmark;
@property (nonatomic, retain) NSString * travelPointsBenchmarkCurrency;
@property (nonatomic, retain) EntityTrip *relTrip;
@property (nonatomic, retain) EntitySegmentLocation *relStartLocation;
@property (nonatomic, retain) EntityBooking *relBooking;
@property (nonatomic, retain) EntitySegmentLocation *relEndLocation;
@property (nonatomic, retain) EntityFlightStats *relFlightStats;

@end
