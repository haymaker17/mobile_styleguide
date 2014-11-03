//
//  IgniteMeetingDetailVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteMeetingDetailVC.h"
#import "IgniteOpportunityCell.h" // For top row - contact
#import "SalesForceUserManager.h"
#import "BoolEditCell.h"
#import "SegmentData.h"
#import "SalesForceCOLAManager.h"
#import "IgniteMeetingTimeCell.h"
#import "DateTimeFormatter.h"
#import "TripManager.h"
#import "ImageUtil.h"

#define SECTION_MTG_CONTACT   @"CONTACT"
#define SECTION_MTG_SUBJECT   @"SUBJECT"
#define SECTION_MTG_TIME      @"TIME"
#define SECTION_MTG_INVITEES  @"INVITEES"
#define SECTION_MTG_NOTES     @"NOTES"
#define SECTION_MTG_INVITE    @"INVITE"

@interface IgniteMeetingDetailVC (Private)
- (void) configureNavBar;
- (void) buttonCancelPressed;
- (void) buttonDonePressed;
- (void) initSections;

- (UITableViewCell*) configureContactCell:(UITableView*)tableView;
- (UITableViewCell*) configureTimeCell:(UITableView*)tableView;
- (UITableViewCell*) configureNotesCell:(UITableView*)tableView;
- (UITableViewCell*) configureSubjectCell:(UITableView*)tableView withIndex:(NSInteger)row;
- (UITableViewCell*) configureInviteesCell:(UITableView*)tableView;
- (UITableViewCell*) configureInviteCell:(UITableView*)tableView;

@end

@implementation IgniteMeetingDetailVC

@synthesize navBar, tableList, segment, opportunity, sections;
@synthesize delegate = _delegate;

- (void)setSeedData:(id<IgnitePopoverModalDelegate, IgniteSegmentEditDelegate>)del withSegment:(EntitySegment *)seg
{
    self.segment = seg;
    self.delegate = del;
    
    // get opportunity associated with the meeting segment
    NSString *oppId = [SegmentData getAttribute:@"OpportunityId" FromMeeting:seg];
    if ([oppId length])
    {
        self.opportunity = [[SalesForceCOLAManager sharedInstance] fetchOpportunityByOppId:oppId];
    }
    
    [self initSections];
}

- (void)initSections
{
    self.sections = [NSMutableArray arrayWithObjects:SECTION_MTG_CONTACT, SECTION_MTG_SUBJECT, SECTION_MTG_TIME, SECTION_MTG_INVITEES, SECTION_MTG_NOTES, SECTION_MTG_INVITE, nil];
}


- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    [self configureNavBar];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


-(void) configureNavBar
{
    // Show custom nav bar
    UIImage *imgNavBar = [ImageUtil getImageByName:@"bar_title_landscape"];
    navBar.tintColor = [UIColor clearColor];
    [navBar setBackgroundImage:imgNavBar forBarMetrics:UIBarMetricsDefault];
    [navBar setBackgroundImage:imgNavBar forBarMetrics:UIBarMetricsLandscapePhone];

    UINavigationItem *navItem = [UINavigationItem alloc];
    
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 360, 44)];
    label.backgroundColor = [UIColor clearColor];
    label.font = [UIFont boldSystemFontOfSize:16.0];
    label.shadowColor = [UIColor colorWithWhite:0.0 alpha:0.5];
    label.textAlignment = NSTextAlignmentCenter;
    label.textColor =[UIColor whiteColor];
    label.text = @"Event Details";		
    navItem.titleView = label;
    
    UIBarButtonItem* btnCancel = [ExSystem makeColoredButton:@"IGNITE_BLUE" W:74 H:31 Text:(NSString *)@"Cancel" SelectorString:@"buttonCancelPressed" MobileVC:self];
    UIBarButtonItem* btnDone = [ExSystem makeColoredButton:@"IGNITE_BLUE" W:74 H:31 Text:(NSString *)@"Done" SelectorString:@"buttonDonePressed" MobileVC:self];
    
	[navItem setLeftBarButtonItem:btnCancel animated:NO];
	[navItem setRightBarButtonItem:btnDone animated:NO];
    
    [self.navBar pushNavigationItem:navItem animated:YES];
}

