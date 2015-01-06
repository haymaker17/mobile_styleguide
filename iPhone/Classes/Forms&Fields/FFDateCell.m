//
//  FFDateCell.m
//  ConcurMobile
//
//  Created by Laurent Mery on 08/12/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FFCells-private.h"




@implementation FFDateCell


#pragma mark - init

NSString *const FFCellReuseIdentifierDate = @"DateCell";


#pragma mark - component

-(UITextField*)createInputValue{
    
    UITextField *input = [super createInputValue];
    
    self.datePicker = [[UIDatePicker alloc] init];
    [self.datePicker setDatePickerMode:UIDatePickerModeDate];
    [self.datePicker setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    
    [self.datePicker addTarget:self action:@selector(onDatePickerSelect:) forControlEvents:UIControlEventValueChanged];
    [input setInputView:self.datePicker];
    
    return input;
}

//public
-(void)updateDataType{
    
    [super updateDataType];
    
    //TODO: switch to date/time/datetime by testing datatype.Type
    
    if (![self.field.dataType isEmpty]){
        
        [self.datePicker setDate:self.field.dataType.Date];
    }
    
    if ([self.field.dataType isDateModeIsDate]){
        
        [self.datePicker setDatePickerMode:UIDatePickerModeDate];
    }
    else if ([self.field.dataType isDateModeIsDateTime]){
        
        [self.datePicker setDatePickerMode:UIDatePickerModeDateAndTime];
    }
    else if ([self.field.dataType isDateModeIsTime]){
        
        [self.datePicker setDatePickerMode:UIDatePickerModeTime];
    }
}


#pragma mark - editor

-(void)onDatePickerSelect:(UIDatePicker *)dtPicker{
    
    [self.field.dataType setDate:dtPicker.date];
    [self updateDataType];
}

@end
