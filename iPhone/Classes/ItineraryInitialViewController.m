//
//  ItineraryInitialViewController.m
//  ConcurMobile
//
//  Created by Wes Barton on 5/7/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ItineraryInitialViewController.h"
#import "ItineraryInformationCell.h"
#import "WaitViewController.h"
#import "Itinerary.h"
#import "CXClient.h"
#import "ItineraryCell.h"
#import "ItineraryTableViewController.h"
#import "ItineraryImportViewController.h"
#import "ItineraryStopViewController.h"
#import "ItineraryConfig.h"
#import "ItineraryStopDetailViewController.h"
#import "AnalyticsManager.h"

@interface ItineraryInitialViewController ()

@end

@implementation ItineraryInitialViewController

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

    UIBarButtonItem *button = self.navigationItem.rightBarButtonItem;
    [button setEnabled:NO];

    if(self.hasCloseButton)
    {
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:self action:@selector(actionBack:)];
        self.navigationItem.leftBarButtonItem = btnClose;
    }

    [self loadItineraryData];
    [self loadTAConfig];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source



- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
#warning Incomplete method implementation.
    // Return the number of rows in the section.
    if(section == 0) // Info section
    {
        return 3;
    }
    else if (section == 1)
    {
        if(self.itineraries != nil) {
            NSUInteger numberOfItineraries = [self.itineraries count];
            if(numberOfItineraries > 0)
            {
                UIBarButtonItem *button = self.navigationItem.rightBarButtonItem;
                [button setEnabled:YES];
            }
            return numberOfItineraries;
        }
        else
        {
            return 0;
        }
    }

    return 0;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *retCell = nil;
    if(indexPath.section == 0)
    {
        ItineraryInformationCell *cell;
        if(indexPath.row == 0)
        {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ItineraryInformation" forIndexPath:indexPath];
        }
        else if(indexPath.row == 1)
        {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ImportItineraryCell" forIndexPath:indexPath];
        }
        else if (indexPath.row == 2)
        {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ManualAddItinerary" forIndexPath:indexPath];
        }
        else if (indexPath.row == 3)
        {
            cell = [tableView dequeueReusableCellWithIdentifier:@"SingleDayItinerary" forIndexPath:indexPath];
        }
        cell.messageText.userInteractionEnabled = NO;

        retCell = cell;
    }
    else if (indexPath.section == 1) // Itineraries
    {
        Itinerary *itinerary = (Itinerary *)[self.itineraries objectAtIndex:indexPath.row];
        ItineraryCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ItineraryCellForInitial" forIndexPath:indexPath];

        cell.clipsToBounds = YES;
        cell.itineraryName.text = itinerary.itinName;
        cell.itinerary = itinerary;
        cell.itineraryConfig = self.itineraryConfig;

        [ItineraryCell composeItineraryDateRange:itinerary cell:cell format:@"MMM dd, YYYY"];

        retCell = cell;
    }

    // Configure the cell...
    
    return retCell;
}

- (CGFloat)tableView:(UITableView *)tableView estimatedHeightForRowAtIndexPath:(NSIndexPath *)indexPath
{
     return 100;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if(indexPath.section == 0)
    {
        if(self.itineraries != nil && [self.itineraries count]>0)
        {
            return 0;
        }

        UITableViewCell *cell;
        if(indexPath.row == 0)
        {
            if([ExSystem is8Plus])
            {
                return UITableViewAutomaticDimension;
            }
            cell = [tableView dequeueReusableCellWithIdentifier:@"ItineraryInformation"];
        }
        else if(indexPath.row == 1)
        {
            BOOL canImportTrips = [[ExSystem sharedInstance] siteSettingCanImportTrips];
            if(canImportTrips)
            {
                if([ExSystem is8Plus])
                {
                    return UITableViewAutomaticDimension;
                }
                cell = [tableView dequeueReusableCellWithIdentifier:@"ImportItineraryCell"];
            }
            else
            {
                return 0;
            }
        }
        else if (indexPath.row == 2)
        {
            if([ExSystem is8Plus])
            {
                return UITableViewAutomaticDimension;
            }
            cell = [tableView dequeueReusableCellWithIdentifier:@"ManualAddItinerary"];
        }
        else if (indexPath.row == 3)
        {
            cell = [tableView dequeueReusableCellWithIdentifier:@"SingleDayItinerary"];
        }

        if(cell != nil)
        {
            return [cell bounds].size.height;
        }
    }
    else if (indexPath.section == 1)
    {
        if([ExSystem is8Plus])
        {
            return UITableViewAutomaticDimension;
        }

        UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ItineraryCellForInitial"];
        return [cell bounds].size.height;
    }
    return 0;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 2;
}

