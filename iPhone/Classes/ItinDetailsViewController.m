//
//  ItinDetailsViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/16/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import "ItinDetailsViewController.h"
#import "ItinDetailsHeaderCell.h"
#import "ItinDetailsCell.h"
#import "ItinDetailsAirCell.h"
#import "ExSystem.h" 

#import "SampleData.h"
#import "ViewConstants.h"
#import "ConcurMobileAppDelegate.h"
#import "WebViewController.h"
#import "FormatUtils.h"
#import "MapViewController.h"
#import "ItinDetailsParkingCell.h"
#import "TripAirSegmentCell.h"
#import "ItinDetailsHotelCell2.h"
#import "TripData.h"
#import "HotelImagesData.h"
#import "FlightStatsViewController.h"
#import "LabelConstants.h"
#import "MobileAlertView.h"
#import "HotelCancel.h"
#import "ViolationDetailsVC.h"
#import "AirCancel.h"
#import "CarCancel.h"
#import "AppsUtil.h"
#import "EntitySegmentLocation.h"
#import "FlightScheduleVC.h"
#import "AmtrakCancel.h"
#import "Config.h"

#if defined(CORP) & defined(ENTERPRISE) & defined(PASSBOOK)
#import "ItinPassbookCell.h"

#endif

#define kAlertConfirmCancelHotel 18541
#define kAlertConfirmCancelCar 18543
#define kAlertConfirmCancelAmtrak 18545
#define kAlertViewHotelCancelSuccessMessage 12828


@interface ItinDetailsViewController ()
#if defined(CORP) & defined(ENTERPRISE) & defined(PASSBOOK)
+ (NSString *) obtainPassDir;
-(void)addToPassbook:(id)sender;
-(void) addToPassBookWithData:(NSData*) data;
#endif
-(void) setupToolbar:(NSDate*) dateOfData;
@end

@implementation ItinDetailsViewController 
@synthesize lblHeading, lblSub1, lblSub2, lblSub3, ivHeaderImage, viewHotelHeader, activityImage, aHotelImageViews, ivHeaderBackground, ivHotelBackground;
@synthesize lblAirConfirm, lblAirline, lblDepart, lblDepartAMPM, lblDepartDate, lblDepartTime, lblDepartTerminal, lblArrive, lblArriveAMPM, lblArriveDate, lblArriveTerminal, lblArriveTime, lblHeadingAir, viewAirHeader, lblArriveGate, lblDepartGate;
@synthesize listData;
@synthesize tableList;
@synthesize navBar;
@synthesize carrierURL;
@synthesize tripKey;
@synthesize segmentKey;
@synthesize segmentType;
@synthesize segment;

@synthesize labelVendor;
@synthesize labelOperatedBy;
@synthesize labelStart;
@synthesize labelEnd;
@synthesize labelLocator;
@synthesize imgHead;
@synthesize labelStartLabel;
@synthesize labelEndLabel;
@synthesize labelLocatorLabel;

@synthesize imgBar1;
@synthesize imgBar2;
@synthesize labelBorder;

@synthesize lvVendor;
@synthesize lvTerminal;
@synthesize lvTerminalValue;
@synthesize lvGate;
@synthesize lvGateValue;
@synthesize lvSeat;
@synthesize lvSeatValue1;
@synthesize lvSeatValue2;
@synthesize lvVendorFlight;
@synthesize lvOperatedBy;
@synthesize lvDeparture;
@synthesize lvArrival;
@synthesize lvFirstName;
@synthesize lvLastName;
@synthesize lvFrequentFlyerNum;
@synthesize lvCarrierFlight;
@synthesize lvClassGate;
@synthesize lvTime;
@synthesize imgBoardingPass;
@synthesize imgVendor, sections, hotelImagesArray, pagePos;
@synthesize	fetchView;
@synthesize	lblFetch;
@synthesize	spinnerFetch;//, hotelCell;
@synthesize	dictHotelImages;
@synthesize	dictHotelImageURLs, trip;
@synthesize keys;

// AJC - BEGIN - please delete this code if present past 2013-12-13
//@synthesize session;
// AJC - END - please delete this code if present past 2013-12-13

NSString * const INFO_NIB = @"ItinDetailsCellInfo";
NSString * const LABEL_NIB = @"ItinDetailsCellLabel";
NSString * const ITIN_DETAILS_VIEW = @"ITIN_DETAILS_VIEW";

// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}


-(NSString *)getViewIDKey
{
	return ITIN_DETAILS;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


-(void) makeRefreshButton:(NSString *)dtRefreshed
{
    if(![ExSystem connectedToNetwork])
        return;
    // MOB-10969 Make this last updated message look the same as in previous screen, 2 lines, align right.
	int refreshDateWidth = 160; //280;
	int refreshDateHeight = 30;//20;
	int numberOfLines = 2;//1;

// AJC - BEGIN - please delete this code if present past 2013-12-13
//	if ([self shouldShowFlightStatsButton:dtRefreshed] || [self shouldShowCancelHotelButton] || [self shouldShowCancelCarButton])
//	{
//		refreshDateWidth = 160;
//		refreshDateHeight = 30;
//		numberOfLines = 2;
//	}
// AJC - END - please delete this code if present past 2013-12-13
    
	UIView *cv = [[UIView alloc] initWithFrame:CGRectMake(0, 0, refreshDateWidth, refreshDateHeight)];
	UILabel *lblText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, refreshDateWidth, refreshDateHeight)];
	lblText.numberOfLines = numberOfLines;
	lblText.lineBreakMode = NSLineBreakByWordWrapping;
	lblText.textAlignment = NSTextAlignmentRight;
	lblText.text = dtRefreshed;
    
    if( false == [ExSystem is7Plus] )
    {
        // iOS6 shows a black shadow behind white refresh text
        // iOS7 shows this as white text on white background so leave colors default
        [lblText setBackgroundColor:[UIColor clearColor]];
        [lblText setTextColor:[UIColor whiteColor]];
        [lblText setShadowColor:[UIColor blackColor]];
        [lblText setShadowOffset:CGSizeMake(0, -1)];
    }
	
    [lblText setFont:[UIFont boldSystemFontOfSize:12.0f]];
	[cv addSubview:lblText];
	
	[lblFetch setText:[Localizer getLocalizedText:@"Fetching Data"]];
	UIBarButtonItem *btnRefresh = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemRefresh target:self action:@selector(refreshData)];
	UIBarButtonItem *btnRefreshDate = [[UIBarButtonItem alloc] initWithCustomView:cv];
	UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	
	NSArray *toolbarItems = nil;
    if ([trip.allowCancel boolValue] && ![segmentType isEqualToString:SEG_TYPE_AIR])
    {
        if ([self shouldShowCancelHotelButton])
        {
            UIBarButtonItem *btnCancelHotel = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Cancel Hotel"] style:UIBarButtonItemStyleBordered target:self action:@selector(cancelHotelPressed)];
            toolbarItems = @[btnCancelHotel, flexibleSpace, btnRefreshDate, btnRefresh];
        }
        else if ([self shouldShowCancelCarButton])
        {
            UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Cancel Car"] style:UIBarButtonItemStyleBordered target:self action:@selector(cancelCarPressed)];
            toolbarItems = @[btnCancel, flexibleSpace, btnRefreshDate, btnRefresh];
        }
        else if ([self shouldShowCancelAmtrakButton])
        {
            UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Cancel Rail"] style:UIBarButtonItemStyleBordered target:self action:@selector(cancelAmtrakPressed)];
            toolbarItems = @[btnCancel, flexibleSpace, btnRefreshDate, btnRefresh];
        }
    }
    else
	{
		toolbarItems = @[flexibleSpace, btnRefreshDate, btnRefresh];
	}
    
    if([toolbarItems count] > 0)
        [self setToolbarItems:toolbarItems animated:NO];
	

}

-(BOOL) shouldShowFlightStatsButton:(NSString*)dtRefreshed
{
	return ([dtRefreshed length] > 0 && segmentType!= nil && [segmentType isEqualToString:SEG_TYPE_AIR]);
}

-(BOOL) shouldShowCancelHotelButton
{
    if(![ExSystem connectedToNetwork])
        return false;
	return (segmentType!= nil && [segmentType isEqualToString:SEG_TYPE_HOTEL]);
}


-(BOOL) shouldShowCancelCarButton
{
    if(![ExSystem connectedToNetwork])
        return false;
	return (segmentType!= nil && [segmentType isEqualToString:SEG_TYPE_CAR]);
}

-(BOOL) shouldShowCancelAmtrakButton
{
    // MOB-18621 Disable cancel rail on Mobile client
    return NO;
}

-(void) refreshData
{
    [self showLoadingViewWithText:[Localizer getLocalizedText:@"Refreshing Data"]];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", @"YES", @"REFRESHING", trip.itinLocator ,@"ITIN_LOCATOR",
								 tripKey, @"TripKey", segmentKey, @"SegmentKey", segmentType, @"SegmentType", nil];
	[[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void) showFlightStats
{
	FlightStatsViewController *flightStatsVC = [[FlightStatsViewController alloc] initWithNibName:@"FlightStatsViewController" bundle:nil];
	flightStatsVC.tripKey = tripKey;
	flightStatsVC.segmentKey = segmentKey;
	[self presentViewController:flightStatsVC animated:YES completion:nil];
}

-(void) showFlightSchedule
{
    FlightScheduleVC *vc = [[FlightScheduleVC alloc] initWithNibName:@"FlightScheduleVC" bundle:nil];
    vc.segment = segment;
    
    UIBarButtonItem *backButton = [[UIBarButtonItem alloc]
                                   initWithTitle: [Localizer getLocalizedText:@"Flight Details"]
                                   style: UIBarButtonItemStyleBordered
                                   target: nil action: nil];
    
    [self.navigationItem setBackBarButtonItem: backButton];
    
    
    [self.navigationController pushViewController:vc animated:YES];
    
}   

#pragma mark -
#pragma mark Hotel Cancel
-(void) cancelHotelPressed
{
    UIAlertView *alert = nil;
    if ([Config isGov])
    {
        alert = [[MobileAlertView alloc]
                 initWithTitle:[Localizer getLocalizedText:@"Please Confirm"]
                 message:[Localizer getLocalizedText:@"Select 'OK' if you are sure you want to cancel this hotel reservation."]
                 delegate:self
                 cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
                 otherButtonTitles:[Localizer getLocalizedText:LABEL_OK_BTN], nil];
    }
    else
    {
        alert = [[MobileAlertView alloc]
                 initWithTitle:[Localizer getLocalizedText:@"Please Confirm"]
                 message:[Localizer getLocalizedText:@"Are you sure you want to cancel this hotel reservation?"]
                 delegate:self
                 cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
                 otherButtonTitles:[Localizer getLocalizedText:LABEL_OK_BTN], nil];
    }
	alert.tag = kAlertConfirmCancelHotel;
	[alert show];
}

-(void) cancelCarPressed
{
    UIAlertView *alert = nil;
    if ([Config isGov])
    {
        alert = [[MobileAlertView alloc]
                 initWithTitle:[Localizer getLocalizedText:@"Please Confirm"]
                 message:[Localizer getLocalizedText:@"Select 'OK' if you are sure you want to cancel this car reservation."]
                 delegate:self
                 cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
                 otherButtonTitles:[Localizer getLocalizedText:LABEL_OK_BTN], nil];
    }
    else
    {
        alert = [[MobileAlertView alloc]
                 initWithTitle:[Localizer getLocalizedText:@"Please Confirm"]
                 message:[Localizer getLocalizedText:@"Are you sure you want to cancel this car reservation?"]
                 delegate:self
                 cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
                 otherButtonTitles:[Localizer getLocalizedText:LABEL_OK_BTN], nil];
    }
	alert.tag = kAlertConfirmCancelCar;
	[alert show];
}

-(void) cancelAmtrakPressed
{
	UIAlertView *alert = [[MobileAlertView alloc]
						  initWithTitle:[Localizer getLocalizedText:@"Please Confirm"]
						  message:[Localizer getLocalizedText:@"Are you sure you want to cancel this rail reservation?"]
						  delegate:self
						  cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
						  otherButtonTitles:[Localizer getLocalizedText:LABEL_OK_BTN], nil];
	alert.tag = kAlertConfirmCancelAmtrak;
	[alert show];
}

-(void) cancelHotel
{
    if(![ExSystem connectedToNetwork])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Operation Not Supported Offline"]
							  delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                              otherButtonTitles:nil];
		[alert show];
    }
    else
    {
        NSMutableDictionary *pBag = [self makeCancellationParameterBag];
        
        pBag[@"Reason"] = @"Hotel cancelled from mobile device";
        
        [[ExSystem sharedInstance].msgControl createMsg:HOTEL_CANCEL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
        
        [self showWaitView];
    }
}

-(void) cancelCar
{
    if(![ExSystem connectedToNetwork])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Operation Not Supported Offline"]
							  delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                              otherButtonTitles:nil];
		[alert show];
    }
    else
    {
        NSMutableDictionary *pBag = [self makeCancellationParameterBag];
        
        pBag[@"Reason"] = @"Car cancelled from mobile device";

        [[ExSystem sharedInstance].msgControl createMsg:CAR_CANCEL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
        
        [self showWaitView];
    }
}

-(void) cancelAmtrak
{
    if(![ExSystem connectedToNetwork])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Operation Not Supported Offline"]
							  delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                              otherButtonTitles:nil];
		[alert show];
    }
    else
    {
        NSMutableDictionary *pBag = [self makeCancellationParameterBag];
        
        pBag[@"Reason"] = @"Rail cancelled from mobile device";
        
        [[ExSystem sharedInstance].msgControl createMsg:AMTRAK_CANCEL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES Options:SILENT_ERROR RespondTo:self];
        
        [self showWaitView];
    }
}

