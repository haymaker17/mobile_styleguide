//
//  ItinerarySummaryViewController.m
//  ConcurMobile
//
//  Created by Wes Barton on 6/18/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ItinerarySummaryViewController.h"
#import "ItineraryConfig.h"
#import "Itinerary.h"
#import "ItineraryCell.h"
#import "CXClient.h"

@interface ItinerarySummaryViewController ()

@end

@implementation ItinerarySummaryViewController

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
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;

//    NSLog(@"self.itinerary.itinName = %@", self.itinerary.itinName);
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
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    return 3;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
//    NSLog(@"//////indexPath = %@", indexPath);
    ItineraryCell *cell = nil;
    if(indexPath.row == 0) {
        cell = [tableView dequeueReusableCellWithIdentifier:@"ItineraryName" forIndexPath:indexPath];

        // Configure the cell...
        cell.itineraryNameEdit.text = self.itinerary.itinName;

        UIToolbar *myToolbar = [[UIToolbar alloc] initWithFrame: CGRectMake(0,0, cell.contentView.frame.size.width, 44)]; // TODO is this necessary to support view resizing?  Doesn't seem to matter
        UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:cell action:@selector(inputAccessoryViewDidFinishinputDidItineraryName)];
        [myToolbar setItems:[NSArray arrayWithObject: doneButton] animated:NO];
        cell.itineraryNameEdit.inputAccessoryView = myToolbar;


    }
    else if(indexPath.row == 1)
    {
        cell = [tableView dequeueReusableCellWithIdentifier:@"ShortDistanceTrip" forIndexPath:indexPath];
        BOOL shortDistance = [self.itinerary.shortDistanceTrip boolValue];
        cell.itineraryExtendedTripSwitch.on = shortDistance;

    }
    else if(indexPath.row == 2)
    {
        cell = [tableView dequeueReusableCellWithIdentifier:@"ItineraryTripLength" forIndexPath:indexPath];

        cell.tripLengthLabel.text = self.itineraryConfig.tripLengthListLabel;

        NSString *tripLength = self.itinerary.tripLength;
        NSString *displayValue = [self.itineraryConfig.tripLengthListValues valueForKey:tripLength];
        cell.tripLengthValue.text = displayValue;


        UIToolbar *myToolbar = [[UIToolbar alloc] initWithFrame: CGRectMake(0,0, 320, 44)]; //should code with variables to support view resizing
        UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:cell action:@selector(inputAccessoryViewDidFinish)];
        //using default text field delegate method here, here you could call
        //myTextField.resignFirstResponder to dismiss the views
        [myToolbar setItems:@[doneButton] animated:NO];
        cell.tripLengthValue.inputAccessoryView = myToolbar;

        cell.tripLengthPickerView = [[UIPickerView alloc] initWithFrame:CGRectMake(0, 0, 320, 44)];
        cell.tripLengthPickerView.delegate = cell;
