//
//  ViolationsViewController.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 27/02/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "MobileViewController.h"
#import "EntityHotelRoom.h"
#import "EntityAirFilterSummary.h"
#import "FareViolationDetails.h"

@interface ViolationsViewController : MobileViewController <UITableViewDelegate, UITableViewDataSource>

- (instancetype)initWithTitle:(NSString*)title;

//@property (nonatomic, strong) NSArray *violationTexts;

//@property (nonatomic, strong) EntityHotelRoom *hotelRoom;
//@property (nonatomic, strong) EntityAirFilterSummary *airSummary;

@property (nonatomic, strong) id<FareViolationDetails> selectedFareOption;
@property (nonatomic, strong) NSArray *violationReasons;
@property (nonatomic, strong) NSArray *violationReasonLabels;

@end
