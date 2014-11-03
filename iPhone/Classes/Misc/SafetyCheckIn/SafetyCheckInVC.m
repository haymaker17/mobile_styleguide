//
//  SafetyCheckInVC.m
//  ConcurMobile
//
//  Created by yiwen on 8/4/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "SafetyCheckInVC.h"
#import "ConcurMobileAppDelegate.h"

#import "FindMe.h"
#import "LocationAnnotation.h"
#import "BasicMapViewController.h"
#import "SafetyCheckInData.h"
#import "Config.h"

@interface SafetyCheckInVC (Private)
@end

@implementation SafetyCheckInVC

@synthesize findMe, btnCheckIn;

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return LOCATION_CHECK_IN;
}


-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

-(void)respondToFoundData:(Msg *)msg
{
	if([msg.idKey isEqualToString:SAFETY_CHECK_IN_DATA])
    {
        [self hideWaitView];
		NSString* errMsg = msg.errBody;
		if (msg.errBody == nil && msg.responseCode != 200)
        {
            errMsg = [Localizer getLocalizedText:@"Check In Failed Message"];
        }
		else if (errMsg == nil && msg.responder != nil)
		{
			SafetyCheckInData* srd = (SafetyCheckInData*) msg.responder;
			if (srd.status != nil && ![srd.status.status isEqualToString:@"SUCCESS"])
            {
                if (srd.status.errMsg != nil)
                    errMsg = srd.status.errMsg;
                else
                    errMsg = [Localizer getLocalizedText:@"Check In Failed Message"];
            }
        }
		if (errMsg != nil) 
		{
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:msg.errCode
								  message:errMsg
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] 
								  otherButtonTitles:nil];
			[alert show];
		}
		else 
        {
            NSDictionary *dict = @{@"Came From": @"LnA View"};
            [Flurry logEvent:@"LnA: Check In" withParameters:dict];

            UIAlertView *alert = [[MobileAlertView alloc] 
                              initWithTitle:[Localizer getLocalizedText:@"Check In Complete"]
                              message:[Localizer getLocalizedText:@"Check In Complete Message"]
                              delegate:self
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] 
                              otherButtonTitles:nil];
        
            [alert show];
        }
    }
}

#pragma mark - Init data
- (void)setSeedData:(NSDictionary*)pBag
{
    counter = 0;
    if (self.findMe == nil)
    {
        self.findMe = [[FindMe alloc] initWithDelegate:self];
        // Give 2 minute to detect location
        [self performSelector:@selector(locationTimeOut) withObject:nil afterDelay:120];
    }
    
    [self initFields];
}

// We don't want to lose wait view
- (void)applicationDidEnterBackground
{
    doReload = YES;
}

#pragma mark - FindMeDelegate
-(void) updateLocation
{
    FormFieldData* field = (self.allFields!= nil && [self.allFields count]>0)? (self.allFields)[0] : nil;
    if (field == nil)
    {
        return;
    }
    
    if (![findMe.longitude length]||
        ([findMe.longitude hasPrefix:@"0.00000"] && [findMe.latitude hasPrefix:@"0.00000"] &&
         ![findMe.country length]))
    {
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"location Note Available"] Level:MC_LOG_DEBU];
        field.fieldValue = @"Not Available";
        if ([self isViewLoaded])
        {
            [btnCheckIn setEnabled:NO];
        }
        
        return;
    }

    // Use GPS to prepopulate this location field
    NSString *City = @"";
    NSString *State = @"";
    NSString *Country = @"";
    LocationAnnotation* locA = [[LocationAnnotation alloc] init];

    locA.city = findMe.city;
    locA.state = findMe.state;
    locA.country = findMe.country;
    locA.longitude = findMe.longitude;
    locA.latitude = findMe.latitude;
    locA.streetAddress = findMe.streetAddress;
    
    if(findMe.city != nil)
        City = [NSString stringWithFormat:@"%@, ", findMe.city];
    if(findMe.state != nil)
        State = findMe.state;
    if(findMe.country != nil)
        Country = findMe.country;
    
    NSString *locName = [NSString stringWithFormat:@"%@%@", City, ([State length] > 0 ? State : Country)];
    
    field.fieldValue = locName;
    field.liKey = [NSString stringWithFormat:@"%@,%@", findMe.longitude, findMe.latitude];
//    [[MCLogging getInstance] log:[NSString stringWithFormat:@"Found location %@ - loc name:%@ ", field.liKey, locName] Level:MC_LOG_DEBU];
    
