//
//  PickerPopoverVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 10/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "PickerPopoverVC.h"
#import "ReferenceData.h"
#import "ExSystem.h" 

#import "Localizer.h"

@implementation PickerPopoverVC
@synthesize		datePicker;
@synthesize		picker;
@synthesize		btnCancel, btnDone;
@synthesize		dateSelected;
@synthesize		key, isDate;
@synthesize delegate = _delegate;


/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) {
        // Custom initialization
    }
    return self;
}
*/

-(void)viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
	
	int currencyRow = 0;
	for (int x = 0; x < [[ReferenceData getSingleton].currencies count]; x++) {
		NSString *code = [[ReferenceData getSingleton] getCurrencyCodeAtIndex:x];
		if ([code isEqualToString:currencyCode]) //entry.crnCode
		{
			currencyRow = x;
			break;
		}
	}
	[picker selectRow:currencyRow inComponent:0 animated:YES];
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
	if(!isDate)
		self.contentSizeForViewInPopover = CGSizeMake(320.0, 215.0);
	else 
		self.contentSizeForViewInPopover = CGSizeMake(320.0, 260.0);
	
    [super viewDidLoad];
}


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}



-(void) initDate:(NSDate *) dateCurrent
{
	[picker setHidden:YES];
	[datePicker setHidden:NO];
	[btnDone setHidden:NO];
	[btnCancel setHidden:NO];
	
	datePicker.timeZone = [NSTimeZone localTimeZone];
	datePicker.date = dateCurrent;
	
	float btnW = 120.0;
	self.btnDone = [ExSystem makeColoredButtonRegular:@"SMOKE" W:btnW H:32.0 Text:[Localizer getLocalizedText:@"Done"] SelectorString:@"dateDone:" MobileVC:self];
	btnDone.frame = CGRectMake(320 - (btnW + 20), 224, btnW, 32);
	[self.view addSubview:btnDone];
	
	self.btnCancel = [ExSystem makeColoredButtonRegular:@"SMOKE" W:btnW H:32.0 Text:[Localizer getLocalizedText:@"Cancel"] SelectorString:@"dateCancel:" MobileVC:self];
	btnCancel.frame = CGRectMake(20, 224, btnW, 32);
	[self.view addSubview:btnCancel];
}


-(void) initPicker:(NSString *) keyCurrent
{
	[picker setHidden:NO];
	[datePicker setHidden:YES];
	[btnDone setHidden:YES];
	[btnCancel setHidden:YES];
	
	currencyCode = keyCurrent;
}

# pragma mark -
#pragma mark Picker Methods
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)thePickerView {
	
	return 1;
}

- (NSInteger)pickerView:(UIPickerView *)thePickerView numberOfRowsInComponent:(NSInteger)component {
	
	return [[ReferenceData getSingleton].currencies count];
}

- (NSString *)pickerView:(UIPickerView *)thePickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component {
	
	
	return [[ReferenceData getSingleton] getCurrencyAtIndex:row].crnName;
}

- (void)pickerView:(UIPickerView *)thePickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component 
{		

	//int row = [picker selectedRowInComponent:0];
	//entry.crnCode = [[ReferenceData getSingleton] getCurrencyCodeAtIndex:row];
	[_delegate pickedItem:row];
	
	
	//	sectionToEdit = section;
	//	rowToEdit = row;
	//	entry.crnCode = [currencyCodes objectAtIndex:row];
	//	
	//	//Amount Row
	//	NSUInteger _path[2] = {sectionToEdit, kAmountRow};
	//	NSIndexPath *_indexPath = [[NSIndexPath alloc] initWithIndexes:_path length:2];
	//	NSArray *_indexPaths = [[NSArray alloc] initWithObjects:_indexPath, nil];
	//	[_indexPath release];
	//	[tableView reloadRowsAtIndexPaths:_indexPaths withRowAnimation:NO];
	//	[_indexPaths release];
	//	
	//	//currency Row
	//	NSUInteger _path2[2] = {sectionToEdit, rowToEdit};
	//	_indexPath = [[NSIndexPath alloc] initWithIndexes:_path2 length:2];
	//	_indexPaths = [[NSArray alloc] initWithObjects:_indexPath, nil];
	//	[_indexPath release];
	//	[tableView reloadRowsAtIndexPaths:_indexPaths withRowAnimation:YES];
	//	[_indexPaths release];
	//	
	//	[self hideAllControls];
	//	
	//////NSLog(@"Selected Color: %@. Index of selected color: %i", [arrayColors objectAtIndex:row], row);
}


-(IBAction)dateDone:(id)sender
{
	[_delegate donePicker:datePicker.date];
}


-(IBAction)dateCancel:(id)sender
{
	[_delegate cancelPicker];
}
@end
