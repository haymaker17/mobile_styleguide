//
//  ItineraryStopDetailViewController.m
//  ConcurMobile
//
//  Created by Wes Barton on 2/7/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ItineraryStopDetailViewController.h"
#import "ItineraryStop.h"
#import "ItineraryStopCell.h"
#import "Itinerary.h"
#import "ItineraryConfig.h"
#import "CXClient.h"
#import "ItineraryStopDetailDateViewController.h"
#import "AnalyticsManager.h"
#import "ItineraryStopCalendarTableViewCell.h"
#import "MRUManager.h"
#import "AnalyticsTracker.h"


@interface ItineraryStopDetailViewController ()

@property BOOL editingDepartureTime;
@property BOOL editingArrivalTime;
@property BOOL editingBorderCrossingTime;


@end

@implementation ItineraryStopDetailViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

/*! Returns the major version of iOS, (i.e. for iOS 6.1.3 it returns 6)
 */
NSUInteger DeviceSystemMajorVersion()
{
    static NSUInteger _deviceSystemMajorVersion = -1;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{

        _deviceSystemMajorVersion = [[[[[UIDevice currentDevice] systemVersion] componentsSeparatedByString:@"."] objectAtIndex:0] intValue];
    });

    return _deviceSystemMajorVersion;
}

#define EMBEDDED_DATE_PICKER (DeviceSystemMajorVersion() >= 7)


