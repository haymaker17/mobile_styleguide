//
//  IgniteItinDetailVC_iPad.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/25/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteItinDetailVC_iPad.h"
#import "ImageUtil.h"
#import "TripManager.h"
#import "ExSystem.h"
#import "IgniteChatterPostVC.h"
#import "IgniteChatterPostPrivateVC.h"
#import "EntityChatterFeedEntry.h"
#import "EntityChatterAuthor.h"
#import "IgniteChatterConversationVC.h"
#import "IgniteItinShareTripVC.h"
#import "IgniteMeetingDetailVC.h"
#import "IgniteChatterOppPostVC.h"
#import "IgniteDiningMapVC.h"
#import "IgniteOpportunityMapVC.h"
#import "IgniteVendorAnnotation.h"
#import "ListDataBase.h"
#import "ReportDetailViewController_iPad.h"

@interface IgniteItinDetailVC_iPad (Private)
- (void)setupSocialViews;
- (void)switchSocialView;
- (void)presentChatterPostVCForFeedEntry:(EntityChatterFeedEntry*)entry;

- (void)handleDragDrop:(UIPanGestureRecognizer *)gestureRecognizer;
- (void)startDraggingFromSrcAtPoint:(CGPoint) point;
- (void)startDragging:(UIPanGestureRecognizer *)gestureRecognizer;
- (void)doDrag:(UIPanGestureRecognizer *)gestureRecognizer;
- (void)stopDragging:(UIPanGestureRecognizer *)gestureRecognizer;

- (void) shareTrip;
- (void) expenseTrip;

- (void)retrieveReports;

@end

@implementation IgniteItinDetailVC_iPad
@synthesize vwTHeaderTrip, vwHeaderSocial, vwTHeaderSocialFeed, vwTHeaderSocialOpportunities;
@synthesize btnTripFeed, btnTripMaximizer, btnOpportunityMap;
@synthesize tblTrip, tblSocialFeed, tblSocialOpportunities;
@synthesize bTripMaximizerPressed;
@synthesize dsTrip, dsSocialOpp, dsSocialFeed;
@synthesize imgBackAll, vcPopover;
@synthesize btnTripAddSegment, lblTripName, lblTripDestination;
@synthesize trip, tripKey, rpt;
@synthesize waitViewSocial;

- (void)dealloc
{
    
    
    dsSocialFeed.delegate = nil;
    dsSocialOpp.delegate = nil;
    dsTrip.delegate = nil;
    
    
    
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Setup nav bar
    self.title = @"Trip Details";
    
    UIBarButtonItem* btnExpenseTrip = [ExSystem makeColoredButton:@"IGNITE_BLUE" W:125 H:31 Text:(NSString *)@"Expense Trip" SelectorString:@"expenseTrip" MobileVC:self];
    UIBarButtonItem* btnShareTrip = [ExSystem makeColoredButton:@"IGNITE_BLUE" W:125 H:31 Text:(NSString *)@"Share Trip" SelectorString:@"shareTrip" MobileVC:self];
    
	self.navigationItem.rightBarButtonItem = nil;
	NSArray * btns = [NSArray arrayWithObjects:btnExpenseTrip, btnShareTrip, nil];
	[self.navigationItem setRightBarButtonItems:btns animated:NO];
    
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];

    self.lblTripName.text = self.trip.tripName;
    self.lblTripDestination.text = self.trip.tripDescription;//@"Dreamforce 2012";

    [self.imgBackAll setBackgroundColor:[UIColor baseBackgroundColor]];
    
    // Set up Trip table 
    self.dsTrip = [[IgniteItinDetailTripDS alloc] init];
    [dsTrip setSeedData:[ad managedObjectContext] withTripKey:trip.tripKey withTrip:trip withTable:self.tblTrip withDelegate:self];
    
    // Do any additional setup after loading the view from its nib.
    self.bTripMaximizerPressed = YES;
    [self setupSocialViews];
    
    // set up gestures
    UIPanGestureRecognizer* panGesture = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handleDragDrop:)];
    [self.view addGestureRecognizer:panGesture];
    
    // Retrieve a list of reports
    [self retrieveReports];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    if (interfaceOrientation == UIInterfaceOrientationLandscapeRight || interfaceOrientation == UIInterfaceOrientationLandscapeLeft)
        return YES;
    return NO;
}

