//
//  SegmentData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/16/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FlightStatsData.h"
#import "EntitySegment.h"
#import "EntitySegmentLocation.h"

@interface SegmentData : NSObject 
{
	NSString				*type, *vendor, *status, *startDateLocal, *endDateLocal, *startDateUtc, *endDateUtc, *statusLocalized,
							*confirmationNumber, *eReceiptStatus, *sentToAirplus, *cliqbookId, *frequentTravelerId, *flightNumber,
							*aircraftCode, *cabin, *classOfService, *fareBasisCode, *miles, *duration, *numStops, *startCityCode,
							*endCityCode, *startTerminal, *endTerminal, *startGate, *endGate, *meals, *specialInstructions, *legId,
							*checkedBaggage, *segmentName, *description, *idKey, *bookingKey, *operatedBy, *operatedByFlightNumber, 
							*startLocation, *parkingName, *parkingLocationId, *startAddress, *startCity, *startPostalCode, *startState, *startCountry, *phoneNumber, *totalRate, *currency, *pin, *dailyRate, *cancellationPolicy
							,*roomDescription, *hotelName
							,*pickupInstructions, *meetingInstructions, *endAddress, *endCity, *endState, *endPostalCode, *dropoffInstructions, *rateDescription, *vendorName
							,*startLatitude, *startLongitude, *endLatitude, *endLongitude, *reservationId, *numPersons, *startAddress2
							,*trainNumber, *operatedByVendor, *operatedByTrainNumber, *startPlatform, *startRailStation, *wagonNumber, *amenities, *endRailStation, *endPlatform, *trainTypeCode
							,*seatNumber, *startCityCodeLocalized, *endCityCodeLocalized, *classOfServiceLocalized
							,*startAirportCity, *startAirportName, *startAirportState, *startAirportCountry, *endAirportCity, *endAirportName, *endAirportState, *endAirportCountry, *startAirportCountryCode, *endAirportCountryCode
							,*rateType, *bodyType, *bodyTypeName, *transmission, *airCond, *discountCode, *specialEquipment, *classOfCar, *classOfCarLocalized, *numRooms, *imageCarURI, *imageVendorURI, *startDateUTC
							,*gdsId, *propertyId, *bookSource
							,*startRailStationLocalized, *endRailStationLocalized, *eTicket, *numCars;

	NSMutableDictionary		*seats;
	NSMutableArray			*seatKeys;
	FlightStatsData			*flightStats;
}
@property (strong, nonatomic) NSString *classOfCarLocalized;
@property (strong, nonatomic) NSString *numCars;
@property (strong, nonatomic) NSString *eTicket;
@property (strong, nonatomic) NSString *gdsId;
@property (strong, nonatomic) NSString *propertyId;
@property (strong, nonatomic) NSString *bookSource;
@property (strong, nonatomic) NSString *startDateUTC;
@property (strong, nonatomic) NSString *type;
@property (strong, nonatomic) NSString *startDateLocal;
@property (strong, nonatomic) NSString *endDateLocal;
@property (strong, nonatomic) NSString *cliqbookId;
@property (strong, nonatomic) NSString *legId;
@property (strong, nonatomic) NSString *confirmationNumber;
@property (strong, nonatomic) NSString *flightNumber;
@property (strong, nonatomic) NSString *duration;
@property (strong, nonatomic) NSString *startCityCode;
@property (strong, nonatomic) NSString *endCityCode;
@property (strong, nonatomic) NSString *startTerminal;
@property (strong, nonatomic) NSString *endTerminal;
@property (strong, nonatomic) NSString *startGate;
@property (strong, nonatomic) NSString *endGate;
@property (strong, nonatomic) NSString *vendor;
@property (strong, nonatomic) NSString *idKey;
@property (strong, nonatomic) NSString *bookingKey;
@property (strong, nonatomic) NSString *operatedBy;
@property (strong, nonatomic) NSString *operatedByFlightNumber;
@property (strong, nonatomic) NSString *status;
@property (strong, nonatomic) NSString *numStops;
@property (strong, nonatomic) NSString *aircraftCode;
@property (strong, nonatomic) NSString *classOfService;
@property (strong, nonatomic) NSString *meals;
@property (strong, nonatomic) NSString *specialInstructions;

@property (strong, nonatomic) NSString *startLocation;
@property (strong, nonatomic) NSString *segmentName;
@property (strong, nonatomic) NSString *parkingName;
@property (strong, nonatomic) NSString *parkingLocationId;
@property (strong, nonatomic) NSString *startAddress;
@property (strong, nonatomic) NSString *startCity;
@property (strong, nonatomic) NSString *startPostalCode;
@property (strong, nonatomic) NSString *startState;
@property (strong, nonatomic) NSString *startCountry; 
@property (strong, nonatomic) NSString *phoneNumber;
@property (strong, nonatomic) NSString *totalRate;
@property (strong, nonatomic) NSString *currency;
@property (strong, nonatomic) NSString *pin;
@property (strong, nonatomic) NSString *dailyRate;
@property (strong, nonatomic) NSString *cancellationPolicy;
@property (strong, nonatomic) NSString *roomDescription;
@property (strong, nonatomic) NSString *hotelName;

