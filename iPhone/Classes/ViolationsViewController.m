//
//  ViolationsViewController.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 27/02/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ViolationsViewController.h"
#import "HotelBookingCell.h"
#import "SystemConfig.h"
#import "ViolationDetailsVC.h"
#import "EntityHotelViolation.h"
#import "HotelOptionsViewController.h"
#import "HotelTextEditorViewController.h"
#import "ViolationReason.h"
#import "UserConfig.h"

@interface ViolationsViewController ()
@property (weak, nonatomic) IBOutlet UILabel *lblHeader;
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (nonatomic, strong) NSArray *rowsInViolationSection;
@end

#define kRowManageViolations @"RowManageViolations"
#define kRowUsingPointsViolations @"RowUsingPointsViolations"
#define kRowViolationsText @"RowViolationsText"
#define kRowViolationReason @"RowViolationReason"
#define kRowViolationJustification @"RowViolationJustification"

@implementation ViolationsViewController

- (instancetype)initWithTitle:(NSString*)title
{
    ViolationsViewController *vc = [[UIStoryboard storyboardWithName:@"TravelPointsBookingFlow" bundle:nil] instantiateViewControllerWithIdentifier:@"ViolationsViewController"];
    vc.navigationItem.title = title;
    return vc;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    self.rowsInViolationSection = @[kRowViolationsText, kRowViolationReason, kRowViolationJustification];// 1. violations text, 2. reason, 3. justification
    self.lblHeader.text = [[self.selectedFareOption getFareType] isEqualToString:@"H"] ? [@"HOTEL_BOOKING_VIEW_MISSING_VIOLATION_REASON" localize] : [@"AIR_WARNING_REASON" localize];
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

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)continueClicked:(id)sender
{
    if ([self allRequiredFieldsHandled]) {
        NSInteger vcCount = [self.navigationController.viewControllers count];
        
        UIViewController *vc = (self.navigationController.viewControllers)[vcCount - 3];
        MobileViewController *mvc = (MobileViewController *)vc;
        Msg *msg = [[Msg alloc] init];
        msg.idKey = @"SHORT_CIRCUIT";
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"DONTPUSHVIEW", @"YES", @"POPTOVIEW", @"YES", @"SHORT_CIRCUIT", nil];
        msg.parameterBag = pBag;
        [mvc respondToFoundData:msg];
        [self.navigationController popToViewController:mvc animated:YES];
    }
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.rowsInViolationSection count];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 30;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return [@"Violations" localize];
}

- (void) respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:@"SHORT_CIRCUIT"] && msg.parameterBag != nil)
	{
		if ((msg.parameterBag)[@"OPTION_TYPE_ID"] != nil)
		{
			// We've returned from the HotelOptionsViewController
			NSNumber* selectedRowIndexNumber = (NSNumber*)(msg.parameterBag)[@"SELECTED_ROW_INDEX"];
			NSUInteger selectedRowIndex = [selectedRowIndexNumber intValue];
			ViolationReason *reason = self.violationReasons[selectedRowIndex];
            [self.selectedFareOption setViolationReasonUserSelection:reason];
		}
		else if ((msg.parameterBag)[@"TEXT"] != nil)
		{
            self.selectedFareOption.violationJustification = (NSString*)(msg.parameterBag)[@"TEXT"];
		}
		
		[self.tableView reloadData];
	}
}

