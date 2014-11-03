//
//  CarDateTimeViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BookingBaseTableViewController.h"


@class CarViewController;


@interface CarDateTimeViewController : BookingBaseTableViewController  <UITableViewDelegate, UITableViewDataSource, UIPickerViewDelegate, UIPickerViewDataSource>
{
	CarViewController	*parentMVC;
	UINavigationBar			*tBar;
	UIBarButtonItem		*cancelBtn;
	UIBarButtonItem		*titleButton;
	UIDatePicker		*datePicker;
	UIPickerView		*hourPicker;
	NSDate				*date;
	BOOL				isPickup;
	BOOL				isEditingDate;
}

@property (nonatomic, strong) CarViewController			*parentMVC;
@property (nonatomic, strong) IBOutlet UINavigationBar		*tBar;
@property (nonatomic, strong) IBOutlet UIBarButtonItem		*cancelBtn;
@property (nonatomic, strong) IBOutlet UIBarButtonItem	*titleButton;
@property (nonatomic, strong) IBOutlet UIDatePicker		*datePicker;
@property (nonatomic, strong) IBOutlet UIPickerView		*hourPicker;
@property (nonatomic, strong) NSDate					*date;
@property (nonatomic) BOOL								isPickup;
@property (nonatomic) BOOL								isEditingDate;

-(IBAction) datePickerValueChanged:(id)sender;
-(IBAction) btnCancel:(id)sender;
-(IBAction) btnDone:(id)sender;

-(void)closeView;

-(void)enterDateMode;
-(void)enterHourMode;

-(void)reloadData;

@end
