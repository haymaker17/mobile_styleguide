//
//  ItineraryTableViewController.m
//  ConcurMobile
//
//  Created by Wes Barton on 3/27/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ItineraryTableViewController.h"
#import "WaitViewController.h"
#import "CXRequest.h"
#import "ItineraryStop.h"
#import "CXClient.h"
#import "AnalyticsManager.h"
#import "Itinerary.h"
#import "ItineraryCell.h"
#import "ItineraryStopViewController.h"
#import "ItineraryConfig.h"
#import "ItineraryStopCell.h"
#import "ItineraryStopDetailViewController.h"
#import "ItineraryAllowanceAdjustmentViewController.h"
#import "ItineraryImport.h"
#import "CTEBadge.h"

@interface ItineraryTableViewController ()

@property NSMutableArray *itineraries;

@end

@implementation ItineraryTableViewController

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


//    [self.navBar setTitle:[Localizer getLocalizedText:@"Itinerary List"]];
    [self.navBar setTitle:[Localizer getLocalizedText:@"Allowance Itinerary"]];
    [self.AllowanceBarButton setEnabled:NO];

    //Move the network call here
    self.itineraries = [[NSMutableArray alloc]init];

    self.role = self.paramBag[@"ROLE"];

    [self loadItineraryData];
    [self loadTAConfig];

}



- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];

}


- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
#warning Potentially incomplete method implementation.
    // Return the number of sections.
    return [self.itineraries count];
}

- (void) headerTappedHandler:(UIGestureRecognizer *)gestureRecognizer
{
    ItineraryCell *cell = gestureRecognizer.view;
    [self performSegueWithIdentifier:@"SelectItinerary" sender:cell];
}

