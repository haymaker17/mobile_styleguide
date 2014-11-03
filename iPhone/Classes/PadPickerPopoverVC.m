//
//  PadPickerPopoverVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "PadPickerPopoverVC.h"


@implementation PadPickerPopoverVC
@synthesize		thePicker;
@synthesize		aList;
@synthesize		selectedValue;
@synthesize delegate = _delegate;
@synthesize		key;
@synthesize		indexPath;

// The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
/*
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization.
    }
    return self;
}
*/

-(void) viewWillAppear:(BOOL)animated
{
	if(key != nil)
		[self selectValue:key];
	
	[super viewWillAppear:animated];
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
	
	self.contentSizeForViewInPopover = CGSizeMake(320.0, 215.0);
}


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}



-(IBAction) closeDown:(id)sender{
}

-(IBAction) closeDone:(id)sender{
}

-(void) onDone{
}



#pragma mark -
#pragma mark Picker view methods
-(void) initList
{

}

#pragma mark -
#pragma mark Picker Delegate Methods
-(NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
	return 1;
}

-(NSInteger)pickerView:(UIPickerView *)pickerView
numberOfRowsInComponent:(NSInteger)component
{
	
	return[aList count];
}

-(NSString *)pickerView:(UIPickerView *)pickerView
			titleForRow:(NSInteger)row
		   forComponent:(NSInteger)component 
{
	NSString *s = aList[row];
	
	return s;
}

- (NSInteger)selectedRowInComponent:(NSInteger)component
{
	return 0;
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
//	DeliveryData *dd = [aDeliveryOptions objectAtIndex:row];
//	lblDeliveryOption.text = dd.name;
//	parentVC.deliveryOption = dd.name;
//	parentVC.deliveryData = dd;
//	[parentVC.btnDelivery setTitle:parentVC.deliveryOption forState:UIControlStateNormal];
	
//	NSInteger extendedHour = [[aTimes objectAtIndex:row] intValue];
//	self.selectedExtendedHour = extendedHour;
	self.key = aList[row];
	[_delegate pickedItemString:key];
}

-(void)selectValue:(NSString *)option
{
	for(int i = 0; i < [aList count]; i++)
	{
		if([option isEqualToString:aList[i]])
		{
			[thePicker selectRow:i inComponent:0 animated:YES];
			return;
		}
	}
}
@end
