//
//  TripsCellBig.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/13/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "TripsCellBig.h"
#import "TripsViewController.h"
#import "TripDetailCell.h"


@implementation TripsCellBig

@synthesize label;
@synthesize labelDateRange;
@synthesize labelLocator;
@synthesize btnDetail;
@synthesize rootVC;
@synthesize currentRow;
@synthesize btnDrill;

@synthesize tripBits;
@synthesize keys;

- (IBAction)buttonDetailPressed:(id)sender
{
	[rootVC expandCell:sender detailType:@"Up" rowNumber:currentRow];
	
}

- (IBAction)buttonDrillPressed:(id)sender
{
	//[rootVC switchViews:sender ParameterBag:nil];	
}


- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        // Initialization code

    }
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}


- (void)dealloc {
	[label dealloc];
	[labelDateRange dealloc];
	[labelLocator dealloc];
	[btnDetail dealloc];
	[rootVC dealloc];
	//[currentRow dealloc];
	[tripBits dealloc]; 
	[keys dealloc];
	[super dealloc];
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [keys count];
    
}

- (NSInteger)tableView:(UITableView *)tableView 
 numberOfRowsInSection:(NSInteger)section
{
    if ([keys count] == 0)
        return 0;
    
    NSString *key = [keys objectAtIndex:section];
    NSArray *nameSection = [tripBits objectForKey:key];
    return [nameSection count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView 
         cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
    
    NSString *key = [keys objectAtIndex:section];
    NSArray *nameSection = [tripBits objectForKey:key];
	
	//Custom Cell that is uber sexy... I think
	TripDetailCell *cell = (TripDetailCell *)[tableView dequeueReusableCellWithIdentifier: @"TripDetailCell"];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TripDetailCell" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[TripDetailCell class]])
				cell = (TripDetailCell *)oneObject;
	}
	
	NSDictionary *rowDict = [nameSection objectAtIndex:row];
	NSString *header = [rowDict objectForKey:@"Header"]; 
	
	cell.rootCell = self;
	cell.currentRow = row;
	
	if (header != @"")
	{
		if (header == @"Flights")
		{
			cell.labelHead.textColor = [UIColor colorWithRed:(119. / 255.) green:(58. / 255.) blue:(141. / 255.) alpha:1.0];
		}
		else if (header == @"Car Rental")
		{
			cell.labelHead.textColor = [UIColor colorWithRed:(39. / 255.) green:(140. / 255.) blue:(154. / 255.) alpha:1.0];
		}
		else if (header == @"Hotels")
		{
			cell.labelHead.textColor = [UIColor colorWithRed:(136. / 255.) green:(70. / 255.) blue:(37. / 255.) alpha:1.0];
		}
		[cell.labelHead setHidden:NO];
		cell.labelHead.text = header; 
		[cell.imgHead setHidden:NO];
		NSString *imageHeader = [rowDict objectForKey:@"ImageHeader"]; 
		UIImage *gotImg = [UIImage imageNamed:imageHeader];
		[cell.imgHead setImage:gotImg];
		//[imageHeader release];
		//[gotImg release];
		[cell.labelActionTime setHidden:YES];
		[cell.labelVendor setHidden:YES];
		[cell.labelFromToLocation setHidden:YES];
		[cell.labelDetails setHidden:YES];
		[cell.labelVendorDetail setHidden:YES];
		[cell.btnAction setHidden:YES];
		[cell.imgVendor setHidden:YES];
	}
	else 
	{
		[cell.labelHead setHidden:YES];
		[cell.imgHead setHidden:YES];
		[cell.labelActionTime setHidden:NO];
		[cell.labelVendor setHidden:NO];
		[cell.labelFromToLocation setHidden:NO];
		[cell.labelDetails setHidden:NO];
		[cell.labelVendorDetail setHidden:NO];
		[cell.btnAction setHidden:YES];
		[cell.imgVendor setHidden:YES];
		cell.labelActionTime.text = [rowDict objectForKey:@"Section"];
		cell.labelVendor.text = [rowDict objectForKey:@"Label1"]; 
		cell.labelFromToLocation.text = [rowDict objectForKey:@"Label2"]; 
		cell.labelVendor.text = [rowDict objectForKey:@"Vendor"]; 
		NSString *vendorAction = [rowDict objectForKey:@"VendorAction"]; 
		if (vendorAction == nil)
		{
			cell.labelVendorDetail.text = [rowDict objectForKey:@"VendorDetail"]; 
		}
		else 
		{
			[cell.labelVendorDetail setHidden:YES];
			[cell.btnAction setHidden:NO];
			cell.btnAction.titleLabel.text = [rowDict objectForKey:@"VendorAction"];
		}
		//[vendorAction release];
		
		NSString *vendorImage = [rowDict objectForKey:@"VendorImage"]; 
		if (vendorImage != @"")
		{
			[cell.imgVendor setHidden:NO]; 
			[cell.labelVendor setHidden:YES];
			UIImage *venImg = [UIImage imageNamed:vendorImage];
			[cell.imgVendor setImage:venImg];
			//[venImg release];
		}
		//[vendorImage release];

	}

    //Static way of doing things below
//    static NSString *SectionsTableIdentifier = @"SectionsTableIdentifier";
//    
//    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:
//                             SectionsTableIdentifier ];
//    if (cell == nil) {
//        cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault 
//                                       reuseIdentifier: SectionsTableIdentifier ] autorelease];
//    }
//    
//    cell.textLabel.text = [nameSection objectAtIndex:row];
//	cell.textLabel.font = [UIFont systemFontOfSize:12.0];
//	UIImage *getimg = [UIImage imageNamed:@"flight.png"];
//	[cell setImage:getimg];
//	[getimg release];

    return cell;
}

//- (NSString *)tableView:(UITableView *)tableView 
//titleForHeaderInSection:(NSInteger)section
//{
//    if ([keys count] == 0)
//        return nil;
//	
//    NSString *key = [keys objectAtIndex:section];
//    if (key == UITableViewIndexSearch)
//        return nil;
//    
//    return key;
//}

//- (NSArray *)sectionIndexTitlesForTableView:(UITableView *)tableView
//{
//    return keys;
//}


#pragma mark -
#pragma mark Table View Delegate Methods
- (NSIndexPath *)tableView:(UITableView *)tableView 
  willSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView reloadData];
    return indexPath;
}

- (NSInteger)tableView:(UITableView *)tableView 
sectionForSectionIndexTitle:(NSString *)title 
               atIndex:(NSInteger)index
{
    NSString *key = [keys objectAtIndex:index];
    if (key == UITableViewIndexSearch)
    {
        [tableView setContentOffset:CGPointZero animated:NO];
        return NSNotFound;
    }
    else return index;
    
}

- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	if (row == 0) 
	{
		return 30;
	}
	else {
		return 50;
	}
}

@end