/*
- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    if(section == 1)
    {
        return [super tableView:tableView viewForHeaderInSection:section];
    }

    ItineraryInformationCell *cell = (ItineraryInformationCell *)[tableView dequeueReusableCellWithIdentifier:@"ItineraryInformation"];

    // Configure the cell...
    cell.clipsToBounds = YES;

    return cell;
}
*/

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 0;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    if (section == 1)
    {
        return @"Trips";
    }
    return @"Placeholder";
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
    NSLog(@"#######segue.identifier = %@", segue.identifier);
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    if([segue.identifier isEqualToString:@"SelectItineraryFromInitial"])
    {
//        id o = segue.destinationViewController;
//        NSLog(@"-------------o = %@", o);
        ItineraryTableViewController *destination = (ItineraryTableViewController *)segue.destinationViewController;
        destination.paramBag = self.paramBag;
        destination.role = self.role;
    }
    else if([segue.identifier isEqualToString:@"SelectItineraryFromInitialToStop"])
    {
        ItineraryCell *cell = (ItineraryCell *)sender;
        ItineraryStopViewController *destination = (ItineraryStopViewController *)segue.destinationViewController;
        destination.paramBag = self.paramBag;
        destination.role = self.role;
        if(cell.itinerary.itinKey == nil)
        {
            destination.itinerary = cell.itinerary;
        }
        else
        {
            destination.selectedItinKey = cell.itinerary.itinKey;
        }

        [destination setOnSuccessfulSaveOfSingleDay:^(NSDictionary *dictionary)
        {
            NSLog(@"dictionary = %@", dictionary);

            //Reload the itineraries.
            //TODO what about errors on save.
            [self loadItineraryData];
            //TODO need to reload the config?
        }];

    }
    else if ([segue.identifier isEqualToString:@"ItineraryImportFromInitial"])
    {
//        ItineraryImportViewController *destination = (ItineraryImportViewController *)[segue.destinationViewController topViewController];
        ItineraryImportViewController *destination = (ItineraryImportViewController *)segue.destinationViewController;
        destination.paramBag = self.paramBag;
        destination.role = self.role;
        destination.itineraryConfig = self.itineraryConfig;
        if(self.itineraries && [self.itineraries count]>0)
        {
            destination.currentItinerary = [self.itineraries objectAtIndex:0];
        }
        else
        {
            // Create the dummy itinerary here?
        }

        [destination setOnSuccessfulImport:^(NSDictionary *dictionary) {
            [self.navigationController popViewControllerAnimated:YES];
//            [self updateTableView];

            [self loadItineraryData];
            [self loadTAConfig];
//            [self.tableView reloadData];

        }];

        [destination setOnFailedImport:^(NSMutableArray *failedItineraries)
        {
            [self.navigationController popViewControllerAnimated:YES];

            self.itineraries = failedItineraries;
            [self.tableView reloadData];


            NSString *statusMessage = nil;
            for (Itinerary *itinerary in failedItineraries)
            {
                for (ItineraryStop *stop in itinerary.stops)
                {
                    if(stop.isFailed)
                    {
                        statusMessage = stop.statusTextLocalized;
                        break;
                    }
                }
            }

            // TODO Needs better content
            [self showItineraryImportError:nil localizedMessage:statusMessage];
            
            NSIndexPath *now = [NSIndexPath indexPathForItem:0 inSection:1];
            UITableViewCell *cell = [self.tableView cellForRowAtIndexPath:now];
//            [self performSegueWithIdentifier:@"SelectItineraryFromInitialToStop" sender:cell];
            NSLog(@"now.row = %i", now.row);

        }];


        NSLog(@"##########################segue = %@", segue);
    }
    else if ([segue.identifier isEqualToString:@"ManuallyAddItinerary"])
    {
        ItineraryStopViewController *destination = (ItineraryStopViewController *)segue.destinationViewController;
        destination.paramBag = self.paramBag;
        destination.role = self.role;

        [destination setOnSuccessfulSaveOfSingleDay:^(NSDictionary *dictionary)
        {
            NSLog(@"dictionary = %@", dictionary);

            //Reload the itineraries.
            //TODO what about errors on save.

            [self loadItineraryData];
            //TODO need to reload the config?
        }];


    }
    else if ([segue.identifier isEqualToString:@"ManuallyAddItineraryFromButton"])
    {
        //Create new Itinerary
        Itinerary *itinerary = [Itinerary getNewItineraryRegular:self.itineraryConfig reportName:[Itinerary getReportName:self.paramBag] rptKey:[Itinerary getRptKey:self.paramBag]];

        //Create an empty ItineraryStop to pass in;
        ItineraryStop *stopFirst = [[ItineraryStop alloc] init];
        [itinerary.stops addObject:stopFirst];

        [ItineraryStop defaultDepartureLocationForStop:self.itineraryConfig stop:stopFirst];
        [ItineraryStop defaultDatesForStop:stopFirst];
        stopFirst.arrivalLocation = [Localizer getLocalizedText:@"Destination Location"];

        ItineraryStopDetailViewController *destination = (ItineraryStopDetailViewController *)segue.destinationViewController;
        destination.paramBag = self.paramBag;
        //        destination.role = self.role;
        destination.isSingleDay = NO;
        destination.itinerary = itinerary;
        destination.itineraryStop = [itinerary.stops firstObject];
        destination.itineraryConfig = self.itineraryConfig;
        destination.showHeaderText = YES;

        [destination setOnSuccessfulSave:^(NSDictionary *dictionary) {
            NSLog(@"dictionary = %@", dictionary);
//            [self dismissViewControllerAnimated:YES completion:nil];
            [self.navigationController popViewControllerAnimated:YES];
//            [self updateTableView];
            [self loadItineraryData];
        }];
    }
    else if ([segue.identifier isEqualToString:@"SingleDayItinerary"])
    {
        //Create new Itinerary
        Itinerary *itinerary = [Itinerary getNewItineraryForSingleDay:self.itineraryConfig itineraryName:[Itinerary getReportName:self.paramBag] rptKey:[Itinerary getRptKey:self.paramBag]];

        ItineraryStopDetailViewController *destination = (ItineraryStopDetailViewController *)segue.destinationViewController;
        destination.paramBag = self.paramBag;
//        destination.role = self.role;
        destination.isSingleDay = YES;
        destination.itinerary = itinerary;
        destination.itineraryConfig = self.itineraryConfig;
        destination.showHeaderText = YES;

        [destination setOnSuccessfulSave:^(NSDictionary *dictionary) {
            NSLog(@"dictionary = %@", dictionary);

            [self dismissViewControllerAnimated:YES completion:nil];
//            [self updateTableView];
        }];
    }

}