//    UIAlertView *alert = [[MobileAlertView alloc] 
//                          initWithTitle:@"Location Found"
//                          message:findMe.longitude
//                          delegate:nil
//                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] 
//                          otherButtonTitles:nil];
//    
//    [alert show];
//    [alert release];

    field.extraDisplayInfo = locA;

    if ([self isViewLoaded])
    {
        [[MCLogging getInstance] log:@"Refreshing" Level:MC_LOG_DEBU];
        [btnCheckIn setEnabled:YES];
        [tableList reloadData];
        [self hideLoadingView];
    }
    else
    {
        doReload = YES;
        [[MCLogging getInstance] log:@"View not loaded. No Refreshing" Level:MC_LOG_DEBU];        
    }
}

-(void) locationFound:(FindMe*) fm
{
//    [[MCLogging getInstance] log:[NSString stringWithFormat:@"Location callback called - city %@ %@ %@", fm.city, fm.state, fm.country] Level:MC_LOG_DEBU];

    [self updateLocation];
}

-(void) locationNotFound:(NSString*) errMsg
{
    if (counter >=0)
        counter = -1;
    else
        // Notify user of failure only once
        return;
    
    if ([self isViewLoaded])
    {
        [btnCheckIn setEnabled:NO];
        [tableList reloadData];
        [self hideLoadingView];
    }
    
    UIAlertView *alert = [[MobileAlertView alloc] 
                          initWithTitle:@"Location Not Found"
                          message:@"Cannot find your current location.  Please try later."
                          delegate:nil
                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] 
                          otherButtonTitles:nil];
    
    [alert show];

}

-(void) locationTimeOut
{
//    NSLog(@"Location time out called");
    if ([self isViewLoaded])
    {
        FormFieldData* field = (self.allFields!= nil && [self.allFields count]>0)? (self.allFields)[0] : nil;
        if (field != nil && field.liKey != nil)
            return;

        [self locationNotFound:@"Timed out"];
    }
}

-(void) initFields
{
    self.sections = [[NSMutableArray alloc] initWithObjects:@"0", @"1", @"2", @"3", nil];
    self.sectionFieldsMap = [[NSMutableDictionary alloc] init];
    
    self.sectionFieldsMap = [[NSMutableDictionary alloc] init];
    
    self.allFields = [[NSMutableArray alloc] initWithObjects: nil];
    
    FormFieldData* field= nil;
    field = [[FormFieldData alloc] initField:@"CurrentLocation" label:[Localizer getLocalizedText:@"Current Location"] value:nil ctrlType:@"MapLocation" dataType:@"MapLocation"];
    field.required = @"Y";
    [allFields addObject:field];
    sectionFieldsMap[@"0"] = @[field];
    [self updateLocation];
    
    field = [[FormFieldData alloc] initField:@"DaysRemaining" label:[Localizer getLocalizedText:@"Days Remaining at Location"] value:[NSString stringWithFormat:@"1 %@", [Localizer getLocalizedText:@"Hour"]] ctrlType:@"picklist" dataType:@"INTEGER"];
    field.required = @"Y";
    field.liKey = @"-1";   // -1 for Not Sure, 7 for week, 30 for month, 365 for year 
    
    [allFields addObject:field];
    sectionFieldsMap[@"1"] = @[field];

    field = [[FormFieldData alloc] initField:@"ImmediateAssistance" label:[Localizer getLocalizedText:@"Immediate Assistance Required"] value:[Localizer getLocalizedText:@"No"] ctrlType:@"edit" dataType:@"BOOLEANCHAR"];
    field.liKey = @"N";
    field.required = @"Y";
    
    [allFields addObject:field];
    sectionFieldsMap[@"2"] = @[field];
    
    
    field = [[FormFieldData alloc] initField:@"CommentPlain" label:[Localizer getLocalizedText:@"Comment"] value:@"" ctrlType:@"textarea" dataType:@"VARCHAR"];
    [allFields addObject:field];
    
    sectionFieldsMap[@"3"] = @[field];
    
	[super initFields];
}

#pragma mark - View lifecycle