- (void)viewDidLoad
{
    [super viewDidLoad];

    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;

    self.editingDepartureTime = false;
    self.editingArrivalTime = false;
    self.editingBorderCrossingTime = false;

    [AnalyticsTracker initializeScreenName:@"ItineraryStopDetail"];

}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    if(self.isSingleDay)
    {
        return 7;
    }
    return 5;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    if(self.isSingleDay)
    {
        return [self tableView:tableView titleForHeaderInSectionSingle:section];
    }
    return [self tableView:tableView titleForHeaderInSectionRegular:section];
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSectionRegular:(NSInteger)section {
    switch (section){
        case HeaderSectionIndex:
            return @"Remove me";
        case DayTripSectionIndex:
            return @"Remove me";
        case RegularFromSectionIndex:
            return [Localizer getLocalizedText:@"From"];
        case RegularBorderCrossingSectionIndex:
            return [Localizer getLocalizedText:@"Border Crossing"];
        case RegularToSectionIndex:
            return [Localizer getLocalizedText:@"To"];
    }
    return [super tableView:tableView titleForHeaderInSection:section];
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSectionSingle:(NSInteger)section {
    switch(section){
        case HeaderSectionIndex:
            return @"Remove Me";
        case DayTripSectionIndex:
            return @"Remove Me";
        case SingleFromSectionIndex:
            return [Localizer getLocalizedText:@"From"];
        case SingleBorderCrossingOneSectionIndex:
            return [Localizer getLocalizedText:@"Border Crossing"];
        case SingleToSectionIndex:
            return [Localizer getLocalizedText:@"To"];
        case SingleBorderCrossingTwoSectionIndex:
            return [Localizer getLocalizedText:@"Border Crossing"];
        case SingleReturnSectionIndex:
            return [Localizer getLocalizedText:@"Return"];
    }
    return @"Placeholder";
}



- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if(self.isSingleDay)
    {
        return [self tableView:tableView numberOfRowsInSectionSingle:section];
    }
    return [self tableView:tableView numberOfRowsInSectionRegular:section];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSectionRegular:(NSInteger)section
{
    // Return the number of rows in the section.
    switch (section){
        case HeaderSectionIndex:
            return 1;
        case DayTripSectionIndex:
            return 1;
        case RegularFromSectionIndex:
            return 5;
        case RegularBorderCrossingSectionIndex:
            if(self.itineraryConfig != nil && !self.itineraryConfig.useBorderCrossTime)
            {
                return 0;
            }
            else
            {
                return 1;
            }
        case RegularToSectionIndex:
           return 5;
    }
    return 0;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSectionSingle:(NSInteger)section
{
    switch(section){
        case HeaderSectionIndex:
            return 1;
        case DayTripSectionIndex:
            return 1;
        case SingleFromSectionIndex:
            return 5;
        case SingleBorderCrossingOneSectionIndex:
            if(self.itineraryConfig != nil && !self.itineraryConfig.useBorderCrossTime)
            {
                return 0;
            }
            else{
                return 1;
            }
        case SingleToSectionIndex:
            return 6;
        case SingleBorderCrossingTwoSectionIndex:
            if(self.itineraryConfig != nil && !self.itineraryConfig.useBorderCrossTime)
            {
                return 0;
            }
            else{
                return 1;
            }
        case SingleReturnSectionIndex:
            return 1;
    }
    return 0;
}

-(NSString *)formatTimeHHmm:(NSDate *)input
{
    NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
    [dateFormat setDateFormat: @"HHmm"];

    // Mob-2568
    [dateFormat setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    // Localizing date
    [dateFormat setLocale:[NSLocale currentLocale]];

    NSString *startFormatted = [dateFormat stringFromDate:input];

    return startFormatted;
}

-(void) timeChangedDeparture:(id)sender
{
    NSLog(@"time changed sender = %@", sender);
    UIDatePicker *picker = (UIDatePicker *)sender;
    NSLog(@"picker.tag = %li", (long)picker.tag);
    NSLog(@"picker = %@", picker.date);

    if(picker.tag != nil)
    {
        NSDate *mergedDate = [ItineraryStopCell mergeDateTime:self.itineraryStop.departureDate mergeTime:picker.date];
        NSLog(@"mergedDate = %@", mergedDate);
        self.itineraryStop.departureDate = mergedDate;

        NSIndexPath *pickerRow = [NSIndexPath indexPathForRow:3 inSection:RegularFromSectionIndex];
        NSArray *indexArray = [NSArray arrayWithObjects:pickerRow, nil];
        [self.tableView reloadRowsAtIndexPaths:indexArray withRowAnimation:UITableViewRowAnimationNone];
    }
}



- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if(self.isSingleDay)
    {
        return [self tableView:tableView cellForRowAtIndexPathSingle:indexPath];
    }
    return [self tableView:tableView cellForRowAtIndexPathRegular:indexPath];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPathSingle:(NSIndexPath *)indexPath
{
    NSTimeZone *tz = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];

    // Get the two stops for a single day itinerary
    ItineraryStop *firstStop = (ItineraryStop *)[self.itinerary.stops firstObject];
    ItineraryStop *lastStop = (ItineraryStop *)[self.itinerary.stops lastObject];

    ItineraryStopCell *cell = nil;
    ItineraryStopCalendarTableViewCell *c = nil;
    if (indexPath.section == HeaderSectionIndex)
    {
        cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopHeaderText"];
        cell.headerText.text = [Localizer getLocalizedText:@"Add Single Day Itinerary Stop Header"];
    }
    if (indexPath.section == DayTripSectionIndex)
    {
        cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopSingleDay"];
        cell.dayTripLabel.text = [Localizer getLocalizedText:@"Day Trip"];
        [cell.dayTripSwitch setOn:self.isSingleDay];
    }
    else if (indexPath.section == SingleFromSectionIndex)
    {
        if (indexPath.row == From0CityIndex)
        {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ItinStopCity" forIndexPath:indexPath];
            cell.stopCity.text = firstStop.departureLocation;
            cell.whichStop = @"FROM";
        }
        else if (indexPath.row == From1DateIndex)
        {
            if([UIDevice isPad])
            {
                cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopDepartureDate" forIndexPath:indexPath];
                NSDateFormatter *timeFormatter= [ItineraryStopDetailViewController getItineraryDateFormatter];
                NSString *dateString = [timeFormatter stringFromDate:firstStop.departureDate];
                cell.departureDate.text = dateString;
            }
            else
            {
                cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopDateWithInput" forIndexPath:indexPath];
                if (firstStop.departureDate != nil)
                {
                    [self prepareDepartureDatePart:cell selectedFunction:[self getSingleDayDateChangedFunction] departureDate:firstStop.departureDate];
                }
                else
                {
                    cell.stopDate.text = @"";
                }
                cell.whichStop = @"FROM";
            }
        }
        else if (indexPath.row == From2DatePickerIndex)
        {
            c = (ItineraryStopCalendarTableViewCell *)[self.tableView dequeueReusableCellWithIdentifier:@"ItinStopCalendar" forIndexPath:indexPath];
            c.onDateSelected = [self getSingleDayDateChangedFunction];
            //TODO this seems like the wrong place to call this method
            [c.calendarView selectDate:firstStop.departureDate makeVisible:YES];
            c.clipsToBounds = YES;
            //What happens
            return c;
        }
        else if (indexPath.row == From3TimeInput)
        {
            cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput" forIndexPath:indexPath];
            if(firstStop.departureDate != nil)
            {
                [self prepareDepartureTime:indexPath tz:tz cell:cell date:firstStop.departureDate stop:firstStop];
            }
            else
            {
                cell.stopTime.text = @"";
            }

            cell.whichStop = @"FROM";
        }
        else if (indexPath.row == From4BIndex)
        {
            cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopDepartureDatePicker"];
            UIDatePicker *picker = cell.DepartureDatePicker;
            picker.timeZone = tz;
            NSDate *departureDate = firstStop.departureDate;
            if(departureDate != nil)
            {
                [picker setDate:departureDate animated:NO];
            }
        }
    }
    else if (indexPath.section == SingleBorderCrossingOneSectionIndex)
    {
        if (indexPath.row == BorderCrossing0DateIndex)
        {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput" forIndexPath:indexPath];
            if(firstStop.borderCrossDate != nil)
            {
                NSDate *date = firstStop.borderCrossDate;
                cell.itineraryStop = firstStop;

                [cell setCellTimeLabels:date];

                //Add the inputview
                UIToolbar *myToolbar = [[UIToolbar alloc] initWithFrame: CGRectMake(0,0, 320, 44)]; //should code with variables to support view resizing
                UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:cell action:@selector(inputAccessoryViewDidFinishFromTimeText)];
                //using default text field delegate method here, here you could call
                //myTextField.resignFirstResponder to dismiss the views
                [myToolbar setItems:[NSArray arrayWithObject: doneButton] animated:NO];
                cell.stopTimeText.inputAccessoryView = myToolbar;

                cell.timePicker = [[UIDatePicker alloc] initWithFrame:CGRectMake(0, 0, 320, 44)];
                cell.timePicker.datePickerMode = UIDatePickerModeTime;
                cell.timePicker.date = date;
                cell.timePicker.timeZone = tz;
                cell.timePicker.tag = indexPath.section;
                [cell.timePicker addTarget:cell action:@selector(timeChangedBorderCrossing:) forControlEvents:UIControlEventValueChanged];

                cell.stopTimeText.inputView = cell.timePicker;
            }
            else
            {
                cell.borderCrossingDate.text = @"";
            }
        }
    }
    else if (indexPath.section == SingleBorderCrossingTwoSectionIndex)
    {
        if (indexPath.row == BorderCrossing0DateIndex)
        {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput" forIndexPath:indexPath];
            if(lastStop.borderCrossDate != nil)
            {
                NSDate *date = lastStop.borderCrossDate;
                cell.itineraryStop = lastStop;

                [cell setCellTimeLabels:date];

                //Add the inputview
                UIToolbar *myToolbar = [[UIToolbar alloc] initWithFrame: CGRectMake(0,0, 320, 44)]; //should code with variables to support view resizing
                UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:cell action:@selector(inputAccessoryViewDidFinishFromTimeText)];
                //using default text field delegate method here, here you could call
                //myTextField.resignFirstResponder to dismiss the views
                [myToolbar setItems:[NSArray arrayWithObject: doneButton] animated:NO];
                cell.stopTimeText.inputAccessoryView = myToolbar;

                cell.timePicker = [[UIDatePicker alloc] initWithFrame:CGRectMake(0, 0, 320, 44)];
                cell.timePicker.datePickerMode = UIDatePickerModeTime;
                cell.timePicker.date = date;
                cell.timePicker.timeZone = tz;
                cell.timePicker.tag = indexPath.section;
                [cell.timePicker addTarget:cell action:@selector(timeChangedBorderCrossing:) forControlEvents:UIControlEventValueChanged];

                cell.stopTimeText.inputView = cell.timePicker;
            }
            else
            {
                cell.borderCrossingDate.text = @"";
            }
        }

    }
    else if (indexPath.section == SingleToSectionIndex)
    {
        if (indexPath.row == To0CityIndex)
        {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ItinStopCity" forIndexPath:indexPath];
            cell.stopCity.text = firstStop.arrivalLocation;

            cell.whichStop = @"TO";
        }
        else if (indexPath.row == To1DateIndex)
        {
            cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopDateWithInput" forIndexPath:indexPath];
            if(firstStop.arrivalDate != nil)
            {
                [self prepareArrivalDatePart:cell selectedFunction:[self getArrivalDateChangedFunction:RegularToSectionIndex itineraryStop:self.itineraryStop] arrivalDate:firstStop.arrivalDate];
            }
            else
            {
                cell.stopDate.text = @"";
            }
            cell.whichStop = @"TO";
        }
        else if (indexPath.row == To2DatePickerIndex)
        {
            c = (ItineraryStopCalendarTableViewCell *)[self.tableView dequeueReusableCellWithIdentifier:@"ItinStopCalendar" forIndexPath:indexPath];
            c.onDateSelected = [self getArrivalDateChangedFunction:SingleToSectionIndex itineraryStop:firstStop];
            //TODO this seems like the wrong place to call this method
            [c.calendarView selectDate:firstStop.arrivalDate makeVisible:YES];
            c.clipsToBounds = YES;
            //What happens
            return c;
        }
        else if (indexPath.row == To3TimeInput)
        {
            cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput" forIndexPath:indexPath];
            cell.stopLabel.text = [Localizer getLocalizedText:@"Arrival Time"];
            if(firstStop.arrivalDate != nil)
            {
                [self prepareArrivalTime:indexPath tz:tz cell:cell date:firstStop.arrivalDate stop:firstStop];
            }
            else
            {
                cell.stopTime.text = @"";
            }

            cell.whichStop = @"TO";
        }
        else if (indexPath.row == To4BIndex)
        {
            cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopArrivalDatePicker"];
            UIDatePicker *picker = cell.ArrivalDatePicker;
            picker.timeZone = tz;
            NSDate *arrivalDate = firstStop.arrivalDate;
            if(arrivalDate != nil)
            {
                [picker setDate:arrivalDate animated:NO];
            }
        }
        else if (indexPath.row == To5CIndex)
        {
            cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput" forIndexPath:indexPath];
            cell.stopLabel.text = [Localizer getLocalizedText:@"Departure Time"];
            if(lastStop.departureDate != nil)
            {
                [self prepareDepartureTime:indexPath tz:tz cell:cell date:lastStop.departureDate stop:lastStop];
            }
            else
            {
                cell.stopTime.text = @"";
            }

            cell.whichStop = @"FROM";
        }
    }
    else if (indexPath.section == SingleReturnSectionIndex)
    {
        if (indexPath.row == Return0TimeInputIndex)
        {
            cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput" forIndexPath:indexPath];
            if(lastStop.arrivalDate != nil)
            {
                [self prepareArrivalTime:indexPath tz:tz cell:cell date:lastStop.arrivalDate stop:lastStop];
            }
            else
            {
                cell.stopTime.text = @"";
            }

            cell.whichStop = @"TO";
        }
    }

    if(cell == nil)
    {
        cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopPlaceHolder"];
    }


    // Configure the cell...
    cell.clipsToBounds = YES;

    return cell;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPathRegular:(NSIndexPath *)indexPath
{
    NSTimeZone *tz = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];

    ItineraryStopCell *cell = nil;
    ItineraryStopCalendarTableViewCell *c = nil;
    if (indexPath.section == HeaderSectionIndex)
    {
        cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopHeaderText"];
        cell.headerText.text = [Localizer getLocalizedText:@"Add Itinerary Stop Header"];
    }
    if (indexPath.section == DayTripSectionIndex)
    {
        cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopSingleDay"];
        cell.dayTripLabel.text = [Localizer getLocalizedText:@"Day Trip"];
        [cell.dayTripSwitch setOn:self.isSingleDay];
    }
    else if (indexPath.section == RegularFromSectionIndex)
    {
        if (indexPath.row == From0CityIndex)
        {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ItinStopCity" forIndexPath:indexPath];
            cell.stopCity.text = self.itineraryStop.departureLocation;

            cell.whichStop = @"FROM";
        }
        else if (indexPath.row == From1DateIndex )
        {
            if([UIDevice isPad])
            {
                cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopDepartureDate" forIndexPath:indexPath];
                NSDateFormatter *timeFormatter= [ItineraryStopDetailViewController getItineraryDateFormatter];
                NSString *dateString = [timeFormatter stringFromDate:self.itineraryStop.departureDate];
                cell.departureDate.text = dateString;
            }
            else
            {
                cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopDateWithInput" forIndexPath:indexPath];
                if (self.itineraryStop.departureDate != nil)
                {
                    [self prepareDepartureDatePart:cell selectedFunction:[self getRegularDepartureDateChangedFunction] departureDate:self.itineraryStop.departureDate];
                }
                else
                {
                    cell.stopDate.text = @"";
                }
                cell.whichStop = @"FROM";
            }
        }
        else if (indexPath.row == From2DatePickerIndex)
        {
            c = (ItineraryStopCalendarTableViewCell *)[self.tableView dequeueReusableCellWithIdentifier:@"ItinStopCalendar" forIndexPath:indexPath];
            c.onDateSelected = [self getRegularDepartureDateChangedFunction];
            //TODO this seems like the wrong place to call this method
            [c.calendarView selectDate:self.itineraryStop.departureDate makeVisible:YES];
            c.clipsToBounds = YES;
            //What happens
            return c;
        }
        else if (indexPath.row == From3TimeInput)
        {
            cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput" forIndexPath:indexPath];
            if(self.itineraryStop.departureDate != nil)
            {
                NSDate *date = self.itineraryStop.departureDate;
                [self prepareDepartureTime:indexPath tz:tz cell:cell date:date stop:self.itineraryStop];

                if(self.itineraryStop.isRowFailureDateTime)
                {
                    cell.stopLabel.textColor = [UIColor redColor];
                }
            }
            else
            {
                cell.stopTime.text = @"";
            }

            cell.whichStop = @"FROM";
        }
        else if (indexPath.row == From4BIndex)
        {
            cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopDepartureDatePicker"];
            UIDatePicker *picker = cell.DepartureDatePicker;
            picker.timeZone = tz;
            NSDate *departureDate = self.itineraryStop.departureDate;
            if(departureDate != nil)
            {
                [picker setDate:departureDate animated:NO];

            }
        }
    }
    else if (indexPath.section == RegularBorderCrossingSectionIndex)
    {
        if (indexPath.row == BorderCrossing0DateIndex)
        {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput" forIndexPath:indexPath];
            if(self.itineraryStop.borderCrossDate != nil)
            {
                NSDate *date = self.itineraryStop.borderCrossDate;
                cell.itineraryStop = self.itineraryStop;

                [cell setCellTimeLabels:date];

                //Add the inputview
                UIToolbar *myToolbar = [[UIToolbar alloc] initWithFrame: CGRectMake(0,0, 320, 44)]; //should code with variables to support view resizing
                UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:cell action:@selector(inputAccessoryViewDidFinishFromTimeText)];
                //using default text field delegate method here, here you could call
                //myTextField.resignFirstResponder to dismiss the views
                [myToolbar setItems:[NSArray arrayWithObject: doneButton] animated:NO];
                cell.stopTimeText.inputAccessoryView = myToolbar;

                cell.timePicker = [[UIDatePicker alloc] initWithFrame:CGRectMake(0, 0, 320, 44)];
                cell.timePicker.datePickerMode = UIDatePickerModeTime;
                cell.timePicker.date = date;
                cell.timePicker.timeZone = tz;
                cell.timePicker.tag = indexPath.section;
                [cell.timePicker addTarget:cell action:@selector(timeChangedBorderCrossing:) forControlEvents:UIControlEventValueChanged];

                cell.stopTimeText.inputView = cell.timePicker;

                if(self.itineraryStop.isRowFailureBorderDateTime)
                {
                    cell.stopLabel.textColor = [UIColor redColor];
                }
            }
            else
            {
                cell.borderCrossingDate.text = @"";
            }
        }
        else if (indexPath.row == BorderCrossingDate1PickerIndex)
        {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ItinStopBorderCrossingDatePicker" forIndexPath:indexPath];
            UIDatePicker *picker = cell.BorderCrossingDatePicker;
            picker.timeZone = tz;
            NSDate *bcDate = self.itineraryStop.borderCrossDate;
            if(bcDate != nil)
            {
                [picker setDate:bcDate animated:NO];
            }
        }
    }
    else if (indexPath.section == RegularToSectionIndex)
    {
        if (indexPath.row == To0CityIndex)
        {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ItinStopCity" forIndexPath:indexPath];
            cell.stopCity.text = self.itineraryStop.arrivalLocation;

            cell.whichStop = @"TO";
        }
        else if (indexPath.row == To1DateIndex)
        {
            if([UIDevice isPad])
            {
                cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopArrivalDate" forIndexPath:indexPath];
                NSDateFormatter *timeFormatter= [ItineraryStopDetailViewController getItineraryDateFormatter];
                NSString *dateString = [timeFormatter stringFromDate:self.itineraryStop.arrivalDate];
                cell.arrivalDate.text = dateString;
            }
            else
            {
                cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopDateWithInput" forIndexPath:indexPath];
                if (self.itineraryStop.arrivalDate != nil)
                {
                    [self prepareArrivalDatePart:cell selectedFunction:[self getArrivalDateChangedFunction:RegularToSectionIndex itineraryStop:self.itineraryStop] arrivalDate:self.itineraryStop.arrivalDate];
                }
                else
                {
                    cell.stopDate.text = @"";
                }
                cell.whichStop = @"TO";
            }
        }
        else if (indexPath.row == To2DatePickerIndex)
        {
            c = (ItineraryStopCalendarTableViewCell *)[self.tableView dequeueReusableCellWithIdentifier:@"ItinStopCalendar" forIndexPath:indexPath];
            c.onDateSelected = [self getArrivalDateChangedFunction:RegularToSectionIndex itineraryStop:self.itineraryStop];
            //TODO this seems like the wrong place to call this method
            [c.calendarView selectDate:self.itineraryStop.arrivalDate makeVisible:YES];
            c.clipsToBounds = YES;
            //What happens
            return c;
        }
        else if (indexPath.row == To3TimeInput)
        {
            cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput" forIndexPath:indexPath];
            if(self.itineraryStop.arrivalDate != nil)
            {
                [self prepareArrivalTime:indexPath tz:tz cell:cell date:self.itineraryStop.arrivalDate stop:self.itineraryStop];
            }
            else
            {
                cell.stopTime.text = @"";
            }

            cell.whichStop = @"TO";
        }
        else if (indexPath.row == To4BIndex)
        {
            cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopArrivalDatePicker"];
            UIDatePicker *picker = cell.ArrivalDatePicker;
            picker.timeZone = tz;
            NSDate *arrivalDate = self.itineraryStop.arrivalDate;
            if(arrivalDate != nil)
            {
                [picker setDate:arrivalDate animated:NO];
            }
        }
    }

    if(cell == nil)
    {
        cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopPlaceHolder"];
    }


    // Configure the cell...
    cell.clipsToBounds = YES;

    return cell;

}

