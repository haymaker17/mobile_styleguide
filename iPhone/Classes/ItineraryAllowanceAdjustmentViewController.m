//
//  ItineraryAllowanceAdjustmentViewController.m
//  ConcurMobile
//
//  Created by Wes Barton on 2/20/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ItineraryAllowanceAdjustmentViewController.h"
#import "ItineraryConfig.h"
#import "Itinerary.h"
#import "CXClient.h"
#import "AllowanceControl.h"
#import "FixedAllowance.h"
#import "FixedAllowanceCell.h"
#import "WaitViewController.h"
#import "AllowanceMealProvidedTableViewController.h"
#import "MealsAmountFormFieldData.h"
#import "AllowanceAmountViewController.h"
#import "CXRequest.h"

@interface ItineraryAllowanceAdjustmentViewController ()

@property NSMutableArray *allowanceRows;
@property AllowanceControl *allowanceControl;

@end

@implementation ItineraryAllowanceAdjustmentViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    // NSLog(@"ItineraryAllowanceAdjustmentViewController.viewddidload");

    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;

    // NSLog(@"self.itinerary.itinKey = %@", self.itinerary.itinKey);
    // NSLog(@"self.itinerary.itinName = %@", self.itinerary.itinName);

    NSLocale *locale = [NSLocale currentLocale];
    self.dateFormatterMedium = [NSDateFormatter dateFormatterWithFormat:nil timeZoneWithAbbreviation:@"GMT" locale:locale];
    [self.dateFormatterMedium setDateStyle:NSDateFormatterMediumStyle];

    // Localize
    self.navBar.title = [Localizer getLocalizedText:@"Allowance Adjustments"];

    if([Itinerary isApproving:self.role] || self.hideGenerateExpenseButton)
    {
        // Hide the toolbar because the approver can't update the allowances
        self.generateExpensesButton.enabled = NO;
        [self.navigationController.toolbar setHidden:YES];
    }
    else
    {
        self.generateExpensesButton.title = [Localizer getLocalizedText:@"Generate Expenses"];
    }

    self.BackButton.title = [Localizer getLocalizedText:@"Back"];

    if(self.hasCloseButton)
    {
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:self action:@selector(actionBack:)];
        self.navigationItem.leftBarButtonItem = btnClose;
    }

    self.allowanceRows = [[NSMutableArray alloc]init];

    // Show the wait screen block
    [WaitViewController showWithText:@"Allowances" animated:YES];

    [self loadAllowanceData:self.rptKey taDayKey:self.taDayKey];


}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    return [self.allowanceRows count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    FixedAllowance * allowance = [self.allowanceRows objectAtIndex:section];
    if(allowance.isCollapsed)
    {
        return 1;
    }

    // Return the number of rows in the section.
    return numberOfRowsInSection;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    FixedAllowance *allowance = [self.allowanceRows objectAtIndex:indexPath.section];
    FixedAllowanceCell *cell = nil;

    if(indexPath.row == dayHeaderCellRowIndex)           // Header
    {
        cell = [tableView dequeueReusableCellWithIdentifier:@"DayHeader" forIndexPath:indexPath];

        //Format better
        if(allowance.allowanceDate != nil)
        {
            cell.date.text = [self.dateFormatterMedium stringFromDate:allowance.allowanceDate];
        }
        else
        {
            cell.date.text = allowance.allowanceDateString;
        }

        cell.tag = indexPath.section;

        cell.collapsedSwitch.on = allowance.isCollapsed;
        cell.collapsedSwitch.tag = indexPath.section;

        cell.location.text = allowance.location;

        NSString *amt = [FormatUtils formatMoney:allowance.allowanceAmount crnCode:self.crnCode];
        cell.amount.text = amt;

        UIImage *plus = [UIImage imageNamed:@"icon_expand_arrow"];
        UIImage *minus = [UIImage imageNamed:@"icon_collapse_arrow"];

        if(allowance.isCollapsed) {
            cell.expandedIndicatorImage.image = plus;
        }else
        {
            cell.expandedIndicatorImage.image = minus;
        }

        if(allowance.markedExcluded)
        {
            cell.excludedText.text = @"excluded";
            [cell.excludedText setHidden:NO];
        }
        else{
            [cell.excludedText setHidden:YES];
        }

        UITapGestureRecognizer *singleTapRecogniser = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(headerTappedHandler:)];
        [singleTapRecogniser setDelegate:self];
        singleTapRecogniser.numberOfTouchesRequired = 1;
        singleTapRecogniser.numberOfTapsRequired = 1;
        [cell addGestureRecognizer:singleTapRecogniser];

    }
    else if (indexPath.row == excludedHeaderCellRowIndex)
    {
        cell = [tableView dequeueReusableCellWithIdentifier:@"ExcludedHeader" forIndexPath:indexPath];
        cell.markedExcluded.on = allowance.markedExcluded;
        cell.markedExcluded.tag = indexPath.section;
        if(allowance.isLocked)
        {
            cell.markedExcluded.enabled = NO;
        }
    }
    else if (indexPath.row == mealAllowanceHeaderCellRowIndex)
    {
        cell = [tableView dequeueReusableCellWithIdentifier:@"MealAllowanceHeader" forIndexPath:indexPath];
        cell.mealAllowanceSectionTitle.text = [Localizer getLocalizedText:@"Meal Allowances"];
    }
    else if (indexPath.row == breakfastAllowanceCellRowIndex)
    {
        if (self.allowanceControl.showBreakfastProvidedPickList)
        {
            if(useSegmentedMealProvided)
            {
                cell = [tableView dequeueReusableCellWithIdentifier:@"BreakfastAllowanceSegmented" forIndexPath:indexPath];
                [self selectMealSegment:cell.breakfastIncludedSegment meal:allowance.breakfastProvided];
                cell.breakfastIncludedSegment.tag = indexPath.section;
            }
            else
            {
                cell = [tableView dequeueReusableCellWithIdentifier:@"MealAllowanceSelectionWithPicker" forIndexPath:indexPath];
                cell.mealSelectedValue.text = [FixedAllowanceCell getMealProvidedValueLabel:allowance.breakfastProvided];
                cell.mealType = @"B";
                cell.mealProvided = allowance.breakfastProvided;

                UIToolbar *myToolbar = [[UIToolbar alloc] initWithFrame: CGRectMake(0,0, 320, 44)]; //should code with variables to support view resizing
                UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:cell action:@selector(inputAccessoryViewDidFinish)];
                //using default text field delegate method here, here you could call
                //myTextField.resignFirstResponder to dismiss the views
                [myToolbar setItems:[NSArray arrayWithObject: doneButton] animated:NO];
                cell.mealSelectedValue.inputAccessoryView = myToolbar;

                cell.mealAllowancePickerView = [[UIPickerView alloc] initWithFrame:CGRectMake(0, 0, 320, 44)];
                cell.mealAllowancePickerView.delegate = cell;
                if(allowance.isLocked)
                {
                    cell.mealSelectedValue.enabled = NO;
                }

                cell.mealSelectedValue.inputView = cell.mealAllowancePickerView;
                cell.onMealAllowanceSelected = ^(NSString *selectedValue)
                {
                    allowance.breakfastProvided = selectedValue;

                    // TODO this seems to be clearing the firstresponder, so the done button is superflous
                    NSIndexPath *pickerRow = [NSIndexPath indexPathForRow:lunchAllowanceCellRowIndex inSection:indexPath.section];
                    NSArray *indexArray = [NSArray arrayWithObjects: pickerRow, nil];
                    [self.tableView reloadRowsAtIndexPaths:indexArray withRowAnimation:UITableViewRowAnimationNone];

                    [self callServerToUpdateAmounts:allowance index:indexPath.section];

                };

                [FixedAllowanceCell setMealAllowancePickerDefault:allowance cell:cell provided:allowance.breakfastProvided];

            }
        }
        else
        {
            // Default to the checkbox
            cell = [tableView dequeueReusableCellWithIdentifier:@"BreakfastAllowanceSimple" forIndexPath:indexPath];
            cell.breakfastIncludedSwitch.on = [allowance.breakfastProvided isEqualToString:@"PRO"];
            cell.breakfastIncludedSwitch.tag = indexPath.section;
            if(allowance.isLocked)
            {
                cell.breakfastIncludedSwitch.enabled = NO;
            }
        }
        cell.breakfastLabel.text = self.allowanceControl.breakfastProvidedLabel;
        cell.mealLabel.text = self.allowanceControl.breakfastProvidedLabel;
    }
    else if (indexPath.row == lunchAllowanceCellRowIndex)
    {
        if (self.allowanceControl.showLunchProvidedPickList)
        {
            if(useSegmentedMealProvided)
            {
                cell = [tableView dequeueReusableCellWithIdentifier:@"LunchAllowanceSegmented" forIndexPath:indexPath];
                [self selectMealSegment:cell.lunchIncludedSegment meal:allowance.lunchProvided];
                cell.lunchIncludedSegment.tag = indexPath.section;
            }
            else
            {

                cell = [tableView dequeueReusableCellWithIdentifier:@"MealAllowanceSelectionWithPicker" forIndexPath:indexPath];
                cell.mealSelectedValue.text = [FixedAllowanceCell getMealProvidedValueLabel:allowance.lunchProvided];
                cell.mealType = @"L";
                cell.mealProvided = allowance.lunchProvided;

                UIToolbar *myToolbar = [[UIToolbar alloc] initWithFrame: CGRectMake(0,0, 320, 44)]; //should code with variables to support view resizing
                UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:cell action:@selector(inputAccessoryViewDidFinish)];
                //using default text field delegate method here, here you could call
                //myTextField.resignFirstResponder to dismiss the views
                [myToolbar setItems:[NSArray arrayWithObject: doneButton] animated:NO];
                cell.mealSelectedValue.inputAccessoryView = myToolbar;

                cell.mealAllowancePickerView = [[UIPickerView alloc] initWithFrame:CGRectMake(0, 0, 320, 44)];
                cell.mealAllowancePickerView.delegate = cell;
                cell.mealSelectedValue.inputView = cell.mealAllowancePickerView;
                cell.onMealAllowanceSelected = ^(NSString *selectedValue)
                {
                    allowance.lunchProvided = selectedValue;

                    // TODO this seems to be clearing the firstresponder, so the done button is superflous
                    NSIndexPath *pickerRow = [NSIndexPath indexPathForRow:lunchAllowanceCellRowIndex inSection:indexPath.section];
                    NSArray *indexArray = [NSArray arrayWithObjects: pickerRow, nil];
                    [self.tableView reloadRowsAtIndexPaths:indexArray withRowAnimation:UITableViewRowAnimationNone];

                    [self callServerToUpdateAmounts:allowance index:indexPath.section];

                };

                [FixedAllowanceCell setMealAllowancePickerDefault:allowance cell:cell provided:allowance.lunchProvided];


            }
        }
        else
        {
            // Default to the checkbox
            cell = [tableView dequeueReusableCellWithIdentifier:@"LunchAllowanceSimple" forIndexPath:indexPath];
            cell.lunchIncludedSwitch.on = [allowance.lunchProvided isEqualToString:@"PRO"];
            cell.lunchIncludedSwitch.tag = indexPath.section;
            if(allowance.isLocked)
            {
                cell.lunchIncludedSwitch.enabled = NO;
            }
        }
        cell.lunchLabel.text = self.allowanceControl.lunchProvidedLabel;
        cell.mealLabel.text = self.allowanceControl.lunchProvidedLabel;
    }
    else if (indexPath.row == dinnerAllowanceCellRowIndex)
    {
        if (self.allowanceControl.showDinnerProvidedPickList)
        {
            if(useSegmentedMealProvided)
            {
                cell = [tableView dequeueReusableCellWithIdentifier:@"DinnerAllowanceSegmented" forIndexPath:indexPath];
                [self selectMealSegment:cell.dinnerIncludedSegment meal:allowance.dinnerProvided];
                cell.dinnerIncludedSegment.tag = indexPath.section;
            }
            else
            {
                cell = [tableView dequeueReusableCellWithIdentifier:@"MealAllowanceSelection" forIndexPath:indexPath];
                cell.mealProvidedLabel.text = [FixedAllowanceCell getMealProvidedValueLabel:allowance.dinnerProvided];
                cell.mealType = @"D";
                cell.mealProvided = allowance.dinnerProvided;
            }
        }
        else
        {
            // Default to the checkbox
            cell = [tableView dequeueReusableCellWithIdentifier:@"DinnerAllowanceSimple" forIndexPath:indexPath];
            cell.dinnerIncludedSwitch.on = [allowance.dinnerProvided isEqualToString:@"PRO"];
            cell.dinnerIncludedSwitch.tag = indexPath.section;
            if(allowance.isLocked)
            {
                cell.dinnerIncludedSwitch.enabled = NO;
            }

        }
        cell.dinnerLabel.text = self.allowanceControl.dinnerProvidedLabel;
        cell.mealLabel.text = self.allowanceControl.dinnerProvidedLabel;

    }
    else if (indexPath.row == otherAllowanceHeaderCellRowIndex)
    {
        cell = [tableView dequeueReusableCellWithIdentifier:@"OtherAllowanceHeader" forIndexPath:indexPath];
        cell.otherAllowanceSectionTitle.text = [Localizer getLocalizedText:@"Other Allowances"];
    }
    else if (indexPath.row == overnightCellRowIndex)
    {
        cell = [tableView dequeueReusableCellWithIdentifier:@"OvernightSwitch" forIndexPath:indexPath];
        cell.overnightLabel.text = self.allowanceControl.overnightLabel;

        cell.overnightSwitch.on = allowance.overnight;
        cell.overnightSwitch.tag = indexPath.section;
        if(allowance.isLocked)
        {
            cell.overnightSwitch.enabled = NO;
        }
    }
    else if (indexPath.row == percentRuleCellRowIndex)
    {
        cell = [tableView dequeueReusableCellWithIdentifier:@"UsePercentageRule" forIndexPath:indexPath];
        cell.usePercentageRuleLabel.text = self.allowanceControl.applyPercentRuleLabel;

        cell.usePercentageRuleSwitch.on = allowance.applyPercentRule;
        cell.usePercentageRuleSwitch.tag = indexPath.section;

        if(allowance.isLocked)
        {
            cell.usePercentageRuleSwitch.enabled = NO;
        }
    }
    else if (indexPath.row == extendedTripCellRowIndex)
    {
        cell = [tableView dequeueReusableCellWithIdentifier:@"ExtendedTripSwitch" forIndexPath:indexPath];
        cell.extendedTripLabel.text = self.allowanceControl.applyExtendedTripRuleLabel;

        cell.extendedTripSwitch.on = allowance.applyExtendedTripRule;
        cell.extendedTripSwitch.tag = indexPath.section;
        if(allowance.isLocked)
        {
            cell.extendedTripSwitch.enabled = NO;
        }
    }
    else if (indexPath.row == lodgingTypeCellRowIndex)
    {
        cell = [tableView dequeueReusableCellWithIdentifier:@"LodgingType" forIndexPath:indexPath];
        cell.lodgingTypeLabel.text = self.allowanceControl.lodgingTypeLabel;

        cell.allowanceControl = self.allowanceControl;

//        cell.lodgingTypeValue.text = allowance.lodgingType;
        cell.lodgingTypeValue.text = [cell getLodgingTypeValueLabel:allowance.lodgingType];

        if(allowance.isLocked)
        {
            cell.lodgingTypeValue.enabled = NO;
        }


        UIToolbar *myToolbar = [[UIToolbar alloc] initWithFrame: CGRectMake(0,0, 320, 44)]; // TODO should code with variables to support view resizing
        UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:cell action:@selector(inputAccessoryViewDidFinishLodgingType)];
        //using default text field delegate method here, here you could call
        //myTextField.resignFirstResponder to dismiss the views
        [myToolbar setItems:[NSArray arrayWithObject: doneButton] animated:NO];
        cell.lodgingTypeValue.inputAccessoryView = myToolbar;

