//
//  RailChoiceData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RailChoiceSegmentData.h"

@interface RailChoiceData : NSObject {
}

@property (strong, nonatomic) NSString	*baseFare;
@property (strong, nonatomic) NSString	*cost;
@property (strong, nonatomic) NSString	*currencyCode;
@property (strong, nonatomic) NSString	*descript;
@property (strong, nonatomic) NSString	*gdsName;
@property (strong, nonatomic) NSString	*imageUri;
@property (strong, nonatomic) NSString	*key;
@property (strong, nonatomic) NSString	*vendorCode;
@property (strong, nonatomic) NSString	*bucket;
@property (strong, nonatomic) NSString	*groupId;
@property (strong, nonatomic) NSString  *choiceId;
@property (nonatomic, retain) NSNumber  *maxEnforcementLevel;

@property (strong, nonatomic) NSMutableArray	*segments;
@property (strong, nonatomic) NSMutableArray	*seats;
@property (strong, nonatomic) NSMutableArray	*violations;
@property (strong, nonatomic) RailChoiceSegmentData	*segment;

-(id)init;
-(void) addSeat:(NSString *)seatBaseFare Cost:(NSString *)seatCost CurrencyCode:(NSString *)seatCurrencyCode Description:(NSString *)seatDescription;

@end
