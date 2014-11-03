//
//  DateEditVC.h
//  ConcurMobile
//
//  Created by yiwen on 5/17/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DateEditDelegate.h"
#import "MobileViewController.h"
#import "RotatingDatePicker.h"

@interface DateEditVC : MobileViewController <UIPickerViewDelegate>  {
 
    id<DateEditDelegate>	__weak _delegate;
    
	RotatingDatePicker		*datePicker;
    UIButton                *btnBack;

	UILabel					*lblDate, *lblTip;
	NSDate					*date;

    NSString                *tipText, *viewTitle;
    
    // Formfield or rowKey, for the delegate to know which object it is editing
    NSObject                *context; 
}

@property (nonatomic, weak) id<DateEditDelegate>			delegate;
@property (strong, nonatomic) IBOutlet RotatingDatePicker	*datePicker;
@property (strong, nonatomic) IBOutlet UILabel				*lblDate;
@property (strong, nonatomic) IBOutlet UILabel				*lblTip;
@property (strong, nonatomic) IBOutlet UIButton				*btnBack;

@property (strong, nonatomic) NSObject                      *context;
@property (strong, nonatomic) NSString                      *tipText;
@property (strong, nonatomic) NSString                      *viewTitle;
@property (strong, nonatomic) NSDate                        *date;

-(IBAction) closeDone:(id)sender;
-(IBAction) datePickerValueChanged:(id)sender;

@end
