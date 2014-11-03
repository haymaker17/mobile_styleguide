//
//  ItinDetailsCellInfo.m
//  ConcurMobile
//
//  Created by Paul Kramer on 2/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ItinDetailsCellInfo.h"
#import "ExSystem.h" 

#import "FindAddress.h"
#import "MapViewController.h"
#import "ItinDetailsViewController.h"

@implementation ItinDetailsCellInfo
@synthesize labelPhone;
@synthesize labelMap;
@synthesize labelAddress1;
@synthesize labelAddress2;
@synthesize btnPhone;
@synthesize btnMap;
@synthesize imgVendor;
@synthesize mapAddress;
@synthesize rootVC;
@synthesize idVC;
@synthesize vendorName;
@synthesize vendorCode;
@synthesize phoneNumber;


NSString * const ITIN_DETAILS_CELL_INFO_REUSE_IDENTIFIER = @"ItinDetailsCellInfo"; // TODO: Make consistent with reusable identifier in Interface Builder!

-(IBAction)goSomeplace
{
//	// Create your query ...
//	NSString* searchQuery = mapAddress; 	
//	// Be careful to always URL encode things like spaces and other symbols that aren't URL friendly
//	searchQuery =  [searchQuery stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding];	
//	// Now create the URL string ...
//	NSString* urlString = [NSString stringWithFormat:@"http://maps.google.com/maps?q=%@", searchQuery];
//	// An the final magic ... openURL!
//	[[UIApplication sharedApplication] openURL:[NSURL URLWithString:urlString]];
	//NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:mapAddress, @"MAP_ADDRESS", nil];
	MapViewController *mapView = [[MapViewController alloc] init];
	mapView.mapAddress = mapAddress;
	mapView.anoTitle = vendorName;
	mapView.anoSubTitle = vendorCode;
	//NSLog(@"mapAddress = %@", mapAddress);
//	FindAddress *fa = [[FindAddress alloc] init];
//	fa.mapView = mapView;
//	[fa getLocationByAddress:mapAddress];
	[idVC presentViewController:mapView animated:YES completion:nil]; 

//	[pBag release];
//	[fa release];
}

-(IBAction)dialNumber
{
	[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", phoneNumber]]];
	//[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", @"4254975946"]]];
}


- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier 
{
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        // Initialization code
    }
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}




@end
