//
//  DatePickerCell.m
//  ConcurMobile
//
//  Created by Sally Yan on 10/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "DatePickerCell.h"
#import "CTEDateUtility.h"

@implementation DatePickerCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self; 
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (IBAction)didChangeDate:(id)sender {
    if (self.onDateSelected) {
        self.onDateSelected(self.datePicker.date);
    }
}
-(void)setCellData:(SearchCriteriaCellData *)cellData
{
    if ([cellData.cellName isEqualToString:@"Check-In Date Picker"]) {
        self.datePicker.minimumDate = [NSDate date];
    }
    else if ([cellData.cellName isEqualToString:@"Check-Out Date Picker"]){
        self.datePicker.minimumDate = [CTEDateUtility addDaysToDate:[NSDate date] daysToAdd:1];
    }

    self.datePicker.date = cellData.keyValue[@"cellvalue"];
    //TODO: Set max date too.

}
@end