#pragma mark - Button handlers
- (void)buttonCancelPressed
{
    // Need to inform parent to close the popover vc
    [self.delegate dismissPopoverModal];
}

- (void)buttonDonePressed
{
    if (![SegmentData isSegmentScheduled:self.segment])
    {
        self.segment.segmentName = [NSString stringWithFormat:@"Meeting for %@", self.opportunity.opportunityName];
        self.segment.status = @""; // set as scheduled
        
        [[TripManager sharedInstance] saveIt:self.segment];
        
        [self.delegate segmentUpdated:segment];    
    }
    else {
        [self.delegate dismissPopoverModal];
    }
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    
    return [self.sections count];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSString *key = [self.sections objectAtIndex:section]; 
    if ([key isEqualToString: SECTION_MTG_SUBJECT])
        return 2;
    return 1;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
    NSString *key = [self.sections objectAtIndex:indexPath.section]; 
    if ([key isEqualToString: SECTION_MTG_CONTACT])
        return [self configureContactCell:tableView];
    else if ([key isEqualToString:SECTION_MTG_TIME])
        return [self configureTimeCell:tableView];
    else if ([key isEqualToString:SECTION_MTG_NOTES])
        return [self configureNotesCell:tableView];
    else if ([key isEqualToString:SECTION_MTG_SUBJECT])
        return [self configureSubjectCell:tableView withIndex:indexPath.row];
    else if ([key isEqualToString: SECTION_MTG_INVITEES])
        return [self configureInviteesCell:tableView];
    else    //SECTION_MTG_INVITE
        return [self configureInviteCell:tableView];
}

- (UITableViewCell*) configureContactCell:(UITableView*)tableView
{
    // Reuse opportunity cell
    
    IgniteOpportunityCell *cell = (IgniteOpportunityCell *)[tableList dequeueReusableCellWithIdentifier: @"IgniteOpportunityCell"];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"IgniteOpportunityCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[IgniteOpportunityCell class]])
                cell = (IgniteOpportunityCell *)oneObject;
    }
    cell.lblName.text = opportunity.contactName;
    cell.lblAccountLocation.text= [NSString stringWithFormat:@"%@, %@", opportunity.accountName, opportunity.accountCity];
    cell.lblName.textColor = [UIColor blackColor];
    cell.lblAccountLocation.textColor = [UIColor blackColor];
    cell.backgroundColor = [UIColor whiteColor];
    cell.lblAmount.text = @"";
    cell.lblPhone.text = @"";
    
    UIImage *img = [UIImage imageNamed: @"placeholder_headshot"];
    [cell.ivProfile setImage: img];
    
    if ([opportunity.contactId length] && [opportunity.contactId hasPrefix:@"/services/data"])
    {//Image url needs "/body" to get the binary image data.
        NSString* imageUrl = [NSString stringWithFormat:@"https://na9.salesforce.com%@/body", opportunity.contactId];
        NSString *imageCacheName = [NSString stringWithFormat:@"Photo_Small_Contact_%@", opportunity.contactName];
        [[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:imageUrl RespondToImage:img IV:cell.ivProfile MVC:nil ImageCacheName:imageCacheName OAuth2AccessToken:[[SalesForceUserManager sharedInstance] getAccessToken]];
    }

    [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
    return cell;
    
}

- (UITableViewCell*) configureTimeCell:(UITableView*)tableView
{
    IgniteMeetingTimeCell *cell = (IgniteMeetingTimeCell *)[tableList dequeueReusableCellWithIdentifier: @"IgniteMeetingTimeCell"];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"IgniteMeetingTimeCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[IgniteMeetingTimeCell class]])
                cell = (IgniteMeetingTimeCell *)oneObject;
    }
    // Long - "EEE, MMM dd h:mm aa", short - "h:mm aa"
    cell.lblStarts.text = @"Start";
    
    NSString *startDateStr = self.segment.relStartLocation.dateLocal;
    NSDate* startDate = [DateTimeFormatter getNSDate:startDateStr Format:@"yyyy-MM-dd'T'HH:mm:ss"  TimeZone:[NSTimeZone timeZoneWithName:@"GMT"]];
    NSString *fmtStartDateStr = [DateTimeFormatter formatDate:startDate Format:@"EEE, MMM dd h:mm aa"  TimeZone:[NSTimeZone timeZoneWithName:@"GMT"]];
    cell.lblStartTime.text = fmtStartDateStr; // @"Tue, Sep 18 2:30 PM";
    
    cell.lblEnds.text = @"Ends";

    NSString *endDateStr = self.segment.relEndLocation.dateLocal;
    NSDate* endDate = [DateTimeFormatter getNSDate:endDateStr Format:@"yyyy-MM-dd'T'HH:mm:ss"  TimeZone:[NSTimeZone timeZoneWithName:@"GMT"]];
    NSString *fmtEndDateStr = [DateTimeFormatter formatDate:endDate Format:@"h:mm aa"  TimeZone:[NSTimeZone timeZoneWithName:@"GMT"]];
    cell.lblEndTime.text = fmtEndDateStr; //@"3:30 AM";
    
    return cell;
}