//        cell.lodgingTypePickerView = [[UIPickerView alloc] initWithFrame:CGRectMake(0, 0, 320, 44)];
        cell.lodgingTypePickerView = [[UIPickerView alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, 44)];
        cell.lodgingTypePickerView.delegate = cell;
        cell.lodgingTypePickerView.dataSource = cell;
        cell.lodgingTypeValue.inputView = cell.lodgingTypePickerView;
        cell.onLodgingTypeSelected = ^(NSString *selectedValue)
        {
            allowance.lodgingType = selectedValue;

            // TODO this seems to be clearing the firstresponder, so the done button is superflous
            NSIndexPath *pickerRow = [NSIndexPath indexPathForRow:lodgingTypeCellRowIndex inSection:indexPath.section];
            NSArray *indexArray = [NSArray arrayWithObjects: pickerRow, nil];
            [self.tableView reloadRowsAtIndexPaths:indexArray withRowAnimation:UITableViewRowAnimationNone];

            [self callServerToUpdateAmounts:allowance index:indexPath.section];

        };

        [cell setLodgingTypePickerDefault:allowance.lodgingType];

    }
    else if (indexPath.row == userEntryBreakfastAmountCellRowIndex)
    {
        cell = [tableView dequeueReusableCellWithIdentifier:@"BreakfastAmountWInput" forIndexPath:indexPath];

        NSString *amt = [FormatUtils formatMoneyWithNumber:allowance.breakfastTransactionAmount crnCode:allowance.breakfastCrnCode withCurrency:NO];

        if(self.allowanceControl.showUserEntryOfBreakfastAmount)
        {
            cell.breakfastAmountLabel.text = [Localizer getLocalizedText:@"Breakfast Amount *"];
        }
        else if (self.allowanceControl.showUserEntryOfMealsAmount)
        {
            cell.breakfastAmountLabel.text = [Localizer getLocalizedText:@"Meals Amount"];
        }
        else
        {
            cell.breakfastAmountLabel.text = [Localizer getLocalizedText:@"User Entry Meal"];
        }

        cell.breakfastAmountText.tag = indexPath.section;
        cell.breakfastAmountText.text = amt;
        cell.breakfastAmount = allowance.breakfastTransactionAmount;

        if(allowance.isLocked)
        {
            cell.breakfastAmountText.enabled = NO;
        }

        UIToolbar *myToolbar = [[UIToolbar alloc] initWithFrame: CGRectMake(0,0, 320, 44)]; // TODO should code with variables to support view resizing
        UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:cell action:@selector(inputAccessoryViewDidFinishBreakfastAmountText)];
        [myToolbar setItems:[NSArray arrayWithObject: doneButton] animated:NO];
        cell.breakfastAmountText.inputAccessoryView = myToolbar;


    }
    else if (indexPath.row == userEntryBreakfastAmountCurrencyCellRowIndex)
    {
        cell = [tableView dequeueReusableCellWithIdentifier:@"BreakfastAmountCurrency" forIndexPath:indexPath];

        cell.breakfastCrnCode = allowance.breakfastCrnCode;
        cell.breakfastCrnKey = allowance.breakfastCrnKey;

        //Set the currency name
        NSLocale *locale = [NSLocale currentLocale];
        NSString *currencyName = [locale displayNameForKey:NSLocaleCurrencyCode value:allowance.breakfastCrnCode];
        cell.breakfastCurrencyValue.text = currencyName;

        if(allowance.isLocked)
        {
            cell.accessoryType = UITableViewCellAccessoryNone;
        }
        else
        {
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
        }


    }
    else if (indexPath.row == userEntryBreakfastAmountExchangeRateCellRowIndex)
    {
        cell = [tableView dequeueReusableCellWithIdentifier:@"BreakfastAmountExchangeRateWInput" forIndexPath:indexPath];
        NSNumberFormatter *style= [self getAllowanceNumberFormatter];

        cell.breakfastExchangeRateValue.text = [style stringFromNumber:allowance.breakfastExchangeRate];
        cell.breakfastExchangeRateText.text = [style stringFromNumber:allowance.breakfastExchangeRate];

        cell.breakfastExchangeRate = allowance.breakfastExchangeRate;

        cell.exchangeRateLabel.text = [Localizer getLocalizedText:@"Exchange Rate"];

        if(allowance.isLocked)
        {
            cell.breakfastExchangeRateText.enabled = NO;
        }

        UIToolbar *myToolbar = [[UIToolbar alloc] initWithFrame: CGRectMake(0,0, 320, 44)]; // TODO should code with variables to support view resizing
        UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:cell action:@selector(inputAccessoryViewDidFinishBreakfastExchangeRateText)];
        [myToolbar setItems:[NSArray arrayWithObject: doneButton] animated:NO];
        cell.breakfastExchangeRateText.inputAccessoryView = myToolbar;

    }
    else
    {
        static NSString *CellIdentifier = @"DummyPlaceholder";
        UITableViewCell *dummyCell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier forIndexPath:indexPath];
        dummyCell.clipsToBounds = YES;
        return dummyCell;
    }

    
    // Configure the cell...
    cell.clipsToBounds = YES;

    cell.section = indexPath.section;

    cell.allowanceControl = self.allowanceControl;

    return cell;
}



