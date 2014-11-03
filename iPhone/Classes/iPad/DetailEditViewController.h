//
//  DetailEditViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol DetailEditDelegate
- (void)cancelSelected;
- (void)saveSelected:(NSString *)valString ValKey:(NSString *)valKey ValDate:(NSDate *)valDate EditType:(int)editType;
@end

@interface DetailEditViewController : UIViewController <UIPickerViewDelegate, UIPickerViewDataSource, UITextViewDelegate>{
	int					editType;
	UITableViewCell		*editCell;
	
	UITextField			*txtField;
	UIToolbar			*tb;
	UIPickerView		*picker;
	UIDatePicker		*datePicker;
	NSString			*valueString;
	NSDate				*valueDate;
	NSString			*valueKey;
	UITextView			*txtView;
	
	id<DetailEditDelegate> __weak _delegate;
}

@property int												editType;
@property (strong, nonatomic)  UITableViewCell				*editCell;

@property (strong, nonatomic) IBOutlet UITextField			*txtField;
@property (strong, nonatomic) IBOutlet UIToolbar			*tb;
@property (strong, nonatomic) IBOutlet UIPickerView			*picker;
@property (strong, nonatomic) IBOutlet UIDatePicker			*datePicker;
@property (strong, nonatomic) IBOutlet UITextView			*txtView;

@property (strong, nonatomic) NSString				*valueString;
@property (strong, nonatomic) NSDate				*valueDate;
@property (strong, nonatomic) NSString				*valueKey;

@property (nonatomic, weak) id<DetailEditDelegate> delegate;

-(void) setPopoverFrame:(int)viewType;
-(void)selectCurrencyRow:(NSString *)crnCode;
-(IBAction)btnSavePressed:(id)sender;
-(IBAction)btnCancelPressed:(id)sender;
@end