- (BOOL)allRequiredFieldsHandled
{
    NSString *msg;
    if (![[self.selectedFareOption getViolationReasonDescription] length])
    {
        if (![self.selectedFareOption.violationJustification length] &&  [SystemConfig getSingleton].ruleViolationExplanationRequired)
        {
            msg = [[self.selectedFareOption getFareType] isEqualToString:@"H"] ? [@"HOTEL_BOOKING_VIEW_MISSING_VIOLATION_REASON_AND_JUSTIFICATION" localize] : [@"AIR_WARNING_REASON_JUSTIFICATION" localize];
        }
        else
        {
            msg = [[self.selectedFareOption getFareType] isEqualToString:@"H"] ? [@"HOTEL_BOOKING_VIEW_MISSING_VIOLATION_REASON" localize] : [@"AIR_WARNING_REASON" localize];
        }
    }
    else if([SystemConfig getSingleton].ruleViolationExplanationRequired)
    {
        msg = [[self.selectedFareOption getFareType] isEqualToString:@"H"] ? [@"HOTEL_BOOKING_VIEW_MISSING_VIOLATION_JUSTIFICATION" localize] : [@"AIR_WARNING_JUSTIFICATION" localize];
    }
    // show alert if there is a message
    if(msg!=nil)
    {
        MobileAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"HOTEL_BOOKING_VIEW_MISSING_VIOLATION_INFO_TITLE"]
                                  message:msg
                                  delegate:nil
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                  otherButtonTitles:nil];
        [alert show];
    }
    return msg == nil;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    HotelBookingCell *cell = (HotelBookingCell*)[tableView dequeueReusableCellWithIdentifier:@"HotelBookingSingleCell"];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"HotelBookingSingleCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[HotelBookingCell class]])
                cell = (HotelBookingCell *)oneObject;
    }
    cell.lblValue.textColor = [UIColor blackColor];
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    
    if ([kRowViolationsText isEqualToString:self.rowsInViolationSection[indexPath.row]])
    {
        NSString *label = [@"Violation" localize];//[self hasDisallowedViolations] ? [@"Violation" localize] : [@"Travel Policy" localize];
        NSString *value = [self.selectedFareOption violationTextsNewLineSeparated];// self.violationTexts ? [self.violationTexts componentsJoinedByString:@"\n"] : @"";
        
        cell.lblLabel.text = label;
        cell.lblValue.text = value;
    }
    else if ([kRowViolationReason isEqualToString:self.rowsInViolationSection[indexPath.row]])
    {
        NSString *reason = [self.selectedFareOption getViolationReasonDescription];
        
        NSString *label = [Localizer getLocalizedText:@"Violation Reason"];
        NSString *value = (reason != nil ? reason : [Localizer getLocalizedText:@"Please specify"]);
        
        cell.lblLabel.text = label;
        cell.lblValue.text = value;
        
        if (reason == nil)
            cell.lblValue.textColor = [UIColor redColor];
    }
    else if ([kRowViolationJustification isEqualToString:self.rowsInViolationSection[indexPath.row]])
    {
        BOOL customLabelSpecified = [[UserConfig getSingleton].customTravelText[@"HotelRulesViolationText"] length] > 0;
        NSString *justification  = self.selectedFareOption.violationJustification;
        NSString *label = customLabelSpecified ? [UserConfig getSingleton].customTravelText[@"HotelRulesViolationText"] : [Localizer getLocalizedText:@"Violation Justification"];
        NSString *value = ([justification length] ? justification : (customLabelSpecified ? @"" : [Localizer getLocalizedText:@"Please specify"]));
        
        cell.lblLabel.text = label;
        cell.lblValue.text = value;
        
        // //MOB-10484
        if (![justification length] && [SystemConfig getSingleton].ruleViolationExplanationRequired)
            cell.lblValue.textColor = [UIColor redColor];
    }
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
	NSUInteger row = [indexPath row];
	if([kRowViolationsText isEqualToString:self.rowsInViolationSection[row]])
    {
        ViolationDetailsVC *vc = [[ViolationDetailsVC alloc] initWithNibName:@"ViolationDetailsVC" bundle:nil];
        vc.violationText = [self.selectedFareOption violationTextsNewLineSeparated];// self.violationTexts ? [self.violationTexts componentsJoinedByString:@"\n"] : @"";
        [self.navigationController pushViewController:vc animated:YES];
    }
    else if ([kRowViolationReason isEqualToString:self.rowsInViolationSection[row]])
    {
        NSString *optionsViewTitle = [Localizer getLocalizedText:@"Select Reason"];
        NSString *optionType = @"VIOLATION_REASON";
        NSArray *labels = self.violationReasonLabels;

        int currentReasonIndex = -1;//[self getIndexForViolationReasonCode:hotelSearch.selectedHotel.detail.selectedRoom.violationReasonCode];
        NSString *currentCode = [self.selectedFareOption getViolationReasonCode];// hotelBookingRoom.relHotelViolationCurrent.code;
        
        if(currentCode != nil)
        {
            for(int i = 0; i < [self.violationReasons count]; i++)
            {
                ViolationReason *reason = self.violationReasons[i];
                
                if([currentCode isEqualToString:reason.code])
                {
                    currentReasonIndex = i;
                    break;
                }
            }
        }

        NSNumber *preferredFontSize = @13.0f;
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"HOTEL_BOOKING", @"FROM_VIEW", optionType, @"OPTION_TYPE_ID", optionsViewTitle, @"TITLE", labels, @"LABELS", preferredFontSize, @"PREFERRED_FONT_SIZE", @"YES", @"SHORT_CIRCUIT", nil];

        if (currentReasonIndex >= 0)
            pBag[@"SELECTED_ROW_INDEX"] = @(currentReasonIndex);