//        if(allowance.isLocked)
//        {
//            cell.mealSelectedValue.enabled = NO;
//        }

        cell.tripLengthValue.inputView = cell.tripLengthPickerView;
        cell.onTripLengthSelected = ^(NSString *selectedValue)
        {
            self.itinerary.tripLength = selectedValue;

            // TODO this seems to be clearing the firstresponder, so the done button is superflous
            NSIndexPath *pickerRow = [NSIndexPath indexPathForRow:indexPath.row inSection:indexPath.section];
            NSArray *indexArray = [NSArray arrayWithObjects: pickerRow, nil];
            [self.tableView reloadRowsAtIndexPaths:indexArray withRowAnimation:UITableViewRowAnimationFade];
        };

        cell.itineraryConfig = self.itineraryConfig;
        cell.itinerary = self.itinerary;
        if(tripLength != nil) {
            [ItineraryCell setTripLengthPickerDefault:self.itinerary cell:cell selectedValue:tripLength];
        }

    }
    cell.itineraryConfig = self.itineraryConfig;
    cell.itinerary = self.itinerary;
    cell.clipsToBounds = YES;
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView estimatedHeightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 100;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if(indexPath.row == 0)
    {
        if([ExSystem is8Plus])
        {
            return UITableViewAutomaticDimension;
        }

        ItineraryCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ItineraryName"];

        return [cell bounds].size.height;
    }
    else if(indexPath.row == 1)
    {
        if(self.itineraryConfig.useShortDistance == YES) {
            if([ExSystem is8Plus])
            {
                return UITableViewAutomaticDimension;
            }

            ItineraryCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ShortDistanceTrip"];
            return [cell bounds].size.height;
        }
        return 0;
    }
    else if(indexPath.row == 2)
    {
        if(self.itineraryConfig.tripLengthList == YES) {
            if([ExSystem is8Plus])
            {
                return UITableViewAutomaticDimension;
            }

            ItineraryCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ItineraryTripLength"];
            return [cell bounds].size.height;
        }
        return 0;
    }

    return 100;
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
    } else if (editingStyle == UITableViewCellEditingStyleInsert) {
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

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

- (BOOL)shouldPerformSegueWithIdentifier:(NSString *)identifier sender:(id)sender
{
    if([identifier isEqualToString:@"ItineraryStopFromSummaryDone"])
    {
        NSLog(@"shouldPerformSegueWithIdentifier=======identifier = %@", identifier);
        void (^success)(NSString *) = ^(NSString *result)
        {
            NSString *status = [Itinerary parseSaveResultForStatus:result];
            if([status isEqualToString:@"SUCCESS"])
            {
                [self completedItinerarySave];
            }
            else if ([status isEqualToString:@"FAILURE"])
            {
                NSString *statusText = [ItineraryStop parseSaveResultForStatusText:result];
                if (statusText != nil)
                {
//                    [self showItineraryStopError:statusText];
                    //TODO any clean up on the page

                }
            }else if ([status isEqualToString:@"WARNING"])
            {
                NSString *statusText = [ItineraryStop parseSaveResultForStatusText:result];
                if (statusText != nil)
                {
//                    [self showItineraryStopWarning:statusText];
                }
            }
        };

        void (^failure)(NSError *) = ^(NSError *error) {
            NSLog(@"error = %@", error);
            NSString *errorMessage = @"There was an error processing your request. It may be due to a poor network connection. Please try again later. If this condition continues, please contact your administrator";
//            [self showItineraryStopError:errorMessage];
        };

        // Make the save call to the mws
        CXRequest *request = [self validateAndSaveItinerarySummary];
        [[CXClient sharedClient] performRequest:request success:success failure:failure];

        // Prevent the segue
        return false;
    }

    return [super shouldPerformSegueWithIdentifier:identifier sender:sender];
}

- (CXRequest *)validateAndSaveItinerarySummary
{
    // Compose the path
    NSString *path = [NSString stringWithFormat:@"Mobile/TravelAllowance/ValidateAndSaveItinerary"];

    NSString *requestBody = nil;
    if(self.itinerary != nil)
    {
        requestBody = [Itinerary composeUpdateItinerarySummary:self.itinerary formatter:[self getDateFormatter]];
    }

    NSLog(@"requestBody = %@", requestBody);


    // Create the request
    CXRequest *cxRequest = [[CXRequest alloc] initWithServicePath:path requestXML:requestBody];

    return cxRequest;
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

- (void)completedItinerarySave {
    NSLog(@"Factory Save");
    if (self.onSuccessfulSave)
    {
        self.onSuccessfulSave(@{@"key":@"it worked"});
    }
    else{
        NSLog(@"No success handler defined");
    }
}
- (IBAction)changedItineraryName:(id)sender {
    UITextField *itineraryNameEdit = (UITextField *)sender;

    NSString *text = itineraryNameEdit.text;
    NSLog(@"itineraryNameEdit.text = %@", text);
    self.itinerary.itinName = text;

}
- (IBAction)changedShortDistanceSwitch:(id)sender {
    UISwitch *sw = (UISwitch *)sender;

    if(sw.on)
    {
        self.itinerary.shortDistanceTrip = @"Y";
    }
    else{
        self.itinerary.shortDistanceTrip = @"N";
    }

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
