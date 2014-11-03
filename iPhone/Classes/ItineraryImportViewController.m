//
//  ItineraryImportViewController.m
//  ConcurMobile
//
//  Created by Wes Barton on 5/7/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ItineraryImportViewController.h"
#import "CXClient.h"
#import "ItineraryImport.h"
#import "WaitViewController.h"
#import "ItineraryImportCell.h"
#import "ItineraryImportRow.h"
#import "ItineraryConfig.h"
#import "ItineraryStop.h"
#import "Itinerary.h"
#import "AnalyticsTracker.h"

@interface ItineraryImportViewController ()

@end

@implementation ItineraryImportViewController

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

    [AnalyticsTracker initializeScreenName:@"Import Travel Itinerary"];
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;

    if(self.currentItinerary == nil)
    {
        //create a default Itinerary to add stops to
        NSLog(@"~~~~Create a default Itinerary");
        self.currentItinerary = [[Itinerary alloc]init];
        self.currentItinerary.stops = [[NSMutableArray alloc] init];

        self.currentItinerary.itinName = [Itinerary getReportName:self.paramBag];
        self.currentItinerary.rptKey = [Itinerary getRptKey:self.paramBag];

        NSLog(@"========self.currentItinerary.rptKey = %@", self.currentItinerary.rptKey);

        // Todo Is this thread safe?
        self.currentItinerary.tacKey = self.itineraryConfig.tacKey;

    }


    [WaitViewController showWithText:@"Get Itineraries" animated:YES];
    [self loadImportData];

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
//    NSInteger count = 1;
//    count ++;
//    NSLog(@"----------------[self.imports count] = %u", [self.imports count]);
//    return [self.imports count] + 1;
//    return count;
    if([self.imports count] >0)
    {
        return 2;
    }
    else
    {
        return 1;
    }
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    if(section == 0)
    {
        return @"Message";
    }
    else
    {
        return @"Trips";
    }
//    ItineraryImport *header = (ItineraryImport *)[self.imports objectAtIndex:(section - 1)];
//    return header.tripName;

}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    if(section == 0)
    {
        return 0;
    }
    return 44;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if(section == 0)
    {
        return 1;
    }
    else if (section == 1)
    {
        return [self.imports count];
    }

//    ItineraryImport *header = (ItineraryImport *)[self.imports objectAtIndex:(section - 1)];
//    Return the number of rows in the section.
//    return [header.rows count];
    return 0;
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

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if(indexPath.section == 0)
    {
        ItineraryImportCell *cell = (ItineraryImportCell*)[tableView dequeueReusableCellWithIdentifier:@"ImportMessageHeader" forIndexPath:indexPath];
        cell.messageText.text = [Localizer getLocalizedText:@"TA Import Heading Text"];
        return cell;
    }
    else if (indexPath.section == 1)
    {
        ItineraryImport *header = (ItineraryImport *)[self.imports objectAtIndex:(indexPath.row)];
        ItineraryImportCell *cell = (ItineraryImportCell *)[tableView dequeueReusableCellWithIdentifier:@"TripLegWithCheck" forIndexPath:indexPath];
        cell.clipsToBounds = YES;
        cell.title.text = header.tripName;
        cell.endDate.text = [self formatDateForStop:header.endDate];
        cell.startDate.text = [self formatDateForStop:header.startDate];

        UIImage *notSelectedImage = [UIImage imageNamed:@"check_unselect"];
        UIImage *selectedImage = [UIImage imageNamed:@"check_greenselect"];

        if(header.include)
        {
            cell.selectedImage.image = selectedImage;
        }
        else
        {
            cell.selectedImage.image = notSelectedImage;
        }

        return cell;
    }

//    ItineraryImportCell *cell = (ItineraryImportCell *)[tableView dequeueReusableCellWithIdentifier:@"TripLegWithCheck" forIndexPath:indexPath];

    // Configure the cell...