- (UITableViewCell*) configureNotesCell:(UITableView*)tableView
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Notes"];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:@"Notes"];
    }
    
    cell.textLabel.text = @"Notes";
    return cell;
}

- (UITableViewCell*) configureSubjectCell:(UITableView*)tableView withIndex:(NSInteger)row
{    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Subject"];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:@"Subject"];
    }
    
    if (row == 0)
    {
        cell.textLabel.text = [NSString stringWithFormat:@"Meeting for %@", self.opportunity.opportunityName]; //@"Unscheduled Meeting";
    }
    else {
        cell.textLabel.text = self.opportunity.accountName;
    }
    
    cell.textLabel.font = [UIFont fontWithName:@"HelveticaNeue" size:17.0f];

    return cell;    
}

- (UITableViewCell*) configureInviteesCell:(UITableView*)tableView
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Invitees"];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:@"Invitees"];
    }
    
    cell.textLabel.text = @"Invitees";
    cell.detailTextLabel.text = @"1 Person";    
    cell.detailTextLabel.textColor = [UIColor blackColor];
    return cell;
}

- (UITableViewCell*) configureInviteCell:(UITableView*)tableView
{
    // Reuse bool edit cell
    BoolEditCell *cell = (BoolEditCell *)[tableList dequeueReusableCellWithIdentifier: @"BoolEditCell"];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"BoolEditCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[BoolEditCell class]])
                cell = (BoolEditCell *)oneObject;
    }
    cell.label.text = @"Email Meeting Invite";
    cell.label.textColor = [UIColor blackColor];
    cell.label.font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:17.0f];

    CGRect rect = cell.switchCtrl.frame;
    rect.origin.y = 8;
    cell.switchCtrl.frame = rect;

    CGRect rect2 = cell.label.frame;
    rect2.origin.y = 3;
    cell.label.frame = rect2;
    return cell;

}

#pragma mark -
#pragma mark Table Delegate Methods 
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    // TODO
}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *key = [self.sections objectAtIndex:indexPath.section]; 
    if ([key isEqualToString: SECTION_MTG_CONTACT])
        return 84;
    else if ([key isEqualToString:SECTION_MTG_TIME] || [key isEqualToString:SECTION_MTG_NOTES])
        return 66;
    else//SECTION_MTG_SUBJECT, , SECTION_MTG_INVITEES, SECTION_MTG_NOTES, SECTION_MTG_INVITE
        return 44;	
}


@end