-(NSMutableDictionary*) makeCancellationParameterBag
{
	NSString *tripId = (trip.cliqbookTripId == nil ? @"" : trip.cliqbookTripId);
	
	NSString *recordLocator = @"";
	EntityBooking* primaryBooking = [TripData getPrimaryBooking:trip];
	if (primaryBooking != nil && primaryBooking.recordLocator != nil)
		recordLocator = primaryBooking.recordLocator;
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:tripId, @"TripId"
								 ,self.segmentKey, @"SegmentKey"
								 ,primaryBooking.bookSource, @"BookingSource"
								 ,recordLocator, @"RecordLocator"
								 , nil];
    return pBag;
}

#pragma mark -
#pragma mark Alert Methods
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	if (alertView.tag == kAlertConfirmCancelHotel)
	{
		if (buttonIndex == 1)
			[self cancelHotel];
	}
    else if (alertView.tag == kAlertConfirmCancelCar)
	{
		if (buttonIndex == 1)
			[self cancelCar];
	}
    else if (alertView.tag == kAlertConfirmCancelAmtrak)
    {
		if (buttonIndex == 1)
			[self cancelAmtrak];
    }
    else if (alertView.tag == kAlertViewHotelCancelSuccessMessage)
    {
        [self navigateAfterSuccessfulHotelCancellation];
    }
}


#pragma mark -
#pragma mark MobileViewController RespondToFoundData
-(void)respondToFoundData:(Msg *)msg
{//respond to data that might be coming from the cache

#if defined(CORP) & defined(ENTERPRISE) & defined(PASSBOOK)
    if ([msg.idKey isEqualToString:@"PASSBOOK_DOWNLOAD"])
    {
        // TODO - handle error
        NSData *mydata = msg.data;
        [self addToPassBookWithData:mydata];
        return;
    }
#endif
	if ([msg.idKey isEqualToString:HOTEL_CANCEL])
	{
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
		
		HotelCancel *hotelCancel = (HotelCancel *)msg.responder;
		if([hotelCancel isKindOfClass:[HotelCancel class]] && hotelCancel.isSuccess && ![UIDevice isPad])
		{
            NSString *confirmationText = hotelCancel.cancellationNumber && [hotelCancel.cancellationNumber length] ?
                [NSString stringWithFormat:[@"Your hotel reservation has been successfully cancelled. Cancellation Number: %@" localize],hotelCancel.cancellationNumber] :
                [@"Concur was unable to obtain a cancellation number for your hotel cancellation. In order to obtain a cancellation number" localize];
            
            MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[@"Booking cancelled" localize]
                                                                    message:confirmationText
                                                                   delegate:self
                                                          cancelButtonTitle:[LABEL_CLOSE_BTN localize]
                                                          otherButtonTitles: nil];
            alert.tag = kAlertViewHotelCancelSuccessMessage;
            [alert show];
		}
        else if ([hotelCancel isKindOfClass:[HotelCancel class]] && !hotelCancel.isSuccess)
        {
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:([hotelCancel.errorMessage length] ? [@"Cancel Failed" localize] : nil)
                                                                 message:([hotelCancel.errorMessage length] ? hotelCancel.errorMessage : [@"Cancel Failed" localize])
                                                                delegate:nil
                                                       cancelButtonTitle:[LABEL_OK_BTN localize]
                                                       otherButtonTitles: nil];
            [av show];
        }
	}
    else if ([msg.idKey isEqualToString:AMTRAK_CANCEL])
    {
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
        
        AmtrakCancel *amtrakCancel = (AmtrakCancel *)msg.responder;
        if([amtrakCancel isKindOfClass:[AmtrakCancel class]] && amtrakCancel.isSuccess)
        {
            TripsViewController *parentVC = (self.navigationController.viewControllers)[[self.navigationController.viewControllers count] - 3];
            if([parentVC isKindOfClass:[TripsViewController class]])
            {
                [parentVC refreshData];
                [self.navigationController popToViewController:parentVC animated:YES];
            }
            else
            {
                UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
                // Force home screen refresh
                if ([homeVC respondsToSelector:@selector(refreshTripsData)])
                {
                    [homeVC performSelector:@selector(refreshTripsData) withObject:nil];
                }
                [self.navigationController popToRootViewControllerAnimated:YES];
            }
        }
        else
        {
            NSString *errorTitle = nil;
            NSString *errorMessage = [Localizer getLocalizedText:@"Cancel Failed"];
            if (amtrakCancel.errorMessage != nil && amtrakCancel.errorMessage.length > 0)
            {
                errorTitle = [Localizer getLocalizedText:@"Cancel Failed"];
                errorMessage = amtrakCancel.errorMessage;
            }
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:errorTitle message:errorMessage delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles: nil];
            [av show];
        }
    }
    else if ([msg.idKey isEqualToString:AIR_CANCEL])
	{
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
		
		AirCancel *airCancel = (AirCancel *)msg.responder;
		if([airCancel isKindOfClass:[AirCancel class]] && airCancel.isSuccess && ![UIDevice isPad])
		{
			NSMutableDictionary *segs = [TripData getSegmentsOrderByDate:trip];
			if([segs count] > 0)
			{
				TripsViewController *parentVC = (self.navigationController.viewControllers)[[self.navigationController.viewControllers count] - 3];
				if([parentVC isKindOfClass:[TripsViewController class]])
				{
					[parentVC refreshData];
					[self.navigationController popToViewController:parentVC animated:YES];
				}
                else
                {
                    UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
                    // Force home screen refresh
                    if ([homeVC respondsToSelector:@selector(refreshTripsData)])
                    {
                        [homeVC performSelector:@selector(refreshTripsData) withObject:nil];
                    }
                    [self.navigationController popToRootViewControllerAnimated:YES];
                }
			}
			else 
			{
				TripsViewController *parentVC = (self.navigationController.viewControllers)[[self.navigationController.viewControllers count] - 3];
				if([parentVC isKindOfClass:[TripsViewController class]])
				{
					[parentVC refreshData];
					[self.navigationController popToViewController:parentVC animated:YES];
				}
                else
                {
                    UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
                    // Force home screen refresh
                    if ([homeVC respondsToSelector:@selector(refreshTripsData)])
                    {
                        [homeVC performSelector:@selector(refreshTripsData) withObject:nil];
                    }
                    [self.navigationController popToRootViewControllerAnimated:YES];
                }
			}
		}
        else
        {
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Cancel Failed"] message:[Localizer getLocalizedText:@"Cancel Failed"] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles: nil];
            [av show];
        }
	}
    else 	if ([msg.idKey isEqualToString:CAR_CANCEL])
	{
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
		
		CarCancel *carCancel = (CarCancel *)msg.responder;
		if([carCancel isKindOfClass:[CarCancel class]] && carCancel.isSuccess && ![UIDevice isPad])
		{
			NSMutableDictionary *segs = [TripData getSegmentsOrderByDate:trip];
            // MOB-11341 - log flurry event
            //Type:<Hotel, Car, Air, Train>, ItemsLeftInItin:<count>
            NSDictionary *dictionary = @{@"Type": @"Car", @"ItemsLeftInItin": [NSString stringWithFormat:@"%d", [segs count]]};
            [Flurry logEvent:@"Book: Cancel" withParameters:dictionary];

            if([segs count] > 1)
            {
                [self navigateBackToViewControllerOf:[TripDetailsViewController class]];
            }
            else
            {
                [self navigateBackToViewControllerOf:[TripsViewController class]];
            }
		}
        else if([carCancel isKindOfClass:[CarCancel class]] && !carCancel.isSuccess)
        {
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:([carCancel.errorMessage length] ? [@"Cancel Failed" localize] : nil)
                                                                 message:([carCancel.errorMessage length] ? carCancel.errorMessage : [@"Cancel Failed" localize])
                                                                delegate:nil
                                                       cancelButtonTitle:[LABEL_OK_BTN localize]
                                                       otherButtonTitles: nil];
            [av show];
        }
	}
	else if ([msg.idKey isEqualToString:HOTEL_IMAGES] && segment != nil)
	{
		HotelImagesData *hid = (HotelImagesData *)msg.responder;
		if([hid.keys count] > 0)
		{
            //this means that we do have images for this hotel
			HotelImagesData *hids = (HotelImagesData *)msg.responder; //should be an array of URLs
			[self fillImageURLs:hids.keys]; //start the image fetching of the URLs

			if( hids != nil && [hids.keys count] > 0)
			{
				HotelImageData *hid = (hids.keys)[0];
				id path = hid.hotelImage; 
				NSURL *url = [NSURL URLWithString:path];
				NSData *data = [NSData dataWithContentsOfURL:url];
				UIImage *img = [[UIImage alloc] initWithData:data];
				ivHeaderImage.image = img;
                [activityImage stopAnimating];
			}
            else
                [activityImage stopAnimating];
		}
        else
            [activityImage stopAnimating];

	}
    else if ([msg.idKey isEqualToString:CAR_IMAGE] && msg.parameterBag != nil && segment != nil)
	{
        UIImage *gotImg = [[UIImage alloc] initWithData:msg.data];
        if (gotImg != nil) 
        {
            UIImage *imgScaled = [self scaleImageToFit:gotImg MaxW:73 MaxH:73]; 
            ivHeaderImage.image = imgScaled;
            [activityImage stopAnimating];
        }
        
        [activityImage stopAnimating];
    }
	else if ([msg.idKey isEqualToString:IMAGE] && msg.parameterBag != nil && segment != nil)
	{
		NSString *goesTo = (msg.parameterBag)[@"GOES_TO"];
		if ([goesTo isEqualToString:@"INFO"])
		{
			//update the info box
			UIImage *gotImg = [[UIImage alloc] initWithData:msg.data];
			ItinDetailsCellInfo *cell = (ItinDetailsCellInfo *)msg.cell;
			[cell.imgVendor setImage:gotImg];
			[cell.imgVendor setHidden:NO];
		}
		else if((msg.parameterBag)[@"IMAGE_VIEW"] != nil)
		{
			UIImage *gotImg = [[UIImage alloc] initWithData:msg.data];
			UIImageView *iv = (msg.parameterBag)[@"IMAGE_VIEW"];
			iv.image = gotImg;
		}
	}
	else if ([msg.idKey isEqualToString:VENDOR_IMAGE] && msg.parameterBag != nil && segment != nil)
	{//segment should already be set
        // Should be able to build a lookip table for this. -prs
		if ((msg.parameterBag)[VENDOR_IMAGE] != nil)
		{
			NSString *vType = nil;
			if ([segment.type isEqualToString:SEG_TYPE_HOTEL])
			{
				vType = @"h";
			}
			else if ([segment.type isEqualToString:SEG_TYPE_AIR])
			{
				vType = @"a";
			}
			else if ([segment.type isEqualToString:SEG_TYPE_RIDE])
			{
				vType = @"l";
			}
			else if ([segment.type isEqualToString:SEG_TYPE_DINING])
			{
				vType = @"d";
			}
			else if ([segment.type isEqualToString:SEG_TYPE_EVENT])
			{
				vType = @"e";
			}
			else if ([segment.type isEqualToString:SEG_TYPE_RAIL])
			{
				vType = @"r";
			}
			else if ([segment.type isEqualToString:SEG_TYPE_CAR])
			{
				vType = @"c";
			}
			else if ([segment.type isEqualToString:SEG_TYPE_PARKING])
			{
				vType = @"p";
			}
			else 
			{
				vType = @"";
			}

			NSString *imageFileNameWithType = [NSString stringWithFormat:@"[%@]%@.gif",vType, segment.vendor];
			NSString *pBagName = (msg.parameterBag)[VENDOR_IMAGE];
			if ([pBagName isEqualToString:imageFileNameWithType])
			{
				//update the vendor
				UIImage *gotImg = [[UIImage alloc] initWithData:msg.data];
				ItinDetailsCellLabel *cell = (ItinDetailsCellLabel *)msg.cell;
				[cell.imgView setImage:gotImg];
				[cell.imgView setHidden:NO];
			}
		}
		
	}
	else if ([msg.idKey isEqualToString:TRIPS_DATA] && (msg.parameterBag)[@"TO_VIEW"] != nil)
	{//below is the pattern of getting the object you want and using it.
        NSString *tKey = (msg.parameterBag)[@"TripKey"];
        
        if (![tKey lengthIgnoreWhitespace] && ![self.tripKey lengthIgnoreWhitespace])
            return;
        
		[fetchView setHidden:YES];
        [self hideLoadingView];
		[spinnerFetch stopAnimating];
		
        //NSString *sKey = [msg.parameterBag objectForKey:@"SegmentKey"];
        
        if ([tKey lengthIgnoreWhitespace])
        {
            self.tripKey = (msg.parameterBag)[@"TripKey"];
            self.segmentKey = (msg.parameterBag)[@"SegmentKey"];
            self.segmentType = (msg.parameterBag)[@"SegmentType"];
        }
        
        // MOB-11059 reload trip and segment
        self.trip = [[TripManager sharedInstance] fetchByTripKey:self.tripKey];//[tripsData.trips objectForKey:key];
        self.segment = [[TripManager sharedInstance] fetchSegmentByIdKey:self.segmentKey tripKey:self.tripKey];
            /* MOB-5560
             Fixed by doing self.segment when loading the segment.  This problem was caused by some old code that is still in play that needed to be updated to use self.  The ultimate problem was that the segment was not getting deallocated when it needed to be and then double deallocated when you did a refresh.*/
		
		if(sections != nil)
			[sections removeAllObjects];
		
        if(segment.confirmationNumber == nil)
            segment.confirmationNumber = @"--";
        
        if([segmentType isEqualToString:SEG_TYPE_HOTEL])
            [self configureHotelHeader];
        else if([segmentType isEqualToString:SEG_TYPE_RIDE])
            [self configureRideHeader];
        else if([segmentType isEqualToString:SEG_TYPE_CAR])
            [self configureCarHeader];
        else if([segmentType isEqualToString:SEG_TYPE_AIR])
            [self configureAirHeader];
        else if([segmentType isEqualToString:SEG_TYPE_RAIL])
            [self configureRailHeader];
        else if([segmentType isEqualToString:SEG_TYPE_PARKING])
            [self configureParkingHeader];
        
		if ([segmentType isEqualToString:SEG_TYPE_PARKING])
			 [self fillParkingSections];
		else if ([segmentType isEqualToString:SEG_TYPE_AIR])
			  [self fillAirSections];
		else if([segmentType isEqualToString:SEG_TYPE_CAR])
			[self fillCarSections];
		else if([segmentType isEqualToString:SEG_TYPE_HOTEL])
			[self fillHotelSections];	
		else if([segmentType isEqualToString:SEG_TYPE_RIDE])
			[self fillRideSections];
		else if([segmentType isEqualToString:SEG_TYPE_DINING])
			[self fillDiningSections];
		else if([segmentType isEqualToString:SEG_TYPE_RAIL])
			[self fillRailSections];
		
        [self setupToolbar:msg.dateOfData];
		[tableList reloadData];
	}
}


