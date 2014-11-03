//
//  IgniteItinDetailVC_iPad.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/25/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "IgniteItinDetailTripDS.h"
#import "IgniteItinDetailTripDelegate.h"
#import "IgniteItinDetailSocialOppDS.h"
#import "IgniteItinDetailSocialOppDelegate.h"
#import "IgniteItinDetailSocialFeedDS.h"
#import "IgniteItinDetailSocialFeedDelegate.h"
#import "IgniteChatterPostDelegate.h"
#import "IgniteChatterConversationVCDelegate.h"
#import "EntityTrip.h"
#import "IgnitePopoverModalDelegate.h"
#import "igniteSegmentEditDelegate.h"
#import "IgniteItinShareTripDelegate.h"
#import "ReportData.h"

@interface IgniteItinDetailVC_iPad : MobileViewController 
    <IgniteItinDetailTripDelegate, 
    IgniteItinDetailSocialOppDelegate,
    IgniteItinDetailSocialFeedDelegate,
    IgnitePopoverModalDelegate,
    IgniteSegmentEditDelegate,
    IgniteChatterPostDelegate,
    IgniteChatterConversationVCDelegate,
    IgniteItinShareTripDelegate>
{
    UITableView                 *tblTrip;
    IgniteItinDetailTripDS      *dsTrip;
    UIView                      *vwTHeaderTrip;
    UIButton                    *btnTripAddSegment;
    UILabel                     *lblTripName;
    UILabel                     *lblTripDestination;
    
    // Social pane
    UITableView                 *tblSocialOpportunities;
    IgniteItinDetailSocialOppDS *dsSocialOpp;
    UITableView                 *tblSocialFeed;
    IgniteItinDetailSocialFeedDS*dsSocialFeed;
    
    // Table header views
    UIView                      *vwTHeaderSocialOpportunities;
    UIView                      *vwTHeaderSocialFeed;
    
    UIView                      *waitViewSocial;
    
    // Social button bar
    UIView                      *vwHeaderSocial;
    UIButton                    *btnTripMaximizer;
    UIButton                    *btnTripFeed;
    BOOL                        bTripMaximizerPressed; 
    
    UIButton                    *btnOpportunityMap;
    
    // Background
    UIImageView                 *imgBackAll;
    
    // Popover
    UIPopoverController         *vcPopover;
    
    NSString                    *tripKey;
    EntityTrip                  *trip;
    
    // For Expense Trip
    ReportData                  *rpt; // report with the same name as the trip
}

@property (nonatomic, strong) IBOutlet UITableView                  *tblTrip;
@property (nonatomic, strong) IgniteItinDetailTripDS                *dsTrip;
@property (nonatomic, strong) IBOutlet UIView                       *vwTHeaderTrip;

@property (nonatomic, strong) IBOutlet UIButton                     *btnTripAddSegment;
@property (nonatomic, strong) IBOutlet UILabel                      *lblTripName;
@property (nonatomic, strong) IBOutlet UILabel                      *lblTripDestination; // subline under trip name

@property (nonatomic, strong) IBOutlet UITableView                  *tblSocialOpportunities;
@property (nonatomic, strong) IgniteItinDetailSocialOppDS           *dsSocialOpp;
@property (nonatomic, strong) IBOutlet UITableView                  *tblSocialFeed;
@property (nonatomic, strong) IgniteItinDetailSocialFeedDS          *dsSocialFeed;

@property (nonatomic, strong) IBOutlet UIView                       *vwHeaderSocial;
@property (nonatomic, strong) IBOutlet UIButton                     *btnTripMaximizer;
@property (nonatomic, strong) IBOutlet UIButton                     *btnTripFeed;
@property BOOL                        bTripMaximizerPressed; 

@property (nonatomic, strong) IBOutlet UIButton                     *btnOpportunityMap;

@property (nonatomic, strong) IBOutlet UIView                       *vwTHeaderSocialOpportunities;
@property (nonatomic, strong) IBOutlet UIView                       *vwTHeaderSocialFeed;
@property (nonatomic, strong) IBOutlet UIView                       *waitViewSocial;

@property (nonatomic, strong) IBOutlet UIImageView                  *imgBackAll;

@property (nonatomic, strong) UIPopoverController                   *vcPopover;

@property (nonatomic, strong) NSString                              *tripKey;
@property (nonatomic, strong) EntityTrip                            *trip;

@property (nonatomic, strong) ReportData                            *rpt;

#pragma social actions
- (IBAction)buttonOpportunityMapPressed:(id)sender;

#pragma social toggle actions
- (IBAction)buttonTripMaximizerPressed:(id)sender;
- (IBAction)buttonTripFeedPressed:(id)sender;

#pragma chatter actions
- (IBAction)buttonChatterPostPressed:(id)sender;
- (IBAction)buttonChatterPostPrivatePressed:(id)sender;

#pragma IgniteItinDetailTripDelegate method
- (void) segmentSelected:(EntitySegment*) segment withCell:(UITableViewCell*) cell;

#pragma IgnitePopoverModalDelegate method
- (void) dismissPopoverModal;
#pragma IgniteSegmentEditDelegate method
- (void) segmentUpdated:(EntitySegment*) segment;

@end