- (void)retrieveReports
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
    NSString* cacheOnly =  [[ExSystem sharedInstance] shouldSendRequestsOverNetwork]? @"NO" :@"YES";

    [[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORTS_DATA CacheOnly:cacheOnly ParameterBag:pBag SkipCache:NO RespondTo:self];
}

// Locate the report with the same name as the trip
- (void)locateReport:(NSDictionary *)reportsByRptKey
{
    NSString *reportName = self.trip.tripName;
    reportName = [reportName stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    for (ReportData *thisRpt in [reportsByRptKey allValues])
    {
        if ([thisRpt.reportName isEqualToString:reportName])
        {
            self.rpt = thisRpt;
        }
    }
}


-(void)respondToFoundData:(Msg *)msg
{   
	if ([msg.idKey isEqualToString:ACTIVE_REPORTS_DATA])
	{
		ListDataBase *lst = (ListDataBase *)msg.responder;
        
		[self locateReport:lst.objDict];
    }
}

#pragma social actions
- (IBAction)buttonOpportunityMapPressed:(id)sender
{
    IgniteOpportunityMapVC *mvc = [[IgniteOpportunityMapVC alloc] initWithNibName:@"IgniteOpportunityMapVC" bundle:nil];
    
    IgniteVendorAnnotation* locA = [[IgniteVendorAnnotation alloc] init];
    
    locA.name = @"The Westin St. Francis";
    locA.longitude = @"-122.408534";
    locA.latitude = @"37.788336";
    locA.address = @"335 Powell Street";
    locA.cityzip = @"San Francisco, CA 94102";

    mvc.currentLoc = locA;
    mvc.delegate = self;
    mvc.lstOpportunityId = [NSMutableArray array];
    mvc.trip = self.trip;
    NSArray* opps = [self.dsSocialOpp.fetchedResultsController fetchedObjects];
    for (EntitySalesOpportunity * opp in opps)
    {
        [mvc.lstOpportunityId addObject:opp.opportunityId];
    }
    UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:mvc];
    localNavigationController.modalPresentationStyle = UIModalPresentationPageSheet;
    [localNavigationController setToolbarHidden:NO];
    localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
    localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
    
    [self presentViewController:localNavigationController animated:YES completion:nil];
}

#pragma social toggle actions
- (IBAction)buttonTripMaximizerPressed:(id)sender
{
    self.bTripMaximizerPressed = YES;
    [self switchSocialView];
}
- (IBAction)buttonTripFeedPressed:(id)sender
{
    self.bTripMaximizerPressed = NO;
    [self switchSocialView];    
}

#pragma mark - IgniteItinDetailSocialFeedDelegate methods
- (void)displayConversationForChatterEntry:(EntityChatterFeedEntry*)entry fromRect:(CGRect)rect inView:(UIView*)theView
{
    IgniteChatterConversationVC *conversationVC = [[IgniteChatterConversationVC alloc] initWithNibName:@"IgniteChatterConversationVC" bundle:nil];
    conversationVC.delegate = self;
    conversationVC.feedEntry = entry;
    conversationVC.contentSizeForViewInPopover = CGSizeMake(546, 620); // size of view in popover
    conversationVC.modalInPopover = NO; // Clicks outside the popover cause it to be dismissed

    self.vcPopover = [[UIPopoverController alloc] initWithContentViewController:conversationVC];
     
    CGPoint localPoint = [self.view convertPoint:rect.origin fromView:theView];
    CGRect localRect = CGRectMake(localPoint.x, localPoint.y, rect.size.width, rect.size.height);
    [vcPopover presentPopoverFromRect:localRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionRight animated:YES];
}