-(void) navigateAfterSuccessfulHotelCancellation
{
    NSMutableDictionary *segs = [TripData getSegmentsOrderByDate:trip];
    // MOB-11341 - log flurry event
    //Type:<Hotel, Car, Air, Train>, ItemsLeftInItin:<count>
    NSDictionary *dictionary = @{@"Type": @"Hotel", @"ItemsLeftInItin": [NSString stringWithFormat:@"%d", [segs count]]};
    [Flurry logEvent:@"Book: Cancel" withParameters:dictionary];
    
    //
    // Hotel data segment is not deleted yet. It will be deleted after call with "TRIPS_DATA"
    // So check for atleast 1 segment in case there is only Hotel booking
    if([segs count] > 1)
    {
        [self navigateBackToViewControllerOf:[TripDetailsViewController class]];
    }
    else
    {
        [self navigateBackToViewControllerOf:[TripsViewController class]];
    }
}

// This function pops VCs from Navigation Controller until either 'class' or 'TripsViewController' or 'Home' is reached.
-(void) navigateBackToViewControllerOf:(Class)class
{
    if ([self.navigationController.viewControllers count])
    {
        for (int i = [self.navigationController.viewControllers count] - 1; i >= 0; i--)
        {
            id parentVC = self.navigationController.viewControllers[i];
            if ([parentVC isKindOfClass:class] || [parentVC isKindOfClass:[TripsViewController class]])
            {
                if ([parentVC respondsToSelector:@selector(refreshData)]) 
                    [parentVC refreshData];
                [self.navigationController popToViewController:parentVC animated:YES];
                return;
            }
        }
        // Navigation backwards to specified 'class' unsuccessful, so Navigate back to Home Screen
        if ([self.navigationController.viewControllers[0] respondsToSelector:@selector(refreshTripsData)])
            [self.navigationController.viewControllers[0] refreshTripsData];
        [self.navigationController popToRootViewControllerAnimated:YES];
    }
}

-(void) setupToolbar:(NSDate*) dateOfData
{
    if (self.isTripApproval) {
        [self setToolbarItems:nil];
        return;
    }
    
    NSString *dt = [DateTimeFormatter formatDateTimeMediumByDateLTZ:dateOfData];
    dt = [NSString stringWithFormat:[Localizer getLocalizedText:@"Last updated"], dt];

    if(![ExSystem connectedToNetwork])
	{
		[self makeOfflineBarWithLastUpdateMsg:dt];
	}
    else
    {
		[self makeRefreshButton:dt];
    }

}
-(void) loadSegment:(NSString *) key Trip:(EntityTrip *)trip SegmentKey:(NSString *)segKey Segment:(EntitySegment *)seg SegmentType:(NSString *)segType
{
	[fetchView setHidden:YES];
	[spinnerFetch stopAnimating];
    [self hideLoadingView];

	if(sections != nil)
		[sections removeAllObjects];
	
	tripKey = key;
	segmentKey = segKey;
	segmentType = segType;
	
	self.segment = seg;
	
	if ([segmentType isEqualToString:SEG_TYPE_PARKING])
		[self fillParkingSections];
	else if ([segmentType isEqualToString:SEG_TYPE_AIR])
		[self fillAirSections];
	else if([segmentType isEqualToString:SEG_TYPE_CAR])
		[self fillCarSections];
	else if([segmentType isEqualToString:SEG_TYPE_HOTEL])
		[self fillHotelSections];	
	else if([segmentType isEqualToString:SEG_TYPE_RIDE])
		[self fillRideSections];
	else if([segmentType isEqualToString:SEG_TYPE_DINING])
		[self fillDiningSections];
	else if([segmentType isEqualToString:SEG_TYPE_RAIL])
		[self fillRailSections];
	
	[tableList reloadData];
}

#pragma mark - View lifecycle
-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    //MOB-12418 Concur app. freezes in flight itinerary upon hitting action "+" key after visiting Alternative Flights
    //Alternative Flights view hide the tool bar from previous view. That causes action showFromToolBar to 'freezes'
    [self.navigationController setToolbarHidden:NO];
}

- (void)viewDidAppear:(BOOL)animated 
{
	// AJC - please delete the below code if present past 2013-12-13
    //[tableList setContentOffset:CGPointMake(0, 0) animated:NO];
	
	[super viewDidAppear:animated];
    
    // AJC - please delete the below code if present past 2013-12-13
	//[session resume];
	
	if([segment.type isEqualToString:SEG_TYPE_AIR])
	{
		self.title = [NSString stringWithFormat:@"%@ to %@", segment.relStartLocation.airportCity, segment.relEndLocation.airportCity];
	}
	else if([segment.type isEqualToString:SEG_TYPE_CAR])
	{
		self.title = segment.relStartLocation.airportName;
        if (![self.title length])
            self.title = [Localizer getLocalizedText:CAR_DETAILS];
	}
	else if([segment.type isEqualToString:SEG_TYPE_HOTEL])
	{
		self.title = segment.segmentName;
	}
}


- (void)viewDidLoad 
{
    [super viewDidLoad];
    
	if([UIDevice isPad])
		self.contentSizeForViewInPopover = CGSizeMake(480.0, 480.0);
	
	self.title = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_SEGMENT_DETAILS"];
	UINavigationItem *navItem;
	navItem = [UINavigationItem alloc];
	
	UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 320, 44)];
	label.backgroundColor = [UIColor clearColor];
	label.font = [UIFont boldSystemFontOfSize:20.0];
	label.shadowColor = [UIColor colorWithWhite:0.0 alpha:0.5];
	label.textAlignment = NSTextAlignmentCenter;
	label.textColor =[UIColor whiteColor];
	label.text=self.title;		
	navItem.titleView = label;
	
	[navBar pushNavigationItem:navItem animated:YES];
	[navBar setDelegate:self]; 
	
	sections = [[NSMutableArray alloc] initWithObjects:nil];
	
	[self makeRefreshButton:@""];
	[fetchView setHidden:YES];
    [self hideLoadingView];
    
    [viewHotelHeader setHidden:YES];
    [viewAirHeader setHidden:YES];
    
    // AJC - BEGIN - please delete this code if present past 2013-12-13
    //NSDictionary *dictionary = @{@"Type": self.segment.type};
    //[Flurry logEvent:@"Itin: View Segment" withParameters:dictionary];
    // AJC - END - please delete this code if present past 2013-12-13
}

// AJC - BEGIN - please delete this code if present past 2013-12-13
/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {
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

/*
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
}
*/
// AJC - END - please delete this code if present past 2013-12-13


- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
    self.lblSub1 = nil;
    self.lblHeading = nil;
    self.lblSub2 = nil;
    self.lblSub3 = nil;
    self.ivHeaderImage = nil;
    self.viewHotelHeader = nil;
    self.ivHeaderBackground = nil;
    self.ivHotelBackground = nil;
    self.lblAirConfirm = nil;
    self.lblAirline = nil;
    self.lblDepart = nil;
    self.lblDepartAMPM = nil;
    self.lblDepartDate = nil;
    self.lblDepartTime = nil;
    self.lblDepartTerminal = nil;
    self.lblArrive = nil;
    self.lblArriveAMPM = nil;
    self.lblArriveDate = nil;
    self.lblArriveTerminal = nil;
    self.lblArriveTime = nil;
    self.lblHeadingAir = nil;
    self.viewAirHeader = nil;
    
    self.lblArriveGate = nil;
    self.lblDepartGate = nil;
}






-(UIImage *)getSegmentTypeIconImage:(NSString *)segType
{
	NSString *imageHeader = @"";
	if ([segmentType isEqualToString:SEG_TYPE_AIR])
	{
		imageHeader = @"airfare_24X24_PNG";
	}
	else if ([segmentType isEqualToString:SEG_TYPE_CAR])
	{
		imageHeader = @"rental_car_24X24_PNG";
	}
	else if ([segmentType isEqualToString:SEG_TYPE_HOTEL])
	{
		imageHeader = @"hotel_24X24_PNG";
	}
	else if ([segmentType isEqualToString:SEG_TYPE_DINING])
	{
		imageHeader = @"dining_24X24_PNG";
	}
	else if ([segmentType isEqualToString:SEG_TYPE_RIDE])
	{
		imageHeader = @"taxi_24X24_PNG";
	}
	else if ([segmentType isEqualToString:SEG_TYPE_EVENT])
	{
		imageHeader = @"24_event";
	}
	else if ([segmentType isEqualToString:SEG_TYPE_RAIL])
	{
		imageHeader = @"rail_24X24_PNG";
	}
	else if ([segmentType isEqualToString:SEG_TYPE_PARKING])
	{
		imageHeader = @"parking_24X24_PNG";
	}
	else 
	{
		imageHeader = @"alert_24X24_PNG";
	}
	
	return [UIImage imageNamed:imageHeader];

}


