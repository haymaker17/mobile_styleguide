//
//  SearchDistanceTableViewCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 7/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SearchCriteriaCellData.h"
#import "SearchDistanceCellData.h"
#import "SearchCriteriaTableViewCell.h"

@interface SearchDistanceTableViewCell : UITableViewCell <UIPickerViewDataSource, UIPickerViewDelegate>
@property (weak, nonatomic) IBOutlet UIPickerView *distancePicker;
@property (nonatomic, copy) void (^ hotelSearchDistanceDidChanged)(NSString *distanceValue);

@end