//    ItineraryImport *header = (ItineraryImport *)[self.imports objectAtIndex:(indexPath.section - 1)];
//    NSLog(@"header = %@", header.tripName);
//    ItineraryImportRow *leg = (ItineraryImportRow *)[header.rows objectAtIndex:indexPath.row];
//    NSLog(@"leg = %@", leg);

//    cell.title.text = leg.location;
//    cell.userInteractionEnabled = NO;


//    return cell;
    return nil;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView deselectRowAtIndexPath:indexPath animated:NO];

    ItineraryImport *header = (ItineraryImport *)[self.imports objectAtIndex:(indexPath.row)];

    header.include = !header.include;

    [tableView reloadRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationNone];


}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if(indexPath.section == 0)
    {
        UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ImportMessageHeader"];
        return [cell bounds].size.height;
    }
    else
    {
        ItineraryImportCell *cell = (ItineraryImportCell *)[tableView dequeueReusableCellWithIdentifier:@"TripLegWithCheck"];
        return [cell bounds].size.height;

    }
//    return 44;
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
    if([segue.identifier isEqualToString:@"unwindFromImportItinerary"])
    {
        NSLog(@"^^^^^^segue = %@", segue);
    }
}

- (NSMutableArray *) sortItineraryRows:(NSMutableArray *)toBeSorted
{
    NSArray *sortedArray;

    NSComparisonResult (^cmptr)(ItineraryStop *, ItineraryStop *) = ^NSComparisonResult(ItineraryStop *a, ItineraryStop *b)
    {
        NSLog(@"a.arrivalDate = %@ -- b.arrivalDate = %@", a.arrivalDate, b.arrivalDate);
        NSLog(@"a.departureDate = %@ -- b.departureDate = %@", a.departureDate, b.departureDate);

        if(a.arrivalDate && b.arrivalDate)
        {
            return [a.arrivalDate compare:b.arrivalDate];
        }

        if(a.departureDate && b.departureDate)
        {
            return [a.departureDate compare:b.departureDate];
        }

        if(!a.arrivalDate && b.arrivalDate && a.departureDate)
        {
            return [a.departureDate compare:b.arrivalDate];
        }

        if(!a.departureDate && b.departureDate && a.arrivalDate)
        {
            return [a.arrivalDate compare:b.departureDate];
        }

        return (NSComparisonResult)NSOrderedDescending;
    };

    NSLog(@"~~~~~toBeSorted = %@", toBeSorted);

    sortedArray = [toBeSorted sortedArrayUsingComparator:cmptr];

    NSLog(@"~~~~~sortedArray = %@", sortedArray);

    return [sortedArray mutableCopy];
}


- (BOOL)shouldPerformSegueWithIdentifier:(NSString *)identifier sender:(id)sender
{
    if([identifier isEqualToString:@"unwindFromImportItinerary"])
    {
        [self importItineraries];

        // Prevent the segue
        return false;
    }
    else
    {
        NSLog(@"identifier = %@", identifier);
        BOOL should = [super shouldPerformSegueWithIdentifier:identifier sender:sender];
        return should;
    }
}

- (NSComparisonResult)compareDatesNoTime:(NSDate *)a b:(NSDate *)b
{
    if(a == nil || b == nil)
    {

        return nil;
    }

    NSCalendar *calendar = [NSCalendar currentCalendar];
    NSInteger comps = (NSDayCalendarUnit | NSMonthCalendarUnit | NSYearCalendarUnit);

    NSDateComponents *aComponents = [calendar components:comps fromDate:a];
    NSDate *dateA = [calendar dateFromComponents:aComponents];
    NSDateComponents *bComponents = [calendar components:comps fromDate:b];
    NSDate *dateB = [calendar dateFromComponents:bComponents];

    NSComparisonResult result = [dateA compare:dateB];
    NSLog(@"/// %@ -- %@", dateA, dateB);
    NSLog(@"result = %d", result);

    return result;
}

