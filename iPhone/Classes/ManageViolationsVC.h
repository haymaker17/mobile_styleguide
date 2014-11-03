//
//  ManageViolationsVC.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 19/02/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "EntityHotelRoom.h"
#import "EntityAirFilterSummary.h"

@interface ManageViolationsVC : MobileViewController <UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, strong) NSString *travelPointsInBank;

@property (nonatomic, strong) EntityHotelRoom *hotelRoom;
@property (nonatomic, strong) EntityAirFilterSummary *airSummary;

@property (nonatomic, strong) NSArray *violationTexts; // Violation Texts for the selected Fare
@property (nonatomic, strong) NSArray *violationReasons; // Reasons that user can select against the violation
@property (nonatomic, strong) NSArray *violationReasonLabels; // Each Reason's description (message shown to user)

- (instancetype)initWithTitle:(NSString *)title;

@end
