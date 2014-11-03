//
//  TripItExpenserViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/29/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "TripItExpenserViewController.h"

#import "TripItCacheData.h"
#import "TripsData.h"
#import "TripItExpenseTripData.h"
#import "ReportViewControllerBase.h"
#import "iPadHomeVC.h"

@interface TripItExpenserViewController (private)
-(void)closeMe;
@end

@implementation TripItExpenserViewController

@synthesize tripitTripId, cacheKey;

#pragma mark - Lifecycle Methods


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidAppear:(BOOL)animated
{
    if (self.cacheKey != nil)
    {
        [self showLoadingViewWithText:[Localizer getLocalizedText:@"Connecting with TripIt"]];
        
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:cacheKey, @"CACHE_KEY", nil]; // This mvc will not be called unless pbag is provided
        [[ExSystem sharedInstance].msgControl createMsg:GET_TRIPIT_CACHE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];

        self.cacheKey = nil;
    }
	[super viewDidAppear:animated];
}

#pragma mark - MessageHandlers

-(void)respondToFoundData:(Msg *)msg
{
	[[MCLogging getInstance] log:[NSString stringWithFormat: @"TripItExpenserViewController::respondToFoundData %@", msg.idKey] Level:MC_LOG_DEBU];
    
	if ([msg.idKey isEqualToString:GET_TRIPIT_CACHE_DATA])
	{
        TripItCacheData *data = (TripItCacheData*)msg.responder;
        
        if (data.tripId == nil || [data.tripId length] == 0)
        {
			MobileAlertView *alert = [[MobileAlertView alloc]
                                      initWithTitle: [Localizer getLocalizedText:@"Error"]
                                      message: [Localizer getLocalizedText:@"TripIt trip key not found"]
                                      delegate:nil 
                                      cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                      otherButtonTitles:nil];
			[alert show];
            
            [self closeMe];
            return;
        }

        self.tripitTripId = [data.tripId intValue];
        if (self.tripitTripId > 0)
        {
            if (data.loginIdOfLinkedAccount == nil || [data.loginIdOfLinkedAccount length] == 0)
            {
                // TripIt account is not linked to any Concur account.  Ask the mobile user if they
                // want to link their TripIt account to the Concur account they are currently logged into?
                // TODO: handle this case.
                [self closeMe];
                MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Cannot expense trip"] message:[Localizer getLocalizedText:@"TripIt not linked"] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
                [av show];
            }
            else
            {
                // The TripIt account is linked to a Concur account.  Let's see if it's linked to the Concur account
                // that we are currently logged into.
                if ([ExSystem sharedInstance].userName == nil || [[ExSystem sharedInstance].userName length] == 0)
                {
                    // We don't know which Concur user we are logged in as, but we know who we need to be logged in as.
                    // This should never happen... if we are logged in, then we must have a user name!
                    [self closeMe];
                }
                else
                {
                    NSString *lowerCaseLoginIdOfLinkedAccount = data.loginIdOfLinkedAccount.lowercaseString;
                    NSString *lowercaseUserNameOfLoggedInUser = [ExSystem sharedInstance].userName.lowercaseString;
                    if ([lowercaseUserNameOfLoggedInUser isEqualToString:lowerCaseLoginIdOfLinkedAccount])
                    {
                        // OK, we're logged in to the account that is linked to tripit, so go ahead and expense the trip.
                        // Do not synchronize.  We just want to expense the trip using the tripipt trip id.  What does need to be done is that whole holding on to this class as something off of the app delegate.  Bad.
                        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
                        [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
                    }
                    else
                    {
                        [self closeMe];
                        // Logged into wrong Concur account (this is not the Concur account that the TripIt account is linked to)
                        MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Cannot expense trip"] message:[Localizer getLocalizedText:@"TripIt not linked together"] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
                        [av show];
                    }
                }
            }
        }
        else
        {
            [self closeMe];
            
            if (data.loginIdOfLinkedAccount != nil || [data.loginIdOfLinkedAccount length] > 0)
            {
                MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Cannot expense trip"] message:[Localizer getLocalizedText:@"Please log in to SmartExpense using the same account already linked to TripIt."] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
                [av show];
            }
            else {
                MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Cannot expense trip"] message:[Localizer getLocalizedText:@"Please log in to SmartExpense and then link your accounts together."] delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
                [av show];
            }
            
        }
    }
    else if ([msg.idKey isEqualToString:EXPENSE_TRIPIT_TRIP])
	{
        [self closeMe];

        TripItExpenseTripData *data = (TripItExpenseTripData*)msg.responder;
        NSString *rptKey = data.rptKey;
        
        if (data.actionStatus != nil && data.actionStatus.status != nil && [data.actionStatus.status isEqualToString:@"SUCCESS"] && rptKey != nil && [rptKey length] > 0)
        {
            [ReportViewControllerBase refreshSummaryData];

            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                         rptKey, @"ID_KEY", 
                                         rptKey, @"RECORD_KEY", 
                                         @"YES", @"SHORT_CIRCUIT",
                                         ROLE_EXPENSE_TRAVELER, @"ROLE", nil];
            if([UIDevice isPad])
            {
                pBag[@"COMING_FROM"] = @"TRIPIT";
                ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
                [delegate.padHomeVC switchToDetail:@"Report" ParameterBag:pBag];
            }
            else 
            {
                pBag[@"POP_TO_ROOT_VIEW"] = @"YES";
                [ConcurMobileAppDelegate switchToView:ACTIVE_ENTRIES viewFrom:HOME_PAGE ParameterBag:pBag];
            }
        }
        else
        {
            NSString *errorMessage = nil;
            
            if (data.actionStatus != nil && [data.actionStatus.errMsg length])
                errorMessage = data.actionStatus.errMsg;
            else
                errorMessage = [Localizer getLocalizedText:@"SmartExpense was unable to expense your trip. Please try again later."];
            
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Error"] message:errorMessage delegate:nil cancelButtonTitle:@"OK" otherButtonTitles: nil];
            [av show];
        }
    }
    else if ([msg.idKey isEqualToString:TRIPS_DATA])
	{
		TripsData* td = (TripsData *)msg.responder;
		NSArray *keys = td.keys;
		NSDictionary *trips = td.trips;
        
        NSString *tripId = [NSString stringWithFormat:@"%i", tripitTripId];
        BOOL alreadyExpensed = NO;
        
        for (NSString *key in keys)
        {
            TripData *trip = trips[key];
            NSString *clientLocator = trip.clientLocator;
            if (clientLocator != nil && [clientLocator isEqualToString:tripId])
            {
                alreadyExpensed = trip.isExpensed;
                break;
            }
        }
        
        if (!alreadyExpensed)
        {
            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: tripId, @"TRIPIT_TRIPID", nil];
            [[ExSystem sharedInstance].msgControl createMsg:EXPENSE_TRIPIT_TRIP CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
        }
        else
        {
            [self closeMe];
            MobileAlertView *av = [[MobileAlertView alloc]
                                   initWithTitle:[Localizer getLocalizedText:@"Error"]
                                   message:[Localizer getLocalizedText:@"Could not expense trip"]
                                   delegate:nil
                                   cancelButtonTitle:@"OK"
                                   otherButtonTitles: nil];
            [av show];
        }
    }
}

#pragma mark - Close

-(void)closeMe
{
    [self.navigationController popViewControllerAnimated:NO];
}

@end
