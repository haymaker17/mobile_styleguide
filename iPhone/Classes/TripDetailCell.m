//
//  TripDetailCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/13/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "TripDetailCell.h"
#import "TripsCellBig.h"
#import "ExSystem.h" 

#import "MapViewController.h"
#import "WebViewController.h"
#import "TripDetailCellBig.h"

#import "ViewConstants.h"
#import "UIDevice+Additions.h"


@implementation TripDetailCell

@synthesize labelHead;
@synthesize labelActionTime;
@synthesize labelVendor;
@synthesize labelFromToLocation;
@synthesize labelDetails;
@synthesize labelVendorDetail;
@synthesize imgHead;
@synthesize imgVendor;
@synthesize btnDrill;
@synthesize btnAction;
@synthesize rootCell;
@synthesize currentRow;
@synthesize idKey;
//@synthesize rootVC;
@synthesize tripKey;
@synthesize segmentType;
@synthesize bookingKey;
@synthesize	segment;
@synthesize txtView, detail, dVC, aDetails, lblDanger;


- (IBAction)buttonDrillPressed:(id)sender
{
	NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:idKey, @"SegmentKey", tripKey, @"TripKey", segmentType, @"SegmentType", segment, @"Segment", nil];

	if ([segmentType isEqualToString:@"AIR"])
	{
		[ConcurMobileAppDelegate switchToView:ITIN_DETAILS_AIR viewFrom:TRIP_DETAILS ParameterBag:dict];
	}
	else if ([segmentType isEqualToString:@"HOTEL"])
	{
		[ConcurMobileAppDelegate switchToView:ITIN_DETAILS_AIR viewFrom:TRIP_DETAILS ParameterBag:dict];
	}
	else if ([segmentType isEqualToString:@"CAR"])
	{
		[ConcurMobileAppDelegate switchToView:ITIN_DETAILS_AIR viewFrom:TRIP_DETAILS ParameterBag:dict];
	}
	else if ([segmentType isEqualToString:@"PARKING"])
	{
		[ConcurMobileAppDelegate switchToView:ITIN_DETAILS_AIR viewFrom:TRIP_DETAILS ParameterBag:dict];
	}
	else if ([segmentType isEqualToString:@"RIDE"])
	{
		[ConcurMobileAppDelegate switchToView:ITIN_DETAILS_AIR viewFrom:TRIP_DETAILS ParameterBag:dict];
	}
	else if ([segmentType isEqualToString:@"DINING"] || [segmentType isEqualToString:@"RAIL"] || [segmentType isEqualToString:@"EVENT"])
	{
		[ConcurMobileAppDelegate switchToView:ITIN_DETAILS_AIR viewFrom:TRIP_DETAILS ParameterBag:dict];
	}
}

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        // Initialization code
    }
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated 
{

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}



#pragma mark -
#pragma mark Utility Menus
-(IBAction)goSomeplace:(id)sender
{
	UIButton *btn = (UIButton *)sender;
	int aPos = btn.tag;
	detail = aDetails[aPos];
	
	NSString *mapAddress = detail.mapAddress;
	NSString *vendorName = detail.codeName;
	//NSString *vendorCode = detail.code;
	MapViewController *mapView = [[MapViewController alloc] init];
	mapView.mapAddress = mapAddress;
	mapView.anoTitle = vendorName;
	mapView.anoSubTitle = mapAddress;
	//NSLog(@"mapAddress = %@", mapAddress);
	if([UIDevice isPad])
		mapView.modalPresentationStyle = UIModalPresentationFormSheet;
	
	[dVC presentViewController:mapView animated:YES completion:nil]; 
	
}


-(void)callNumber:(id)sender
{
	//(NSString *)phoneNum
	//[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", phoneNum]]];
}


-(IBAction)loadWebView:(id)sender
{
	//(NSString *)specialValueWeb WebViewTitle:(NSString *)webViewTitle
	//do web view
	UIButton *btn = (UIButton *)sender;
	int aPos = btn.tag;
	detail = aDetails[aPos];
	WebViewController *webView = [[WebViewController alloc] init];
//	webView.rootViewController = rootVC;
	webView.url = [NSString stringWithFormat:@"http://%@", detail.url];
	webView.viewTitle = detail.detailTitle;
	if([UIDevice isPad])
		webView.modalPresentationStyle = UIModalPresentationFormSheet;

	[dVC presentViewController:webView animated:YES completion:nil]; 
	
}
@end