#pragma mark - Fill Sections
-(void) fillAirSections
{
	//test code
    //	segment.operatedByVendor = @"Delta";
    //	segment.operatedByFlightNumber = @"153";
    //	segment.operatedBy = @"DL";
	
	NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
	
	SegmentRow *segRow = nil;
// AJC - BEGIN - please delete this code if present past 2013-12-13
// [[SegmentRow alloc] init];
//	segRow.rowLabel = nil;
//	segRow.rowValue = nil;
//	segRow.isAirVendor = YES;
//	[section addObject:segRow];
//	[segRow release];
//	
//	segRow = [[SegmentRow alloc] init];
//	segRow.isSpecialCell = YES;
//	segRow.specialCellType = @"AIR";
//	segRow.segment = segment;
//	[section addObject:segRow];
//	[segRow release];
// AJC - END - please delete this code if present past 2013-12-13
    
    //Short statuses (useful):
    //Cancelled
    //Delayed
    //Early
    //No status
    //On time
    //
    //Long statuses (a mix of useful and confusing so we’ll steer clear):
    //No delays posted
    //Cancelled flight
    //Cancelled
    //Check with airline
    //No delays posted
    //Estimated time of arrival
    //Estimated time of departure
    //Arrived in the gate
    //Cancelled leg
    //Not Available
    //No-op
    //Departed off the ground
    //Arrived on the ground
    //Departed out of the gate
    //Preliminary estimated time
    //Schedule Change
    //
    //And for completeness, reason:
    //Aviation System Delay
    //Weather Delay
    //Airline Controlled Delay
    //Downline Delay
    //Other
    //None
    if (!self.isTripApproval) {
        if([segment.relFlightStats.departureShortStatus isEqualToString:@"DY"] || [segment.relFlightStats.departureShortStatus isEqualToString:@"Delayed"]
           || [segment.relFlightStats.departureShortStatus isEqualToString:@"Cancelled"])
        {
            segRow = [[SegmentRow alloc] init];
            segRow.rowLabel = [Localizer getLocalizedText:@"Flight Status"];
            segRow.rowValue = segment.relFlightStats.departureShortStatus;
            segRow.isEmblazoned = YES;
            segRow.isFlightStats = YES;
            [section addObject:segRow];
        }
        else if(segment.relFlightStats.departureShortStatus != nil)
        {
            segRow = [[SegmentRow alloc] init];
            segRow.rowLabel = [Localizer getLocalizedText:@"Flight Status"];
            segRow.rowValue = segment.relFlightStats.departureShortStatus;
            segRow.isFlightStats = YES;
            [section addObject:segRow];
        }
        else
        {
            segRow = [[SegmentRow alloc] init];
            segRow.rowLabel = [Localizer getLocalizedText:@"Flight Status"];
            segRow.rowValue = [Localizer getLocalizedText: @"Scheduled On-time"];
            segRow.isFlightStats = YES;
            [section addObject:segRow];
        }
    }
    
    if(segment.operatedBy != nil && segment.operatedByFlightNumber != nil && segment.operatedByVendor != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Operated By"];
		segRow.rowValue = [NSString stringWithFormat:@"%@ (%@) %@", segment.operatedBy, segment.operatedByVendor, segment.operatedByFlightNumber];
		[section addObject:segRow];
	}
	
	if(segment.phoneNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = nil;
		segRow.rowValue = segment.phoneNumber; // @"425-497-5946";
		segRow.isPhone = YES;
		[section addObject:segRow];
	}
	
	//class and seat
    
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [@"Class & Seat Number" localize];
	NSString *classy = segment.classOfServiceLocalized;
	NSString *seatNum = segment.seatNumber;
	if(segment.classOfServiceLocalized == nil)
		classy = @"";
	else 
		classy = [NSString stringWithFormat:@"Class: %@", segment.classOfServiceLocalized];
	
	if(segment.seatNumber == nil)
		seatNum = @"";
	else 
		seatNum = [NSString stringWithFormat:@"Seat: %@", segment.seatNumber];

	segRow.rowValue = [NSString stringWithFormat:@"%@ %@", classy, seatNum];
	segRow.isWeb = NO;

	[section addObject:segRow];
    
	if([segment.vendor isEqualToString:@"WN"])
		segment.aircraftCode = @"737";
	
	if(segment.aircraftCode != nil)
	{
		segRow = [[SegmentRow alloc] init];
		
		NSString *airURL = [self getAircraftURL:segment.vendor AircraftCode:segment.aircraftCode];

		
		if(airURL == nil)
		{
			segRow.rowLabel = [Localizer getLocalizedText:@"Aircraft"];
			segRow.rowValue =  [segment.aircraftName length] ? segment.aircraftName : segment.aircraftCode;
		}
		else 
        {
			segRow.rowLabel = [Localizer getLocalizedText:@"Aircraft"];
			segRow.isWeb = YES;
			segRow.isSeat = YES;
			segRow.url = airURL;
			NSString* strAirCraft = segment.aircraftCode == nil? @"Flight":segment.aircraftCode;
			
			NSString *aircraftCode = [NSString stringWithFormat:@"%@", segment.aircraftCode];
			if([aircraftCode intValue] > 300 && [aircraftCode intValue] < 400)
				aircraftCode = [NSString stringWithFormat:@"Airbus %@", aircraftCode];
			else if([aircraftCode intValue] > 700 && [aircraftCode intValue] < 800)
				aircraftCode = [NSString stringWithFormat:@"Boeing %@", aircraftCode];
			
			segRow.rowValue = [segment.aircraftName length] ? segment.aircraftName : aircraftCode;
			segRow.viewTitle = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Seat Map for t"], strAirCraft];
			
		}
        
		[section addObject:segRow];
	}
	
	//confirmation number
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
	segRow.rowValue = segment.confirmationNumber;
    segRow.isCopyEnable = YES;
	[section addObject:segRow];
	
	
	//Departure Details
	//section dump and reset
	[sections addObject:section];
	section = [[NSMutableArray alloc] initWithObjects:nil];
	
	//new section
	if(segment.relFlightStats.departureScheduled != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Scheduled Departure"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.departureScheduled];
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.departureEstimated != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Estimated Departure"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.departureEstimated];
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.departureActual != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Actual Departure"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.departureActual];
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.departureStatusReason != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Departure Status"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = segment.relFlightStats.departureStatusReason;
		if ([segment.relFlightStats.departureShortStatus isEqualToString:@"DY"] || [segment.relFlightStats.departureShortStatus isEqualToString:@"Delayed"]) 
			segRow.isEmblazoned = YES;
		[section addObject:segRow];
	}
	
	//flight duration
	if(segment.duration != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Flight Duration"]; 
		int flightMinutes = [segment.duration intValue];
		int flightHours = flightMinutes / 60;
		
		if (flightHours > 0) 
			flightMinutes = flightMinutes - (flightHours * 60);
		
		NSString *dur = [NSString stringWithFormat:[Localizer getLocalizedText:@"%d Hours and %d Minute(s)"], flightHours, flightMinutes];
		
		if(flightHours < 1)
			dur = [NSString stringWithFormat:[Localizer getLocalizedText:@"%d Minute(s)"], flightMinutes];
		else if (flightHours == 1)
			[NSString stringWithFormat:[Localizer getLocalizedText:@"%d Hour and %d Minute(s)"], flightHours, flightMinutes];
		
		segRow.rowValue = dur;
		[section addObject:segRow];
	}
    
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_AIRPORT"];
    segRow.rowValue = [SegmentData getAirportFullName:segment.relStartLocation];
	segRow.isWeb = YES;
	segRow.isAirport = YES;
	segRow.iataCode = segment.relStartLocation.cityCode;
	segRow.url = [[ExSystem sharedInstance] getURLMap:@"AIRPORTS" LocalConstant:segment.relStartLocation.cityCode];
	segRow.viewTitle = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal Map for t"], segment.relStartLocation.cityCode];
	[section addObject:segRow];
	
	//Departure Flight Details section
	//section dump and reset
	[sections addObject:section];
	section = [[NSMutableArray alloc] initWithObjects:nil];
    
	if(segment.status != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Reservation Status"]; 
		// AJC - please delete the below code if present past 2013-12-13
        //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATUS"];
		segRow.rowValue = segment.status;
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.equipmentScheduled != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Scheduled Equipment"];
		segRow.rowValue = segment.relFlightStats.equipmentScheduled;
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.equipmentActual != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Actual Equipment"];
		segRow.rowValue = segment.relFlightStats.equipmentActual;
		[section addObject:segRow];
	}
	
	if(segment.meals != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_MEAL"];
		segRow.rowValue = segment.meals;
		[section addObject:segRow];
	}
    
	if(segment.specialInstructions != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_SPECIAL_INSTRUCTIONS"];
		segRow.rowValue = segment.specialInstructions;
		[section addObject:segRow];
	}
	
	//airline web site
	NSString *urlMap = [[ExSystem sharedInstance] getURLMap:@"AIRLINES" LocalConstant:segment.vendor];
	if(urlMap != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Web Site"];
		// AJC - BEGIN - please delete this code if present past 2013-12-13
        //	NSLog(@"URL=%@", [rootViewController getURLMap:@"AIRLINES" LocalConstant:segment.vendor]);
		//	NSString *airlineURL = [rootViewController getURLMap:@"AIRLINES" LocalConstant:segment.vendor];
		//	if (airlineURL != nil & [airlineURL length] > 0) 
		//	{
		//		[self loadWebView:airlineURL WebViewTitle:[NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Web site for t"], segment.vendorName]];
		//	}
		// AJC - END - please delete this code if present past 2013-12-13
        
        segRow.rowValue = [[ExSystem sharedInstance] getURLMap:@"AIRLINES" LocalConstant:segment.vendor];
		segRow.url = [[ExSystem sharedInstance] getURLMap:@"AIRLINES" LocalConstant:segment.vendor];
		segRow.viewTitle = segment.vendorName;
		segRow.isWeb = YES;
		[section addObject:segRow];
	}

	//on hold until Mobiata gives me a good link
	if(segment.confirmationNumber != nil && segment.flightNumber != nil && segment.vendor != nil && segment.relStartLocation.cityCode != nil && segment.relEndLocation.cityCode != nil && segment.relStartLocation.dateLocal != nil && segment.relEndLocation.dateLocal != nil && ![[ExSystem sharedInstance] isGovernment] && !self.isTripApproval)
	{
		NSString *depDate = [NSString stringWithFormat:@"%@%@", [DateTimeFormatter formatDateyyyyMMdd:segment.relStartLocation.dateLocal], [DateTimeFormatter formatTimeHHmm:segment.relStartLocation.dateLocal]];
		NSString *arrDate = [NSString stringWithFormat:@"%@%@", [DateTimeFormatter formatDateyyyyMMdd:segment.relEndLocation.dateLocal], [DateTimeFormatter formatTimeHHmm:segment.relEndLocation.dateLocal]];

        NSString *flightTrackURL = [NSString stringWithFormat:
                                    @"moflighttrack:saveFlight?departureDate=%@&departureAirportID=%@&arrivalAirportID=%@&airlineID=%@&flightNumber=%@&arrivalDate=%@&notes=Concur&confirmationNumber=%@&source=concur"
                                    ,depDate, segment.relStartLocation.cityCode, segment.relEndLocation.cityCode
                                    , segment.vendor, segment.flightNumber, arrDate, segment.confirmationNumber];
        
        segRow = [[SegmentRow alloc] init];
        segRow.rowLabel = @"Flight Tracker";
        segRow.rowValue = @"Track your flight";
        segRow.url = flightTrackURL;
        segRow.isApp = YES;
        [section addObject:segRow];
	}
	
	//Arrival Section
	//section dump and reset
	[sections addObject:section];
	section = [[NSMutableArray alloc] initWithObjects:nil];
	
	//arrival date time
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"Scheduled Arrival"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
	segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relEndLocation.dateLocal];
	[section addObject:segRow];
	
	if(segment.relFlightStats.arrivalEstimated != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Estimated Arrival"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.arrivalEstimated];
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.arrivalActual != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Actual Arrival"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.arrivalActual];
		[section addObject:segRow];
	}
	
	//arrival airport
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
	NSString *airportURL = [[ExSystem sharedInstance] getURLMap:@"AIRPORTS" LocalConstant:segment.relEndLocation.cityCode];
	segRow.url = airportURL; //@"www.airportterminalmaps.com/SEATAC-airport-terminal-map.html";
	segRow.viewTitle = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal Map for t"], segment.relEndLocation.cityCode];
    NSString *airportState = segment.relEndLocation.airportState;
    if (airportState == nil)
        segRow.rowValue = [NSString stringWithFormat:@"(%@) %@", segment.relEndLocation.cityCode, segment.relEndLocation.airportName];
    else
        segRow.rowValue = [NSString stringWithFormat:@"(%@) %@, %@", segment.relEndLocation.cityCode, segment.relEndLocation.airportName, airportState];
	if (airportURL == nil || [airportURL length] <= 0) 
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_AIRPORT"]; 
	segRow.isWeb = YES;
	segRow.isAirport = YES;
	segRow.iataCode = segment.relEndLocation.cityCode;
	[section addObject:segRow];
	
	//arrival terminal gate
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [NSString stringWithFormat:@"%@\n%@", [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_TERMINAL"], 
                       [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_GATE"]];

    NSMutableString *term = [NSMutableString string];
    NSMutableString *gate = [NSMutableString string];
    
    [SegmentData getArriveTermGate:segment terminal:term gate:gate];
	segRow.rowValue = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal t Gate t"], term, gate];
	[section addObject:segRow];
	
	if(segment.relFlightStats.baggageClaim != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText: @"Baggage Claim"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
		segRow.rowValue = segment.relFlightStats.baggageClaim;
		[section addObject:segRow];
	}
	
	[sections addObject:section];
    
    
    //Flight schedule section
    if (!self.isTripApproval) {
        section = [[NSMutableArray alloc] initWithObjects:nil];
        segRow = [[SegmentRow alloc] init];
        segRow.rowLabel = [Localizer getLocalizedText:@"Flight Schedules"];
        segRow.rowValue = [Localizer getLocalizedText:@"See Alternative Flights"];
        segRow.showDisclosure = YES;
        segRow.isFlightSchedule = YES;
        [section addObject:segRow];
        [sections addObject:section];
    
        section = [[NSMutableArray alloc] initWithObjects:nil];
        if ([segment.travelPointsPosted length] || [segment.travelPointsPending length] || [segment.travelPointsBenchmark length]) {
            NSString *rowLabelForTravelPoints = [@"Travel Points Booked" localize];
            NSString *rowValueForTravelPoints = @"--";
            
            if ([segment.travelPointsPosted length])
            {
                rowLabelForTravelPoints = [@"Travel Points Awarded" localize];
                rowValueForTravelPoints = segment.travelPointsPosted;
            }
            else if ([segment.travelPointsPending length])
            {
                rowLabelForTravelPoints = [@"Travel Points Booked" localize];
                rowValueForTravelPoints = segment.travelPointsPending;
            }
            
            segRow = [[SegmentRow alloc] init];
            segRow.rowLabel = rowLabelForTravelPoints;
            segRow.rowValue = rowValueForTravelPoints;
            [section addObject:segRow];
        
            segRow = [[SegmentRow alloc] init];
            segRow.rowLabel = [@"Price to Beat" localize];
            segRow.rowValue = [segment.travelPointsBenchmark length] ? [FormatUtils formatMoney:segment.travelPointsBenchmark crnCode:segment.travelPointsBenchmarkCurrency] : @"--";
            [section addObject:segRow];
        }
        
        if ([section count]) {
            [sections addObject:section];
        }
    }
}

//the old was was a cluster and a pain to update.  This is a lot more flexible.
-(void) fillParkingSections
{
	NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
	SegmentRow *segRow;
    
    if(segment.relStartLocation.city != nil)
    {
        segRow = [[SegmentRow alloc] init];
        segRow.viewTitle = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_MAP"];
        segRow.mapAddress = [SegmentData getMapAddress:segment.relStartLocation];
        // AJC - please delete the below code if present past 2013-12-13
        //[NSString stringWithFormat:@"%@ %@, %@ %@", segment.relStartLocation.address, segment.relStartLocation.city, segment.relStartLocation.state, segment.relStartLocation.postalCode];
        segRow.isMap = YES;
        [section addObject:segRow];
        
        [sections addObject:section];
        section = [[NSMutableArray alloc] initWithObjects:nil];
    }
    
	if(segment.confirmationNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = segment.confirmationNumber;
        segRow.isCopyEnable = YES;
		[section addObject:segRow];
	}
    
	if(segment.status != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATUS"];
		segRow.rowValue = segment.status;
		[section addObject:segRow];
	}
	
	if(segment.totalRate != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_RATE"];
		segRow.rowValue = [FormatUtils formatMoneyWithNumber:segment.totalRate crnCode:segment.currency]; // [NSString stringWithFormat:@"%@ (%@)", segment.totalRate, segment.currency];
		[section addObject:segRow];
	}
	
	[sections addObject:section];
    
}


-(void) fillCarSections
{
	NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
	
	SegmentRow *segRow = nil;
    // AJC - please delete the below if present past 2013-12-13
    // [[SegmentRow alloc] init];
	
	if(segment.phoneNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Phone"];
		segRow.rowValue = segment.phoneNumber;
		segRow.isPhone = YES;
		[section addObject:segRow];
	}

	if(segment.relStartLocation.cityCode != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Pickup Location"];
        NSString *startCityCode = segment.relStartLocation.cityCode;
        NSString *startAirportName = segment.relStartLocation.airportName;
        NSString *startAirportCity = segment.relStartLocation.airportCity;
        NSString *startAirportState = segment.relStartLocation.airportState;
        if(startCityCode == nil)
            startCityCode = @"";
        if(startAirportName == nil)
            startAirportName = @"";
        if(startAirportCity == nil)
            startAirportCity = @"";
        if(startAirportState == nil)
            startAirportState = @"";
		segRow.rowValue = [NSString stringWithFormat:@"(%@) %@\n%@, %@", startCityCode, startAirportName, startAirportCity, startAirportState];
		segRow.isMap = YES;
		segRow.url = [NSString stringWithFormat:@"(%@) %@\n%@, %@", startCityCode, startAirportName, startAirportCity, startAirportState];
		[section addObject:segRow];
	}
	
	if([[ExSystem sharedInstance] getURLMap:@"CAR_RENTERS" LocalConstant:segment.vendor] != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Vendor Web Site"];
		segRow.rowValue = [[ExSystem sharedInstance] getURLMap:@"CAR_RENTERS" LocalConstant:segment.vendor];
        
        // AJC - BEGIN - please delete the below code if present past 2013-12-13
        //NSLog(@"url = %@", [[ExSystem sharedInstance] getURLMap:@"CAR_RENTERS" LocalConstant:segment.vendor]);
        if([segment.vendor isEqualToString:@"ZL"]) //mob-4264
        {
            segRow.url = @"www.nationalcar.com";
            segRow.rowValue = segRow.url;
        }
        else
            segRow.url = [[ExSystem sharedInstance] getURLMap:@"CAR_RENTERS" LocalConstant:segment.vendor];
		segRow.viewTitle = segment.vendorName;
		segRow.isWeb = YES;
		[section addObject:segRow];
	}
	
	//confirmation number
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
	segRow.rowValue = segment.confirmationNumber;
    segRow.isCopyEnable = YES;
	[section addObject:segRow];

#if defined(CORP) & defined(ENTERPRISE) & defined(PASSBOOK)
    //Add to passbook
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = @"Passbook";
    segRow.isCopyEnable = NO;
	[section addObject:segRow];
#endif
    
	//Car Details
	//section dump and reset
	segRow = [[SegmentRow alloc] init];
	[sections addObject:section];
	section = [[NSMutableArray alloc] initWithObjects:nil];

	
	if(segment.status != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATUS"];
		if(segment.statusLocalized != nil)
			segRow.rowValue = segment.statusLocalized;
		else 
			segRow.rowValue = segment.status;
		[section addObject:segRow];
	}
	
	if(segment.totalRate != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_RATE"];
		segRow.rowValue = [FormatUtils formatMoneyWithNumber:segment.totalRate crnCode:segment.currency];
		[section addObject:segRow];
	}
	
	if(segment.dailyRate != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DAILY_RATE"];
		segRow.rowValue = [FormatUtils formatMoneyWithNumber:segment.dailyRate crnCode:segment.currency]; 
		[section addObject:segRow];
	}

	if(segment.rateType != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_RATE_TYPE"];
		segRow.rowValue = segment.rateType;
		[section addObject:segRow];
	}

	if(segment.bodyTypeName != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_BODY"];
		segRow.rowValue = segment.bodyTypeName;
		[section addObject:segRow];
	}

	if(segment.bodyTypeName != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_TRANSMISSION"];
		segRow.rowValue = segment.transmission;
		[section addObject:segRow];
	}
	
	if(segment.airCond != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_AIRCOND"];
		segRow.rowValue = segment.airCond;
		[section addObject:segRow];
	}

	if(segment.discountCode != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DISCOUNT"];
		segRow.rowValue = segment.discountCode;
		[section addObject:segRow];
	}

	if(segment.specialEquipment != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_EQUIP"];
		segRow.rowValue = segment.specialEquipment;
		[section addObject:segRow];
	}
    
	[sections addObject:section];
	
}


-(void) fillHotelSections
{
	NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
	
	SegmentRow *segRow = nil; // [[SegmentRow alloc] init];
    
    if(segment.phoneNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText: @"Phone"];
		segRow.rowValue = segment.phoneNumber;
		segRow.isPhone = YES;
		[section addObject:segRow];
	}
    
	if(segment.relStartLocation.address != nil)
	{
		segRow = [[SegmentRow alloc] init];
        segRow.rowLabel = [Localizer getLocalizedText: @"Map Address"];
		// @"%@\n%@, %@, %@"
		NSString *location = [SegmentData getMapAddress:segment.relStartLocation];
		
		segRow.rowValue = location; 
		segRow.isMap = YES;
		
		[section addObject:segRow];
	}
	
#if defined(CORP) & defined(ENTERPRISE) & defined(PASSBOOK)
    //Add to passbook
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = @"Passbook";
    segRow.isCopyEnable = NO;
	[section addObject:segRow];
#endif
	
	//Hotel Details
	//section dump and reset
	[sections addObject:section];
	section = [[NSMutableArray alloc] initWithObjects:nil];

	if(segment.status != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATUS"];
		segRow.rowValue = segment.status;
		[section addObject:segRow];
	}
	
	if(segment.confirmationNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = segment.confirmationNumber;
        segRow.isCopyEnable = YES;
		[section addObject:segRow];
	}

	if(segment.dailyRate != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DAILY_RATE"];
		segRow.rowValue = [FormatUtils formatMoneyWithNumber:segment.dailyRate crnCode:segment.currency]; 
		[section addObject:segRow];
	}

	if(segment.totalRate != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_TOTAL_RATE"];
		segRow.rowValue = [FormatUtils formatMoneyWithNumber:segment.totalRate crnCode:segment.currency]; 
		[section addObject:segRow];
	}
	
	if(segment.cancellationPolicy != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CANCEL_POLICY"];
		segRow.rowValue = segment.cancellationPolicy;
        segRow.isDescription = YES;
		[section addObject:segRow];
	}
	
	if(segment.roomDescription != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ROOM"];
		segRow.rowValue = segment.roomDescription;
		[section addObject:segRow];
	}
    
    segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"Vendor"];
	segRow.rowValue = segment.vendorName; 
    segRow.isCopyEnable = YES;
	[section addObject:segRow];

	if(segment.segmentName != nil && ![segment.segmentName isEqualToString:segment.vendorName])
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Hotel Name"];
		segRow.rowValue = segment.segmentName; 
        segRow.isCopyEnable = YES;
		[section addObject:segRow];
	}

	[sections addObject:section];
    
    if (!self.isTripApproval) {
        section = [[NSMutableArray alloc] initWithObjects:nil];
        if ([segment.travelPointsPosted length] || [segment.travelPointsPending length] || [segment.travelPointsBenchmark length]) {
            NSString *rowLabelForTravelPoints = [@"Travel Points Booked" localize];
            NSString *rowValueForTravelPoints = @"--";
            
            if ([segment.travelPointsPosted length])
            {
                rowLabelForTravelPoints = [@"Travel Points Awarded" localize];
                rowValueForTravelPoints = segment.travelPointsPosted;
            }
            else if ([segment.travelPointsPending length])
            {
                rowLabelForTravelPoints = [@"Travel Points Booked" localize];
                rowValueForTravelPoints = segment.travelPointsPending;
            }
            
            segRow = [[SegmentRow alloc] init];
            segRow.rowLabel = rowLabelForTravelPoints;
            segRow.rowValue = rowValueForTravelPoints;
            [section addObject:segRow];
        
            segRow = [[SegmentRow alloc] init];
            segRow.rowLabel = [@"Price to Beat" localize];
            segRow.rowValue = [segment.travelPointsBenchmark length] ? [FormatUtils formatMoney:segment.travelPointsBenchmark crnCode:segment.travelPointsBenchmarkCurrency] : @"--";
            [section addObject:segRow];
        }
        
        if ([section count]) {
            [sections addObject:section];
        }
    }
	
}