-(void)btnCheckInClicked
{
    [self showWaitViewWithText:@"Checking In ..."];
    
    // http://localhost/mobile/SafetyCheckIn.ashx?long=12.345&lat=-12.9876&city=redmond&state=wa&ctry=us&assist=Y&days=2&comment=quick
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: 
                                 [self getViewIDKey], @"TO_VIEW", @"YES", @"SKIP_CACHE", nil];
    
    for(FormFieldData *field in allFields)
    {
        if ([field.iD isEqualToString:@"CurrentLocation"])
        {
            LocationAnnotation* locA = (LocationAnnotation*)field.extraDisplayInfo;
            if(locA.city != nil)
                pBag[@"CITY"] = locA.city;
            if(locA.state != nil)
                pBag[@"STATE"] = locA.state;
            if(locA.country != nil)
                pBag[@"CTRY"] = locA.country;
            if(locA.longitude != nil)
                pBag[@"LONG"] = locA.longitude;
            if(locA.latitude != nil)
                pBag[@"LAT"] = locA.latitude;
            
        }
        else if([field.iD isEqualToString:@"DaysRemaining"])
        {
            pBag[@"DAYS"] = field.liKey;
        }
        else if([field.iD isEqualToString:@"ImmediateAssistance"])
        {
            pBag[@"ASSIST"] = field.liKey;
        }
        else if([field.iD isEqualToString:@"CommentPlain"])
        {
            if ([field.fieldValue length])
                pBag[@"COMMENT"] = field.fieldValue;
        }
    }
    [[ExSystem sharedInstance].msgControl createMsg:SAFETY_CHECK_IN_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];

}

-(void) loadHeaderView
{
    float w = 320;//[self isLandscape]? 480:320;//self.view.frame.size.width;
    NSString *emptyText = [Localizer getLocalizedText:@"SAFETY_CHECK_IN_HELP"];
    
    CGSize maxSize = CGSizeMake(w-20, 24);
    CGSize s = [emptyText sizeWithFont:[UIFont systemFontOfSize:12.0f] constrainedToSize:maxSize];
    
    CGRect lblRect =  CGRectMake(8, 5, w-12, s.height+1); // 6 px paddling
    UILabel* labelView = [[UILabel alloc] initWithFrame:lblRect];
    UIView* footerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, w, s.height+6)];
    footerView.autoresizingMask =UIViewAutoresizingFlexibleWidth;
    footerView.backgroundColor = [UIColor clearColor];
    [labelView setText:emptyText];
    [labelView setBackgroundColor:[UIColor clearColor]];
    [labelView setTextAlignment:NSTextAlignmentCenter];
    [labelView setFont:[UIFont systemFontOfSize:12.0f]];
    [labelView setTextColor:[UIColor darkGrayColor]];
    [labelView setShadowColor:[UIColor whiteColor]]; 
    [labelView setShadowOffset:CGSizeMake(1.0f, 1.0f)];
    labelView.numberOfLines = 0;
    labelView.lineBreakMode = NSLineBreakByWordWrapping;
    labelView.autoresizingMask = UIViewAutoresizingFlexibleWidth; // Do not adjust height
    [footerView addSubview:labelView];
    self.tableList.tableHeaderView = footerView;
    
    self.btnCheckIn = [UIButton buttonWithType:UIButtonTypeCustom];
    btnCheckIn.autoresizingMask = UIViewAutoresizingFlexibleWidth;
    CGRect btnRect =  CGRectMake(20, 3, 280, 45);
    btnCheckIn.frame = btnRect;
    	
	NSString *btnImage = @"checkin_button";
    [btnCheckIn setBackgroundImage:[[UIImage imageNamed:btnImage]
                                    stretchableImageWithLeftCapWidth:12.0f 
                                    topCapHeight:0.0f]
                          forState:UIControlStateNormal];
	
	[btnCheckIn addTarget:self action:@selector(btnCheckInClicked) forControlEvents:UIControlEventTouchUpInside];
	
	[btnCheckIn setTitle:[Localizer getLocalizedText:@"Check In"] forState:UIControlStateNormal];
	btnCheckIn.titleLabel.font = [UIFont boldSystemFontOfSize:17.0];

    footerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, w, 50)];
    footerView.autoresizingMask =UIViewAutoresizingFlexibleWidth;
    footerView.backgroundColor = [UIColor clearColor];
    [footerView addSubview:btnCheckIn];
    if (findMe.latitude == nil)
    {
        [btnCheckIn setEnabled:FALSE];
        [self showLoadingViewWithText:[Localizer getLocalizedText:@"LNA_LOADING_TEXT"]];
    }
    
//    [[MCLogging getInstance] log:[NSString stringWithFormat:@"Creating check in btn, lat- %@, city - %@ ", findMe.latitude, findMe.city] Level:MC_LOG_DEBU];

    self.tableList.tableFooterView = footerView;

}


-(void)viewWillAppear:(BOOL)animated 
{ 
	if(doReload)
	{
		doReload = NO;
		[tableList reloadData];
        [[MCLogging getInstance] log:@"viewWillAppear:reload table" Level:MC_LOG_DEBU];
	}
	
	[super viewWillAppear:animated]; 
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad
{
    [super viewDidLoad];
    self.title = [Localizer getLocalizedText:@"Safety Check In"];

    [self loadHeaderView];
    
    //[self setupToolbar];
}

// override super version of this.
- (void)setupToolbar
{
    if([UIDevice isPad])
	{
		UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeView:)];
		self.navigationItem.leftBarButtonItem = nil;
		self.navigationItem.leftBarButtonItem = btnCancel;
	} else {
        [super setupToolbar];
    }
}