- (void)replyToChatterEntry:(EntityChatterFeedEntry*)entry
{
    [self presentChatterPostVCForFeedEntry:entry];
}

#pragma mark - IgniteChatterConversationVCDelegate methods
- (void)replyToConversationForFeedEntry:(EntityChatterFeedEntry*)entry
{
    [self.vcPopover dismissPopoverAnimated:YES]; // First close the conversation VC
    [self presentChatterPostVCForFeedEntry:entry];
}

#pragma mark - Trip buttons
- (void) shareTrip
{
	IgniteItinShareTripVC *shareTripVC = [[IgniteItinShareTripVC alloc] initWithNibName:@"IgniteItinShareTripVC" bundle:nil];
    shareTripVC.delegate = self;
    shareTripVC.itinLocator = [self.trip itinLocator];
    shareTripVC.contentSizeForViewInPopover = CGSizeMake(420, 321); // size of view in popover
    shareTripVC.modalInPopover = YES; // Clicks outside the popover are ignored
    
	self.vcPopover = [[UIPopoverController alloc] initWithContentViewController:shareTripVC];
    
	CGRect myRect = CGRectMake(342, 392, 375, 0);
    [self.vcPopover presentPopoverFromRect:myRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionDown animated:YES];    
}

- (void) expenseTrip
{
    ReportDetailViewController_iPad *newDetailViewController = [[ReportDetailViewController_iPad alloc] initWithNibName:@"ReportDetailViewController_iPad" bundle:nil];
    newDetailViewController.role = ROLE_EXPENSE_TRAVELER;
    newDetailViewController.iPadHome = nil;
    newDetailViewController.isReport = YES;
    
    // Dismiss the popover if it's present.
    if (self.vcPopover != nil)
    {
        [vcPopover dismissPopoverAnimated:YES];
        self.vcPopover = nil;
    }
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.rpt, @"REPORT", nil];

    [self.navigationController pushViewController:newDetailViewController animated:YES];
    
    [newDetailViewController loadReport:pBag];
    // Method not implemented so commenting it out
//    if([ExSystem isLandscape])
//        [newDetailViewController adjustForLandscape];
    
}

#pragma mark - Chatter buttons
- (IBAction)buttonChatterPostPressed:(id)sender
{
    [self presentChatterPostVCForFeedEntry:nil];
}

- (IBAction)buttonChatterPostPrivatePressed:(id)sender;
{
	IgniteChatterPostPrivateVC *postVC = [[IgniteChatterPostPrivateVC alloc] initWithNibName:@"IgniteChatterPostPrivateVC" bundle:nil];
    postVC.delegate = self;
    postVC.contentSizeForViewInPopover = CGSizeMake(420, 321); // size of view in popover
    postVC.modalInPopover = YES; // Clicks outside the popover are ignored
    
	self.vcPopover = [[UIPopoverController alloc] initWithContentViewController:postVC];
    
	CGRect myRect = CGRectMake(342, 392, 375, 0);
    [self.vcPopover presentPopoverFromRect:myRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionDown animated:YES];    
}

-(void) presentChatterPostVCForFeedEntry:(EntityChatterFeedEntry*)entry
{
	IgniteChatterPostVC *postVC = [[IgniteChatterPostVC alloc] initWithNibName:@"IgniteChatterPostVC" bundle:nil];
    postVC.delegate = self;
    postVC.feedEntry = entry;
    postVC.contentSizeForViewInPopover = CGSizeMake(480, 321); // size of view in popover
    postVC.modalInPopover = YES; // Clicks outside the popover are ignored
    
	self.vcPopover = [[UIPopoverController alloc] initWithContentViewController:postVC];
    
	CGRect myRect = CGRectMake(342, 392, 375, 0);
    [self.vcPopover presentPopoverFromRect:myRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionDown animated:YES];    
}

