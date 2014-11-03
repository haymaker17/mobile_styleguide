//
//  DatePickerCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 10/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SearchCriteriaCellData.h"

@interface DatePickerCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UIDatePicker *datePicker;
- (IBAction)didChangeDate:(id)sender;

@property (copy,nonatomic) void(^onDateSelected)(NSDate *date);

-(void)setCellData:(SearchCriteriaCellData *)cellData;

@end
