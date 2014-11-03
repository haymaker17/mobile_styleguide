//
//  CarMileageData.m
//  ConcurMobile
//
//  Created by ernest cho on 3/22/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CarMileageData.h"
#import "ReportEntryFormData.h"
#import "ReportEntryViewController.h"

@interface CarMileageData()
@property (weak, nonatomic) SelectReportViewController *selectReportVC;
@end

@implementation CarMileageData

// This class is only for SelectReportViewController!  It is basically a delegate that handles all the car mileage related stuff for the select report view controller.  It's specific to that class and is not meant to be reused.
- (void)userSelectedReport:(NSString*) rptKey rpt:(ReportData*) rpt inView:(SelectReportViewController *)view
{
    self.selectReportVC = view;
    
    [self.selectReportVC showWaitView];
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: @"MILEG", @"EXP_KEY", rpt.rptKey, @"RPT_KEY", rpt, @"rpt", nil];
    
	[[ExSystem sharedInstance].msgControl createMsg:REPORT_ENTRY_FORM_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

- (void)didProcessMessage:(Msg *)msg
{
    [self respondToFoundData:msg]; // TODO: handle case where msg.didConnectionFail is YES
}

- (void)respondToFoundData:(Msg *)msg
{
    [self.selectReportVC hideWaitView];
	if ([msg.idKey isEqualToString:REPORT_ENTRY_FORM_DATA]) {
		if (msg.errBody != nil) {
			UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"ADD_CAR_MILEAGE_HOME_NOT_SUPPORTED"] message:msg.errBody delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] otherButtonTitles:nil];
			[alert show];
		} else {
            if (msg.responseCode == 200) {
                [self presentPersonalCarMileageForm:msg];
            }
		}
	}
}

- (void)presentPersonalCarMileageForm:(Msg *)msg
{
	// MOB-5133 Reuse showEntryView to make sure DistanceToDate is fetched for mileage.
	ReportEntryFormData* resp = (ReportEntryFormData*) msg.responder;
	ReportData *rpt = (msg.parameterBag)[@"rpt"];
	
	resp.rpt.entry.parentRpeKey = @"";
	// Temp fix, b/c server passes back garbage
	resp.rpt.entry.rpeKey = nil;
	resp.rpt.entry.rptKey =rpt.rptKey;
	resp.rpt.entry.transactionCrnCode = rpt.crnCode;
    
    /*
    // pretty sure this is no longer needed since the request to load it should be called earlier by the CarMileageDataLoader
	if(self.carRatesData == nil)
	{
		NSMutableDictionary *pBag2 = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
		[[ExSystem sharedInstance].msgControl createMsg:CAR_RATES_DATA CacheOnly:@"NO" ParameterBag:pBag2 SkipCache:NO RespondTo:self];
	}
     */
	
	
    NSMutableDictionary* pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: @"YES", @"SHORT_CIRCUIT", resp.rpt.entry, @"ENTRY", rpt, @"REPORT", nil];
    pBag[@"ROLE"] = ROLE_EXPENSE_TRAVELER;
    
    [self showEntryViewWithParameterBag:pBag];
}

- (void)showEntryViewWithParameterBag:(NSMutableDictionary*)pBag
{
	ReportEntryViewController *vc = [[ReportEntryViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
    vc.isCarMileage = YES;
    vc.isFromHome = YES;
    vc.title = [Localizer getLocalizedText:@"Add Car Mileage"];
    
	Msg *msg = [[Msg alloc] init];
	msg.parameterBag = pBag;
    
    [self.selectReportVC.navigationController pushViewController:vc animated:YES];
	
    /*
     // pretty sure this is no longer needed since we do not push this from home anymore
     if([UIDevice isPad])
     {
     // MOB-8533 allow dismiss keyboard using modal form sheet
     UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:vc];
     localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
     [localNavigationController setToolbarHidden:NO];
     localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
     localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
     
     [parentVC presentViewController:localNavigationController animated:YES completion:nil];
     } else {
     [parentVC.navigationController pushViewController:vc animated:YES];
     }
     */
    
	[vc respondToFoundData:msg];
}


@end