@property (strong, nonatomic) NSString *pickupInstructions;
@property (strong, nonatomic) NSString *meetingInstructions;
@property (strong, nonatomic) NSString *endAddress;
@property (strong, nonatomic) NSString *endCity;
@property (strong, nonatomic) NSString *endState;
@property (strong, nonatomic) NSString *endPostalCode;
@property (strong, nonatomic) NSString *dropoffInstructions;
@property (strong, nonatomic) NSString *rateDescription;

@property (strong, nonatomic) NSString *vendorName;

@property (strong, nonatomic) NSString *startLatitude;
@property (strong, nonatomic) NSString *startLongitude;
@property (strong, nonatomic) NSString *endLatitude;
@property (strong, nonatomic) NSString *endLongitude;

@property (strong, nonatomic) NSString *reservationId;
@property (strong, nonatomic) NSString *numPersons;
@property (strong, nonatomic) NSString *startAddress2;

@property (strong, nonatomic) NSString *trainNumber;
@property (strong, nonatomic) NSString *operatedByVendor;
@property (strong, nonatomic) NSString *operatedByTrainNumber;
@property (strong, nonatomic) NSString *startPlatform;
@property (strong, nonatomic) NSString *startRailStation;
@property (strong, nonatomic) NSString *wagonNumber;
@property (strong, nonatomic) NSString *amenities;
@property (strong, nonatomic) NSString *endRailStation;
@property (strong, nonatomic) NSString *endPlatform;
@property (strong, nonatomic) NSString *trainTypeCode;
@property (strong, nonatomic) NSString *cabin;

@property (strong, nonatomic) NSString *miles;
@property (strong, nonatomic) NSString *seatNumber;
@property (strong, nonatomic) NSString *startCityCodeLocalized;
@property (strong, nonatomic) NSString *endCityCodeLocalized;

@property (strong, nonatomic) NSString *startAirportCity;
@property (strong, nonatomic) NSString *startAirportName;
@property (strong, nonatomic) NSString *startAirportState;
@property (strong, nonatomic) NSString *startAirportCountry;
@property (strong, nonatomic) NSString *endAirportCity;
@property (strong, nonatomic) NSString *endAirportName;
@property (strong, nonatomic) NSString *endAirportState;
@property (strong, nonatomic) NSString *endAirportCountry;

@property (strong, nonatomic) NSString *classOfServiceLocalized;

@property (strong, nonatomic) NSString *rateType;
@property (strong, nonatomic) NSString *bodyType;
@property (strong, nonatomic) NSString *bodyTypeName;
@property (strong, nonatomic) NSString *transmission;
@property (strong, nonatomic) NSString *airCond;
@property (strong, nonatomic) NSString *discountCode;
@property (strong, nonatomic) NSString *specialEquipment;

@property (strong, nonatomic) NSString *startAirportCountryCode;
@property (strong, nonatomic) NSString *endAirportCountryCode;
@property (strong, nonatomic) NSString *classOfCar;
@property (strong, nonatomic) NSString *numRooms;

@property (strong, nonatomic) NSString *imageCarURI;
@property (strong, nonatomic) NSString *imageVendorURI;

@property (strong, nonatomic) NSString *statusLocalized;
@property (strong, nonatomic) NSString *startRailStationLocalized;
@property (strong, nonatomic) NSString *endRailStationLocalized;

@property (strong, nonatomic) FlightStatsData			*flightStats;

-(NSComparisonResult) startDateCompare:(SegmentData *)seg;

+(void) getArriveTimeString:(EntitySegment*) segment timeStr:(NSMutableString*) time dateStr:(NSMutableString*) date;
+(void) getDepartTimeString:(EntitySegment*) segment timeStr:(NSMutableString*) time dateStr:(NSMutableString*) date;
+(void) getArriveTermGate:(EntitySegment*) segment terminal:(NSMutableString *) termStr gate:(NSMutableString*) gateStr;
+(void) getDepartTermGate:(EntitySegment*) segment terminal:(NSMutableString *) termStr gate:(NSMutableString*) gateStr;

+(NSString *)getAirportFullAddress:(EntitySegmentLocation *)segLoc;
+(NSString *)getAirportNameCode:(EntitySegmentLocation *)segLoc;
+(NSString *)getAirportFullName:(EntitySegmentLocation *)segLoc;
+(NSString *)getAirportCity:(EntitySegmentLocation *)segLoc;
+(NSString *)getCityState:(EntitySegmentLocation *)segLoc;
+(NSString *)getCityStateZip:(EntitySegmentLocation *)segLoc;

+(NSString *)getRailStation:(EntitySegmentLocation *)segLoc;
+(NSString *)getMapAddress:(EntitySegmentLocation *)segLoc;
+(NSString *)getMapAddress:(EntitySegmentLocation *)segLoc withLineBreaker:(BOOL) flb withDelimitor:(BOOL) fdel;


@end