- (NSNumberFormatter *)getAllowanceNumberFormatter {
    NSNumberFormatter *style = [[NSNumberFormatter alloc] init];
    [style setFormatterBehavior:NSNumberFormatterBehavior10_4];
    [style setNumberStyle:NSNumberFormatterDecimalStyle];
    [style setMinimumFractionDigits:4];
    return style;
}




- (void)selectMealSegment:(UISegmentedControl *)segment meal:(NSString *)mealStatus {
//    NSLog(@"mealStatus = %@", mealStatus);
    if([mealStatus isEqualToString:@"NPR"])
    {
        segment.selectedSegmentIndex = 0;
    }
    else if ([mealStatus isEqualToString:@"PRO"])
    {
        segment.selectedSegmentIndex = 1;
    }
    else if ([mealStatus isEqualToString:@"TAX"])
    {
        segment.selectedSegmentIndex = 2;
    }
    else
    {
        // Should probably be an error
        NSLog(@"No Match mealStatus = %@", mealStatus);
        segment.selectedSegmentIndex = 0;
    }
}


- (IBAction)changeExcludedSwitch:(id)sender {
    UISwitch *sw = (UISwitch *)sender;

    FixedAllowance *allowance = [self.allowanceRows objectAtIndex:sw.tag];

    if([sw isOn])
    {
        allowance.markedExcluded = YES;
    }
    else
    {
        allowance.markedExcluded = NO;
    }

    [self callServerToUpdateAmounts:allowance index:sw.tag];

//    [self.tableView reloadData];
//    [self.tableView reloadSections:[NSIndexSet indexSetWithIndex:sw.tag] withRowAnimation:UITableViewRowAnimationFade];

    // Not sure if this is what I want, but it does recalculate the height
//    NSIndexPath *targetedCellIndexPath = [NSIndexPath indexPathForRow:0 inSection:sw.tag];
//    NSArray *indexArray = [NSArray arrayWithObject:targetedCellIndexPath];
//    [self.tableView reloadRowsAtIndexPaths:indexArray withRowAnimation:UITableViewRowAnimationFade];

}