- (void)prepareArrivalTime:(NSIndexPath *)indexPath tz:(NSTimeZone *)tz cell:(ItineraryStopCell *)cell date:(NSDate *)date stop:(ItineraryStop *)stop
{
    cell.itineraryStop = stop;

    [cell setCellTimeLabels:date];

    //Add the inputview
    UIToolbar *myToolbar = [[UIToolbar alloc] initWithFrame: CGRectMake(0,0, 320, 44)]; //should code with variables to support view resizing
    UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:cell action:@selector(inputAccessoryViewDidFinishFromTimeText)];
    //using default text field delegate method here, here you could call
    //myTextField.resignFirstResponder to dismiss the views
    [myToolbar setItems:[NSArray arrayWithObject: doneButton] animated:NO];
    cell.stopTimeText.inputAccessoryView = myToolbar;

    cell.timePicker = [[UIDatePicker alloc] initWithFrame:CGRectMake(0, 0, 320, 44)];
    cell.timePicker.datePickerMode = UIDatePickerModeTime;
    cell.timePicker.date = date;
    cell.timePicker.timeZone = tz;
    cell.timePicker.tag = indexPath.section;
    [cell.timePicker addTarget:cell action:@selector(timeChangedArrival:) forControlEvents:UIControlEventValueChanged];

    cell.stopTimeText.inputView = cell.timePicker;
}


- (void)prepareDepartureTime:(NSIndexPath *)indexPath tz:(NSTimeZone *)tz cell:(ItineraryStopCell *)cell date:(NSDate *)date stop:(ItineraryStop *)stop
{
    cell.itineraryStop = stop;

    [cell setCellTimeLabels:date];

    //Add the inputview
    UIToolbar *myToolbar = [[UIToolbar alloc] initWithFrame: CGRectMake(0,0, 320, 44)]; //TODO should code with variables to support view resizing
    UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:cell action:@selector(inputAccessoryViewDidFinishFromTimeText)];
    //using default text field delegate method here, here you could call
    //myTextField.resignFirstResponder to dismiss the views
    [myToolbar setItems:[NSArray arrayWithObject: doneButton] animated:NO];
    cell.stopTimeText.inputAccessoryView = myToolbar;

    cell.timePicker = [[UIDatePicker alloc] initWithFrame:CGRectMake(0, 0, 320, 44)];
    cell.timePicker.datePickerMode = UIDatePickerModeTime;
    cell.timePicker.date = date;
    cell.timePicker.timeZone = tz;
    cell.timePicker.tag = indexPath.section;
    [cell.timePicker addTarget:cell action:@selector(timeChangedDeparture:) forControlEvents:UIControlEventValueChanged];

    cell.stopTimeText.inputView = cell.timePicker;
}

- (void)prepareDepartureDatePart:(ItineraryStopCell *)cell selectedFunction:(void (^)(NSDate *))selectedFunction departureDate:(NSDate *)departureDate
{
    NSDate *date = departureDate;
    [cell setCellDateLabels:date];

    cell.calendarView = [[CKCalendarView alloc] initWithStartDay:startSunday];
    cell.calendarView.allowMultipleSelection = NO;
    cell.calendarView.adaptHeightToNumberOfWeeksInMonth = NO;
    cell.calendarView.delegate = cell;
    [cell.calendarView selectDate:date makeVisible:YES];
    cell.stopDateText.inputView = cell.calendarView;
    cell.onDateSelected = selectedFunction;

    UIToolbar *myToolbar = [[UIToolbar alloc] initWithFrame: CGRectMake(0,0, 320, 44)]; //should code with variables to support view resizing
    UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:cell action:@selector(inputAccessoryViewDidFinishFromDateText)];
    //using default text field delegate method here, here you could call
    //myTextField.resignFirstResponder to dismiss the views
    [myToolbar setItems:[NSArray arrayWithObject: doneButton] animated:NO];
    cell.stopDateText.inputAccessoryView = myToolbar;
}