- (void) presentChatterPostVCForOpportunity:(EntitySalesOpportunity *)opp fromRect:(CGRect) frame
{
	IgniteChatterOppPostVC *postVC = [[IgniteChatterOppPostVC alloc] initWithNibName:@"IgniteChatterOppPostVC" bundle:nil];
    postVC.delegate = self;
    postVC.opportunity = opp;
    postVC.contentSizeForViewInPopover = CGSizeMake(540, 392); // size of view in popover
    postVC.modalInPopover = YES; // Clicks outside the popover are ignored
    
	self.vcPopover = [[UIPopoverController alloc] initWithContentViewController:postVC];
    CGRect myRect = CGRectMake(342, 392, 375, 0);
    [self.vcPopover presentPopoverFromRect:myRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionDown animated:YES];    
    
}

#pragma mark - IgniteChatterPostDelegate methods
-(void) didPostToChatter
{
    [self.dsSocialFeed updateChatterFeed];
}

-(void) closeChatterPostVC
{
    [self.vcPopover dismissPopoverAnimated:YES];
    self.vcPopover = nil;
}

#pragma mark - IgniteItinShareTripDelegate methods
-(void) didShareTrip
{
    [self.dsSocialFeed updateChatterFeed];
}

-(void) closeShareTripVC
{
    [self.vcPopover dismissPopoverAnimated:YES];
    self.vcPopover = nil;
}

#pragma mark - IgnitePopoverModalDelegate methods
- (void) dismissPopoverModal
{
    [self.vcPopover dismissPopoverAnimated:YES];
    self.vcPopover = nil;
}

#pragma mark - IgniteSegmentEditDelegate methods
- (void) segmentUpdated:(EntitySegment*) segment
{
    // reorder segments and reload table.
    [dsTrip segmentUpdated:segment];
    [self dismissPopoverModal];
}

#pragma mark - Social views
- (void)switchSocialView
{
    [UIView beginAnimations:@"ToggleSocialViews" context:nil];
    [UIView setAnimationDuration:0.5];
    
    // Make the animatable changes.    
    if (self.bTripMaximizerPressed)
    {
        [self.view bringSubviewToFront:self.tblSocialOpportunities];
        [self.view bringSubviewToFront:self.vwTHeaderSocialOpportunities];
        // Need to hide table behind, since table is transparent.
        [self.view sendSubviewToBack:self.tblSocialFeed];
        [self.view sendSubviewToBack:self.vwTHeaderSocialFeed];
        // change the button images
        
        [self.btnTripMaximizer setBackgroundImage:[ImageUtil getImageByName:@"button__blue_medium"] forState:UIControlStateNormal];
        [self.btnTripFeed setBackgroundImage:[ImageUtil getImageByName:@"button__drk_blue_medium"] forState:UIControlStateNormal];
    }
    else 
    {
        [self.view bringSubviewToFront:self.tblSocialFeed];
        [self.view bringSubviewToFront:self.vwTHeaderSocialFeed];
        
        [self.view sendSubviewToBack:self.tblSocialOpportunities];
        [self.view sendSubviewToBack:self.vwTHeaderSocialOpportunities];
        
        // change the button images
        [self.btnTripMaximizer setBackgroundImage:[ImageUtil getImageByName:@"button__drk_blue_medium"] forState:UIControlStateNormal];
        [self.btnTripFeed setBackgroundImage:[ImageUtil getImageByName:@"button__blue_medium"] forState:UIControlStateNormal];

    }

    // Commit the changes and perform the animation.    
    [UIView commitAnimations];
}

- (void)setupSocialViews
{
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];

    // #TODO# - hook up table with delegates
    self.dsSocialOpp = [[IgniteItinDetailSocialOppDS alloc] init];
    [dsSocialOpp setSeedData:[ad managedObjectContext] withTripKey:tripKey withTrip:trip withTable:self.tblSocialOpportunities withDelegate:self];

    self.dsSocialFeed = [[IgniteItinDetailSocialFeedDS alloc] init];
    [dsSocialFeed setSeedData:[ad managedObjectContext] withTripKey:tripKey withTrip:trip withTable:self.tblSocialFeed withDelegate:self];
    
    [self switchSocialView];
}