-(IBAction)changeCollapsedSwitch:(id)sender {
    UISwitch *sw = (UISwitch *)sender;

    FixedAllowance *allowance = [self.allowanceRows objectAtIndex:sw.tag];

    if([sw isOn])
    {
        allowance.isCollapsed = YES;
    }
    else
    {
        allowance.isCollapsed = NO;
    }

    [self.tableView reloadSections:[NSIndexSet indexSetWithIndex:sw.tag] withRowAnimation:UITableViewRowAnimationNone];
    // Not sure if this is what I want, but it does recalculate the height
//    NSIndexPath *targetedCellIndexPath = [NSIndexPath indexPathForRow:0 inSection:sw.tag];
//    NSArray *indexArray = [NSArray arrayWithObject:targetedCellIndexPath];
//    [self.tableView reloadRowsAtIndexPaths:indexArray withRowAnimation:UITableViewRowAnimationFade];
}

- (void) headerTappedHandler:(UIGestureRecognizer *)gestureRecognizer
{
    FixedAllowanceCell *cell = gestureRecognizer.view;
    NSLog(@"cell.tag = %li", (long)cell.tag);

    FixedAllowance *allowance = [self.allowanceRows objectAtIndex:cell.tag];

    allowance.isCollapsed = !allowance.isCollapsed;
    [self.tableView reloadSections:[NSIndexSet indexSetWithIndex:cell.tag] withRowAnimation:UITableViewRowAnimationNone];
}

- (IBAction)changeBreakfastProvidedSegment:(id)sender{
    UISegmentedControl *segment = (UISegmentedControl *)sender;

    FixedAllowance *allowance = [self.allowanceRows objectAtIndex:segment.tag];

    if(segment.selectedSegmentIndex == 0)
    {
        allowance.breakfastProvided = @"NPR";
    }
    else if(segment.selectedSegmentIndex == 1)
    {
        allowance.breakfastProvided = @"PRO";
    }
    else if(segment.selectedSegmentIndex == 2)
    {
        allowance.breakfastProvided = @"TAX";
    }

    [self callServerToUpdateAmounts:allowance index:segment.tag];
}

- (IBAction)changeBreakfastProvidedSwitch:(id)sender{
    UISwitch *sw = (UISwitch *)sender;

    FixedAllowance *allowance = [self.allowanceRows objectAtIndex:sw.tag];
    if([sw isOn])
    {
        allowance.breakfastProvided = @"PRO";
    }
    else
    {
        allowance.breakfastProvided = @"NPR";
    }
    NSLog(@"allowance = %@", allowance.breakfastProvided);

    [self callServerToUpdateAmounts:allowance index:sw.tag];
}