- (void)prepareArrivalDatePart:(ItineraryStopCell *)cell  selectedFunction:(void (^)(NSDate *))selectedFunction arrivalDate:(NSDate *)arrivalDate
{
    [cell setCellDateLabels:arrivalDate];

    cell.calendarView = [[CKCalendarView alloc] initWithStartDay:startSunday];
    cell.calendarView.allowMultipleSelection = NO;
    cell.calendarView.adaptHeightToNumberOfWeeksInMonth = NO;
    cell.calendarView.delegate = cell;
    [cell.calendarView selectDate:arrivalDate makeVisible:YES];
    cell.stopDateText.inputView = cell.calendarView;
    cell.onDateSelected = selectedFunction;

    UIToolbar *myToolbar = [[UIToolbar alloc] initWithFrame: CGRectMake(0,0, 320, 44)]; //should code with variables to support view resizing
    UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:cell action:@selector(inputAccessoryViewDidFinishFromDateText)];
    //using default text field delegate method here, here you could call
    //myTextField.resignFirstResponder to dismiss the views
    [myToolbar setItems:[NSArray arrayWithObject: doneButton] animated:NO];
    cell.stopDateText.inputAccessoryView = myToolbar;
}



+ (NSDateFormatter *)getItineraryTimeFormatter
{
    NSTimeZone *tz = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];
    NSDateFormatter *timeFormatter = [[NSDateFormatter alloc] init];
    [timeFormatter setDateFormat:@"hh:mm a"];
    [timeFormatter setTimeZone:tz];
    return timeFormatter;
}

+ (NSDateFormatter *)getItineraryDateFormatter
{
    NSTimeZone *tz = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd"];
    [dateFormatter setTimeZone:tz];

    return dateFormatter;
}



- (BOOL)hasPickerForIndexPath:(NSIndexPath *)indexPath
{
    BOOL hasDatePicker = false;

    /*NSInteger targetedRow = indexPath.row;
    targetedRow++;

    UITableViewCell *checkDatePickerCell = [self.tableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:targetedRow inSection:indexPath.section]];
    if(checkDatePickerCell == nil)
    {
        return false;
    }

    UIDatePicker *checkDatePicker = (UIDatePicker *)[checkDatePickerCell viewWithTag:99];
    hasDatePicker = (checkDatePicker != nil);
*/

    return hasDatePicker;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    if (section == HeaderSectionIndex || section == DayTripSectionIndex)
    {
        // Hide the first Section Header
        return 0;
    }
    else if (section == RegularBorderCrossingSectionIndex || section == SingleBorderCrossingOneSectionIndex || section == SingleBorderCrossingTwoSectionIndex)
    {
        if(self.itineraryConfig != nil && !self.itineraryConfig.useBorderCrossTime)
        {
            return 0;
        }
    }
    return tableView.sectionHeaderHeight;
}

- (void)tableView:(UITableView *)tableView willDisplayHeaderView:(UIView *)view forSection:(NSInteger)section
{
    UITableViewHeaderFooterView *header = (UITableViewHeaderFooterView *)view;

//    header.textLabel.textColor = [UIColor redColor];
    header.textLabel.font = [UIFont systemFontOfSize:16];
    header.tintColor = [UIColor colorWithRed:(247.0/255) green:(250.0/255) blue:(253.0/255) alpha:1.0];

}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if(self.isSingleDay)
    {
        return [self tableView:tableView heightForRowAtIndexPathSingle:indexPath];
    }
    return [self tableView:tableView heightForRowAtIndexPathRegular:indexPath];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPathSingle:(NSIndexPath *)indexPath
{
    CGFloat d = [super tableView:tableView heightForRowAtIndexPath:indexPath];
    UITableViewCell *cell;
    switch(indexPath.section)
    {
        case HeaderSectionIndex:
            if(self.showHeaderText)
            {
                cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopHeaderText"];
                d = cell.frame.size.height;
            }
            else
            {
                d = 0;
            }
            break;
        case DayTripSectionIndex:
            if([self showDayTripSwitch])
            {
                cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopSingleDay"];
                d = cell.frame.size.height;
            }
            else
            {
                d = 0;
            }
            break;
        case SingleFromSectionIndex:
            switch (indexPath.row)
            {
                case From0CityIndex:
                    cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopCity"];
                    d = cell.frame.size.height;
                    break;
                case From1DateIndex:
                    if([UIDevice isPad])
                    {
                        cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopDepartureDate"];
                        d = cell.frame.size.height;
                    }
                    else
                    {
                        cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopDateWithInput"];
                        d = cell.frame.size.height;
                    }
                    break;
                case From2DatePickerIndex:
                    d = 0;
                    if([UIDevice isPad])
                    {
                        ItineraryStop *firstStop = [self.itinerary.stops firstObject];
                        if(firstStop.showDepartureDateCalendar)
                        {
                            cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopCalendar"];
                            d = cell.frame.size.height;

                            CGRect rect = self.view.superview.bounds;
                            if (rect.size.width > 0)
                            {
                                d = rect.size.width;
                            }
                        }
                        else
                        {
                            d = 0;
                        }
                    }
                    break;
                case From3TimeInput:
                    cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput"];
                    d = cell.frame.size.height;
                    break;
                case From4BIndex:
                    cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopDepartureDatePicker"];
                    d = cell.frame.size.height;
                    d = 0;
                    break;
            }
            break;
        case SingleToSectionIndex:
            switch (indexPath.row)
            {
                case To0CityIndex:
                    cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopCity"];
                    d = cell.frame.size.height;
                    break;
                case To1DateIndex:
                    d = 0;
                    break;
                case To2DatePickerIndex:
                    cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopCalendar"];
                    d = cell.frame.size.height;
                    d = 0;
                    break;
                case To3TimeInput:
                    cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput"];
                    d = cell.frame.size.height;
                    break;
                case To4BIndex:
                    cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopArrivalDatePicker"];
                    d = cell.frame.size.height;
                    d = 0;
                    break;
                case To5CIndex:
                    cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput"];
                    d = cell.frame.size.height;
                    break;
            }
            break;
        case SingleBorderCrossingOneSectionIndex:
            switch (indexPath.row)
            {
                case BorderCrossing0DateIndex:
                    cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput"];
                    d = cell.frame.size.height;
                    break;
                default:
                    d = 100;
            }
            break;
        case SingleBorderCrossingTwoSectionIndex:
            switch (indexPath.row)
            {
                case BorderCrossing0DateIndex:
                    cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput"];
                    d = cell.frame.size.height;
                    break;
                default:
                    d = 100;
            }
            break;
        case SingleReturnSectionIndex:
            cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput"];
            d = cell.frame.size.height;
            break;
        default:

            break;
    }
    return d;
}

