//
//  ManageViolationsVC.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 19/02/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ManageViolationsVC.h"
#import "TextViewController.h"
#import "ViolationsViewController.h"
#import "EntityHotelRoom+ViolationDetails.h"
#import "EntityAirFilterSummary+ViolationDetails.h"

@interface ManageViolationsVC ()

@property (nonatomic, strong) NSString *travelPointsToUse;

@property (weak, nonatomic) IBOutlet UILabel *lblHeader;
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (nonatomic) int selectionChoice;

@end

@implementation ManageViolationsVC

- (instancetype)initWithTitle:(NSString*)title
{
    ManageViolationsVC *vc = [[UIStoryboard storyboardWithName:@"TravelPointsBookingFlow" bundle:nil] instantiateViewControllerWithIdentifier:@"ManageViolationsVC"];
    vc.navigationItem.title = title;
    return vc;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    self.lblHeader.text = [@"P2B_AIR_BOOKING_MANAGE_VIOLATIONS_HEADER" localize];
    UIBarButtonItem *continueButton = [[UIBarButtonItem alloc] initWithTitle:[@"Continue" localize] style:UIBarButtonItemStyleBordered target:self action:@selector(continueClicked:)];
    self.navigationItem.rightBarButtonItem = continueButton;
}

- (void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.lblHeader sizeToFit];
    CGFloat tableHeightOffset = self.lblHeader.frame.origin.y + self.lblHeader.frame.size.height + 8 - self.tableView.frame.origin.y;
    self.tableView.frame = CGRectMake(self.tableView.frame.origin.x, self.lblHeader.frame.origin.y + self.lblHeader.frame.size.height + 8, self.tableView.frame.size.width, self.tableView.frame.size.height - tableHeightOffset);
}

- (void)continueClicked:(id)sender
{
    [self logFlurryEventsForTravelPoints];
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@(self.selectionChoice == 0), @"USE_TRAVEL_POINTS", @"YES", @"DONTPUSHVIEW", @"YES", @"POPTOVIEW", @"YES", @"SHORT_CIRCUIT", nil];
    
    
    int vcCount = [self.navigationController.viewControllers count];
    
    UIViewController *vc = (self.navigationController.viewControllers)[vcCount - 2];
    MobileViewController *mvc = (MobileViewController *)vc;
    Msg *msg = [[Msg alloc] init];
    msg.idKey = @"SHORT_CIRCUIT";
    msg.parameterBag = pBag;
    [mvc respondToFoundData:msg];
    
    if(self.selectionChoice == 0) //Use Travel Points
    {
        [self.navigationController popViewControllerAnimated:YES];
    }
    else // Manager approval
    {
        ViolationsViewController *vc = [[ViolationsViewController alloc] initWithTitle:[@"Violations" localize]];
        vc.selectedFareOption = self.hotelRoom ? self.hotelRoom : self.airSummary;
        vc.violationReasons = self.violationReasons;
        vc.violationReasonLabels = self.violationReasonLabels;
        [self.navigationController pushViewController:vc animated:YES];
    }
}

- (void)logFlurryEventsForTravelPoints
{
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
    dict[@"Type"] = self.airSummary ? @"Air" : @"Hotel";
    if (self.travelPointsInBank)
        dict[@"Travel Points In Bank"] = self.travelPointsInBank;
    if (self.travelPointsToUse)
        dict[@"Travel Points To Use"] = self.travelPointsToUse;
    if ((self.hotelRoom && self.hotelRoom.isUsingPointsAgainstViolations == nil) || (self.airSummary && self.airSummary.isUsingPointsAgainstViolations == nil))
        dict[@"Use Travel Points Selected"] = @"First Selection";
    else
        dict[@"Use Travel Points Selected"] = ([self.hotelRoom.isUsingPointsAgainstViolations boolValue] || [self.airSummary.isUsingPointsAgainstViolations boolValue]) ? @"Yes" : @"No";
    dict[@"Changed options To"] = self.selectionChoice == 0 ? @"Use Points" : @"Manager Approval";
    [Flurry logEvent:@"Price-to-Beat: Manage Violations Viewed" withParameters:dict];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return section + 1;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    if (section == 1)
        return [@"How would you like to book?" localize];
    return nil;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return section == 0 ? 1 : 30;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *cellIdentifier = @"PriceToBeatCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:cellIdentifier];
        cell.textLabel.minimumScaleFactor = 0.6;
        cell.textLabel.adjustsFontSizeToFitWidth = YES;
    }
    
    if (indexPath.section == 0) {
        cell.textLabel.text = [NSString stringWithFormat:[@"You have %@ Travel Points in your bank." localize],self.travelPointsInBank ?: @"--"];
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    }
    else {
        if (indexPath.row == 0) {
            cell.textLabel.text = [NSString stringWithFormat:[@"Use %@ Travel Points" localize],self.travelPointsToUse];
        }
        else {
            cell.textLabel.text = [NSString stringWithFormat:[@"Manager approval" localize],self.travelPointsToUse];
        }
        cell.accessoryType = self.selectionChoice == indexPath.row ? UITableViewCellAccessoryCheckmark : UITableViewCellAccessoryNone;
    }
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (indexPath.section == 0) {
        TextViewController *tvc = [[TextViewController alloc] initWithTitle:[@"Using Points" localize]];
        tvc.text = self.airSummary ? [@"P2B_AIR_BOOKING_USING_POINTS_HEADER" localize] : [@"P2B_HOTEL_BOOKING_USING_POINTS_HEADER" localize];
        [self.navigationController pushViewController:tvc animated:YES];
    }
    else if (self.selectionChoice != indexPath.row) {
        self.selectionChoice = (self.selectionChoice + 1) % 2;
        [self.tableView reloadData];
    }
}

- (NSString *)tableView:(UITableView *)tableView titleForFooterInSection:(NSInteger)section
{
    if (section == 1 && [self.violationTexts count]) {
        NSMutableArray *violationTextsWithBulletPoints = [[NSMutableArray alloc] initWithCapacity:[self.violationTexts count]];
        [violationTextsWithBulletPoints addObject:[@"Violations" localize]];
        for (NSString *text in self.violationTexts) {
            [violationTextsWithBulletPoints addObject:[@"\u2022 " stringByAppendingString:text]];
        }
        return [violationTextsWithBulletPoints componentsJoinedByString:@"\n"];
    }
    return nil;
}

- (void)setHotelRoom:(EntityHotelRoom *)hotelRoom
{
    _hotelRoom = hotelRoom;
    self.travelPointsToUse = [@(abs([hotelRoom.travelPoints intValue])) stringValue];
    if (hotelRoom.isUsingPointsAgainstViolations && ![hotelRoom.isUsingPointsAgainstViolations boolValue])
        self.selectionChoice = 1;
}

- (void)setAirSummary:(EntityAirFilterSummary *)airSummary
{
    _airSummary = airSummary;
    self.travelPointsToUse = [@(abs([airSummary.travelPoints intValue])) stringValue];
    if (airSummary.isUsingPointsAgainstViolations && ![airSummary.isUsingPointsAgainstViolations boolValue])
        self.selectionChoice = 1;
}

@end