-(void)fillRideSections
{
	NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
	
	SegmentRow *segRow = nil; // [[SegmentRow alloc] init];
		
	if(segment.relStartLocation.dateLocal != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Pickup"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relStartLocation.dateLocal];
		[section addObject:segRow];
	}
	
	if(segment.relStartLocation.address != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_PICKUP_ADDRESS"];
		// @"%@\n%@, %@, %@"
		NSString *location = [SegmentData getMapAddress:segment.relStartLocation];
		
		segRow.rowValue = location;
		segRow.isMap = YES;

		[section addObject:segRow];
	}
	
	if(segment.phoneNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Phone"];
		segRow.rowValue = segment.phoneNumber;
		segRow.isPhone = YES;
		[section addObject:segRow];
	}
	
	if(segment.pickupInstructions != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_PICKUP_INSTRUCTIONS"];
		segRow.rowValue = segment.pickupInstructions;
		[section addObject:segRow];
	}
	
	if(segment.meetingInstructions != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_MEETING_INSTRUCTIONS"];
		segRow.rowValue = segment.meetingInstructions;
		[section addObject:segRow];
	}
	
	if(segment.relEndLocation.dateLocal != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DROP_OFF"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relEndLocation.dateLocal];
		[section addObject:segRow];
	}
	
	if(segment.relEndLocation.address != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DROP_OFF_ADDRESS"];
		segRow.rowValue = [SegmentData getMapAddress:segment.relEndLocation];
        //[NSString stringWithFormat:@"%@\n%@ %@, %@", segment.endAddress, segment.endCity, segment.endState, segment.endPostalCode];
		[section addObject:segRow];
	}
	
	if(segment.dropoffInstructions != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DROP_OFF_INSTRUCTIONS"];
		segRow.rowValue = segment.dropoffInstructions;
		[section addObject:segRow];
	}
	
	if(segment.status != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATUS"];
		segRow.rowValue = segment.status;
		[section addObject:segRow];
	}
	
	if(segment.confirmationNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = segment.confirmationNumber;
		[section addObject:segRow];
	}
	
	if(segment.totalRate != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_RATE"];
		segRow.rowValue = [NSString stringWithFormat:@"%@ (%@)", segment.totalRate, segment.currency];
		[section addObject:segRow];
	}
	
	if(segment.rateDescription != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_RATE_DESCRIPTION"];
		segRow.rowValue = segment.rateDescription;
		[section addObject:segRow];
	}
	
	if(segment.cancellationPolicy != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CANCEL_POLICY"];
		segRow.rowValue = segment.cancellationPolicy;
		[section addObject:segRow];
	}

	[sections addObject:section];
	
}

-(void)fillDiningSections
{
	NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
	
	SegmentRow *segRow = [[SegmentRow alloc] init];
	
	if(segment.segmentName != nil)
	{
		segRow.rowLabel = nil;
		segRow.rowValue = segment.segmentName; // [rootViewController getURLMap:@"CAR_RENTERS" LocalConstant:segment.vendor];
		segRow.isVendorRow = YES;
		segRow.vendorType = @"d";
		[section addObject:segRow];
		segRow = [[SegmentRow alloc] init];
	}
	
	segRow.isSpecialCell = YES;
	segRow.specialCellType = @"DINING";
	segRow.segment = segment;
	[section addObject:segRow];
	
	
	if(segment.relStartLocation.dateLocal != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Reservation Time"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relStartLocation.dateLocal];
		[section addObject:segRow];
	}
	
	if(segment.relStartLocation.address != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_PICKUP_ADDRESS"];
		// @"%@\n%@, %@, %@"
		NSString *location = [SegmentData getMapAddress:segment.relStartLocation];
		
		segRow.rowValue = location; 
		segRow.isMap = YES;
		
		[section addObject:segRow];
	}
	
	if(segment.phoneNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Phone"];
		segRow.rowValue = segment.phoneNumber;
		segRow.isPhone = YES;
		[section addObject:segRow];
	}

	if(segment.segmentName != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Vendor"];
		segRow.rowValue = segment.segmentName;
		[section addObject:segRow];
	}
	
	if(segment.reservationId != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_REZ"];
		segRow.rowValue = segment.reservationId;
		[section addObject:segRow];
	}
	
	if(segment.numPersons != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_NUMBER_IN_PARTY"];
		segRow.rowValue = [NSString stringWithFormat:@"%@", segment.numPersons];
		[section addObject:segRow];
	}
	
	
	[sections addObject:section];
}