- (void) headerTappedHandlerExpandCollapse:(UIGestureRecognizer *)gestureRecognizer
{
    ItineraryCell *cell = gestureRecognizer.view;
    NSLog(@"cell.tag = %li", (long)cell.tag);

    Itinerary *itinerary = (Itinerary *)[self.itineraries objectAtIndex:cell.tag];

    itinerary.isCollapsed = !itinerary.isCollapsed;

    [self.tableView reloadSections:[NSIndexSet indexSetWithIndex:cell.tag] withRowAnimation:UITableViewRowAnimationNone];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    Itinerary *itinerary = (Itinerary *)[self.itineraries objectAtIndex:section];
    if(itinerary.isCollapsed)
    {
        return 1;
    }

    // Return the number of rows in the section.
    NSUInteger count = [itinerary.stops count] + 1;

//    if(![Itinerary isApproving:self.role])
//    {
//        count++;
//    }

    return count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    ItineraryStopCell *cell = nil;

    Itinerary *itinerary = (Itinerary *)[self.itineraries objectAtIndex:indexPath.section];

    if(indexPath.row == 0)
    {
        //Make this the header row
        //    return [super tableView:tableView viewForHeaderInSection:section];
        Itinerary *itinerary = (Itinerary *)[self.itineraries objectAtIndex:indexPath.section];

        ItineraryCell *cell = (ItineraryCell *)[tableView dequeueReusableCellWithIdentifier:@"ItineraryListCell"];

        // Configure the cell...
        cell.itineraryName.text = itinerary.itinName;
        cell.numberOfStops.text = [NSString stringWithFormat:@"%lu Stops", (unsigned long)[itinerary.stops count]];

        //TODO Config for the Approver, not the target
//        NSString *tripLengthValue = [self.itineraryConfig.tripLengthListValues valueForKey:itinerary.tripLength];
//        cell.itineraryTripLength.text = tripLengthValue;
        //TODO This needs to point at the config of the employee being approved, so  we'll leave it blank for now.
        cell.itineraryTripLength.text = @"";

        [ItineraryCell composeItineraryDateRange:itinerary cell:cell format:@"MMM dd"];

        UIImage *plus = [UIImage imageNamed:@"icon_expand_arrow"];
        UIImage *minus = [UIImage imageNamed:@"icon_collapse_arrow"];

        if(itinerary.isCollapsed)
        {
            cell.expandedIndicatorImage.image = plus;
        }else
        {
            cell.expandedIndicatorImage.image = minus;
        }

        cell.itinerary = itinerary;
        cell.clipsToBounds = YES;
        cell.tag = indexPath.section;

        BOOL isApproving = [Itinerary isApproving:self.role];
        if(isApproving)
        {
            // Approving the report
            UITapGestureRecognizer *singleTapRecogniser = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(headerTappedHandlerExpandCollapse:)];
            [singleTapRecogniser setDelegate:self];
            singleTapRecogniser.numberOfTouchesRequired = 1;
            singleTapRecogniser.numberOfTapsRequired = 1;
            [cell addGestureRecognizer:singleTapRecogniser];

        }
        else
        {
            //  TODO  Remove this to skip the extra screen
            UITapGestureRecognizer *singleTapRecogniser = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(headerTappedHandler:)];
            [singleTapRecogniser setDelegate:self];
            singleTapRecogniser.numberOfTouchesRequired = 1;
            singleTapRecogniser.numberOfTapsRequired = 1;
            [cell addGestureRecognizer:singleTapRecogniser];

            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
        }

        return cell;


    }
    else
    {
        NSInteger stopTranslation = indexPath.row - 1;

        ItineraryStop *stop = [itinerary.stops objectAtIndex:stopTranslation];

        cell = [tableView dequeueReusableCellWithIdentifier:@"ItinStop2ProtoCell" forIndexPath:indexPath];
        cell.itineraryStop = stop;
        cell.itineraryIndex = indexPath.section;

        [cell.stopBadge updateBadgeCount:stop.stopNumber];
//        int xxx = [stop.stopNumber intValue] + [@100 intValue];
//        [cell.stopBadge updateBadgeCount:[NSNumber numberWithInteger:(xxx)]];

        [cell.stopBadge updateBadgeColor:[UIColor colorWithRed:0.0/255.0 green:120.0/255.0 blue:200.0/255.0 alpha:1]];

        cell.departureCity.text = stop.departureLocation;
        cell.arrivalCity.text = stop.arrivalLocation;

        cell.arrivalDate.text = @"2014 01 23";
        cell.departureDate.text = @"2014 01 23";

        cell.stopNumber.text = [stop.stopNumber stringValue];

        cell.stopLabel.text = [Localizer getLocalizedText:@"Stop"];
        cell.FromLabel.text = [Localizer getLocalizedText:@"From"];
        cell.ToLabel.text = [Localizer getLocalizedText:@"To"];
        cell.RateLocationLabel.text = [Localizer getLocalizedText:@"Rate Location:"];


        //TODO change this
//    NSMutableString *departureDate = [[NSMutableString alloc] initWithString:[NSString stringWithFormat:@"%@: ",[@"Approve by" localize]]];
        NSString *departureDate = nil;
        if (stop.departureDate == nil)
        {
            departureDate = [@"No date specified" localize];
        }
        else
        {
            departureDate =[self formatDateForStop:stop.departureDate];
        }
        cell.departureDate.text = departureDate;    // Need to format the text output

        NSString *arrivalDate = nil;
        if (stop.arrivalDate == nil)
        {
            arrivalDate =[@"No date specified" localize];
        }
        else
        {
//        [arrivalDate appendString:[NSString stringWithFormat:@"%@ %@ %@",[DateTimeFormatter formatDateEEEByDate:stop.arrivalDate],[DateTimeFormatter formatDateMediumByDate:stop.arrivalDate TimeZone:[NSTimeZone localTimeZone]],[DateTimeFormatter formatDate:stop.arrivalDate Format:([DateTimeFormatter userSettingsPrefers24HourFormat]?@"HH:mm zzz":@"hh:mm aaa zzz") TimeZone:[NSTimeZone localTimeZone]]]]; // Change to appropriate Deadline message
            arrivalDate =[self formatDateForStop:stop.arrivalDate];
        }
        cell.arrivalDate.text = arrivalDate;    // Need to format the text output

        cell.arrivalRateLocation.text = stop.arrivalRateLocation;

    }

    cell.clipsToBounds = YES;
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView estimatedHeightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 86;
}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    ItineraryStopCell  *cell = nil;
    if(indexPath.row == 0)
    {
        if([ExSystem is8Plus])
        {
            return UITableViewAutomaticDimension;
        }
        cell = (ItineraryCell *)[tableView dequeueReusableCellWithIdentifier:@"ItineraryListCell"];
    }
    else
    {
        if([ExSystem is8Plus])
        {
            return UITableViewAutomaticDimension;
        }
        cell = [tableView dequeueReusableCellWithIdentifier:@"ItinStop2ProtoCell"];
    }
    return [cell bounds].size.height;
}

