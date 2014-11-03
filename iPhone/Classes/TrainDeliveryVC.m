//
//  TrainDeliveryVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainDeliveryVC.h"
#import "TrainDeliveryData.h"
#import "DeliveryData.h"


@implementation TrainDeliveryVC

#pragma mark -
#pragma mark MVC Methods
-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:TRAIN_DELIVERY])
	{

		//			self.aList = [[NSMutableArray alloc] initWithObjects:nil];
		
		TrainDeliveryData *trainDeliveryData = (TrainDeliveryData *)msg.responder;
		self.aDeliveryOptions = [[NSMutableArray alloc] initWithObjects:nil];
		
		for(NSString *typeKey in trainDeliveryData.keys)
		{
			DeliveryData *dd = (trainDeliveryData.items)[typeKey];
			[self.aDeliveryOptions addObject:dd];
		}

		if([self.aDeliveryOptions count] > 0)
		{
			DeliveryData *dd = self.aDeliveryOptions[0];
			self.lblDeliveryOption.text = dd.name;
			self.parentVC.deliveryOption = dd.name;
			self.parentVC.deliveryData = dd;
			[self.parentVC.btnDelivery setTitle:self.parentVC.deliveryOption forState:UIControlStateNormal];
		}
		
		[self.dPicker reloadComponent:0];
		
		[self.viewLoading setHidden:YES];
	}
	
}

#pragma mark -
#pragma mark Fetching Methods
-(void)fetchDeliveryOptions:(id)sender
{	
	//NSLog(@"groupid %@", parentVC.railChoice.groupId);
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"TRAIN_DELIVERY_VIEW", @"TO_VIEW"
								 , self.parentVC.railChoice.groupId, @"GROUP_ID"
								 , self.parentVC.railChoice.bucket, @"BUCKET",  nil];
	
	[[ExSystem sharedInstance].msgControl createMsg:TRAIN_DELIVERY CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	
}

#pragma mark -
#pragma mark View Controller Meths

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
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    if (self.aDeliveryOptions == nil)
        [self.viewLoading setHidden:NO];
    else
        [self.viewLoading setHidden:YES];
    
	self.lblLoading.text = [Localizer getLocalizedText:@"Loading delivery options"];
    [super viewDidLoad];
	
//	self.aDeliveryOptions = [[NSArray alloc] initWithObjects:
//						   @"Quik-Trak Machine",@"Bob, the teller",@"Mail",@"e-Mail",@"Telepathy", nil];
//	[aDeliveryOptions release];
	
	self.title = [Localizer getLocalizedText:@"Delivery Options"];
	//self.navigationItem.prompt = @"Select how you would like your tickets delivered to you";
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
    
    // Release any cached data, images, etc. that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
	self.lblDeliveryOption = nil;
	self.dPicker = nil;
	self.btnBackground = nil;
	
	self.viewLoading = nil;
	self.activity = nil;
	self.lblLoading = nil;
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

	return[self.aDeliveryOptions count];
}

-(NSString *)pickerView:(UIPickerView *)pickerView
			titleForRow:(NSInteger)row
		   forComponent:(NSInteger)component 
{
	DeliveryData *dd = self.aDeliveryOptions[row];
	
	return dd.name;
}

- (NSInteger)selectedRowInComponent:(NSInteger)component
{
	return 0;
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
	DeliveryData *dd = self.aDeliveryOptions[row];
	self.lblDeliveryOption.text = dd.name;
	self.parentVC.deliveryOption = dd.name;
	self.parentVC.deliveryData = dd;
    /*MOB-5928
     The delivery option was assigning the picked value in the old way, which to update a button, not a row.*/
    [self.parentVC pickedItemString:dd.name];
    [self.parentVC.tableList reloadData];
	//[parentVC.btnDelivery setTitle:parentVC.deliveryOption forState:UIControlStateNormal];
}

-(void)setSelectedOption:(NSString *)option
{
	for(int i = 0; i < [self.aDeliveryOptions count]; i++)
	{
		if([option isEqualToString:self.aDeliveryOptions[i]])
		{
			[self.dPicker selectRow:i inComponent:0 animated:YES];
			return;
		}
	}
}

@end