-(void)fillRailSections
{
	NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
	
	SegmentRow *segRow = nil;
	
	if(segment.relStartLocation.address != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_PICKUP_ADDRESS"];
		// @"%@\n%@, %@, %@"
		NSString *location = [SegmentData getMapAddress:segment.relStartLocation];
		
		segRow.rowValue = location; //[NSString stringWithFormat:@"%@\n%@, %@ %@", segment.startAddress, segment.startCity, segment.startState, segment.startPostalCode];;
		segRow.isMap = YES;
		
		[section addObject:segRow];
	}
	
	if(segment.vendor != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Info"];
		if (segment.operatedByVendor != nil)
			segRow.rowValue = [NSString stringWithFormat:[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_FLIGHT_WITH_OB"], segment.vendor, segment.trainNumber, [DateTimeFormatter formatDateTimeForTravel:segment.relStartLocation.dateLocal], segment.operatedByVendor, segment.operatedByTrainNumber];
		else 
			segRow.rowValue = [NSString stringWithFormat:@"%@ #%@ %@", segment.vendor, segment.trainNumber, [DateTimeFormatter formatDateTimeForTravel:segment.relStartLocation.dateLocal]];

		[section addObject:segRow];
	}
	
	if(segment.relStartLocation.platform != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_PLATFORM"];
		segRow.rowValue = segment.relStartLocation.platform;
		[section addObject:segRow];
	}
	
	if(segment.relStartLocation.railStation != nil)
	{
		NSString *railStation = [SegmentData getRailStation:segment.relStartLocation];
		
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATION"];
		segRow.rowValue = railStation;
		[section addObject:segRow];
	}
	
	if(segment.trainNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Train Number"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CLASS"];
		segRow.rowValue = segment.trainNumber;
		[section addObject:segRow];
	}
	
	if(segment.classOfService != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CLASS"];
		segRow.rowValue = segment.classOfService;
		[section addObject:segRow];
	}
	
	if(segment.cabin != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CABIN"];
		segRow.rowValue = segment.cabin;
		[section addObject:segRow];
	}
	
	if(segment.relStartLocation.cityCode != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CITY"];
		segRow.rowValue = segment.relStartLocation.cityCode;
		[section addObject:segRow];
	}

	if(segment.wagonNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_WAGON_NUM"];
		segRow.rowValue = segment.wagonNumber;
		[section addObject:segRow];
	}
	
	if(segment.amenities != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_AMENITIES"];
		segRow.rowValue = segment.amenities;
		[section addObject:segRow];
	}
	
	
	NSString *sDuration = [DateTimeFormatter formatDuration:segment.relStartLocation.dateLocal endDate:segment.relEndLocation.dateLocal];
		
    segRow = [[SegmentRow alloc] init];
    segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DURATION"];
	segRow.rowValue = sDuration; //[NSString stringWithFormat:[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_MINUTES"], segment.duration];
		[section addObject:segRow];
	
    NSNumber *stops = segment.numStops;
	if(stops != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STOPS"];
		segRow.rowValue = [NSString stringWithFormat:@"%@", stops];
		[section addObject:segRow];
	}
	
	if(segment.status != nil)
	{
		NSString *status = segment.statusLocalized;
		if(status == nil)
			status = segment.statusLocalized;
		
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Reservation Status"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATUS"];
		segRow.rowValue = status;
		[section addObject:segRow];
	}
	
	if(segment.meals != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_MEAL"];
		segRow.rowValue = segment.meals;
		[section addObject:segRow];
	}
	
    NSNumber *persons = segment.numPersons;
	if(0 != [persons intValue])
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_NUMBER_OF_PERSONS"];
		segRow.rowValue = [NSString stringWithFormat:@"%@", persons];
		[section addObject:segRow];
	}
	
    NSNumber *rate = segment.totalRate;
	if(0.0 != [rate floatValue])
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_TOTAL_RATE"];
        
        NSString *currency = segment.currency;
        if (currency == nil)
        {
             segRow.rowValue = [NSString stringWithFormat:@"%@", segment.totalRate];
        }
        else
        {
            segRow.rowValue = [NSString stringWithFormat:@"%@ (%@)", segment.totalRate, currency];
        }
		[section addObject:segRow];
	}
	
	if(segment.specialInstructions != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_SPECIAL_INSTRUCTIONS"];
		segRow.rowValue = segment.specialInstructions;
		[section addObject:segRow];
	}

	
	//arrival details
	//section dump and reset
	[sections addObject:section];
	section = [[NSMutableArray alloc] initWithObjects:nil];
	if(segment.relStartLocation.dateLocal != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relStartLocation.dateLocal];
		[section addObject:segRow];
	}
	
	if(segment.relEndLocation.cityCode != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CITY"];
		segRow.rowValue = segment.relEndLocation.cityCode;
		[section addObject:segRow];
	}
	
	if(segment.relEndLocation.railStation != nil)
	{
		NSString *endRailStation = [SegmentData getRailStation:segment.relEndLocation];
		
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATION"];
		segRow.rowValue = endRailStation;
		[section addObject:segRow];
	}
	
	if(segment.relEndLocation.platform != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_PLATFORM"];
		segRow.rowValue = segment.relEndLocation.platform;
		[section addObject:segRow];
	}
	
	
	//Carrier details
	//section dump and reset
	[sections addObject:section];
	section = [[NSMutableArray alloc] initWithObjects:nil];
	if(segment.vendorName != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Vendor"];
		segRow.rowValue = segment.vendorName;
		[section addObject:segRow];
	}
	
	if(segment.trainTypeCode != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_TRAIN_TYPE"];
		segRow.rowValue = segment.trainTypeCode;
		[section addObject:segRow];
	}
	
	if(segment.phoneNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONTACT"];
		segRow.rowValue = segment.phoneNumber;
		segRow.isPhone = YES;
		[section addObject:segRow];
	}
	
	
	[sections addObject:section];
}


-(UITableViewCell *) fillEventCell:(NSIndexPath *)indexPath 
{
    NSUInteger row = [indexPath row];
	NSUInteger section = [indexPath section];
	
	if (section == 1 && row == 1)
	{//big fat details
		ItinDetailsCellInfo *cell = (ItinDetailsCellInfo *)[tableList dequeueReusableCellWithIdentifier: INFO_NIB];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:INFO_NIB owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[ItinDetailsCellInfo class]])
					cell = (ItinDetailsCellInfo *)oneObject;
		}
		
		[cell.btnPhone setTitle:[NSString stringWithFormat:@"  %@", segment.phoneNumber] forState:UIControlStateNormal];
		cell.phoneNumber = segment.phoneNumber;
		cell.labelMap.text = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_MAP"];
		
		cell.mapAddress = [SegmentData getMapAddress:segment.relStartLocation withLineBreaker:NO withDelimitor:YES];
		// AJC - please delete the code below if present past 2013-12-13
        //[NSString stringWithFormat:@"%@, %@, %@ %@", segment.startAddress, segment.startCity, segment.startState, segment.startPostalCode];
		cell.idVC = self;
		cell.vendorCode = segment.vendor;
		cell.vendorName = segment.vendorName;
		
		if (segment.relStartLocation.address2 != nil)
		{
			cell.labelAddress1.text = [NSString stringWithFormat:@"%@\n%@", segment.relStartLocation.address, segment.relStartLocation.address2];
		}
		else {
			cell.labelAddress1.text = segment.relStartLocation.address;
		}

		cell.labelAddress2.text = [NSString stringWithFormat:@"%@, %@ %@", segment.relStartLocation.city, segment.relStartLocation.state, segment.relStartLocation.postalCode];
		
		
		UIImage *gotImg = [UIImage imageNamed:@"fakeParty"]; //todo: hard coded place holder.  Right now we have no images to insert here
		[cell.imgVendor setImage:gotImg];
		
		return cell;
	}
	else 
	{
		ItinDetailsCellLabel *cell = (ItinDetailsCellLabel *)[tableList dequeueReusableCellWithIdentifier: LABEL_NIB];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:LABEL_NIB owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[ItinDetailsCellLabel class]])
					cell = (ItinDetailsCellLabel *)oneObject;
		}
		
		[cell.btn1 setHidden:YES];
		
		if (section == 0) 
		{//reservation info
			if (row == 0) 
			{
				cell.labelLabel.text = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
				cell.labelValue.text = segment.confirmationNumber;
			}
			else if (row == 1) 
			{//type
				cell.labelLabel.text = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_NUMBER_IN_PARTY"];
				cell.labelValue.text = [NSString stringWithFormat:@"%@", segment.numPersons];
			}
			else if (row == 2) 
			{//type
				cell.labelLabel.text = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_TOTAL_RATE"];
				cell.labelValue.text = [NSString stringWithFormat:@"%@ (%@)", segment.totalRate, segment.currency];
			}
			else if (row == 3) 
			{//type
				cell.labelLabel.text = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATUS"];
				cell.labelValue.text = segment.status;
			}
			
		}
		else if (section == 1) 
		{//Vendor Info
			if (row == 0)
			{
				cell.labelLabel.text = @"";
				cell.labelValue.text = @"";
				cell.labelVendor.text = segment.vendorName;

				[[ExSystem sharedInstance].imageControl getVendorImageAsynch:segment.vendor VendorType:@"e" RespondToCell:cell];
			}
		}
		return cell;
	}
}


#pragma mark - Navigation Stuff
-(void)goSomeplace:(NSString *)mapAddress VendorName:(NSString *)vendorName VendorCode:(NSString *)vendorCode
{
	MapViewController *mapView = [[MapViewController alloc] init];
	mapView.mapAddress = mapAddress;
	mapView.anoTitle = vendorName;
	mapView.anoSubTitle = mapAddress;
    
    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:mapView];

	[self presentViewController:navi animated:YES completion:nil]; 

}


-(void)callNumber:(NSString *)phoneNum
{
	[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", phoneNum]]];
}


-(IBAction)loadWebView:(NSString *)specialValueWeb WebViewTitle:(NSString *)webViewTitle
{
	
	//do web view
	WebViewController *webView = [[WebViewController alloc] init];
	webView.url = [NSString stringWithFormat:@"http://%@", specialValueWeb];
	webView.viewTitle = webViewTitle;
	[self presentViewController:webView animated:YES completion:nil]; 
	
}

-(IBAction)loadWebViewSeat:(NSString *)specialValueWeb WebViewTitle:(NSString *)webViewTitle
{
	WebViewController *webView = [[WebViewController alloc] init];
	webView.url = specialValueWeb;
    // AJC - please delete the below code if present past 2013-12-13
    // [NSString stringWithFormat:@"http://%@", specialValueWeb];
	webView.viewTitle = webViewTitle;
	[self presentViewController:webView animated:YES completion:nil]; 
}




-(NSString *)getGateTerminal:(NSString *)gate Terminal:(NSString *)terminal
{
	NSString *location = @"";
	
	if (terminal == nil)
	{
		location = [NSString stringWithFormat:@"%@ - %@ -", [Localizer getLocalizedText:@"SLV_TERMINAL"]
					, [Localizer getLocalizedText:@"SLV_GATE"]];
	}
	else 
	{
		if (gate == nil)
			gate = @"-";
		location = [NSMutableString stringWithFormat:@"%@ %@ %@ %@", [Localizer getLocalizedText:@"SLV_TERMINAL"], terminal, 
					[Localizer getLocalizedText:@"SLV_GATE"], gate];	
	}
	
	
	return location;
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event 
{
	//int x = 0;
}

-(void)reloadHotelImages
{

}

#pragma mark Passbook APIs
#if defined(CORP) & defined(ENTERPRISE) & defined(PASSBOOK)

+ (NSString *) obtainPassDir
{
	NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString* documentsDir = [paths objectAtIndex:0];
	NSString* passPath = [documentsDir stringByAppendingPathComponent:@"pass"];
    NSFileManager *filemgr = [NSFileManager defaultManager];
    BOOL isDir;

    if (!([filemgr fileExistsAtPath:passPath isDirectory:&isDir] && isDir))
    {
        [filemgr createDirectoryAtPath:passPath withIntermediateDirectories:YES attributes:nil error:nil];
    }
	return passPath;
}

-(NSString*) getPassFilePath
{
    // rowValue is the pass file path
    NSString* passDir = [ItinDetailsViewController obtainPassDir];

    NSString* filePath = [NSString stringWithFormat:@"%@/%@", passDir, @"Car_PasstoolsPass.pkpass"];
    if (![self.segmentType isEqualToString:SEG_TYPE_CAR])
        filePath = [NSString stringWithFormat:@"%@/%@", passDir, @"Hotel_PasstoolsPass.pkpass"];
    return filePath;
}

-(void) addToPassBook:(id) sender
{
    NSString* filePath = [self getPassFilePath];
    
    NSData *data = nil;
    if ([[NSFileManager defaultManager] fileExistsAtPath:filePath])
        data = [NSData dataWithContentsOfFile:filePath];

    if (data != nil)
        [self addToPassBookWithData:data];
    else
    {
        [self showWaitView];
    
        NSMutableDictionary* pBag =[[NSMutableDictionary alloc] init];
        NSString* param = @"car";
        if (![self.segmentType isEqualToString:SEG_TYPE_CAR])
            param = @"hotel";
        NSString * passUrl = [NSString stringWithFormat:@"http://172.17.41.26/mobile/PassbookDemo.ashx?passbookdemo=%@", param];
        [RequestController retrieveImageFromUrl:passUrl MsgId:@"PASSBOOK_DOWNLOAD" SessionID:[ExSystem sharedInstance].sessionID MVC:self ParameterBag:pBag];
    }
}

-(void) addToPassBookWithData:(NSData*) data
{
    // Save file
    NSString* filePath = [self getPassFilePath];
    if (data != nil) {
        if (![[NSFileManager defaultManager] fileExistsAtPath:filePath])
            [data writeToFile:filePath atomically:YES];
        [self hideWaitView];
        
        PKPassLibrary *passLib = [[PKPassLibrary alloc] init];
        
        NSError *error;
        PKPass *pass = [[PKPass alloc] initWithData:data error:&error];
        NSLog(@"Error = [%@]",[error localizedDescription]);
        
        if([passLib containsPass:pass]) {
            
            UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Pass Exists" message:@"The pass you are trying to add to Passbook is already present." delegate:nil cancelButtonTitle:@"OK"otherButtonTitles:nil];
            [alertView show];
            
        } else {
            
            PKAddPassesViewController *vc = [[PKAddPassesViewController alloc] initWithPass:pass];
            vc.delegate = self;
            [self presentViewController:vc animated:YES completion:nil];
        }
    }
    else
        [self hideWaitView];

}

-(void) addPassesViewControllerDidFinish:(PKAddPassesViewController *)controller
{
    [controller dismissModalViewControllerAnimated:YES];
}
#endif

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return [sections count];
}


- (NSInteger)tableView:(UITableView *)tableView 
 numberOfRowsInSection:(NSInteger)section
{
	NSMutableArray *rows = sections[section];
    return [rows count]; // count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView 
         cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
    NSUInteger row = [indexPath row];
	NSUInteger section = [indexPath section];
	
	NSMutableArray *sectionData = sections[section];
	SegmentRow *segRow = sectionData[row];

#if defined(CORP) & defined(ENTERPRISE) & defined(PASSBOOK)
    if ([segRow.rowLabel isEqualToString:@"Passbook"])
    {
        ItinPassbookCell *cell = (ItinPassbookCell *)[tableView dequeueReusableCellWithIdentifier: @"ItinPassbookCell"];
        if (cell == nil)
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ItinPassbookCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[ItinPassbookCell class]])
                    cell = (ItinPassbookCell *)oneObject;
        }
        
        [cell.btnAddToPassbook addTarget:self action:@selector(addToPassBook:) forControlEvents:UIControlEventTouchUpInside];

        return cell;
    }