//
////        if([UIDevice isPad])
////        {
            HotelOptionsViewController *nextController = [[HotelOptionsViewController alloc] initWithNibName:@"HotelOptionsViewController" bundle:nil];
            Msg *msg = [[Msg alloc] init];
            msg.parameterBag = pBag;
            msg.idKey = @"SHORT_CIRCUIT";
            [nextController respondToFoundData:msg];
            [self.navigationController pushViewController:nextController animated:YES];
////        }
////        else
////            [ConcurMobileAppDelegate switchToView:HOTEL_OPTIONS viewFrom:[self getViewIDKey] ParameterBag:pBag];
    }
    else if ([kRowViolationJustification isEqualToString:self.rowsInViolationSection[row]])
    {
        BOOL customLabelSpecified = [[UserConfig getSingleton].customTravelText[@"HotelRulesViolationText"] length] > 0;
        NSString *customTitle = customLabelSpecified ? [UserConfig getSingleton].customTravelText[@"HotelRulesViolationText"] : [Localizer getLocalizedText:@"HOTEL_BOOKING_VIOLATION_JUSTIFICATION_TITLE"];
        NSString *placeholder = customLabelSpecified ? [UserConfig getSingleton].customTravelText[@"HotelRulesViolationText"] : [Localizer getLocalizedText:@"HOTEL_BOOKNIG_VIOLATION_JUSTIFICATION_PLACEHOLDER_TEXT"];
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"HOTEL_BOOKING", @"FROM_VIEW",placeholder, @"PLACEHOLDER", customTitle, @"TITLE", @"YES", @"SHORT_CIRCUIT", nil];
        
        NSString *justification = self.selectedFareOption.violationJustification;
        if (justification != nil)
            pBag[@"TEXT"] = justification;
        
//        if([UIDevice isPad])
//        {
            HotelTextEditorViewController *nextController = [[HotelTextEditorViewController alloc] initWithNibName:@"HotelTextEditorViewController" bundle:nil];
            Msg *msg = [[Msg alloc] init];
            msg.parameterBag = pBag;
            msg.idKey = @"SHORT_CIRCUIT";
            nextController.title = customTitle;
            nextController.parentVC = self;
            [nextController respondToFoundData:msg];
            [self.navigationController pushViewController:nextController animated:YES];
//        }
//        else
//            [ConcurMobileAppDelegate switchToView:HOTEL_TEXT_EDITOR viewFrom:[self getViewIDKey] ParameterBag:pBag];
    }
}


//-(NSString*)getViolationJustification
//{
//	return self.selectedFareOption.violationJustification;
//}
//
//-(NSString*)getViolationReason
//{
////	if (self.hotelRoom)
////        return self.hotelRoom.relHotelViolationCurrent.message;
////    else {
////        NSString *reason = nil;
////        
////        if (violationReasonCode != nil)
////        {
////            
////            TravelViolationReasons *travelViolationReasons = [TravelViolationReasons getSingleton];
////            if (travelViolationReasons != nil)
////            {
////                ViolationReason *violationReason = (travelViolationReasons.violationReasons)[violationReasonCode];
////                reason = violationReason.description;
////            }
////        }
////        return reason;
////    }
//}

@end
