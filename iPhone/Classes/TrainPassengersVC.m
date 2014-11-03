//
//  TrainPassengersVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainPassengersVC.h"
#import "TrainEditCell.h"

@implementation TrainPassengersVC
@synthesize tableList, aSections, aList, bcdPass;


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}


/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) {
        // Custom initialization
    }
    return self;
}
*/


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
	

    [super viewDidLoad];
	
	[self initPassengerList];
	self.title = [Localizer getLocalizedText:@"Passengers"];
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




-(IBAction)cancelView:(id)sender
{
	[self dismissViewControllerAnimated:YES completion:nil];	
}

-(IBAction)doneView:(id)sender
{
	for(int i = 0; i < [aList count]; i++)
	{
		[bcdPass.values removeObjectAtIndex:i];
		[bcdPass.values insertObject:aList[i] atIndex:i];
	}
	
	NSString *val = [NSString stringWithFormat:@"%@ Adults %@ Children %@ Infants", (bcdPass.values)[0], (bcdPass.values)[1], (bcdPass.values)[2]];
	bcdPass.val = val;
	[self dismissViewControllerAnimated:YES completion:nil];	
}

-(void) initPassengerList
{
	aSections = [[NSMutableArray alloc] initWithObjects:@"Adults", @"Children", @"Infants", nil];
	aList = [[NSMutableArray alloc] initWithObjects:(bcdPass.values)[0], (bcdPass.values)[1], (bcdPass.values)[2], nil];
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return [aSections count];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return 1;
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
//	NSUInteger row = [indexPath row];
	NSUInteger section = [indexPath section];
	
	TrainEditCell *cell = (TrainEditCell*)[tableView dequeueReusableCellWithIdentifier:@"TrainEditCell"];
	if (cell == nil)
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TrainEditCell" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[TrainEditCell class]])
				cell = (TrainEditCell *)oneObject;
	}
	
	NSString *val = aList[section];
	
	if(section == 0)
		cell.lbl.text = [Localizer getLocalizedText:@"Adults"];
	else if(section == 1)
		cell.lbl.text = [Localizer getLocalizedText:@"Children"];
	else if(section == 2)
		cell.lbl.text = [Localizer getLocalizedText:@"Infants"];
	
	cell.txt.text = val;
	cell.parentVC = self;
	cell.row = section;
	
	return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{

}

@end