#endif
    
    ItinDetailCell *cell = (ItinDetailCell *)[tableView dequeueReusableCellWithIdentifier: @"ItinDetailCell"];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ItinDetailCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[ItinDetailCell class]])
                cell = (ItinDetailCell *)oneObject;
    }
    
    [cell setAccessoryType:UITableViewCellAccessoryNone];

    cell.ivDot.hidden = YES;
    if(segRow.isFlightStats)
    {
        cell.ivDot.hidden = NO;
        cell.lblValue.frame = CGRectMake(30, 25, 280, 21);
        if([segment.relFlightStats.departureShortStatus isEqualToString:@"DY"] || [segment.relFlightStats.departureShortStatus isEqualToString:@"Delayed"]
           || [segment.relFlightStats.departureShortStatus isEqualToString:@"Cancelled"])
            cell.ivDot.image = [UIImage imageNamed:@"flight_status_red"];
        else
            cell.ivDot.image = [UIImage imageNamed:@"flight_status_green"];
        [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
    }
    else if([segment.type isEqualToString:SEG_TYPE_AIR])
        cell.lblValue.frame = CGRectMake(10, 25, 290, 21);
    /*MOB-5875
     The flightstats check was readjusting the length of the cell.  Well, we don't want that to happen for non air segments.*/
    
    cell.lblLabel.text = segRow.rowLabel;
    cell.lblValue.text = segRow.rowValue;
    if(segRow.isApp || segRow.isMap || segRow.isPhone || segRow.isDescription || segRow.isFlightSchedule)
    {
        [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
    }
    
    return cell;
}


- (NSInteger)tableView:(UITableView *)tableView 
sectionForSectionIndexTitle:(NSString *)title 
               atIndex:(NSInteger)index
{
// AJC - BEGIN - please delete the below code if present past 2013-12-13
//    NSString *key = [keys objectAtIndex:index];
//    if (key == UITableViewIndexSearch)
//    {
//        [tableView setContentOffset:CGPointZero animated:NO];
//        return NSNotFound;
//    }
//    else
// AJC - END - please delete the below code if present past 2013-12-13
    return index;
    
}


//need to make sure that a click on header means nothing
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
	NSUInteger section = [newIndexPath section];
    NSUInteger row = [newIndexPath row];
	
	NSMutableArray *sectionData = sections[section];
	SegmentRow *segRow = sectionData[row];
	
	if(segRow.isApp)
	{
        if([[ExSystem sharedInstance] hasRole:@"FlightTracker_User"])
        {
            NSDictionary *dictionary = @{@"Type": @"Mobiata"};
            [Flurry logEvent:@"External App: Launch" withParameters:dictionary];
            BOOL didLaunch = [[UIApplication sharedApplication] openURL:[NSURL URLWithString:segRow.url]];

            if (didLaunch == NO) 
            {
                NSURL *appStoreUrl = [NSURL URLWithString:@"http://www.mobiata.com/flighttrack-app-concur"];  // Mobiata
                [[UIApplication sharedApplication] openURL:appStoreUrl];
            }
        }
	}
	else if (segRow.isFlightStats)
	{
        // AJC - please delete the below code if present past 2013-12-13
//        if([[ExSystem sharedInstance] hasRole:@"FlightTracker_User"])
            [self showFlightStats];
	}
	else if(segRow.isWeb && segRow.isAirport)
	{
        if([[ExSystem sharedInstance] hasRole:@"GateGuru_User"])
        {
            // gate guru disabled for apple demo. do nothing. don't prompt user because it's not obvious that clicking field should have had an action to begin with
            NSDictionary *dictionary = @{@"Type": @"GateGuru"};
            [Flurry logEvent:@"External App: Launch" withParameters:dictionary];
        
            NSString *url = [NSString stringWithFormat:@"gateguru://airports/%@", segRow.iataCode];
            BOOL didLaunch = [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
            if (didLaunch == NO)
            {
                NSURL *appStoreUrl = [NSURL URLWithString:@"http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewSoftware?id=326862399&mt=8"];
                [[UIApplication sharedApplication] openURL:appStoreUrl];
            }
        }
	}
	else if(segRow.isWeb)
	{
		if(!segRow.isSeat)
        {

            [self loadWebView:segRow.url WebViewTitle:[NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Web site for t"], segRow.viewTitle]];
        }
        else
		{
            [self loadWebViewSeat:segRow.url WebViewTitle:[NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Web site for t"], segRow.viewTitle]];
        }
    }
	else if(segRow.isMap)
	{
		NSString *mapAddress = [SegmentData getMapAddress:segment.relStartLocation withLineBreaker:NO withDelimitor:YES];
        // AJC - please delete the below code if present past 2013-12-13
        //[NSString stringWithFormat:@"%@, %@, %@ %@", segment.startAddress, segment.startCity, segment.startState, segment.startPostalCode];
		if ([mapAddress length])
		{
            NSString *vendorName;
            if (segment.segmentName != nil)
                vendorName = segment.segmentName;
            else if (segment.vendorName != nil)
                vendorName = segment.vendorName;
            else 
                vendorName = segment.vendor;
            
			[self goSomeplace:mapAddress VendorName:vendorName VendorCode:segment.vendor];
		} 
		else 
		{
			NSString *mapAddress = [NSString stringWithFormat:@"(%@) %@\n%@, %@", segment.relStartLocation.cityCode, segment.relStartLocation.airportName, segment.relStartLocation.airportCity, segment.relStartLocation.airportState];
			[self goSomeplace:mapAddress VendorName:[NSString stringWithFormat:@"%@ %@", segment.vendorName, segment.segmentName == nil? segment.relStartLocation.airportName : segment.segmentName] VendorCode:segment.vendor];
		}
	}
    else if(segRow.isDescription)
    {
        ViolationDetailsVC *vc = [[ViolationDetailsVC alloc] initWithNibName:@"ViolationDetailsVC" bundle:nil];
        vc.violationText = segRow.rowValue;
        [self.navigationController pushViewController:vc animated:YES];
    }
	else if (segRow.isPhone)
    {
		[self callNumber:segment.phoneNumber];
    }
    else if (segRow.isFlightSchedule)
    {

        [self showFlightSchedule];
    }
	
    
    [tableView deselectRowAtIndexPath:newIndexPath animated:YES];
	
	return;
	
}

- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
#if defined(CORP) & defined(ENTERPRISE) & defined(PASSBOOK)
    NSUInteger row = [indexPath row];
	NSUInteger section = [indexPath section];
	
	NSMutableArray *sectionData = [sections objectAtIndex:section];
	SegmentRow *segRow = [sectionData objectAtIndex:row];
    
    if ([segRow.rowLabel isEqualToString:@"Passbook"])
    {
        return 60;
    }
#endif
    return 50;
}


-(UIView *) tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    if(section == 0 && [segmentType isEqualToString:SEG_TYPE_AIR])
        return viewAirHeader;
    else if(section == 0 && [segmentType isEqualToString:SEG_TYPE_RAIL])
        return viewAirHeader;
    else if(section == 0 && [segmentType isEqualToString:SEG_TYPE_CAR])
        return viewHotelHeader;
    else if(section == 0 && [segmentType isEqualToString:SEG_TYPE_HOTEL])
        return viewHotelHeader;
    else if(section == 0 && [segmentType isEqualToString:SEG_TYPE_RIDE])
        return viewHotelHeader;
    else
        return nil;
}

-(CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    if(section == 0)
    {
        if ([segmentType isEqualToString:SEG_TYPE_PARKING])
            return 40;
		else if ([segmentType isEqualToString:SEG_TYPE_AIR])
            return 206; // 196;
		else if([segmentType isEqualToString:SEG_TYPE_CAR])
			return 93;
		else if([segmentType isEqualToString:SEG_TYPE_HOTEL])
			return 93;	
		else if([segmentType isEqualToString:SEG_TYPE_RIDE])
			return 74;
		else if([segmentType isEqualToString:SEG_TYPE_RAIL])
			return 186;
    }
    
    return 30;
}

//MOB-9451 handle holding operation on isCopyEnable cells
-(BOOL) tableView:(UITableView *) tableView shouldShowMenuForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger selectedSection = [indexPath section];
    NSUInteger selectedRow = [indexPath row];
    
    NSMutableArray *sectionData = sections[selectedSection];
	SegmentRow *segRow = sectionData[selectedRow];
    
    if (segRow.isCopyEnable) 
    {
        return YES;
    }
    else 
        return false;
}

-(BOOL) tableView:(UITableView *)tableView canPerformAction:(SEL)action forRowAtIndexPath:(NSIndexPath *)indexPath withSender:(id)sender
{
    return (action == @selector(copy:));
}

-(void) tableView:(UITableView *)tableView performAction:(SEL)action forRowAtIndexPath:(NSIndexPath *)indexPath withSender:(id)sender
{
    NSUInteger selectedSection = [indexPath section];
    NSUInteger selectedRow = [indexPath row];
    
    NSMutableArray *sectionData = sections[selectedSection];
	SegmentRow *segRow = sectionData[selectedRow];    

    NSString *copyText = nil;
    copyText = segRow.rowValue;
    UIPasteboard *pasteboard = [UIPasteboard generalPasteboard];
    [pasteboard setString:copyText];
}

#pragma mark - Aircraft URL
-(NSString *)getAircraftURL:(NSString *)vendorCode AircraftCode:(NSString *)aircraftCode
{
	NSString *path = [[NSBundle mainBundle] bundlePath];
	NSString *finalPath = [path stringByAppendingPathComponent:@"AirlineInfo.plist"]; //en_Configuration
	NSDictionary *airDict = [NSDictionary dictionaryWithContentsOfFile:finalPath];
	
	if([aircraftCode intValue] > 300 && [aircraftCode intValue] < 400)
		aircraftCode = [NSString stringWithFormat:@"A%@", aircraftCode];
	
	NSString *key = [NSString stringWithFormat:@"%@,%@", vendorCode, aircraftCode];
	
	if(airDict[key] != nil)
	{
		return airDict[key];
	}
	else 
	{
		//let's get kind of fuzzy here and try to see if our aircraft and vendor string exists inside of any keys
		for(NSString *currKey in airDict)
		{
			NSRange match;
			match = [currKey rangeOfString: key];
			
			if(match.location != NSNotFound)
				return airDict[currKey];
		}
		return nil;
	}
}


#pragma mark -
#pragma mark Image Handlers
-(void) fillImageURLs: (NSMutableArray*) imageURLs
{
	//we have the URLs, now get the images associated with those urls
	
	float w = 75;
	float h = 75; 
	
	NSMutableArray *imageURLArray = [[NSMutableArray alloc] initWithObjects:nil];
    //clean out duplpicates
	for(int i = 0; i < [imageURLs count]; i++)
	{
		//try to clear out any duplicate urls
		HotelImageData *hid = imageURLs[i];
		BOOL hasImage = NO;
		for (NSString *img in imageURLArray)
		{
			if([img isEqualToString:hid.hotelImage])
			{
				hasImage = YES;
				break;
			}
		}
		if(!hasImage)
			[imageURLArray addObject:hid.hotelImage];
	}
	
	self.aHotelImageViews = [[NSMutableArray alloc] initWithObjects:nil];

	int iPos = 0;
	for(NSString *imageURL in imageURLArray)
	{
		UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, w, h)];
		//do things to load the actual image
		UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
		[iv setImage:img];
		[[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:imageURL RespondToImage:img IV:iv MVC:self]; //firing off the fetch, loads the image into the imageview
        [aHotelImageViews addObject:iv]; 
        if(iPos == 0)
            ivHeaderImage.image = iv.image;
        iPos++;
	}

}


#pragma mark - Header Configuration
-(void) configureAirHeader
{
    [viewHotelHeader setHidden:YES];
    [viewAirHeader setHidden:NO];
    
    lblHeadingAir.text = [NSString stringWithFormat:@"%@ %@ %@", segment.relStartLocation.airportCity, [Localizer getLocalizedText:@"SLV_TO"], segment.relEndLocation.airportCity];
    lblAirConfirm.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"], segment.confirmationNumber];
    
    // AJC - please delete the below code if present past 2013-12-13
    //tableList.frame = CGRectMake(0, 196, 320, 190);
    
    //Airline
    NSString *vendor = [NSString stringWithFormat:@"%@ %@", segment.vendorName ?: @"", segment.flightNumber ?: @""];
    if (segment.operatedBy != nil) 
    {
        NSString *opVendor = segment.operatedByVendor;
        if(segment.operatedByVendor == nil)
            opVendor = segment.operatedBy;
        
        NSString *flightNum = segment.operatedByFlightNumber;
        
        if(segment.operatedByFlightNumber == nil)
            flightNum = @"";
        else 
            flightNum = [NSString stringWithFormat:@" %@", flightNum];
        
        NSString *OBvendor = [NSString stringWithFormat:@"(%@%@)", opVendor, flightNum];
        
        vendor = [NSString stringWithFormat:@"%@ %@", vendor, OBvendor];
    }
    lblAirline.text = vendor;
    
    //Departure Airport
    lblDepart.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"Depart"], segment.relStartLocation.cityCode];
    
    //Arrive Airport
    lblArrive.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"Arrive"], segment.relEndLocation.cityCode];
    
    //Departure time and date
    NSMutableString *departTime = [NSMutableString string];
    NSMutableString *departDate = [NSMutableString string];
    [SegmentData getDepartTimeString:segment timeStr:departTime dateStr:departDate];
    
    NSArray *aTime = [departTime componentsSeparatedByString:@" "];
    if([aTime count] == 2)
    {
        lblDepartTime.text = aTime[0];
        lblDepartAMPM.text = aTime[1];
    }
    else
    {
        lblDepartTime.text = departTime;
        lblDepartAMPM.text = @"";
    }

    lblDepartDate.text = departDate;

    
    //Arrival Time and Date
    NSMutableString *arriveTime = [NSMutableString string];
    NSMutableString *arriveDate = [NSMutableString string];
    [SegmentData getArriveTimeString:segment timeStr:arriveTime dateStr:arriveDate];
    lblArriveDate.text = arriveDate;
    
    aTime = [arriveTime componentsSeparatedByString:@" "];
    if([aTime count] == 2)
    {
        lblArriveTime.text = aTime[0];
        lblArriveAMPM.text = aTime[1];
    }
    else
    {
        lblArriveTime.text = arriveTime;
        lblArriveAMPM.text = @"";
    }
    
    //Depart Gate Terminal
    NSMutableString *term = [NSMutableString string];
    NSMutableString *gate = [NSMutableString string];
    
    [SegmentData getDepartTermGate:segment terminal:term gate:gate];
    
    lblDepartTerminal.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"Terminal token"], term];
    lblDepartGate.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"Gate token"], gate];
    
    
    //Arrival Gate and Terminal
    term = [NSMutableString string];
    gate = [NSMutableString string];
    [SegmentData getArriveTermGate:segment terminal:term gate:gate];
    lblArriveTerminal.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"Terminal token"], term];
    lblArriveGate.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"Gate token"], gate];

 
}