- (BOOL)showDayTripSwitch
{
//    if(self.itinerary != nil && self.itinerary.itinKey != nil)
//    {
//        return NO;
//    }

    if(self.itineraryStop != nil && self.itineraryStop.irKey != nil)  // They have already saved a stop
    {
        return NO;
    }

    return YES;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPathRegular:(NSIndexPath *)indexPath
{
    CGFloat d = [super tableView:tableView heightForRowAtIndexPath:indexPath];
    UITableViewCell *cell;
    switch (indexPath.section)
    {
        case HeaderSectionIndex:
            if(self.showHeaderText)
            {
                cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopHeaderText"];
                d = cell.frame.size.height;
            }
            else
            {
                d = 0;
            }
            break;
        case DayTripSectionIndex:
            if([self showDayTripSwitch])
            {
                cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopSingleDay"];
                d = cell.frame.size.height;
            }
            else
            {
                d = 0;
            }
            break;
        case RegularFromSectionIndex:
            switch (indexPath.row)
            {
                case From0CityIndex:
                    cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopCity"];
                    d = cell.frame.size.height;
                    break;
                case From1DateIndex:
                    if([UIDevice isPad])
                    {
                        cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopDepartureDate"];
                        d = cell.frame.size.height;
                    }
                    else
                    {
                        cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopDateWithInput"];
                        d = cell.frame.size.height;
                        NSLog(@"d = %f", d);
                        if(d == 0)
                        {
                            d = 64;  //TODO  This is getting zero'd when switching back from single day
                        }
                    }
                    break;
                case From2DatePickerIndex:
                    d = 0;

                    if([UIDevice isPad])
                    {
                        if(self.itineraryStop.showDepartureDateCalendar)
                        {

                            cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopCalendar"];
                            d = cell.frame.size.height;

                            CGRect rect = self.view.superview.bounds;
                            if (rect.size.width > 0)
                            {
                                d = rect.size.width;
                            }
                        }
                        else
                        {
                            d = 0;
                        }
                    }
                    break;
                case From3TimeInput:
                    cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput"];
                    d = cell.frame.size.height;
                    break;
                case From4BIndex:   // TODO REMOVE
//                    cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopDepartureDatePicker"];
                    d = 0;
                    break;
            }
            break;
        case RegularBorderCrossingSectionIndex:
            if (indexPath.row  == BorderCrossing0DateIndex)
            {
                cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput"];
                d = cell.frame.size.height;
            }
            else if(indexPath.row == BorderCrossingDate1PickerIndex) // TODO REMOVE
            {
//                cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopBorderCrossingDatePicker"];
                d = 0;
            }
            break;
        case RegularToSectionIndex:
            switch (indexPath.row)
            {
                case To0CityIndex:
                    cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopCity"];
                    d = cell.frame.size.height;
                    break;
                case To1DateIndex:
                    if([UIDevice isPad])
                    {
                        cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopArrivalDate"];
                        d = cell.frame.size.height;
                    }
                    else
                    {
                        cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopDateWithInput"];
                        d = cell.frame.size.height;
                        if(d == 0)
                        {
                            d = 64; //TODO  This is getting zero'd when switching back from single day
                        }
                    }
                    break;
                case To2DatePickerIndex:
                    d = 0;

                    if([UIDevice isPad])
                    {
                        if(self.itineraryStop.showArrivalDateCalendar)
                        {

                            cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopCalendar"];
                            d = cell.frame.size.height;

                            CGRect rect = self.view.superview.bounds;
                            if (rect.size.width > 0)
                            {
                                d = rect.size.width;
                            }
                        }
                        else
                        {
                            d = 0;
                        }
                    }
                    break;
                case To3TimeInput:
                    cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopTimeWithInput"];
                    d = cell.frame.size.height;
                    break;
                case To4BIndex:  //TODO Remove
//                    cell = [self.tableView dequeueReusableCellWithIdentifier:@"ItinStopArrivalDatePicker"];
                    d = 0;
                    break;
            }
            break;
        default:

            break;
    }

    return d;
}


- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView cellForRowAtIndexPath:indexPath];

    NSLog(@"didSelectRowAtIndexPath cell.reuseIdentifier = %@", cell.reuseIdentifier);

    if ([cell.reuseIdentifier isEqualToString:@"ItinStopDepartureDate"] )
    {
        self.itineraryStop.showDepartureDateCalendar = !self.itineraryStop.showDepartureDateCalendar;
        //TODO set the first date
        ItineraryStop *firstStop = (ItineraryStop *)[self.itinerary.stops firstObject];
        firstStop.showDepartureDateCalendar = !firstStop.showDepartureDateCalendar;


        [self displayInlineDatePickerForRowAtIndexPath:indexPath];
    }
    else if([cell.reuseIdentifier isEqualToString:@"ItinStopArrivalDate"] )
    {
        self.itineraryStop.showArrivalDateCalendar = !self.itineraryStop.showArrivalDateCalendar;

        [self displayInlineDatePickerForRowAtIndexPath:indexPath];
    }
    else if( [cell.reuseIdentifier isEqualToString:@"ItinStopBorderCrossingDate"])
    {
        if (EMBEDDED_DATE_PICKER)
        {
            [self displayInlineDatePickerForRowAtIndexPath:indexPath];
        }
//        else
//            [self displayExternalDatePickerForRowAtIndexPath:indexPath];
    }
    else
    {
        [tableView deselectRowAtIndexPath:indexPath animated:YES];
    }

}


/*! Adds or removes a UIDatePicker cell below the given indexPath.

 @param indexPath The indexPath to reveal the UIDatePicker.
 */
- (void)toggleDatePickerForSelectedIndexPath:(NSIndexPath *)indexPath
{
    NSArray *indexPaths = @[[NSIndexPath indexPathForRow:indexPath.row inSection:indexPath.section]];
    NSLog(@"toggle indexPath = %@", indexPath);
    if(indexPath.section == RegularFromSectionIndex)
    {
//        ItineraryStopCalendarTableViewCell *cell = (ItineraryStopCalendarTableViewCell *)[self.tableView dequeueReusableCellWithIdentifier:@"ItinStopCalendar" forIndexPath:indexPath];


    }
}

- (void)XXXtoggleDatePickerForSelectedIndexPath:(NSIndexPath *)indexPath
{
    [self.tableView beginUpdates];

    NSArray *indexPaths = @[[NSIndexPath indexPathForRow:indexPath.row inSection:indexPath.section]];
    NSLog(@"toggle indexPath = %@", indexPath);
    if(indexPath.section == RegularFromSectionIndex)
    {
        if(self.editingDepartureTime)
        {
            self.editingDepartureTime = false;
            NSLog(@"Remove Departure Picker");
            [self.tableView deleteRowsAtIndexPaths:indexPaths withRowAnimation:UITableViewRowAnimationFade];
        }
        else{
            self.editingDepartureTime = true;
            NSLog(@"Add Departure Picker");
            [self.tableView insertRowsAtIndexPaths:indexPaths withRowAnimation:UITableViewRowAnimationFade];
        }
    }
    else if(indexPath.section == RegularBorderCrossingSectionIndex)
    {
        if(self.editingBorderCrossingTime)
        {
            self.editingBorderCrossingTime = false;
            NSLog(@"Remove Arrival Picker");


            [self.tableView deleteRowsAtIndexPaths:indexPaths withRowAnimation:UITableViewRowAnimationFade];
        }
        else{
            self.editingBorderCrossingTime = true;
            NSLog(@"Add Arrival Picker");
            [self.tableView insertRowsAtIndexPaths:indexPaths withRowAnimation:UITableViewRowAnimationFade];
        }

    }
    else if(indexPath.section == RegularToSectionIndex)
    {
        if(self.editingArrivalTime)
        {
            self.editingArrivalTime = false;
            NSLog(@"Remove BC Picker");
            [self.tableView deleteRowsAtIndexPaths:indexPaths withRowAnimation:UITableViewRowAnimationFade];
        }
        else{
            self.editingArrivalTime = true;
            NSLog(@"Add Picker");
            [self.tableView insertRowsAtIndexPaths:indexPaths withRowAnimation:UITableViewRowAnimationFade];

//            dispatch_block_t to_do = ^{
//                [self.tableView scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionBottom animated:NO];
//            };
//            dispatch_sync(dispatch_get_main_queue(), to_do);


            //Make this work to show the bottom date picker
//            [self.tableView scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionBottom animated:NO];
        }
    }

    [self.tableView endUpdates];
}




- (void (^)(NSDate *))getRegularDepartureDateChangedFunction
{
    void (^onDateSelected)(NSDate *) = ^(NSDate *selectedDate)
    {
        NSLog(@"getRegularDepartureDateChangedFunction -----------------------------------------------------");
        NSDate *newDate = [ItineraryStopCell mergeDateTime:selectedDate mergeTime:self.itineraryStop.departureDate];
        NSLog(@"newDate = %@", newDate);

        self.itineraryStop.departureDate = newDate;

        NSIndexPath *dateRow = [NSIndexPath indexPathForRow:1 inSection:RegularFromSectionIndex];
        NSIndexPath *calendarRow = [NSIndexPath indexPathForRow:2 inSection:RegularFromSectionIndex];
        NSIndexPath *timeRow = [NSIndexPath indexPathForRow:3 inSection:RegularFromSectionIndex];
        NSIndexPath *pickerRow = [NSIndexPath indexPathForRow:4 inSection:RegularFromSectionIndex];
        NSArray *indexArray = [NSArray arrayWithObjects:dateRow, calendarRow, timeRow, pickerRow, nil];
        self.itineraryStop.showDepartureDateCalendar = NO;

        [self.tableView reloadRowsAtIndexPaths:indexArray withRowAnimation:UITableViewRowAnimationFade];


    };
    return onDateSelected;
}

- (void (^)(NSDate *))getSingleDayDateChangedFunction
{
    void (^onDateSelected)(NSDate *) = ^(NSDate *selectedDate)
    {
        NSLog(@"getSingleDayDateChangedFunction -----------------------------------------------------");
        NSLog(@"selectedDate = %@", selectedDate);

        ItineraryStop *firstStop = [self.itinerary.stops firstObject];
        ItineraryStop *lastStop = [self.itinerary.stops lastObject];

        firstStop.departureDate = [ItineraryStopCell mergeDateTime:selectedDate mergeTime:firstStop.departureDate];
        NSLog(@"firstStop.departureDate = %@", firstStop.departureDate);

        firstStop.arrivalDate = [ItineraryStopCell mergeDateTime:selectedDate mergeTime:firstStop.arrivalDate];
        NSLog(@"firstStop.arrivalDate = %@", firstStop.arrivalDate);

        firstStop.borderCrossDate = [ItineraryStopCell mergeDateTime:selectedDate mergeTime:firstStop.borderCrossDate];
        NSLog(@"firstStop.borderCrossDate = %@", firstStop.borderCrossDate);

        lastStop.departureDate = [ItineraryStopCell mergeDateTime:selectedDate mergeTime:lastStop.departureDate];
        NSLog(@"lastStop.departureDate = %@", lastStop.departureDate);

        lastStop.arrivalDate = [ItineraryStopCell mergeDateTime:selectedDate mergeTime:lastStop.arrivalDate];
        NSLog(@"lastStop.arrivalDate = %@", lastStop.arrivalDate);

        lastStop.borderCrossDate = [ItineraryStopCell mergeDateTime:selectedDate mergeTime:lastStop.borderCrossDate];
        NSLog(@"lastStop.borderCrossDate = %@", lastStop.borderCrossDate);


        NSIndexPath *dateRow = [NSIndexPath indexPathForRow:1 inSection:RegularFromSectionIndex];
        NSIndexPath *calendarRow = [NSIndexPath indexPathForRow:2 inSection:RegularFromSectionIndex];
        NSIndexPath *timeRow = [NSIndexPath indexPathForRow:3 inSection:RegularFromSectionIndex];
        NSIndexPath *pickerRow = [NSIndexPath indexPathForRow:4 inSection:RegularFromSectionIndex];
        NSArray *indexArray = [NSArray arrayWithObjects:dateRow, calendarRow, timeRow, pickerRow, nil];

        firstStop.showDepartureDateCalendar = NO;
        firstStop.showArrivalDateCalendar = NO;
        lastStop.showDepartureDateCalendar = NO;
        lastStop.showArrivalDateCalendar = NO;

        [self.tableView reloadRowsAtIndexPaths:indexArray withRowAnimation:UITableViewRowAnimationNone];
    };
    return onDateSelected;
}

