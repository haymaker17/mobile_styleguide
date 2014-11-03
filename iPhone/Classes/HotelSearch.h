//
//  HotelSearch.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/27/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@class HotelSearchCriteria;
@class HotelResult;

@interface HotelSearch : NSObject
{
	HotelSearchCriteria				*hotelSearchCriteria;
	NSMutableArray					*hotels;
	NSNumber						*selectedHotelIndex;
	NSString						*tripKey;
    NSString                        *pollingID;
    BOOL                            isFinal;
    BOOL                            isPolling;
    BOOL                            ratesFound;
    CFTimeInterval                  searchStartTime;
}

@property (nonatomic, strong) HotelSearchCriteria	*hotelSearchCriteria;
@property (nonatomic, strong) NSMutableArray		*hotels;
@property (nonatomic, strong) NSNumber				*selectedHotelIndex;
@property (nonatomic, strong) NSString				*tripKey;
@property (nonatomic, strong) NSString              *pollingID;
@property (nonatomic) BOOL                          isFinal;
@property (nonatomic) BOOL                          isPolling;
@property (nonatomic) BOOL                          ratesFound;
@property (nonatomic) CFTimeInterval                searchStartTime;

@property (weak, nonatomic, readonly) HotelResult			*selectedHotel;

-(void)selectHotel:(NSUInteger)hotelIndex;

@end
