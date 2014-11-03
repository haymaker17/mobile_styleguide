    //
//  DetailEditViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 5/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "DetailEditViewController.h"
#import "ReferenceData.h"

@implementation DetailEditViewController
@synthesize editType;
@synthesize editCell;
@synthesize txtField;
@synthesize tb;
@synthesize picker;
@synthesize datePicker;
@synthesize	valueString,valueDate,valueKey, txtView;
@synthesize delegate = _delegate;

#define kPICKER_H 316.0
#define kDATE_H 260.0
#define kTEXT_H 107.0
#define kCOMMENT_H 175.0

#define kDATE_PICKER 0
#define kCURRENCY_PICKER 1
#define kAMOUNT 2
#define kVENDOR 3
#define kLOCATION 4
#define kCOMMENT 5

/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) {
        // Custom initialization
    }
    return self;
}
*/

/*
// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
}
*/

- (void)changeDateInLabel:(id)sender
{
	//Use NSDateFormatter to write out the date in a friendly format
	//	NSDateFormatter *df = [[NSDateFormatter alloc] init];
	//	df.dateStyle = NSDateFormatterMediumStyle;
	//	//entry.tranDate = datePicker.date;
	//	//label.text = [NSString stringWithFormat:@"%@",
	//				  //[df stringFromDate:datePicker.date]];
	//	[df release];
	//	[datePicker setHidden:YES];
	//	[tableView reloadData];
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
//	picker.delegate = self;
//	picker.dataSource = self;
	picker.showsSelectionIndicator = YES;
	
    [super viewDidLoad];
	
	//self.contentSizeForViewInPopover = CGSizeMake(320.0, 480.0);
	//self.contentSizeForViewInPopover = CGSizeMake(320.0, kDATE_H);
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200	
	if(editType == kDATE_PICKER)
	{
		self.contentSizeForViewInPopover = CGSizeMake(320.0, kDATE_H);
		txtField.hidden = YES;
		picker.hidden = YES;
		txtView.hidden = YES;
		//datePicker.delegate = self;
		datePicker.date = valueDate;
		//[datePicker addTarget:self action:@selector(changeDateInLabel:) forControlEvents:UIControlEventValueChanged];
	}
	else if(editType == kCURRENCY_PICKER)
	{
		self.contentSizeForViewInPopover = CGSizeMake(320.0, kPICKER_H);
		
		txtField.text = valueString;
		[txtField becomeFirstResponder];
		datePicker.hidden = YES;
		txtView.hidden = YES;

	}
	else if(editType == kCOMMENT)
	{
		self.contentSizeForViewInPopover = CGSizeMake(320.0, kCOMMENT_H);
		picker.hidden = YES;
		datePicker.hidden = YES;
		txtField.hidden = YES;
		txtView.hidden = NO;
		[txtView becomeFirstResponder];
	}
	else if(editType > kCURRENCY_PICKER)
	{
		self.contentSizeForViewInPopover = CGSizeMake(320.0, kTEXT_H);
		[txtField becomeFirstResponder];
		picker.hidden = YES;
		datePicker.hidden = YES;
		txtView.hidden = YES;
	}
#endif
}



- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
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


- (void)dealloc {
	
	
	//[valueString release];
	//[valueDate release];
	//[valueKey release];
	
	self.delegate = nil;
	
}


-(void) setPopoverFrame:(int)viewType
{
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200
	if(viewType == kDATE_PICKER)
	{
		self.contentSizeForViewInPopover = CGSizeMake(320.0, kDATE_H);
		txtField.hidden = YES;
		picker.hidden = YES;
	}
	else if(viewType == kCURRENCY_PICKER)
	{
		self.contentSizeForViewInPopover = CGSizeMake(320.0, 316);
		//txtField.hidden = YES;
		datePicker.hidden = YES;
	}
	else if(editType == kCOMMENT)
	{
		self.contentSizeForViewInPopover = CGSizeMake(320.0, kCOMMENT_H);
		picker.hidden = YES;
		datePicker.hidden = YES;
		txtView.hidden = NO;
		txtField.hidden = YES;
		txtView.text = valueString;
	}
	else if(viewType > kCURRENCY_PICKER)
	{
		self.contentSizeForViewInPopover = CGSizeMake(320.0, kTEXT_H);
		picker.hidden = YES;
		datePicker.hidden = YES;
		txtField.text = valueString;
	}
#endif
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


//method to load up the crncy dialog for the proper currency
-(void)selectCurrencyRow:(NSString *)crnCode
{
	int currencyRow = 0;
	for (int x = 0; x < [[ReferenceData getSingleton].currencies count]; x++) {
		NSString *code = [[ReferenceData getSingleton] getCurrencyCodeAtIndex:x];
		if ([code isEqualToString:crnCode]) 
		{
			currencyRow = x;
			break;
		}
	}
	[picker selectRow:currencyRow inComponent:0 animated:YES];
}

-(IBAction)btnSavePressed:(id)sender
{
	if (_delegate != nil && editType == kCURRENCY_PICKER)
	{
		NSString *key = [[ReferenceData getSingleton] getCurrencyCodeAtIndex:[picker selectedRowInComponent:0]];
		[_delegate saveSelected:txtField.text ValKey:key ValDate:nil EditType:editType];
	}
	else if (_delegate != nil && editType == kDATE_PICKER)
	{
		valueDate = datePicker.date;
		[_delegate saveSelected:nil ValKey:nil ValDate:valueDate EditType:editType];
	}
	else if (_delegate != nil && editType == kCOMMENT)
	{
		NSString *val = txtView.text;
		[_delegate saveSelected:val ValKey:nil ValDate:nil EditType:editType];
	}
	else if (_delegate != nil && editType >= kVENDOR)
	{
		NSString *val = txtField.text;
		[_delegate saveSelected:val ValKey:nil ValDate:nil EditType:editType];
	}
}

-(IBAction)btnCancelPressed:(id)sender
{
	if (_delegate != nil) {
		
		[_delegate cancelSelected];
	}

}

@end
