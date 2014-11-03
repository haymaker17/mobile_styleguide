//
//  FancyDatePickerView.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/16/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol FancyDatePickerDelegate;

@interface FancyDatePickerView : UIView

@property (weak, nonatomic) IBOutlet UIView *view;
@property (weak, nonatomic) IBOutlet UIDatePicker *picker;

@property (weak, nonatomic) id<FancyDatePickerDelegate> delegate;

- (IBAction)didChangeDate:(id)sender;
- (IBAction)didDismiss:(id)sender;

@end

@protocol FancyDatePickerDelegate <NSObject>

- (void)datePickerDidDismiss:(FancyDatePickerView *)picker;
- (void)datePicker:(FancyDatePickerView *)picker didChangeDate:(NSDate *)date;

@end