#pragma mark SocialOppDelegate methods
- (void) loadingSocialOppData
{
    self.waitViewSocial.hidden = NO;
    [self.view bringSubviewToFront:self.waitViewSocial];
}

- (void) socialOppDataReceived
{
    self.waitViewSocial.hidden = YES;
    [self.view sendSubviewToBack:self.waitViewSocial];
}

- (void) opportunitySelected:(EntitySalesOpportunity*) opp withFrame:(CGRect) frame
{
    CGRect rect = [self.view convertRect:frame fromView:self.tblSocialOpportunities];
    [self presentChatterPostVCForOpportunity:opp fromRect:rect];
}

- (BOOL) isOpportunityScheduled:(EntitySalesOpportunity*) opp
{
    return [self.dsTrip isOpportunityScheduled:opp.opportunityId];
}

- (void) addScheduledOpportunity:(EntitySalesOpportunity*) opp
{
    [self.dsTrip addScheduledOpportunity:opp.opportunityId];
}

- (void) removeScheduledOpportunity:(EntitySalesOpportunity *)opp
{
    [self.dsTrip.dictScheduledOppNames removeObject:opp.opportunityId];
}

#pragma mark Table action methods
- (void) meetingSelected:(EntitySegment*) segment  withCell:(UITableViewCell*) cell
{
    IgniteMeetingDetailVC *mtgVc = [[IgniteMeetingDetailVC alloc] initWithNibName:@"IgniteMeetingDetailVC" bundle:nil];
    mtgVc.contentSizeForViewInPopover = CGSizeMake(480, 600); // size of view in popover
    mtgVc.modalInPopover = YES; // Clicks outside the popover are ignored
    [mtgVc setSeedData:self withSegment:segment];
    
    self.vcPopover = [[UIPopoverController alloc] initWithContentViewController:mtgVc];
    
    CGRect myRect = [self.view convertRect:cell.frame fromView:self.tblTrip];
    [self.vcPopover presentPopoverFromRect:myRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionLeft animated:YES];    
}

- (void) diningSelected:(EntitySegment*) segment  withCell:(UITableViewCell*) cell
{
    IgniteDiningMapVC *mvc = [[IgniteDiningMapVC alloc] initWithNibName:@"IgniteDiningMapVC" bundle:nil];
    
    IgniteVendorAnnotation* locA = [[IgniteVendorAnnotation alloc] init];
    
    locA.name = @"Moscone Center";
    locA.longitude = @"-122.401303";
    locA.latitude = @"37.784401";
    locA.address = @"";
    [mvc setSeedData:self loc:locA seg:segment];
    
    UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:mvc];
    localNavigationController.modalPresentationStyle = UIModalPresentationPageSheet;
    [localNavigationController setToolbarHidden:NO];
    localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
    localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
    
//    [parentVC presentViewController:localNavigationController animated:YES completion:nil];

    [self presentViewController:localNavigationController animated:YES completion:nil];
}

- (void) segmentSelected:(EntitySegment*) segment withCell:(UITableViewCell*) cell
{
    // Switch to segment detail view
    if ([segment.type isEqualToString:SEG_TYPE_EVENT])
        [self meetingSelected:segment withCell:cell];
    else if ([segment.type isEqualToString:SEG_TYPE_DINING] && ![SegmentData isSegmentScheduled:segment])
        [self diningSelected:segment withCell:cell];
}

#pragma mark -
#pragma mark UIGestureRecognizer

- (void)handleDragDrop:(UIPanGestureRecognizer *)gestureRecognizer
{
    if (!self.bTripMaximizerPressed)
        return;
    
    switch ([gestureRecognizer state]) {
        case UIGestureRecognizerStateBegan:
            [self startDragging:gestureRecognizer];
            break;
        case UIGestureRecognizerStateChanged:
            [self doDrag:gestureRecognizer];
            break;
        case UIGestureRecognizerStateEnded:
        case UIGestureRecognizerStateCancelled:
        case UIGestureRecognizerStateFailed:
            [self stopDragging:gestureRecognizer];
            break;
        default:
            break;
    }
}