- (IBAction)changeLunchProvidedSegment:(id)sender{
    UISegmentedControl *segment = (UISegmentedControl *)sender;

    FixedAllowance *allowance = [self.allowanceRows objectAtIndex:segment.tag];

    if(segment.selectedSegmentIndex == 0)
    {
        allowance.lunchProvided = @"NPR";
    }
    else if(segment.selectedSegmentIndex == 1)
    {
        allowance.lunchProvided = @"PRO";
    }
    else if(segment.selectedSegmentIndex == 2)
    {
        allowance.lunchProvided = @"TAX";
    }
}

- (IBAction)changeLunchProvidedSwitch:(id)sender{
    UISwitch *sw = (UISwitch *)sender;

    FixedAllowance *allowance = [self.allowanceRows objectAtIndex:sw.tag];
    if([sw isOn])
    {
        allowance.lunchProvided = @"PRO";
    }
    else
    {
        allowance.lunchProvided = @"NPR";
    }
    [self callServerToUpdateAmounts:allowance index:sw.tag];
}

- (IBAction)changeDinnerProvidedSegment:(id)sender{
    UISegmentedControl *segment = (UISegmentedControl *)sender;

    FixedAllowance *allowance = [self.allowanceRows objectAtIndex:segment.tag];

    if(segment.selectedSegmentIndex == 0)
    {
        allowance.dinnerProvided = @"NPR";
    }
    else if(segment.selectedSegmentIndex == 1)
    {
        allowance.dinnerProvided = @"PRO";
    }
    else if(segment.selectedSegmentIndex == 2)
    {
        allowance.dinnerProvided = @"TAX";
    }
}

- (IBAction)changeDinnerProvidedSwitch:(id)sender{
    UISwitch *sw = (UISwitch *)sender;

    FixedAllowance *allowance = [self.allowanceRows objectAtIndex:sw.tag];
    if([sw isOn])
    {
        allowance.dinnerProvided = @"PRO";
    }
    else
    {
        allowance.dinnerProvided = @"NPR";
    }
    [self callServerToUpdateAmounts:allowance index:sw.tag];
}

- (IBAction)changeOvernightSwitch:(id)sender{
    UISwitch *sw = (UISwitch *)sender;
    FixedAllowance *allowance = [self.allowanceRows objectAtIndex:sw.tag];
    if([sw isOn])
    {
        allowance.overnight = YES;
    }
    else
    {
        allowance.overnight = NO;
    }
    [self callServerToUpdateAmounts:allowance index:sw.tag];
}

- (IBAction)changeExtendedTripSwitch:(id)sender{
    UISwitch *sw = (UISwitch *)sender;
    FixedAllowance *allowance = [self.allowanceRows objectAtIndex:sw.tag];
    if([sw isOn])
    {
        allowance.applyExtendedTripRule = YES;
    }
    else
    {
        allowance.applyExtendedTripRule = NO;
    }
    [self callServerToUpdateAmounts:allowance index:sw.tag];
}

- (IBAction)changePercentageRuleSwitch:(id)sender{
    UISwitch *sw = (UISwitch *)sender;
    FixedAllowance *allowance = [self.allowanceRows objectAtIndex:sw.tag];
    if([sw isOn])
    {
        allowance.applyPercentRule = YES;
    }
    else
    {
        allowance.applyPercentRule = NO;
    }
    [self callServerToUpdateAmounts:allowance index:sw.tag];
}