- (void (^)(NSDate *))getArrivalDateChangedFunction:(int const)section itineraryStop:(ItineraryStop *)itineraryStop
{
    void (^onDateSelected)(NSDate *) = ^(NSDate *selectedDate)
    {
        NSLog(@"getArrivalDateChangedFunction -----------------------------------------------------");
        NSDate *newDate = [ItineraryStopCell mergeDateTime:selectedDate mergeTime:itineraryStop.arrivalDate];
        NSLog(@"newDate = %@", newDate);

        itineraryStop.arrivalDate = newDate;

        NSIndexPath *dateRow = [NSIndexPath indexPathForRow:1 inSection:section];
        NSIndexPath *calendarRow = [NSIndexPath indexPathForRow:2 inSection:section];
        NSIndexPath *timeRow = [NSIndexPath indexPathForRow:3 inSection:section];
        NSIndexPath *pickerRow = [NSIndexPath indexPathForRow:4 inSection:section];
        NSArray *indexArray = [NSArray arrayWithObjects:dateRow, calendarRow, timeRow, pickerRow, nil];

        self.itineraryStop.showArrivalDateCalendar = NO;

        [self.tableView reloadRowsAtIndexPaths:indexArray withRowAnimation:UITableViewRowAnimationNone];

    };
    return onDateSelected;
}

- (IBAction)departureDatePickerChangedAction:(id)sender
{
    NSLog(@"departureDatePickerChangedAction --------------------------------------------------");
    UIDatePicker *targetedDatePicker = sender;
    NSDate *pickerDate = targetedDatePicker.date;

    NSDate *newDate = [ItineraryStopCell mergeDateTime:self.itineraryStop.departureDate mergeTime:pickerDate];

    // Change the date in the stop
    self.itineraryStop.departureDate = newDate;

    NSIndexPath *dateRow = [NSIndexPath indexPathForRow:1 inSection:RegularFromSectionIndex];
    NSIndexPath *calendarRow = [NSIndexPath indexPathForRow:2 inSection:RegularFromSectionIndex];
    NSIndexPath *timeRow = [NSIndexPath indexPathForRow:3 inSection:RegularFromSectionIndex];
    NSIndexPath *pickerRow = [NSIndexPath indexPathForRow:4 inSection:RegularFromSectionIndex];
    NSArray *indexArray = [NSArray arrayWithObjects:dateRow, calendarRow, timeRow, pickerRow, nil];
    self.itineraryStop.showDepartureDateCalendar = NO;
    [self.tableView reloadRowsAtIndexPaths:indexArray withRowAnimation:UITableViewRowAnimationNone];

}



- (IBAction)arrivalDatePickerChangedAction:(id)sender
{
    NSLog(@"arrivalDatePickerChangedAction --------------------------------------------------");
    UIDatePicker *targetedDatePicker = sender;
    NSDate *pickerDate = targetedDatePicker.date;

    NSDate *newDate = [ItineraryStopCell mergeDateTime:self.itineraryStop.arrivalDate mergeTime:pickerDate];

    // Change the date in the stop
    self.itineraryStop.arrivalDate = newDate;

    NSIndexPath *dateRow = [NSIndexPath indexPathForRow:1 inSection:RegularToSectionIndex];
    NSIndexPath *calendarRow = [NSIndexPath indexPathForRow:2 inSection:RegularToSectionIndex];
    NSIndexPath *timeRow = [NSIndexPath indexPathForRow:3 inSection:RegularToSectionIndex];
    NSIndexPath *pickerRow = [NSIndexPath indexPathForRow:4 inSection:RegularToSectionIndex];
    NSArray *indexArray = [NSArray arrayWithObjects:dateRow, calendarRow, timeRow, pickerRow, nil];
    self.itineraryStop.showArrivalDateCalendar = NO;
    [self.tableView reloadRowsAtIndexPaths:indexArray withRowAnimation:UITableViewRowAnimationFade];

}

- (IBAction)borderCrossingDatePickerChangedAction:(id)sender
{
    NSLog(@"borderCrossingDatePickerChangedAction --------------------------------------------------");
    UIDatePicker *targetedDatePicker = sender;
    NSDate *pickerDate = targetedDatePicker.date;

    NSDate *newDate = [ItineraryStopCell mergeDateTime:self.itineraryStop.borderCrossDate mergeTime:pickerDate];

    // Change the date in the stop
    self.itineraryStop.borderCrossDate = newDate;

    NSIndexPath *timeRow = [NSIndexPath indexPathForRow:0 inSection:RegularBorderCrossingSectionIndex];
    NSIndexPath *pickerRow = [NSIndexPath indexPathForRow:1 inSection:RegularBorderCrossingSectionIndex];
    NSArray *indexArray = [NSArray arrayWithObjects:timeRow, pickerRow, nil];
    [self.tableView reloadRowsAtIndexPaths:indexArray withRowAnimation:UITableViewRowAnimationNone];

}

/*! Reveals the date picker inline for the given indexPath, called by "didSelectRowAtIndexPath".

 @param indexPath The indexPath to reveal the UIDatePicker.
 */
- (void)displayInlineDatePickerForRowAtIndexPath:(NSIndexPath *)indexPath
{
    // display the date picker inline with the table content

    NSLog(@"indexPath = %@", indexPath);
    // always deselect the row containing the start or end date
    [self.tableView deselectRowAtIndexPath:indexPath animated:NO];

    if ([self hasPickerForIndexPath:indexPath])
    {
        NSLog(@"Index has picker");
    }
    else
    {
        NSIndexPath *indexPathToReveal = [NSIndexPath indexPathForRow:(indexPath.row + 1) inSection:indexPath.section];
//        [self toggleDatePickerForSelectedIndexPath:indexPathToReveal];
//        [self.tableView reloadRowsAtIndexPaths:indexPathToReveal withRowAnimation:UITableViewRowAnimationFade];
        [self.tableView reloadSections:[NSIndexSet indexSetWithIndex:indexPathToReveal.section]  withRowAnimation:UITableViewRowAnimationFade];
    }

}

/*! Updates the UIDatePicker's value to match with the date of the cell above it.
 */
/*- (void)updateDatePicker:(NSIndexPath *)indexPath
{
    NSLog(@"updateDatePicker");
    ItineraryStopCell *associatedDatePickerCell = [self.tableView cellForRowAtIndexPath:indexPath];
    
    UILabel *departureDateLabel = associatedDatePickerCell.departureDate;
    NSString *departureDate = departureDateLabel.text;

    UIDatePicker *picker = associatedDatePickerCell.DepartureDatePicker;

    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyy-MM-dd"];
    NSDate *date = [formatter dateFromString:@"2013-05-13"];

    [picker setDate:date animated:NO];

}*/



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
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    NSLog(@"Seque Detail");

    if ([segue.identifier isEqualToString:@"ItineraryStopFromDone"])
    {
        // Save the changes, or should this be done in a prepare for segue
        NSLog(@"YYY segue.identifier = %@", segue.identifier);

    }
    else if ([segue.identifier isEqualToString:@"ItinStopDetailDepartureDatePicker"])
    {
        ItineraryStopDetailDateViewController *c = (ItineraryStopDetailDateViewController *)[segue destinationViewController];

        c.workingDate = self.itineraryStop.departureDate;
        c.whichDate = @"DEPARTURE";

//        UINavigationController * navController = [segue destinationViewController];
//        NSLog(@"navController = %@", navController);
//        ItineraryStopDetailDateViewController *destinationController = (ItineraryStopDetailDateViewController *)navController.topViewController;
//        UIViewController *controller = navController.topViewController;


        NSLog(@"YYY segue.identifier = %@", segue.identifier);
    }


}

