//
//  PickerPopoverVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 10/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"


@protocol PickerPopoverDelegate
- (void)cancelPicker;
- (void)donePicker:(NSDate *)dateSelected;
- (void)pickedDate;
- (void)pickedItem:(int)row;
@end

@interface PickerPopoverVC : MobileViewController <UIPickerViewDelegate, UIPickerViewDataSource>{
	UIDatePicker		*datePicker;
	UIPickerView		*picker;
	UIButton			*btnCancel, *btnDone;
	id<PickerPopoverDelegate> __weak _delegate;
	BOOL isDate;
	NSDate				*dateSelected;
	NSString			*key;
	NSString			*currencyCode;
}

@property (nonatomic, weak) id<PickerPopoverDelegate> delegate;
@property BOOL isDate;
@property (strong, nonatomic) IBOutlet UIDatePicker		*datePicker;
@property (strong, nonatomic) IBOutlet UIPickerView		*picker;
@property (strong, nonatomic) IBOutlet UIButton			*btnCancel;
@property (strong, nonatomic) IBOutlet UIButton			*btnDone;

@property (strong, nonatomic) NSDate					*dateSelected;
@property (strong, nonatomic) NSString					*key;

-(void) initDate:(NSDate *) dateCurrent;
-(void) initPicker:(NSString *) keyCurrent;
-(IBAction)dateDone:(id)sender;
-(IBAction)dateCancel:(id)sender;
@end