-(void) configureCarHeader
{
    [viewHotelHeader setHidden:NO];
    NSString *hotelName = segment.vendorName;
    if(segment.segmentName != nil)
        hotelName = [NSString stringWithFormat:@"%@ (%@)", hotelName, segment.segmentName];
    lblHeading.text = hotelName;
    lblSub1.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"], segment.confirmationNumber];
    lblSub2.text = [NSString stringWithFormat:@"%@: %@",[Localizer getLocalizedText:@"Pickup"], [DateTimeFormatter formatDateTimeForTravel:segment.relStartLocation.dateLocal]];
    lblSub3.text = [NSString stringWithFormat:@"%@: %@", [Localizer getLocalizedText:@"Return"], [DateTimeFormatter formatDateTimeForTravel:segment.relEndLocation.dateLocal]];
    
    [ivHeaderImage setContentMode:UIViewContentModeCenter];
    ivHotelBackground.image = [UIImage imageNamed:@"image_bckgrdwhite"];
    
    UIImage *gotImg = [self getCarImageAsynch:segment.vendor CountryCode:segment.relStartLocation.airportCountryCode ClassOfCar:segment.classOfCar BodyType:segment.bodyType FetchURI:segment.imageCarURI];
    if (gotImg != nil) 
    {
        UIImage *imgScaled = [self scaleImageToFit:gotImg MaxW:73 MaxH:73]; 
        ivHeaderImage.image = imgScaled;
        [activityImage stopAnimating];
    }
    else
    {
        ivHeaderImage.image = [UIImage imageNamed:@"car_placeholder"];
    }
}


-(void) configureHotelHeader
{
    [viewHotelHeader setHidden:NO];

// AJC - BEGIN - please delete the below code if present past 2013-12-13
//    NSString *hotelName = segment.vendorName;
//    if(segment.segmentName != nil)
//        hotelName = [NSString stringWithFormat:@"%@ (%@)", hotelName, segment.segmentName];
// AJC - END - please delete the below code if present past 2013-12-13
    NSString *vendorName;
    if (segment.segmentName != nil)
        vendorName = segment.segmentName;
    else if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    lblHeading.text = vendorName;
    lblSub1.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"], segment.confirmationNumber];
    lblSub2.text = [NSString stringWithFormat:@"%@: %@",[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CHECK_IN"], [DateTimeFormatter formatDateForTravel:segment.relStartLocation.dateLocal]];
    lblSub3.text = [NSString stringWithFormat:@"%@: %@", [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CHECK_OUT"], [DateTimeFormatter formatDateForTravel:segment.relEndLocation.dateLocal]];
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"HOTELCELL", @"TO_VIEW", @"YES", @"REFRESHING"
                                 , segment.gdsId, @"GDS", segment.propertyId, @"PROPERTY_ID", segment, @"SEGMENT", nil]; 
    [[ExSystem sharedInstance].msgControl createMsg:HOTEL_IMAGES CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


-(void) configureRailHeader
{
    [viewHotelHeader setHidden:YES];
    [viewAirHeader setHidden:NO];
    
    NSString *railStation = [SegmentData getRailStation: segment.relStartLocation];
    NSString *endRailStation = [SegmentData getRailStation: segment.relEndLocation];
    
    lblHeadingAir.text = [NSString stringWithFormat:@"%@ %@ %@", railStation, [Localizer getLocalizedText:@"SLV_TO"], endRailStation];
    lblAirConfirm.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"], segment.confirmationNumber];
    
    // AJC - please delete the below code if present past 2013-12-13
    //tableList.frame = CGRectMake(0, 186, 320, 190);
    
    //Airline
    NSString *vendor = [NSString stringWithFormat:@"%@ %@", segment.vendorName, segment.trainNumber];
    if(segment.trainNumber == nil)
        vendor = segment.vendorName;

    lblAirline.text = vendor;
    
    //Departure Airport
    lblDepart.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"Depart"], railStation];
    
    //Arrive Airport
    lblArrive.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"Arrive"], endRailStation];
    
    //Departure time and date
    NSMutableString *departTime = [NSMutableString string];
    NSMutableString *departDate = [NSMutableString string];
    [SegmentData getDepartTimeString:segment timeStr:departTime dateStr:departDate];    
    
    NSArray *aTime = [departTime componentsSeparatedByString:@" "];
    if([aTime count] == 2)
    {
        lblDepartTime.text = aTime[0];
        lblDepartAMPM.text = aTime[1];
    }
    else
    {
        lblDepartTime.text = departTime;
        lblDepartAMPM.text = @"";
    }
    
    lblDepartDate.text = departDate;
    
    
    //Arrival Time and Date
    NSMutableString *arriveTime = [NSMutableString string];
    NSMutableString *arriveDate = [NSMutableString string];
    [SegmentData getArriveTimeString:segment timeStr:arriveTime dateStr:arriveDate];
    lblArriveDate.text = arriveDate;
    
    aTime = [arriveTime componentsSeparatedByString:@" "];
    if([aTime count] == 2)
    {
        lblArriveTime.text = aTime[0];
        lblArriveAMPM.text = aTime[1];
    }
    else
    {
        lblArriveTime.text = arriveTime;
        lblArriveAMPM.text = @"";
    }
    
    //Depart Gate Terminal
    if(segment.numStops != nil)
		lblDepartTerminal.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STOPS"] , segment.numStops];
    else
        lblDepartTerminal.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STOPS"] , @"--"];
    
    //Arrival Gate and Terminal
    NSString *sDuration = [DateTimeFormatter formatDuration:segment.relStartLocation.dateLocal endDate:segment.relEndLocation.dateLocal];
    if(sDuration != nil)
		lblArriveTerminal.text = sDuration; //[NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DURATION"] , 
    else
        lblArriveTerminal.text = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DURATION"] , @"--"];
    
    lblDepartGate.text = @"";
    lblArriveGate.text = @"";
    
}


-(void) configureParkingHeader
{    
    [self configureBasicHeader:segment.vendorName sub1Text:[DateTimeFormatter formatDateTimeForTravel: segment.relStartLocation.dateLocal] imageName:@"itin_icon_parking44"];
}

-(void) configureRideHeader
{    
    [self configureBasicHeader:segment.vendorName sub1Text:[DateTimeFormatter formatDateTimeForTravel: segment.relStartLocation.dateLocal] imageName:@"itin_icon_taxi44"];
}


-(void) configureBasicHeader:(NSString*) headerText sub1Text:(NSString *)sub1Text imageName:(NSString *)imageName
{
    [viewHotelHeader setHidden:NO];
    
    [lblSub2 setHidden:YES];
    [lblSub3 setHidden:YES];
    [ivHeaderImage setHidden:YES];
    [activityImage stopAnimating];
    [activityImage setHidden:YES];
   
    ivHeaderBackground.image = [UIImage imageNamed: @"header_itin"];
    ivHeaderBackground.frame = CGRectMake(0, 0, 320, 64);
    
    ivHotelBackground.frame = CGRectMake(10, 10, 44, 44);
    ivHotelBackground.image = [UIImage imageNamed:imageName];
    
    [lblHeading setFont:[UIFont fontWithName:@"HelveticaNeue-Bold" size:20.0]];
    lblHeading.frame = CGRectMake(64, 12, 250, 22);
    
    lblSub1.frame = CGRectMake(64, 35, 250, 17);
    
    // AJC - please delete the below code if present past 2013-12-13
    //tableList.frame = CGRectMake(0, 64, 320, 299 + 64);
    viewHotelHeader.frame = CGRectMake(0, 0, 320, 74);
    [viewHotelHeader setBackgroundColor:[UIColor baseBackgroundColor]];
    [tableList setBackgroundColor:[UIColor baseBackgroundColor]];
    
    lblHeading.text = headerText;
    lblSub1.text = sub1Text;
}


#pragma mark - Button Stuff
-(IBAction) buttonPressedViewHotelImages:(id)sender
{
    if([activityImage isAnimating])
        return;
    
    iPadImageViewerVC *vc = [[iPadImageViewerVC alloc] initWithNibName:@"iPadImageViewerVC" bundle:nil];
    vc.imageArray = aHotelImageViews; // Load array before presenting view
    vc.modalPresentationStyle = UIModalPresentationFormSheet;
    [self presentViewController:vc animated:YES completion:nil];
}

#pragma mark - Image Loader
-(UIImage *)getCarImageAsynch:(NSString *)vCode CountryCode:(NSString *)countryCode ClassOfCar:(NSString *)classOfCar BodyType:(NSString *)bodyType FetchURI:(NSString *)fetchURI
{
	NSString *imageFileName = [NSString stringWithFormat:@"%@%@%@%@999.jpg", vCode, countryCode, classOfCar, bodyType];
    NSData *imageData = [[ExSystem sharedInstance].imageControl fetchFromDiskPure:imageFileName];
	if (imageData != nil || ([imageData length] > 50))
	{
        __autoreleasing UIImage* result = [[UIImage alloc] initWithData:imageData];
        return result;
    }
    //Discovered this error in Apple Demo. FetchURI can be nil, and we car sending "www.concursolutions.com(null)" request to fetch for car image.
    //This will cause a connection error alert.
    //Suspect nil fetchURI is caused by the type of car booking. It maybe an openBooking which is not supported on mobile yet.
    //Mobile car booking only works with off-airpot booking for now. SW 05-04-13
    else if (fetchURI != nil)
    {
        NSString *xURL = [NSString stringWithFormat:@"%@%@", [ExSystem sharedInstance].entitySettings.uriNonSSL, fetchURI];
        
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:imageFileName, @"IMAGE_NAME",  xURL, @"XURL", @"INFO", @"GOES_TO", vCode, @"VendorCode", [self getViewIDKey], @"TO_VIEW", @"YES", @"SKIP_CACHE", nil ];
        
        [[ExSystem sharedInstance].msgControl createMsg:CAR_IMAGE CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
    return nil;
}
                              
-(UIImage *)scaleImageToFit:(UIImage *) img MaxW:(float)maxW MaxH:(float)maxH
{
    int w = img.size.width;
    int h = img.size.height;
    float scaler = (float)w / maxW;
    if(w <= maxW && h > maxH)
    {
        scaler = (float)h / maxH;
        w = w / scaler;
        return [ImageUtil imageWithImage:img scaledToSize:CGSizeMake(w, maxH)];
    }
    else 
    {
        h = h / scaler;
        if(h > maxH)
        {
            scaler = (float)h / maxH;
            w = maxW / scaler;
            return [ImageUtil imageWithImage:img scaledToSize:CGSizeMake(w, maxH)];
        }
        else 
            return [ImageUtil imageWithImage:img scaledToSize:CGSizeMake(maxW, h)];
        // AJC - please delete the below code if present past 2013-12-13
        //[[UIImage alloc] initWithData:mydata];
    }
}

@end