- (NSDateFormatter *)getDateFormatter {
    static NSDateFormatter *dateFormatter = nil; // Caching- Creating a date formatter is not a cheap operation and this one does not depend on UserSettings so it is not going to change
    if (dateFormatter == nil) {
        NSLocale *enUSPOSIXLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];

        dateFormatter = [[NSDateFormatter alloc] init];
        [dateFormatter setLocale:enUSPOSIXLocale];
        [dateFormatter setTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]]; // GMT time Zone
        [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm"];
    }
    return dateFormatter;
}


- (CXRequest *)validateAndSaveItineraryRow:(ItineraryStop *)stop itinerary:(Itinerary *)itinerary
{
//    NSString *reportKey = self.paramBag[@"RECORD_KEY"];

    // Compose the path
    NSString *path = [NSString stringWithFormat:@"Mobile/TravelAllowance/ValidateAndSaveItinerary"];

    NSString *requestBody = nil;
    requestBody = [Itinerary composeItineraryElementWithRow:stop itinerary:itinerary formatter:[self getDateFormatter]];

    NSLog(@"requestBody = %@", requestBody);


    // Create the request
    CXRequest *cxRequest = [[CXRequest alloc] initWithServicePath:path requestXML:requestBody];

    return cxRequest;

}

- (CXRequest *)validateAndSaveItineraryAllRows:(Itinerary *)itinerary
{
    // Compose the path
    NSString *path = [NSString stringWithFormat:@"Mobile/TravelAllowance/ValidateAndSaveItinerary"];
    NSString *requestBody = nil;

    requestBody = [Itinerary composeItineraryWithRows:itinerary formatter:[self getDateFormatter]];

    NSLog(@"requestBody = %@", requestBody);
    // Create the request
    CXRequest *cxRequest = [[CXRequest alloc] initWithServicePath:path requestXML:requestBody];

    return cxRequest;
}

- (void)showItineraryStopError:(NSString *)statusResult localizedMessage:(NSString *)localizedMessage
{
    NSBlockOperation *op = [NSBlockOperation blockOperationWithBlock:^{
        [[AnalyticsManager sharedInstance] logCategory:@"Expense" withName:@"Itinerary Save"];

        NSString *message = localizedMessage;

        if(localizedMessage == nil)
        {
            // No Message was found
            //TravelAllowance.ItineraryRow.Error.InvalidArrivalDepartureTime
            NSArray *splits = [statusResult componentsSeparatedByString:@"."];
            if(splits != nil && [splits count]>0)
            {
                message = [splits lastObject];
            }
            else
            {
                message = statusResult;
            }
        }

        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Failed to save Itinerary Stop"]
                                                  message:message
                                                  delegate:nil
                                                  cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                                  otherButtonTitles:nil];

        [alert show];
    }];

    [[NSOperationQueue mainQueue] addOperation:op];
}

- (BOOL)validateItineraryStop:(Itinerary *)itinerary stop:(ItineraryStop *)stop singleDay:(BOOL)singleDay
{
    NSString *message = nil;
    
    // Check the itinerary
    if(singleDay)
    {
        //Leave Times to the server

        ItineraryStop *firstStop = [itinerary.stops firstObject];
        ItineraryStop *lastStop = [itinerary.stops lastObject];

        if(firstStop.departLnKey == nil) //Check Departure Location
        {
            message = @"Please select a starting location";
        }
        else if(firstStop.arrivalLnKey == nil) //Check Arrival Location
        {
            message = @"Please select a destination location";
        }
        else if(lastStop.departLnKey == nil) //Check Departure Location
        {
            message = @"Please select a destination location";
        }
        else if(lastStop.arrivalLnKey == nil) //Check Arrival Location
        {
            message = @"Please select a starting location";
        }
    }
    else
    {
        //TODO Checking for regular stop.
        if(stop.departLnKey == nil) //Check Departure Location
        {
            message = @"Please select a starting location";
        }
        else if(stop.arrivalLnKey == nil) //Check Arrival Location
        {
            message = @"Please select a destination location";
        }
    }

    if(message != nil)
    {

        NSBlockOperation *op = [NSBlockOperation blockOperationWithBlock:^{
            [[AnalyticsManager sharedInstance] logCategory:@"Expense" withName:@"Itinerary Save"];

            NSString *localizedMessage;

            localizedMessage = [Localizer getLocalizedText:message];
            if([localizedMessage isEqualToString:@"Localized string not available"])
            {
                // No Message was found
                //TravelAllowance.ItineraryRow.Error.InvalidArrivalDepartureTime
                localizedMessage = message;
            }


            UIAlertView *alert = [[UIAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Missing required information for Itinerary Stop"]
                                                            message:localizedMessage
                                                           delegate:nil
                                                  cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                                  otherButtonTitles:nil];

            [alert show];
        }];

        [[NSOperationQueue mainQueue] addOperation:op];
        return NO;
    }

    return YES;
}

- (void)showItineraryStopWarning:(NSString *)statusResult
{
    NSBlockOperation *op = [NSBlockOperation blockOperationWithBlock:^{
        [[AnalyticsManager sharedInstance] logCategory:@"Expense" withName:@"Itinerary Save"];

        NSString *localizedMessage;

        localizedMessage = [Localizer getLocalizedText:statusResult];
        if([localizedMessage isEqualToString:@"Localized string not available"])
        {
            // No Message was found
            //TravelAllowance.ItineraryRow.Error.InvalidArrivalDepartureTime
            NSArray *splits = [statusResult componentsSeparatedByString:@"."];
            if(splits != nil && [splits count]>0)
            {
                localizedMessage = [splits lastObject];
            }
            else
            {
                localizedMessage = statusResult;
            }
        }

        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Warning on Itinerary Stop update"]
                                                  message:localizedMessage
                                                  delegate:self
                                                  cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                                  otherButtonTitles:nil];

        [alert show];
    }];

    [[NSOperationQueue mainQueue] addOperation:op];
}

- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    [self completedItinerarySave:@{@"key" : @"it worked"}];
}

- (BOOL)shouldPerformSegueWithIdentifier:(NSString *)identifier sender:(id)sender {
    if([identifier isEqualToString:@"ItineraryStopFromDone"])       //Press the Save button
    {
        void (^success)(NSString *) = ^(NSString *result)
        {
            NSLog(@"result = %@", result);

            NSString *status = [ItineraryStop parseSaveResultForStatus:result];
            if([status isEqualToString:@"SUCCESS"])
            {
                NSMutableDictionary *mutableDictionary = [[NSMutableDictionary alloc] init];
                [mutableDictionary setValue:@"it worked" forKey:@"key"];
                [mutableDictionary setValue:self.itinerary.itinName forKey:@"itinName"];
                [mutableDictionary setValue:self.isSingleDay ? @"Y" : @"N" forKey:@"wasSingleDay"];

                if(self.isSingleDay)
                {
                    [AnalyticsTracker logEventWithCategory:@"Travel Allowance" eventAction:@"Save Single Day Itinerary" eventLabel:@"Success" eventValue:nil];
                }
                else
                {
                    [AnalyticsTracker logEventWithCategory:@"Travel Allowance" eventAction:@"Save Itinerary" eventLabel:@"Success" eventValue:nil];
                }

                //TODO success message to control whether to refresh the list.

                [self completedItinerarySave:mutableDictionary];
            }
            else if ([status isEqualToString:@"FAILURE"])
            {
                //Need to handle an error
                NSMutableArray *array = [Itinerary parseItinerariesXML:result rptKey:self.itinerary.rptKey crnCode:self.itinerary.crnCode];
                if(array != nil && [array count] == 1)
                {
                    Itinerary *returnedItinerary = [array firstObject];
                    NSMutableArray *returnedStops = returnedItinerary.stops;
                    if(self.itineraryStop != nil)
                    {
                        self.itineraryStop = [returnedStops firstObject];
                        //Reload
                        [self.tableView reloadData];
                    }
                }
                
                NSString *statusText = [ItineraryStop parseSaveResultForStatusText:result];
                NSString *statusTextLocalized = [ItineraryStop parseSaveResultForStatusTextLocalized:result];
                if (statusText != nil)
                {
                    [self showItineraryStopError:statusText localizedMessage:statusTextLocalized];
                    //TODO any clean up on the page
                }
            }else if ([status isEqualToString:@"WARNING"])
            {
                NSString *statusText = [ItineraryStop parseSaveResultForStatusText:result];
                if (statusText != nil)
                {
                    [self showItineraryStopWarning:statusText];
                }
            }
        };

        void (^failure)(NSError *) = ^(NSError *error) {
            NSLog(@"error = %@", error);
            NSString *errorMessage = @"There was an error processing your request. It may be due to a poor network connection. Please try again later. If this condition continues, please contact your administrator";
            [self showItineraryStopError:errorMessage localizedMessage:nil];
        };

        // Make the save call to the mws
        if(self.isSingleDay)
        {
            // Map the locations
            ItineraryStop *firstStop = (ItineraryStop *)[self.itinerary.stops firstObject];
            ItineraryStop *lastStop =  (ItineraryStop *)[self.itinerary.stops lastObject];
            lastStop.departLnKey = firstStop.arrivalLnKey;
            lastStop.arrivalLnKey = firstStop.departLnKey;

            BOOL valid = [self validateItineraryStop:self.itinerary stop:nil singleDay:YES];
            if(valid)
            {
                CXRequest *request = [self validateAndSaveItineraryAllRows:self.itinerary];
                [[CXClient sharedClient] performRequest:request success:success failure:failure];
            }
            else
            {
                return false;
            }
        }
        else
        {

            //TODO local validation
            BOOL valid = [self validateItineraryStop:self.itinerary stop:self.itineraryStop singleDay:NO];
            if(valid)
            {
                CXRequest *request = [self validateAndSaveItineraryRow:self.itineraryStop itinerary:self.itinerary];
                [[CXClient sharedClient] performRequest:request success:success failure:failure];
            }
            else
            {
                return false;
            }
        }

        // Prevent the segue
        return false;

    }
    else if([identifier isEqualToString:@"StopLocation"])
    {
        ItineraryStopCell *cell = (ItineraryStopCell *)sender;
//        NSLog(@"~~~identifier = %@", identifier);
//        NSLog(@"cell.whichStop = %@", cell.whichStop);

        ListItem *lastUsedLocation = [[MRUManager sharedInstance] getLastUsedLocation];
//        NSLog(@"lastUsedLocation = %@", lastUsedLocation.liName);

        if([cell.whichStop isEqualToString:@"FROM"])
        {
            NSString *lnKey = self.itineraryStop.departLnKey;

            // Display the Picker
            [self showListEditor:@"LnKey" label:[self getDepartureLocationPickerLabel] lnKey:lnKey];
        }
        else if ([cell.whichStop isEqualToString:@"TO"])
        {
            NSString *lnKey = self.itineraryStop.arrivalLnKey;

            // Display the Picker
            [self showListEditor:@"LnKey" label:[self getArrivalLocationPickerLabel] lnKey:lnKey];
        }
        return false;
    }
    else if([identifier isEqualToString:@"ArrivalLocation"])
    {
        return false;
    }


    return [super shouldPerformSegueWithIdentifier:identifier sender:sender];
}