- (CGFloat)tableView:(UITableView *)tableView estimatedHeightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 60;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {

    CGFloat d = [super tableView:tableView heightForRowAtIndexPath:indexPath];

    FixedAllowance *allowance = [self.allowanceRows objectAtIndex:indexPath.section];

    if(indexPath.row == dayHeaderCellRowIndex)
    {
        if([ExSystem is8Plus])
        {
            return UITableViewAutomaticDimension;
        }
        UITableViewCell *headerText = [self.tableView dequeueReusableCellWithIdentifier:@"DayHeader"];
        d = headerText.frame.size.height;
    }
    else if (indexPath.row == mealAllowanceHeaderCellRowIndex)
    {
        if([self.allowanceControl hasMealAllowances])
        {
            UITableViewCell *headerText = [self.tableView dequeueReusableCellWithIdentifier:@"MealAllowanceHeader"];
            d = headerText.frame.size.height;
        }
        else
        {
            d = hiddenRowHeight;
        }
    }
    else if (indexPath.row == breakfastAllowanceCellRowIndex)
    {
        UITableViewCell *cell = nil;
        // Is the field enabled
        BOOL cb = self.allowanceControl.showBreakfastProvidedCheckBox;
        if(cb)
        {
            cell = [self.tableView dequeueReusableCellWithIdentifier:@"BreakfastAllowanceSimple"];
            d = cell.frame.size.height;
        }
        else if (self.allowanceControl.showBreakfastProvidedPickList)
        {
            if(useSegmentedMealProvided)
            {
                cell = [self.tableView dequeueReusableCellWithIdentifier:@"BreakfastAllowanceSegmented"];
            }
            else
            {
                cell = [self.tableView dequeueReusableCellWithIdentifier:@"MealAllowanceSelection"];
            }
            d = cell.frame.size.height;
        }
        else
        {
            // Make it zero
            d = hiddenRowHeight;
        }


    }
    else if (indexPath.row == lunchAllowanceCellRowIndex)
    {
//        NSLog(@"self.allowanceControl.showLunchProvidedCheckBox = %p", self.allowanceControl.showLunchProvidedCheckBox);

        UITableViewCell *cell = nil;
        // Is the field enabled
        if(self.allowanceControl.showLunchProvidedCheckBox)
        {
            cell = [self.tableView dequeueReusableCellWithIdentifier:@"LunchAllowanceSimple"];
            d = cell.frame.size.height;
        }
        else if (self.allowanceControl.showLunchProvidedPickList)
        {
            if(useSegmentedMealProvided)
            {
                cell = [self.tableView dequeueReusableCellWithIdentifier:@"LunchAllowanceSegmented"];
            }
            else
            {
                cell = [self.tableView dequeueReusableCellWithIdentifier:@"MealAllowanceSelection"];
            }
            d = cell.frame.size.height;
        }
        else
        {
            // Make it zero
            d = hiddenRowHeight;
        }


    }
    else if (indexPath.row == dinnerAllowanceCellRowIndex)
    {

        UITableViewCell *cell = nil;
        // Is the field enabled

        if(self.allowanceControl.showDinnerProvidedCheckBox)
        {
            cell = [self.tableView dequeueReusableCellWithIdentifier:@"DinnerAllowanceSimple"];
            d = cell.frame.size.height;
        }
        else if (self.allowanceControl.showDinnerProvidedPickList)
        {
            if(useSegmentedMealProvided)
            {
                cell = [self.tableView dequeueReusableCellWithIdentifier:@"DinnerAllowanceSegmented"];
            }
            else
            {
                cell = [self.tableView dequeueReusableCellWithIdentifier:@"MealAllowanceSelection"];
            }
            d = cell.frame.size.height;
        }
        else
        {
            // Make it zero
            d = hiddenRowHeight;
        }
    }
    else if (indexPath.row == otherAllowanceHeaderCellRowIndex)
    {
        if([self.allowanceControl hasOtherAllowances])
        {
            UITableViewCell *headerText = [self.tableView dequeueReusableCellWithIdentifier:@"OtherAllowanceHeader"];
            d = headerText.frame.size.height;
        }
        else
        {
            d = hiddenRowHeight;
        }
    }
    else if (indexPath.row == overnightCellRowIndex)
    {
        if(self.allowanceControl.showOvernightCheckBox)
        {
            UITableViewCell *headerText = [self.tableView dequeueReusableCellWithIdentifier:@"OvernightSwitch"];
            d = headerText.frame.size.height;
        }
        else
        {
            d = hiddenRowHeight;
        }
    }
    else if (indexPath.row == percentRuleCellRowIndex)
    {
        if(self.allowanceControl.showPercentRuleCheckBox)
        {
            UITableViewCell *headerText = [self.tableView dequeueReusableCellWithIdentifier:@"UsePercentageRule"];
            d = headerText.frame.size.height;
        }
        else
        {
            d = hiddenRowHeight;
        }
    }
    else if (indexPath.row == extendedTripCellRowIndex)
    {
        if(self.allowanceControl.showExtendedTripCheckBox)
        {
            UITableViewCell *headerText = [self.tableView dequeueReusableCellWithIdentifier:@"ExtendedTripSwitch"];
            d = headerText.frame.size.height;
        }
        else
        {
            d = hiddenRowHeight;
        }
    }
    else if (indexPath.row == lodgingTypeCellRowIndex)
    {
        if(self.allowanceControl.showLodgingTypePickList)
        {
            UITableViewCell *headerText = [self.tableView dequeueReusableCellWithIdentifier:@"LodgingType"];
            d = headerText.frame.size.height;
        }
        else
        {
            d = hiddenRowHeight;
        }
    }
    else if (indexPath.row == userEntryBreakfastAmountCellRowIndex)
    {
        if(self.allowanceControl.showUserEntryOfBreakfastAmount || self.allowanceControl.showUserEntryOfMealsAmount)
        {
            UITableViewCell *headerText = [self.tableView dequeueReusableCellWithIdentifier:@"BreakfastAmount"];
            d = headerText.frame.size.height;
        }
        else
        {
            d = hiddenRowHeight;
        }
    }else if (indexPath.row == userEntryBreakfastAmountCurrencyCellRowIndex)
    {
        if(self.allowanceControl.showUserEntryOfBreakfastAmount || self.allowanceControl.showUserEntryOfMealsAmount)
        {
            UITableViewCell *headerText = [self.tableView dequeueReusableCellWithIdentifier:@"BreakfastAmountCurrency"];
            d = headerText.frame.size.height;
        }
        else
        {
            d = hiddenRowHeight;
        }
    }else if (indexPath.row == userEntryBreakfastAmountExchangeRateCellRowIndex)
    {
        if(self.allowanceControl.showUserEntryOfBreakfastAmount || self.allowanceControl.showUserEntryOfMealsAmount)
        {
            UITableViewCell *headerText = [self.tableView dequeueReusableCellWithIdentifier:@"BreakfastAmountExchangeRate"];
            d = headerText.frame.size.height;
        }
        else
        {
            d = hiddenRowHeight;
        }
    }else if (indexPath.row == excludedHeaderCellRowIndex)
    {
        UITableViewCell *headerText = [self.tableView dequeueReusableCellWithIdentifier:@"ExcludedHeader"];
        d = headerText.frame.size.height;
    }

    return d;
}


- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    CGFloat d = 10;
    return d;
}


- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    CGFloat d = 1; // Has to be non zero for some reason
    return d;
}

- (BOOL)shouldPerformSegueWithIdentifier:(NSString *)identifier sender:(id)sender {
    if([identifier isEqualToString:@"GenerateAllowanceAdjustments"])
    {
        // Show the wait screen block
        [WaitViewController showWithText:@"GenerateAllowances" animated:YES];

        void (^success)(NSString *) = ^(NSString *result)
        {
            [FixedAllowance parseUpdateAllowancesResult:result];
    
            [self completedAllowanceAdjustmentSave];
    
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
        };
        void (^failure)(NSError *) = ^(NSError *error) {
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            //            [self.refreshControl endRefreshing];
            //            [self showError];
        };

        // Make the save call to the mws
        CXRequest *request = [self updateFixedAllowances];
        [[CXClient sharedClient] performRequest:request success:success failure:failure];

        // Prevent the segue
        return false;
    }
    else if([identifier isEqualToString:@"ChooseBreakfastAmountCurrency"])
    {
        // Currency picker
        FixedAllowanceCell *cell = (FixedAllowanceCell *)sender;
        FixedAllowance *allowance = [self.allowanceRows objectAtIndex:cell.section];

        if(!allowance.isLocked)
        {
            // Display the Picker
            [self showListEditor:@"TransactionCurrencyName" label:[Localizer getLocalizedText:@"Currency"] crnKey:cell.breakfastCrnKey section:cell.section meal:@"B"];
        }

        return false;
    }
    else if([identifier isEqualToString:@"AllowanceAmountSegue"])
    {

    }

    BOOL should = [super shouldPerformSegueWithIdentifier:identifier sender:sender];
    return should;
}

- (void)showListEditor:(NSString *)fieldName label:(NSString *)label crnKey:(NSInteger )crnKey section:(NSInteger )section meal:(NSString *)meal
{

    NSString *intValue = [NSString stringWithFormat:@"%ld", (long)crnKey];

    MealsAmountFormFieldData *field = [[MealsAmountFormFieldData alloc] initField:fieldName label:label value:intValue ctrlType:@"edit" dataType:@"CURRENCY"];

    field.liKey = intValue;

    field.section = section;
    field.meal = meal;

    ListFieldEditVC *lvc = nil;

    lvc = [[ListFieldEditVC alloc] initWithNibName:@"ListFieldEditVC" bundle:nil];

    [lvc setSeedData:field delegate:self keysToExclude:nil];

//    [lvc hideSearchBar];

    [lvc view];

//    lvc.searchText = currentValue;

    if([UIDevice isPad])
        lvc.modalPresentationStyle = UIModalPresentationFormSheet;

    [self.navigationController pushViewController:lvc animated:YES];

    lvc.sectionKeys = [[NSMutableArray alloc] initWithObjects: @"MAIN_SECTION", nil];
    lvc.sections = [[NSMutableDictionary alloc] initWithObjectsAndKeys: [self getListItems], @"MAIN_SECTION", nil];

    //We are seeing this issue in iOS 7
    double delayInSeconds = 0.05;
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        [self prefetchForListEditor:lvc];
    });

    [lvc hideLoadingView];

}