-(void)closeView:(id)sender
{
	[self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - Alert view delegate
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
    if ([UIDevice isPad])
        [self dismissViewControllerAnimated:YES completion:NULL];
    else
        [self.navigationController popViewControllerAnimated:YES];
}

#pragma mark - Custom field Edit
-(void)showMapLocation:(FormFieldData*)field
{
    BasicMapViewController *lvc = [[BasicMapViewController alloc] initWithNibName:@"BasicMapViewController" bundle:nil];;
    [lvc setSeedData:(LocationAnnotation*)field.extraDisplayInfo];
    lvc.title = [Localizer getLocalizedText:@"Safety Check In"];

	if([UIDevice isPad])
		lvc.modalPresentationStyle = UIModalPresentationFormSheet;
	[self.navigationController pushViewController:lvc animated:YES];

}

-(ListItem*) makeListItem:(NSString*)value key:(NSString*) key
{
    __autoreleasing ListItem* item = [[ListItem alloc] init];
    item.liKey = key;
    item.liName = value;
    return item;
    
}
-(void)prefetchForListEditor:(ListFieldEditVC*) lvc // fetch list data
{
    //FormFieldData* field = lvc.field;
    
    ListItem* notSureItem = [self makeListItem:[NSString stringWithFormat:@"1 %@", [Localizer getLocalizedText:@"Hour"]] key:@"-1"];
    ListItem* oneDayItem = [self makeListItem:[NSString stringWithFormat:@"1 %@", [Localizer getLocalizedText:@"Day"]] key:@"1"];
    ListItem* twoDayItem = [self makeListItem:[NSString stringWithFormat:@"2 %@", [Localizer getLocalizedText:@"Days"]] key:@"2"];
    ListItem* threeDayItem = [self makeListItem:[NSString stringWithFormat:@"3 %@", [Localizer getLocalizedText:@"Days"]] key:@"3"];
    ListItem* fourDayItem = [self makeListItem:[NSString stringWithFormat:@"4 %@", [Localizer getLocalizedText:@"Days"]] key:@"4"];
    ListItem* fiveDayItem = [self makeListItem:[NSString stringWithFormat:@"5 %@", [Localizer getLocalizedText:@"Days"]] key:@"5"];
    ListItem* sixDayItem = [self makeListItem:[NSString stringWithFormat:@"6 %@", [Localizer getLocalizedText:@"Days"]] key:@"6"];
    ListItem* weekItem = [self makeListItem:[Localizer getLocalizedText:@"At Least 1 Week"] key:@"7"];
    ListItem* monthItem = [self makeListItem:[Localizer getLocalizedText:@"At Least 1 Month"] key:@"30"];
//    ListItem* yearItem = [self makeListItem:@"At Least 1 Year" key:@"365"];
    
    NSMutableArray *sectionValues = [[NSMutableArray alloc] initWithObjects:notSureItem, oneDayItem, twoDayItem, threeDayItem, fourDayItem, fiveDayItem, sixDayItem, weekItem, monthItem, nil];
    
    lvc.sectionKeys = [[NSMutableArray alloc] initWithObjects: @"DAYS_REMAINING", nil];
    lvc.sections = [[NSMutableDictionary alloc] initWithObjectsAndKeys: sectionValues, @"DAYS_REMAINING", nil];
    
    if ([lvc isViewLoaded])
    {
        [lvc.searchBar removeFromSuperview];
        CGRect frame = lvc.resultsTableView.frame;
        lvc.resultsTableView.frame = CGRectMake(frame.origin.x, frame.origin.y-44, frame.size.width, frame.size.height+44);
        [lvc.resultsTableView reloadData];
        [lvc hideLoadingView];
    }
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    FormFieldData* field = [self findFieldWithIndexPath:newIndexPath];
	if (field.dataType != nil  && [field.dataType isEqualToString:@"MapLocation"]) // Do not localize!
    {
        if (field.extraDisplayInfo != nil) // MOB-8748 handle location turned off
        {
            [self showMapLocation:field];
            return;
        }
    }
    [super tableView:tableView didSelectRowAtIndexPath:newIndexPath];
}

-(void)updateSaveBtn
{
    self.navigationItem.rightBarButtonItem = nil;
    self.isDirty = FALSE;
}

@end
