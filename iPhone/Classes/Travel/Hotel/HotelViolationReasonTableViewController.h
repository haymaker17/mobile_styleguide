//
//  HotelViolationReasonTableViewController.h
//  ConcurMobile
//
//  Created by ernest cho on 10/9/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface HotelViolationReasonTableViewController : UITableViewController

- (id)initWithReason:(NSString *)selectedReasonCode completion:(void (^)(NSString *selectedReasonCode, NSString *selectedReasonDescription))completion;

@end