-(void)prefetchForListEditor:(ListFieldEditVC*) lvc
{
    MealsAmountFormFieldData* field = lvc.field;
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: field, @"FIELD", @"Y", @"MRU", nil];
    [[ExSystem sharedInstance].msgControl createMsg:LIST_FIELD_SEARCH_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:lvc];
}



- (NSArray* )getListItems
{
    NSMutableArray* result = [[NSMutableArray alloc] init];
    return result;
}

// Handles the return from the Currency picker
-(void) fieldUpdated:(MealsAmountFormFieldData*) field
{
    FixedAllowance *allowance = (FixedAllowance *)[self.allowanceRows objectAtIndex:field.section];

    if([field.meal isEqualToString:@"B"])
    {
        void (^success)(NSString *) = ^(NSString *result)
        {
            RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
            if (rootXML != nil) {

            }

            NSString *status = [rootXML child:@"Status"].text;
            NSString *exchangeRate = [rootXML child:@"ExchangeRate"].text;

            if([status isEqualToString:@"SUCCESS"])
            {
                allowance.breakfastExchangeRate = [NSDecimalNumber decimalNumberWithString:exchangeRate];
                [allowance recalculateBreakfastPostedAmount];

                //Replace with a row reload
                [self.tableView reloadData];

                [self callServerToUpdateAmounts:allowance index:field.section];
            }
            else
            {
                //WTFTD

            }
        };

        void (^failure)(NSError *) = ^(NSError *error) {
            NSLog(@"~~~~error = %@", error);
        };

        // How to get the exchange rate?
        allowance.breakfastCrnKey = (NSInteger *)[field.liKey integerValue];
        allowance.breakfastCrnCode = field.liCode;

        if([field.liCode isEqualToString:self.crnCode])
        {
            // Setting it back to the same currency
            allowance.breakfastExchangeRate = [NSDecimalNumber decimalNumberWithString:@"1"];
            [allowance recalculateBreakfastPostedAmount];

            //Replace with a row reload
            [self.tableView reloadData];

            [self callServerToUpdateAmounts:allowance index:field.section];
        }
        else
        {
            // Currency is not the same as the report
            NSDate *exchangeDate = allowance.allowanceDate;
            if(allowance.allowanceDate == nil)
            {
                exchangeDate  = [NSDate date];
            }

            NSString *forDate = [CCDateUtilities formatDateYYYYMMddByNSDate:exchangeDate];

            NSString *path = [NSString stringWithFormat:@"Mobile/Expense/ExchangeRate/%@/%@/%@", allowance.breakfastCrnCode, self.crnCode, forDate];
            CXRequest *cxRequest = [[CXRequest alloc] initWithServicePath:path];
            [[CXClient sharedClient] performRequest:cxRequest success:success failure:failure];
        }
    }

    //Reload
    [self.tableView reloadData];
}

/*
<ActionStatus xmlns = "http://schemas.datacontract.org/2004/07/Snowbird" xmlns:i = "http://www.w3.org/2001/XMLSchema-instance" >
< Status > SUCCESS </Status>
<ExchangeRate>0.7254</ExchangeRate>
</ActionStatus>
*/

- (void)completedAllowanceAdjustmentSave {
    if (self.onSuccessfulSave)
    {
        self.onSuccessfulSave(@{@"key":@"it worked"});
    }
    else{
        NSLog(@"No success handler defined");
    }
}

- (CXRequest *)updateFixedAllowances {
    // Compose the path
    NSString *rptKey = self.rptKey;
    CXRequest *cxRequest= [FixedAllowance getUpdateAllowancesRequest:rptKey];
    cxRequest.requestXML = [self composeFixedAllowanceRows];

    return cxRequest;
}

- (NSString *)composeFixedAllowanceRows {
    NSMutableString *body = [[NSMutableString alloc] init];
    AllowanceControl *control = self.allowanceControl;

    NSMutableArray *rows = self.allowanceRows;
    for (FixedAllowance *fixedAllowance in rows) {
        NSString *row = [fixedAllowance createUpdateSegmentXML:control];
        [body appendString:row];
    }
    
    return body;
}

- (void)callServerToUpdateAmounts:(FixedAllowance *)allowance index:(NSUInteger )index {

    void (^success)(NSString *) = ^(NSString *result)
    {
        NSString *e = [FixedAllowance parseGetUpdatedFixedAllowanceAmountsResult:result];
        FixedAllowance *updatedAllowance = [FixedAllowance extractUpdatedFixedAllowance:result];

        // We have the data back from the server
        allowance.allowanceAmount = updatedAllowance.allowanceAmount;
        allowance.aboveLimitAmount = updatedAllowance.aboveLimitAmount;

        //TODO The Breakfast Provided field can be updated when the Amount field is changed
        // This is missing functionality in CTE, The fields are not returned by Midtier GetUpdatedFixedAllowanceAmounts

        NSMutableArray *indexArray = [[NSMutableArray alloc] init];

        NSIndexPath *targetedCellHeaderIndexPath = [NSIndexPath indexPathForRow:0 inSection:index];
        [indexArray addObject:targetedCellHeaderIndexPath];

        NSIndexPath *targetedBreakfastProvidedHeaderIndexPath = [NSIndexPath indexPathForRow:breakfastAllowanceCellRowIndex inSection:index];
        [indexArray addObject:targetedBreakfastProvidedHeaderIndexPath];

        [self.tableView reloadRowsAtIndexPaths:indexArray withRowAnimation:UITableViewRowAnimationFade];

    };

    void (^failure)(NSError *) = ^(NSError *error) {
        NSLog(@"error = %@", error);
    };

    CXRequest *request = [FixedAllowance getUpdatedFixedAllowanceAmounts:allowance rptKey:self.rptKey allowanceControl:self.allowanceControl];
    [[CXClient sharedClient] performRequest:request success:success failure:failure];

}