#pragma mark -
#pragma mark Helper methods for drag & drop
- (BOOL)initDraggedCellWithCell:(UITableViewCell*)cell AtPoint:(CGPoint)point WithIndexPath:(NSIndexPath*) indexPath
{
    UITableViewCell* draggedCell = [dsSocialOpp startDraggedCellWithCell:cell AtPoint:point WithIndexPath:indexPath];
    if (draggedCell != nil)
        [self.view addSubview:draggedCell];
    
    return draggedCell != nil;
}

- (void)startDragging:(UIPanGestureRecognizer *)gestureRecognizer
{
    CGPoint pointInSrc = [gestureRecognizer locationInView:self.tblSocialOpportunities];
    
    if([self.tblSocialOpportunities pointInside:pointInSrc withEvent:nil])
    {
        [self startDraggingFromSrcAtPoint:pointInSrc];
        //      dragFromSource = YES;
    }
}

- (void)startDraggingFromSrcAtPoint:(CGPoint)point
{
    NSIndexPath* indexPath = [tblSocialOpportunities indexPathForRowAtPoint:point];
    UITableViewCell* cell = [tblSocialOpportunities cellForRowAtIndexPath:indexPath];
    if(cell != nil)
    {
        CGPoint origin = cell.frame.origin;
        origin.x += tblSocialOpportunities.frame.origin.x;
        origin.y += tblSocialOpportunities.frame.origin.y;
        
        if ([self initDraggedCellWithCell:cell AtPoint:origin WithIndexPath:indexPath])
            cell.highlighted = NO;
        
    }
}

- (UITableViewCell*)currentDraggedCell
{
    return dsTrip.droppedCell != nil? (UITableViewCell*) dsTrip.droppedCell : (UITableViewCell*) dsSocialOpp.draggedCell;
}

- (BOOL)isDraggedCellInTripTable
{
    UITableViewCell*draggedCell = [self currentDraggedCell];
    if(draggedCell != nil)
    {
        CGPoint centerPtInTbl = [self.view convertPoint:draggedCell.center toView:self.tblTrip];
        return [self.tblTrip pointInside:centerPtInTbl withEvent:nil];
    }
    return NO;
}