- (NSString *)getDepartureLocationPickerLabel {
    return [Localizer getLocalizedText:@"Departure"];
}

- (NSString *)getArrivalLocationPickerLabel {
    return [Localizer getLocalizedText:@"Arrival"];
}


- (void)showListEditor:(NSString *)ret label:(NSString *)label lnKey:(NSString *)lnKey
{
    FormFieldData * fld = [[FormFieldData alloc] initField:ret label:label value:@"" ctrlType:@"edit" dataType:@"LOCATION"];

    fld.liKey = lnKey;

    ListFieldEditVC *lvc = nil;

    lvc = [[ListFieldEditVC alloc] initWithNibName:@"ListFieldEditVC" bundle:nil];

    [lvc setSeedData:fld delegate:self keysToExclude:nil];

    [lvc view];

//    lvc.searchText = currentValue;

    if([UIDevice isPad])
        lvc.modalPresentationStyle = UIModalPresentationFormSheet;

    [self.navigationController pushViewController:lvc animated:YES];

    lvc.sectionKeys = [[NSMutableArray alloc] initWithObjects: @"MAIN_SECTION", nil];
    lvc.sections = [[NSMutableDictionary alloc] initWithObjectsAndKeys: [self getListItems], @"MAIN_SECTION", nil];

    [lvc hideLoadingView];
}

-(void) fieldUpdated:(FormFieldData*) field
{
    NSLog(@"field.iD = %@", field.iD);

    NSLog(@"field.liKey = %@", field.liKey);

    NSLog(@"field.fieldValue = %@", field.fieldValue);

    NSLog(@"field.label = %@", field.label);

    ItineraryStop *stop = self.itineraryStop;

    if(self.isSingleDay)
    {
        stop = [self.itinerary.stops firstObject];
    }

//    Update the itinerary stop
    if([field.label isEqualToString:[self getDepartureLocationPickerLabel]])
    {
        stop.departLnKey = field.liKey;
        stop.departureLocation = field.fieldValue;
    }
    else if([field.label isEqualToString:[self getArrivalLocationPickerLabel]])
    {
        stop.arrivalLnKey = field.liKey;
        stop.arrivalLocation = field.fieldValue;
    }

    //Reload
    [self.tableView reloadData];
}


- (NSArray* )getListItems
{
    NSMutableArray* result = [[NSMutableArray alloc] init];
    BOOL addDepart = YES;
    BOOL addArrival = YES;

    // Get the MRU
    NSArray *locations = [[MRUManager sharedInstance] getLocations];
    for (ListItem *location in locations)
    {
        if([location.liKey isEqualToString:self.itineraryStop.departLnKey])
        {
            //Don't add the location to the list
            addDepart = NO;
        }

        if([location.liKey isEqualToString:self.itineraryStop.arrivalLnKey])
        {
            //Don't add the location to the list
            addArrival = NO;
        }
    }

    if(addDepart)
    {
//    Add the existing item
        ListItem * li = [[ListItem alloc] init];
        li.liKey = self.itineraryStop.departLnKey;
        li.liName = self.itineraryStop.departureLocation;
        [result addObject:li];
    }

    if(addArrival)
    {
        //    Add the existing item
        ListItem * arrivalli = [[ListItem alloc] init];
        arrivalli.liKey = self.itineraryStop.arrivalLnKey;
        arrivalli.liName = self.itineraryStop.arrivalLocation;
        [result addObject:arrivalli];
    }

    return result;
}

- (void)completedItinerarySave:(NSDictionary *)dictionary
{
    NSArray *viewControllers = self.navigationController.viewControllers;
    if(viewControllers != nil && [viewControllers count] > 1)
    {
        if(self.isSingleDay) // Pop back two steps when
        {
            NSInteger currentIndex = [viewControllers indexOfObject:self];
            if (currentIndex - 2 >= 0)
            {
                [self.navigationController popToViewController:[viewControllers objectAtIndex:currentIndex - 2] animated:YES];
            }
        }
        else // Not Single day
        {
            [self.navigationController popViewControllerAnimated:YES];
        }
    }
    else
    {
        [self dismissViewControllerAnimated:NO completion:nil]; // Dismiss the modal
    }

    if (self.onSuccessfulSave)
    {
        self.onSuccessfulSave(dictionary);
    }
    else{
        NSLog(@"No success handler defined");
    }
}

-(IBAction)unwindToList:(UIStoryboardSegue *)segue
{
    NSLog(@"segue.identifier = %@", segue.identifier);


}

-(IBAction)unwindToListFromPicker:(UIStoryboardSegue *)segue
{

    NSLog(@"unwindToListFromPicker - segue.identifier = %@", segue.identifier);
//    get the view controller from the segue and use it to update the date.

//    switch on the field that is being changed.
    ItineraryStopDetailDateViewController *source = (ItineraryStopDetailDateViewController *)[segue sourceViewController];
    NSLog(@"source.whichDate = %@", source.whichDate);

    if([source.whichDate isEqualToString:@"DEPARTURE"])
    {
        NSDate *dateFromPicker = source.workingDate;
        NSLog(@"dateFromPicker = %@", dateFromPicker);

        self.itineraryStop.departureDate = dateFromPicker;

    }


    //Reload
    [self.tableView reloadData];

}

- (IBAction)dayTripSwitchChanged:(id)sender
{
    UISwitch *sw = (UISwitch *)sender;

    if(sw.isOn)
    {
        // Switch to Day Trip
        self.isSingleDay = YES;

        //Store the current Itinerary and Stop
        self.itinerarySwitched = self.itinerary;

        // Need to change the Itinerary
        Itinerary *singleDayItinerary = [Itinerary getNewItineraryForSingleDay:self.itineraryConfig itineraryName:[Itinerary getReportName:self.paramBag] rptKey:[Itinerary getRptKey:self.paramBag]];

        self.itinerary = singleDayItinerary;

    }
    else
    {
        // Switch to Regular Trip
        self.isSingleDay = NO;

        //Switch back to the old Itinerary
        if(self.itinerarySwitched != nil)
        {
            self.itinerary = self.itinerarySwitched;
        }
        else
        {
            // Create a new Itinerary
            [Itinerary getNewItineraryRegular:self.itineraryConfig reportName:[Itinerary getReportName:self.paramBag] rptKey:[Itinerary getRptKey:self.paramBag]];
        }

        // Create a new empty stop
        [ItineraryStop getNewStop:self.itineraryConfig itinerary:self.itinerary];

    }
    //Reload
    [self.tableView reloadData];

}

-(void)tableView:(UITableView *)tableView willDisplayCell:(UITableViewCell *)cell forRowAtIndexPath:(NSIndexPath *)indexPath{
    NSLog(@"indexPath = %@", indexPath);
    if([tableView respondsToSelector:@selector(setSeparatorInset:)])
    {
        [tableView setSeparatorInset:UIEdgeInsetsZero];
    }
    if([tableView respondsToSelector:@selector(setLayoutMargins:)])
    {
        [tableView setLayoutMargins:UIEdgeInsetsZero];
    }
    if([cell respondsToSelector:@selector(setLayoutMargins:)])
    {
        [cell setLayoutMargins:UIEdgeInsetsZero];
    }
}




@end