-(void)loadAllowanceData:(NSString *)rptKey taDayKey:(NSString *)taDayKey{
    void (^success)(NSString *) = ^(NSString *result)
    {
        // Parse the response
        self.allowanceRows = [FixedAllowance parseFixedAllowanceXML:result];
        if(taDayKey != nil) // Restrict to the selected ta day
        {
            self.allowanceRows = [FixedAllowance filterAllowancesByDay:self.allowanceRows taDayKey:taDayKey];
        }

        if(self.expandAllDays)
        {
            // Mark all the rows expanded.
            for (FixedAllowance *allowance in self.allowanceRows) {
                if(!allowance.isLocked) {
                    allowance.isCollapsed = NO;
                }
            }
        }

        self.allowanceControl = [AllowanceControl parseAllowanceControlXML:result];

//        [self.allowanceControl printContents];


        [self.tableView reloadData];

        [WaitViewController hideAnimated:YES withCompletionBlock:nil];

    };
    void (^failure)(NSError *) = ^(NSError *error) {
        NSLog(@"error = %@", error);
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
//        [self showError];
    };

    CXRequest *request = [FixedAllowance getTaFixedAllowances:rptKey];
    [[CXClient sharedClient] performRequest:request success:success failure:failure];
}




/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/


#pragma mark - Navigation



// In a story board-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    FixedAllowanceCell *cell = (FixedAllowanceCell *)sender;

    if ([segue.identifier isEqualToString:@"MealStatusPicker"])
    {
        AllowanceMealProvidedTableViewController *c = (AllowanceMealProvidedTableViewController *)[segue destinationViewController];
        c.mealType = cell.mealType;
        c.mealProvided = cell.mealProvided;
        c.section = cell.section;

        if([cell.mealType isEqualToString:@"B"])
        {
            c.mealName = self.allowanceControl.breakfastProvidedLabel;
        }
    }
    else if ([segue.identifier isEqualToString:@"AllowanceAmountSegue"])
    {
        AllowanceAmountViewController *c = (AllowanceAmountViewController *)[segue destinationViewController];
        c.meal = @"B";
        c.section = cell.section;
        c.mode = @"AMOUNT";
        c.amount = [cell.breakfastAmount stringValue];
    }
    else if ([segue.identifier isEqualToString:@"AllowanceExchangeRateSegue"])
    {
        AllowanceAmountViewController *c = (AllowanceAmountViewController *)[segue destinationViewController];
        c.meal = @"B";
        c.section = cell.section;
        c.mode = @"EXCHANGERATE";
        c.amount = [cell.breakfastExchangeRate stringValue];
    }
}

-(IBAction)unwindToAllowanceAdjustmentFromMealPicker:(UIStoryboardSegue *)segue
{
    if([segue.identifier isEqualToString:@"selectMealStatus"])
    {
        AllowanceMealProvidedTableViewController *c = (AllowanceMealProvidedTableViewController *)[segue sourceViewController];

        FixedAllowance *allowance = (FixedAllowance *)[self.allowanceRows objectAtIndex:c.section];
        if([c.mealType isEqualToString:@"B"])
        {
            allowance.breakfastProvided = c.mealProvided;
        }
        else if([c.mealType isEqualToString:@"L"])
        {
            allowance.lunchProvided = c.mealProvided;
        }
        else if([c.mealType isEqualToString:@"D"])
        {
            allowance.dinnerProvided = c.mealProvided;
        }

        [self callServerToUpdateAmounts:allowance index:c.section];

        //TODO can this be replaced with a section specific one?
        [self.tableView reloadData];
    }

}

-(IBAction)unwindToAllowanceAdjustmentFromAmountEntry:(UIStoryboardSegue *)segue
{
    AllowanceAmountViewController *c = (AllowanceAmountViewController *)[segue sourceViewController];
    FixedAllowance *allowance = [self.allowanceRows objectAtIndex:c.section];

    NSDecimalNumber *decimalAmount = [NSDecimalNumber decimalNumberWithString:c.amount];

    if([c.meal isEqualToString:@"B"])
    {
        if([c.mode isEqualToString:@"EXCHANGERATE"])
        {
            allowance.breakfastExchangeRate = decimalAmount;
        }
        else if ([c.mode isEqualToString:@"AMOUNT"])
        {

            NSDecimalNumber *txAmount = decimalAmount;
            allowance.breakfastTransactionAmount = txAmount;
        }
        // Recalculate the values
        [allowance recalculateBreakfastPostedAmount];
    }

    //Replace with a row reload
    [self.tableView reloadData];

    [self callServerToUpdateAmounts:allowance index:c.section];

}

- (IBAction)breakfastAmountEndEdit:(id)sender {

    UITextField *textField = (UITextField *)sender;
    NSString *string = textField.text;

    if ([[string stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]] length] == 0) {
        // The string is empty
        string = @"0";
    }

    FixedAllowance *allowance = [self.allowanceRows objectAtIndex:textField.tag];

    NSDecimalNumber *decimalAmount = [NSDecimalNumber decimalNumberWithString:string];

    if(![allowance.breakfastTransactionAmount isEqualToNumber:decimalAmount])
    {

        allowance.breakfastTransactionAmount = decimalAmount;

        // Recalculate the values
        [allowance recalculateBreakfastPostedAmount];

        //Replace with a row reload
        [self.tableView reloadData];

        [self callServerToUpdateAmounts:allowance index:textField.tag];
    }
}

- (IBAction)breakfastExchangeRateEndEdit:(id)sender {
    UITextField *textField = (UITextField *)sender;
    NSString *string = textField.text;

    if ([[string stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]] length] == 0) {
        // The string is empty
        string = @"0";
    }

    FixedAllowance *allowance = [self.allowanceRows objectAtIndex:textField.tag];

    NSDecimalNumber *decimalAmount = [NSDecimalNumber decimalNumberWithString:string];

    if(![allowance.breakfastExchangeRate isEqualToNumber:decimalAmount])
    {
        allowance.breakfastExchangeRate = decimalAmount;

        // Recalculate the values
        [allowance recalculateBreakfastPostedAmount];

        //Replace with a row reload
        [self.tableView reloadData];

        [self callServerToUpdateAmounts:allowance index:textField.tag];
    }

}

-(void) actionBack:(id)sender
{
    if ([UIDevice isPad])
    {
        if ([self.navigationController.viewControllers count]>1)
            [self.navigationController popViewControllerAnimated:YES];
        else {
            [self dismissViewControllerAnimated:YES completion:nil];
        }
    }
    else
        [self.navigationController popViewControllerAnimated:YES];
}

@end
