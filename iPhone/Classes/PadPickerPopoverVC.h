//
//  PadPickerPopoverVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"

@protocol PadPickerPopoverDelegate
- (void)cancelPicker;
- (void)pickedItem:(NSInteger)pickedRow;
- (void)pickedItemString:(NSString*)pickedKey;
@end

@interface PadPickerPopoverVC : MobileViewController <UIPickerViewDelegate, UIPickerViewDataSource>{

	UIPickerView		*thePicker;
	NSMutableArray		*aList;
	int					selectedValue;
	id<PadPickerPopoverDelegate> __weak _delegate;
	NSString			*key;
	NSIndexPath			*indexPath;
}

@property (strong, nonatomic) NSString					*key;
@property (strong, nonatomic) IBOutlet UIPickerView		*thePicker;
@property (strong, nonatomic) NSMutableArray			*aList;
@property int											selectedValue;
@property (strong, nonatomic)NSIndexPath				*indexPath;

@property (nonatomic, weak) id<PadPickerPopoverDelegate> delegate;

-(IBAction) closeDown:(id)sender;

-(IBAction) closeDone:(id)sender;

-(void) onDone;

-(void) initList;
-(void) selectValue:(NSString *)option;

@end