- (NSIndexPath *)tableView:(UITableView *)tableView willSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    Itinerary *itinerary = (Itinerary *)[self.itineraries objectAtIndex:indexPath.section];
    ItineraryStop *stop = [itinerary.stops objectAtIndex:indexPath.row - 1];
    if (stop.rowLocked)
    {
        return nil;
    }

    return indexPath;
}

-(NSString *)formatDateForStop:(NSDate *)input
{
    NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
    [dateFormat setDateFormat: @"MMM dd - hh:mm a"];

    // Mob-2568
    [dateFormat setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    // Localizing date
    [dateFormat setLocale:[NSLocale currentLocale]];

    NSString *startFormatted = [dateFormat stringFromDate:input];

    return startFormatted;
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

#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].

    // Pass the selected object to the new view controller.
    if([segue.identifier isEqualToString:@"SelectItinerary"])
    {
        ItineraryStopViewController *destinationController = (ItineraryStopViewController *)[segue destinationViewController];

        destinationController.paramBag = self.paramBag;


        ItineraryCell *cell = (ItineraryCell *)sender;
        if(cell != nil)
        {
            destinationController.selectedItinKey = cell.itinerary.itinKey;
        }
    }
    else if ([segue.identifier isEqualToString:@"SkipToItinStopSegue"])
    {
        ItineraryStopViewController *destinationController = (ItineraryStopViewController *)[segue destinationViewController];

        destinationController.paramBag = self.paramBag;
    }
    else if([segue.identifier isEqualToString:@"AllowanceAdjustmentsMergedSegue"])
    {
        UINavigationController * navController = [segue destinationViewController];
        ItineraryAllowanceAdjustmentViewController *destinationController = (ItineraryAllowanceAdjustmentViewController *)navController.topViewController;



        destinationController.rptKey = [Itinerary getRptKey:self.paramBag];
        destinationController.crnCode = self.paramBag[@"CrnCode"];
        destinationController.role = self.role;


        [destinationController setOnSuccessfulSave:^(NSDictionary *dictionary) {

            [self dismissViewControllerAnimated:YES completion:nil];
//            [self updateTableView];
            [self loadItineraryData];
        }];
    }
    else if([segue.identifier isEqualToString:@"SelectStopSegue"])
    {
        ItineraryStopCell *cell = (ItineraryStopCell *)sender;

        UINavigationController * navController = [segue destinationViewController];

        ItineraryStopDetailViewController *destinationController = (ItineraryStopDetailViewController *)navController.topViewController;

        Itinerary *itinerary = (Itinerary *)[self.itineraries objectAtIndex:cell.itineraryIndex];

        destinationController.showHeaderText = ([itinerary.stops count] < 2); //Remove the header text if there are already two stops

        destinationController.itinerary = itinerary;

        destinationController.itineraryStop = cell.itineraryStop;

        destinationController.paramBag = self.paramBag;

        destinationController.itineraryConfig = self.itineraryConfig;
        [destinationController setOnSuccessfulSave:^(NSDictionary *dictionary) {
            [self dismissViewControllerAnimated:YES completion:nil];
//            [self updateTableView];
//            [self.tableView reloadData];
            [self loadItineraryData];
        }];

    }
    else if ([segue.identifier isEqualToString:@"AddItineraryFromCellSegue"])
    {
        ItineraryStopCell *cell = (ItineraryStopCell *)sender;

        Itinerary *itinerary = (Itinerary *)[self.itineraries objectAtIndex:cell.itineraryIndex];

        UINavigationController * navController = [segue destinationViewController];
        ItineraryStopDetailViewController *destinationController = (ItineraryStopDetailViewController *)navController.topViewController;


        destinationController.showHeaderText = ([itinerary.stops count] < 2); //Remove the header text if there are already two stops
//        destinationController.showHeaderText = true; //Remove the header text if there are already two stops

        //Is there an itinerary, or are we creating the first stop
        if(itinerary == nil)
        {
            //Create a placeholder
            itinerary = [[Itinerary alloc]init];

            itinerary.itinName = [Itinerary getReportName:self.paramBag];
            itinerary.rptKey = [Itinerary getRptKey:self.paramBag];
            // Todo Is this thread safe?
            itinerary.tacKey = self.itineraryConfig.tacKey;

//            destinationController.itineraryConfig = self.itineraryConfig;
        }

        destinationController.itinerary = itinerary;
        destinationController.itineraryConfig = self.itineraryConfig;

        destinationController.paramBag = self.paramBag;

        [destinationController setOnSuccessfulSave:^(NSDictionary *dictionary) {
            [self dismissViewControllerAnimated:YES completion:nil];
//            [self updateTableView];
//            [self.tableView reloadData];
            [self loadItineraryData];
        }];


        // Are there any stops
        BOOL hasItineraryStops = [itinerary.stops count] > 0;


        //Create an empty ItineraryStop to pass in;
        ItineraryStop *stop = [[ItineraryStop alloc] init];
        stop.departureLocation = @"Default Departure";
        stop.arrivalLocation = [Localizer getLocalizedText:@"Destination Location"];


        // set default dates
        NSDate *now = [NSDate date];

        NSTimeZone *localTz = [NSTimeZone systemTimeZone];
        NSInteger secondsFromGMT = [localTz secondsFromGMTForDate:now];

        now = [NSDate dateWithTimeInterval:secondsFromGMT sinceDate:now];


        stop.departureDate = now;
        stop.arrivalDate = now;
        stop.borderCrossDate = now;

        destinationController.itineraryStop = stop;


    }
}

-(void)loadItineraryData {

    [WaitViewController showWithText:@"Get Itineraries" animated:YES];

    NSString *rptKey = self.paramBag[@"RECORD_KEY"];
    NSString *crnCode = self.paramBag[@"CrnCode"];


    void (^success)(NSString *) = ^(NSString *result)
    {

        //clear the existing entries
//        [self.itineraryStops removeAllObjects];

        // Parse the response
        self.itineraries = [Itinerary parseItinerariesXML:result rptKey:rptKey crnCode:crnCode];


        if(self.itineraries != nil )
        {
            for (Itinerary *itinerary in self.itineraries) {
                if(itinerary.stops != nil && [itinerary.stops count] > 0)
                {
                    [self.AllowanceBarButton setEnabled:YES];
                }
            }

        }

        [self.tableView reloadData];
        [self bothComplete];

        if([self.itineraries count] == 0)
        {
            NSLog(@"No Itineraries");

        }
        else if([self.itineraries count] < 2)
        {
            NSLog(@"Only one itinerary, forward to stop list");
//            [self performSelector:@selector(forwardToStops) withObject:nil afterDelay:0.5];
        }

    };

    void (^failure)(NSError *) = ^(NSError *error) {
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
        [self showError];
    };

    CXRequest *request = [Itinerary getTAItinerariesRequest:rptKey roleCode:self.role];
    [[CXClient sharedClient] performRequest:request success:success failure:failure];
}

-(void)loadTAConfig
{
    if(self.itineraryConfig == nil)
    {
        void (^success)(NSString *) = ^(NSString *result)
        {
//            sleep(5);
            // Parse the reponse
            NSMutableArray *configs = [ItineraryConfig parseTAConfigXML:result];
            if([configs count] == 1)
            {
                id itineraryConfig = [configs objectAtIndex:0];

                self.itineraryConfig = itineraryConfig;

                [self bothComplete];

            }
            else
            {
                // Handle missing config
                // They shouldn't have been able to get here without a config
            }

        };

        void (^failure)(NSError *) = ^(NSError *error) {
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            [self showError];
        };

        CXRequest *request = [ItineraryConfig getTAConfig];
        [[CXClient sharedClient] performRequest:request success:success failure:failure];
    }else
    {
        NSLog(@"ItineraryConfig already loaded");
    }
}

- (void)bothComplete
{
    NSLog(@"---------bothComplete-----------");
    if(self.itineraries != nil && self.itineraryConfig != nil)
    {
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
    }
}

-(IBAction)unwindToList:(UIStoryboardSegue *)segue
{

    ItineraryStopDetailViewController *source = [segue sourceViewController];

    if([segue.identifier isEqualToString:@"ItineraryStopFromCancel"])
    {
        // Discard
    }
    else if ([segue.identifier isEqualToString:@"ItineraryStopFromDone"])
    {
        // Save the changes, or should this be done in a prepare for segue

    }

    //Reload
//    [self.tableView reloadData];
    [self loadItineraryData];
}

-(IBAction)unwindToListFromAllowanceAdjustment:(UIStoryboardSegue *)segue
{

    //Reload
    [self loadItineraryData];
}


-(IBAction)unwindFromAllowanceAdjustment:(UIStoryboardSegue *)segue
{
    NSLog(@"unwindFromAllowanceAdjustment - segue = %@", segue);

}




- (void) forwardToStops
{
    [self performSegueWithIdentifier:@"SkipToItinStopSegue" sender:self];
}



- (void)showError {
    NSBlockOperation *op = [NSBlockOperation blockOperationWithBlock:^{
        [[AnalyticsManager sharedInstance] logCategory:@"Expense" withName:@"Itinerary Lookup"];

        /*[UIView animateWithDuration:0.25 animations:^{
            self.waitView.alpha = 0;
        }];

        [self.activityIndicator stopAnimating];
        [self.waitView removeFromSuperview];*/

        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Error retrieving Itineraries"
                                                        message:@"Sorry! Something went wrong. Please try again later."
                                                       delegate:nil
                                              cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                              otherButtonTitles:nil];

        [alert show];
    }];

    [[NSOperationQueue mainQueue] addOperation:op];
}
- (IBAction)addButtonPressed:(id)sender
{
    UIActionSheet *sheet = [[UIActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:nil destructiveButtonTitle:nil otherButtonTitles:nil];
    NSInteger index = [sheet addButtonWithTitle:@"One"];
    [sheet addButtonWithTitle:@"Two"];
    [sheet addButtonWithTitle:@"Three"];

    sheet.cancelButtonIndex = [sheet addButtonWithTitle:@"Cancel"];

    [sheet showInView:self.view];

}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    NSLog(@"actionSheet = %@", actionSheet);
    NSLog(@"buttonIndex = %li", (long)buttonIndex);

}

-(CGFloat)tableView:(UITableView*)tableView heightForHeaderInSection:(NSInteger)section
{
    return 1.0;
}

-(CGFloat)tableView:(UITableView*)tableView heightForFooterInSection:(NSInteger)section
{
    return 10.0;
}

-(UIView*)tableView:(UITableView*)tableView viewForHeaderInSection:(NSInteger)section
{
    return [[UIView alloc] initWithFrame:CGRectMake(0, 0, 0, 0)];
}

-(UIView*)tableView:(UITableView*)tableView viewForFooterInSection:(NSInteger)section
{
    return [[UIView alloc] initWithFrame:CGRectMake(0, 0, 0, 0)];
}




@end