- (void)doDrag:(UIPanGestureRecognizer *)gestureRecognizer
{
    UITableViewCell*draggedCell = [self currentDraggedCell];
    if(draggedCell != nil)
    {
        CGPoint translation = [gestureRecognizer translationInView:self.view];
        [draggedCell setCenter:CGPointMake([draggedCell center].x+translation.x, [draggedCell center].y + translation.y)];
        [gestureRecognizer setTranslation:CGPointZero inView:self.view];
        
        CGPoint centerPtInTbl = [self.view convertPoint:draggedCell.center toView:tblTrip];
        CGPoint topPt = CGPointMake(draggedCell.center.x, draggedCell.center.y - draggedCell.frame.size.height/2);
        CGPoint topPtInTbl = [self.view convertPoint:topPt toView:tblTrip];
        
        // Open a cell and morph the dragged cell, if center is in Trip table
        if([self isDraggedCellInTripTable])
        {
            NSIndexPath* indexPath = [tblTrip indexPathForRowAtPoint:centerPtInTbl];
            NSIndexPath* topIndexPath = [tblTrip indexPathForRowAtPoint:topPtInTbl];
            if(indexPath != nil)
            {
                // MOB-10459 Calculating the closest cell to move below.  If placed at the top of a section, use -1 as row index for calculations below.
                if (topIndexPath == nil)
                {
                    if (indexPath.section == 0 && indexPath.row == 0)
                        indexPath = [NSIndexPath indexPathForRow:-1 inSection:0];
                }
                else if (indexPath.section != topIndexPath.section)
                {
                    indexPath = [NSIndexPath indexPathForRow:-1 inSection:indexPath.section];
                }
                else if (indexPath.row != topIndexPath.row) 
                {
                    indexPath = topIndexPath;
                }
                //NSLog(@"Move to %@", indexPath);

                IgniteSegmentCell* existingDroppedCell = self.dsTrip.droppedCell;
                
                if (existingDroppedCell == nil)
                {
                    [self.dsTrip addMeeting:(EntitySalesOpportunity*) dsSocialOpp.draggedData afterIndexPath:(NSIndexPath*) indexPath atPoint:dsSocialOpp.draggedCell.frame.origin];
                    // Need to morph cells
                    [UIView beginAnimations:@"Fade" context:nil];
                    [UIView setAnimationDelegate:self];
                    [UIView setAnimationDuration:.5];
                    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
                    [self.view addSubview:self.dsTrip.droppedCell];
                    [self.view bringSubviewToFront:self.dsTrip.droppedCell];
                    [self.view sendSubviewToBack:self.dsSocialOpp.draggedCell];
                    [UIView commitAnimations];
                }
                else
                {
                    // move cell
                    NSIndexPath *newPath = [NSIndexPath indexPathForRow:indexPath.row+1 inSection:indexPath.section];
                    
                    NSIndexPath *droppedCellPath = [self.dsTrip getDroppedCellIndexPath];
                    BOOL cellMoved = !([droppedCellPath isEqual:newPath] || [droppedCellPath isEqual:indexPath]);
                    if (cellMoved)
                    {
                        //NSLog(@"Cell moved %@", indexPath);
                        [self.dsTrip moveMeetingAfterIndexPath:indexPath];
                    }

                }
            }
        }
    }
}

- (void)selectMeeting:(NSIndexPath*) path
{
    UITableViewCell* cell = [self.tblTrip cellForRowAtIndexPath:path];
    EntitySegment* seg = [self.dsTrip segmentFromIndexPath:path];
    [self meetingSelected:seg withCell:cell];
}

- (void)stopDraggingAnimation
{
    NSIndexPath* path = [self.dsTrip getDroppedCellIndexPath];
    
    [dsSocialOpp.draggedCell removeFromSuperview];
    dsSocialOpp.draggedCell = nil;
    dsSocialOpp.draggedData = nil;
    dsTrip.droppedData = nil;
    [dsTrip.droppedCell removeFromSuperview];
    dsTrip.droppedCell = nil;
    dsTrip.droppedData = nil;
    if (path != nil)
    {
        [self.tblTrip reloadRowsAtIndexPaths:[NSArray arrayWithObject:path] withRowAnimation:UITableViewRowAnimationFade];
        
        [self performSelector:@selector(selectMeeting:) withObject:path afterDelay:0.5f];
    }
}

- (void)stopDragging:(UIPanGestureRecognizer *)gestureRecognizer
{
    if([gestureRecognizer state] == UIGestureRecognizerStateEnded
       && [self isDraggedCellInTripTable])
    {
        NSIndexPath* droppedCellIndex = [self.dsTrip getDroppedCellIndexPath];
        UITableViewCell* cell = [self.tblTrip cellForRowAtIndexPath:droppedCellIndex];
        if (cell != nil)
        {
            [UIView beginAnimations:@"Fade" context:nil];
            [UIView setAnimationDelegate:self];
            [UIView setAnimationDuration:.5];
            [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
            CGPoint pt = [self.view convertPoint:cell.frame.origin fromView:self.tblTrip];
            dsTrip.droppedCell.frame = CGRectMake(pt.x, pt.y, cell.frame.size.width, cell.frame.size.height);
            [UIView setAnimationDidStopSelector:@selector(stopDraggingAnimation)];

            [UIView commitAnimations];

            return;
        }
    }

    [self.dsTrip removeDroppedData];

    [self.dsSocialOpp cancelDrop];
}

@end