- (void)importItineraries
{
    void (^success)(NSString *) = ^(NSString *result)
    {
        NSMutableArray *itineraries = [ItineraryImport parseItineraryImportResult:result rptKey:[Itinerary getRptKey:self.paramBag]];
        if([itineraries count] == 1)
        {
            BOOL success = YES;

            Itinerary *itinerary = [itineraries firstObject];
            for (ItineraryStop *itineraryRow in itinerary.stops)
            {
                if ([itineraryRow.status isEqualToString:@"SUCCESS"])
                {
                    // Added row
                }
                else
                {
                    //Need to involve the user
                    success = NO;
                }
            }
            if(success)
            {
                [AnalyticsTracker logEventWithCategory:@"Travel Allowance" eventAction:@"Import Itinerary From Travel" eventLabel:@"Success" eventValue:nil];

                // Get the modified
                if (self.onSuccessfulImport)
                {
                    self.onSuccessfulImport(@{@"key" : @"it worked"});
                }
                else
                {
                    NSLog(@"No success handler defined");
                }
            }
            else
            {
                [AnalyticsTracker logEventWithCategory:@"Travel Allowance" eventAction:@"Import Itinerary From Travel" eventLabel:@"Failure" eventValue:nil];

                if(self.onFailedImport)
                {
                    self.onFailedImport(itineraries);
                }
                else
                {
                    NSLog(@"No failure handler defined");
                }
            }
        }
        else
        {
            //TODO How?
        }
    };

    void (^failure)(NSError *) = ^(NSError *error) {
        NSLog(@"error = %@", error);
//        NSString *errorMessage = @"There was an error processing your request. It may be due to a poor network connection. Please try again later. If this condition continues, please contact your administrator";

    };


    //Get the selected rows
    NSMutableArray *toImport= [self getSelectedTripsToImport];
    //Sort the rows/legs
    self.currentItinerary.stops = [self sortItineraryRows:self.currentItinerary.stops];


    if([toImport count]>0)
    {
        // Get the keys to pass to the server
        CXRequest *request = [self importTravelAllowanceItinerary:toImport reportKey:[Itinerary getRptKey:self.paramBag] role:self.role];

        // Make the save call to the mws
        [[CXClient sharedClient] performRequest:request success:success failure:failure];
    }
    else
    {
        //TODO Nothing selected. Now what?  Do the segue?
    }
}

- (CXRequest *)importTravelAllowanceItinerary:(NSMutableArray *)toImport reportKey:(NSString *)reportKey role:(NSString *)role
{
    // Compose the path
    NSString *path = [NSString stringWithFormat:@"Mobile/TravelAllowance/ImportTravelAllowanceItinerary"];

    //TODO Support importing to an existing itinerary
    NSString *requestBody = [ItineraryImport composeImportTravelAllowanceItinerary:toImport rptKey:reportKey itinKey:nil role:role];

    // Create the request
    CXRequest *cxRequest = [[CXRequest alloc] initWithServicePath:path requestXML:requestBody];

    return cxRequest;
}



- (NSMutableArray *)getSelectedTripsToImport
{
    NSMutableArray *toImport = [[NSMutableArray alloc] init];
    for (ItineraryImport *importRow in self.imports)
    {
        if(importRow.include)
        {
            [toImport addObject:importRow.taImportId];
        }
    }
    return toImport;
}


- (void)loadImportData
{
    void (^success)(NSString *) = ^(NSString *result)
    {
        NSLog(@"IMPORT : result = %@", result);

        // Parse the response
        self.imports = [ItineraryImport parseImportXML:result];

        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
        [self.tableView reloadData];

    };

    void (^failure)(NSError *) = ^(NSError *error) {
        NSLog(@"error = %@", error);
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
        NSLog(@"error = %@", error);
//        [self showError];
    };

    CXRequest *request = [ItineraryImport getTravelAllowanceImport:[Itinerary getRptKey:self.paramBag]];
    [[CXClient sharedClient] performRequest:request success:success failure:failure];

}

@end
