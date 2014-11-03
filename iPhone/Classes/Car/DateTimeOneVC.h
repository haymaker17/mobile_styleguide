//
//  DateTimeOneVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/5/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "RotatingDatePicker.h"
#import "MobileViewController.h"
#import "DateTimeOneDelegate.h"

@interface DateTimeOneVC : MobileViewController <UIPickerViewDelegate>  {
    
    id<DateTimeOneDelegate>	__weak _delegate;
    
	RotatingDatePicker		*datePicker;
    UIButton                *btnBack;
    
	UILabel					*lblDate, *lblLabel;
    
	NSDate					*date;
    NSInteger               timeInMinutes;
    NSString                *label, *viewTitle;
    
    // Formfield or rowKey, for the delegate to know which object it is editing
    NSObject                *context; 
}

@property (nonatomic, weak) id<DateTimeOneDelegate>			delegate;
@property (strong, nonatomic) IBOutlet RotatingDatePicker	*datePicker;
@property (strong, nonatomic) IBOutlet UILabel				*lblDate;
@property (strong, nonatomic) IBOutlet UILabel				*lblLabel;
@property (strong, nonatomic) IBOutlet UIButton				*btnBack;

@property (strong, nonatomic) NSObject                      *context;
@property (strong, nonatomic) NSString                      *label;
@property (strong, nonatomic) NSString                      *viewTitle;
@property (strong, nonatomic) NSDate                        *date;
@property NSInteger timeInMinutes;

-(void)setSeedData:(id<DateTimeOneDelegate>)del withFullDate:(NSDate*) dt withLabel:(NSString*) lbl withContext:(NSObject*) con;

-(IBAction) datePickerValueChanged:(id)sender;

+(NSString*) getDateString:(NSDate*) date withTimeInMinutes:(NSInteger) tmInMinutes;



@end
