//
//  FieldOptionsViewController.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HotelOptionsViewController.h"
#import "EntityTravelCustomFieldAttribute.h"
#import "EntityTravelCustomFields.h"

@interface FieldOptionsViewController : HotelOptionsViewController <UITableViewDelegate, UITableViewDataSource> 
@property (nonatomic, strong) EntityTravelCustomFields *tcf;
@end