-(void)loadItineraryData {
    NSLog(@"LoadItineraryData");

    [WaitViewController showWithText:@"Get Itineraries" animated:YES];

    NSString *rptKey = [Itinerary getRptKey:self.paramBag];
    NSString *crnCode = self.paramBag[@"CrnCode"];

    NSLog(@"rptKey = %@", rptKey);

    void (^success)(NSString *) = ^(NSString *result)
    {
        NSLog(@"getTAItinerariesRequest : result = %@", result);

        // Parse the response
        self.itineraries = [Itinerary parseItinerariesXML:result rptKey:rptKey crnCode:crnCode];

        NSLog(@"####self.itineraries = %@", self.itineraries);

        if(self.itineraries != nil )
        {
            for (Itinerary *itinerary in self.itineraries) {
                if(itinerary.stops != nil && [itinerary.stops count] > 0)
                {
//                    [self.AllowanceBarButton setEnabled:YES];
                }
            }
        }

        [self.tableView reloadData];
        [self bothComplete];
//        [WaitViewController hideAnimated:YES withCompletionBlock:nil];

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
        NSLog(@"error = %@", error);
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
        NSLog(@"error = %@", error);
//        [self showError];
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
//            [self showError];
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
//    if(!self.itineraries && self.itineraryConfig != nil )
//    {
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
//    }
//
//    if(self.itinerary.stops != nil && self.itineraryConfig != nil)
//    {
//        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
//    }
}


-(IBAction)unwindFromImportItinerary:(UIStoryboardSegue *)segue
{
    NSLog(@"XXXXXX - segue = %@", segue);

}

// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    if(indexPath.section == 1) {
        // Get the data object
        Itinerary *itinerary = (Itinerary *)[self.itineraries objectAtIndex:indexPath.row];
        if([itinerary.areAllRowsLocked isEqualToString:@"Y"])
        {
            return NO;
        }
        // Return NO if you do not want the specified item to be editable.
        return YES;
    }else
    {
        return NO;
    }
}

// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSLog(@"editingStyle = %d", editingStyle);
    if (editingStyle == UITableViewCellEditingStyleDelete)
    {
        UITableView *tView = self.tableView;

        ItineraryCell *cell = [tView cellForRowAtIndexPath:indexPath];
        Itinerary *itinerary = cell.itinerary;

        void (^success)(NSString *) = ^(NSString *result)
        {                                                                        
            BOOL *success = [Itinerary wasDeleteItinerarySuccessful:result];
            NSLog(@"Delete Stop : success = %p", success);
            if(success) {
                // Reload Table
                [tView beginUpdates];

                [self.itineraries removeObjectAtIndex:indexPath.row];

                NSArray *arg = @[indexPath];
                [tView deleteRowsAtIndexPaths:arg withRowAnimation:UITableViewRowAnimationFade];

                [tView endUpdates];
            }
        };

        void (^failure)(NSError *) = ^(NSError *error) {
//            [self showError];    //TODO error
            NSLog(@"error = %@", error);
        };

        if(itinerary != nil && itinerary.itinKey != nil)
        {
            CXRequest *request = [Itinerary deleteItinerary:itinerary.itinKey rptKey:itinerary.rptKey];
//            CXRequest *request = [Itinerary unassignItinerary:itinerary.itinKey rptKey:itinerary.rptKey];
            [[CXClient sharedClient] performRequest:request success:success failure:failure];
        }
        else{
            // How do you get here without local storage of the itin and no network
        }
    }
}

-(IBAction)unwindToList:(UIStoryboardSegue *)segue
{
    NSLog(@"Stop - unwindToList - segue.identifier = %@", segue.identifier);

//    ItineraryStopDetailViewController *source = [segue sourceViewController];

//    if([segue.identifier isEqualToString:@"ItineraryStopFromCancel"])
//    {
//        Discard
//    }
//    else if ([segue.identifier isEqualToString:@"ItineraryStopFromDone"])
//    {
//        Save the changes, or should this be done in a prepare for segue
//
//    }

    //Reload
    [self.tableView reloadData];

}

- (void)showItineraryImportError:(NSString *)statusResult localizedMessage:(NSString *)localizedMessage
{
    NSBlockOperation *op = [NSBlockOperation blockOperationWithBlock:^{
        [[AnalyticsManager sharedInstance] logCategory:@"Expense" withName:@"Itinerary Import"];

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

        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Failed to import Itinerary"]
                                              message:message
                                              delegate:nil
                                              cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                              otherButtonTitles:nil];

        [alert show];
    }];

    [[NSOperationQueue mainQueue] addOperation:op];
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
