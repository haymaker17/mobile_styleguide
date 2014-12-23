//
//  ItineraryStopViewController.m
//  ConcurMobile
//
//  Created by Wes Barton on 1/16/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ItineraryStopViewController.h"
#import "ItineraryStop.h"
#import "ItineraryStopCell.h"

#import "Itinerary.h"
#import "CXClient.h"
#import "AnalyticsManager.h"
#import "ItineraryStopDetailViewController.h"
#import "ItineraryConfig.h"
#import "ItineraryAllowanceAdjustmentViewController.h"
#import "WaitViewController.h"
#import "ItineraryCell.h"
#import "ItinerarySummaryViewController.h"
#import "CTETriangleBadge.h"
#import "CTEBadge.h"

@interface ItineraryStopViewController ()


@end

@implementation ItineraryStopViewController

@synthesize navBar;

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

//    [self setupRefreshControl];

    //Localize
    [self.navBar setTitle:[Localizer getLocalizedText:@"Itinerary"]];
    [self.saveGenerateButton setTitle:[Localizer getLocalizedText:@"Calculate Allowances"]];
    self.view.backgroundColor = [UIColor clearColor];


    self.role = self.paramBag[@"ROLE"];

//    self.itineraryStops = [[NSMutableArray alloc]init];
//    [self.refreshControl beginRefreshing];    // ???
    self.hasItineraries = YES;
    [self loadItineraryData:[Itinerary getRptKey:self.paramBag] crnCode:self.paramBag[@"CrnCode"]];
    [self loadTAConfig];

    [self.saveGenerateButton setEnabled:NO];

}
-(void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];

//    NSLog(@"viewWillAppear");
//    [self.refreshControl beginRefreshing];

//    CGFloat refreshHeight = self.refreshControl.frame.size.height;
//    self.tableView.contentOffset = CGPointMake(0, -refreshHeight);
    // TODO text is not in the right place

}
 /*
-(void)setupRefreshControl{
    UIRefreshControl *refreshControl = [[UIRefreshControl alloc] init];
    [refreshControl addTarget:self action:@selector(refreshControlRequest) forControlEvents:UIControlEventValueChanged];
    NSString *title = [Localizer getLocalizedText:@"Pull to Refresh"];
//    NSString *title = @"Loading Itinerary Stops";
    refreshControl.attributedTitle = [[NSAttributedString alloc] initWithString:title];
    [self setRefreshControl:refreshControl];
}  */

          /*
-(void)refreshControlRequest
{
    [self performSelector:@selector(updateTableView)withObject:nil];
}           */

-(void)updateTableView
{
        NSString *title = [Localizer getLocalizedText:@"Pull to Refresh"];
//    self.refreshControl.attributedTitle = [[NSAttributedString alloc] initWithString:title];

    NSLog(@"updateTableView");
    [self loadItineraryData:[Itinerary getRptKey:self.paramBag] crnCode:self.paramBag[@"CrnCode"]];

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

//                [self redirectToAddStop];

                NSLog(@"/////self.itineraryConfig.tripLengthList = %d", self.itineraryConfig.tripLengthList);
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

- (void)loadItineraryData:(NSString *)rptKey crnCode:(NSString *)crnCode
{
    NSLog(@"LoadItineraryData");

    if(rptKey == nil)
    {
        NSLog(@"No Report Key");
    }
    else
    {

        [WaitViewController showWithText:@"Get Itineraries" animated:YES];

        void (^success)(NSString *) = ^(NSString *result)
        {
            NSLog(@"getTAItinerariesRequest : result = %@", result);
            
            //clear the existing entries
//        [self.itineraryStops removeAllObjects];

            // Parse the response
            NSMutableArray *itineraries = [Itinerary parseItinerariesXML:result rptKey:rptKey crnCode:crnCode];
            if([itineraries count] == 0)
            {
                NSLog(@"~~ No Itineraries");
                self.hasItineraries = NO;
            }
            else if([itineraries count] == 1)
            {
                self.itinerary = [itineraries objectAtIndex:0];
            }
            else
            {
                NSString *itinKey = self.selectedItinKey;
                NSLog(@"itinKey to match = %@", itinKey);
                for (Itinerary *itinFromList in itineraries) {
                    if([itinFromList.itinKey isEqualToString:itinKey])
                    {
                        self.itinerary = itinFromList;
                    }
                }
                if(self.itinerary == nil)
                {
                    self.itinerary = [itineraries objectAtIndex:0];
                }
            }

            if(self.itinerary != nil && self.itinerary.stops != nil && [self.itinerary.stops count] > 0 )
            {
                if(self.itinerary.hasFailures)
                {
                    [self.saveGenerateButton setEnabled:NO];
                }
                else
                {
                    [self.saveGenerateButton setEnabled:YES];
                }
            }

//            [self.tableView reloadData];

            [self bothComplete];

//            [self redirectToAddStop];

        };

        void (^failure)(NSError *) = ^(NSError *error) {
            NSLog(@"error = %@", error);
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            NSLog(@"error = %@", error);
            [self showError];
        };

//        [self.tableView beginUpdates];

        CXRequest *request = [Itinerary getTAItinerariesRequest:rptKey roleCode:self.role];
        [[CXClient sharedClient] performRequest:request success:success failure:failure];
    }
}

- (void)bothComplete
{
    if(!self.hasItineraries && self.itineraryConfig != nil )
    {
        [self.tableView reloadData];
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
    }

    if(self.itinerary.stops != nil && self.itineraryConfig != nil)
    {
        [self.tableView reloadData];
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
    }
}

- (void)redirectToAddStop {
    if([self.itinerary.stops count] == 0 && self.itineraryConfig != nil)
    {
        // They don't have any stops, take them to the AddStop screen

        NSLog(@"+++++++++++++ AddStopSegue");
//        [self performSegueWithIdentifier:@"AddStopSegue" sender:self];
    }
}

// <?xml version="1.0" encoding="UTF-8"?><Response><Header><Version>1.0</Version><Log><Level>None</Level></Log><TravelerUID>0</TravelerUID><ExpenseUID>37</ExpenseUID><CliqSessionID>A9DE8F96-D1EE-4A54-893C-3D2FD87D1B1A</CliqSessionID><LoginID>acsontos@outtask.com</LoginID><EntityID>phos123488</EntityID><CompanyID>1</CompanyID><SUVersion>100.0</SUVersion><IsMobile>Y</IsMobile><IsTestUser>N</IsTestUser><SkipVersionCheck>Y</SkipVersionCheck><HmcUserKey /><RequestOrigin>MOBILE</RequestOrigin><PerfData><TotalDuration>70</TotalDuration><DBPerfItemTotal>42</DBPerfItemTotal></PerfData></Header>
// <Body>
// <Itinerary>
// <ItinKey>nNYL70NG$pPPsWPqO5FlaeL1s</ItinKey>
// <Name>TA 2</Name>
// <ShortDistanceTrip>N</ShortDistanceTrip><EmpKey>37</EmpKey><TacKey>7</TacKey><TacName>GenericStd Fixed/Fixed</TacName><DepartDateTime>2013-12-19 12:00</DepartDateTime><DepartLocation>Seattle, Washington</DepartLocation><ArrivalDateTime>2013-12-23 18:00</ArrivalDateTime><ArrivalLocation>San Jose, California</ArrivalLocation><IsLocked>N</IsLocked><AreAllRowsLocked>N</AreAllRowsLocked>
// <ItineraryRows>

// <ItineraryRow><IrKey>n9z$siF5gHmq5cPIU$s$p0ZI4S4</IrKey><ArrivalDateTime>2013-12-19 14:04</ArrivalDateTime><ArrivalLocation>San Jose, California</ArrivalLocation><ArrivalLnKey>30050</ArrivalLnKey><DepartDateTime>2013-12-19 14:04</DepartDateTime><DepartLocation>Oakland, California</DepartLocation><DepartLnKey>28790</DepartLnKey><ArrivalRlKey>49</ArrivalRlKey><ArrivalRateLocation>California, UNITED STATES</ArrivalRateLocation><BorderCrossDateTime>2013-12-19 14:04</BorderCrossDateTime><IsRowLocked>N</IsRowLocked><IsArrivalRateLocationEditable>N</IsArrivalRateLocationEditable></ItineraryRow>
// <ItineraryRow><IrKey>n9ht3rWvRimPBe7POr3j73fE</IrKey><ArrivalDateTime>2013-12-23 18:00</ArrivalDateTime><ArrivalLocation>Seattle, Washington</ArrivalLocation><ArrivalLnKey>29928</ArrivalLnKey><DepartDateTime>2013-12-23 15:00</DepartDateTime><DepartLocation>San Jose, California</DepartLocation><DepartLnKey>30050</DepartLnKey><ArrivalRlKey>68</ArrivalRlKey><ArrivalRateLocation>Seattle, Washington, US</ArrivalRateLocation><BorderCrossDateTime>2013-12-23 15:00</BorderCrossDateTime><IsRowLocked>N</IsRowLocked><IsArrivalRateLocationEditable>N</IsArrivalRateLocationEditable></ItineraryRow>
// </ItineraryRows>
// </Itinerary>
// </Body>
// </Response>"

- (void)parseItinerariesXML:(NSString *)result stops:(NSMutableArray *)stops rptKey:(NSString *)rptKey crnCode:(NSString *)crnCode {

    RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
    if (rootXML != nil) {

    }

    RXMLElement *body = [rootXML child:@"Body"];
    // Restrict to only one Itinerary
    NSArray *itins = [body children:@"Itinerary"];

    if ([itins count] > 0)
    {
        if ([itins count] > 1)
        {
            NSLog(@"Too many Itineraries");
        }
        RXMLElement *itin =[itins objectAtIndex:0];

        self.itinerary = [Itinerary processItineraryXML:itin rptKey:rptKey];

        self.itinerary.crnCode = crnCode;

        [ItineraryStop processItineraryRowXML:itin stops:stops];

        [ItineraryStop applyStopNumbers:stops];
//        [self breakStopsIntoSectionsByDate];
//        NSLog(@"self.itineraryStopsBySection = %@", self.itineraryStopsBySection);
    }

}




/*
- (void)breakStopsIntoSectionsByDate
{
    self.itineraryStopsBySection = [[NSMutableArray alloc]init];

    NSDate *currentDateOnly =nil;
    NSMutableArray *bucket = nil;

    for (int i = 0; i < [self.itineraryStops count]; i++)
    {
        ItineraryStop *stop = [self.itineraryStops objectAtIndex:i];

        NSDate *date = stop.departureDate;
        unsigned int flags = NSYearCalendarUnit | NSMonthCalendarUnit | NSDayCalendarUnit;
        NSCalendar* calendar = [NSCalendar currentCalendar];

        NSDateComponents* components = [calendar components:flags fromDate:date];

        NSDate* dateOnly = [calendar dateFromComponents:components];

//        NSLog(@"dateOnly = %@", dateOnly);


        if (currentDateOnly == nil)
        {
            //Create a new Bucket
            bucket = [[NSMutableArray alloc]init];
            //Add it to the top level
            [self.itineraryStopsBySection addObject:bucket];

            currentDateOnly = dateOnly;
        }

        if([currentDateOnly compare:dateOnly] == NSOrderedSame)
        {
            [bucket addObject:stop];
        }
        else
        {
            //Create a new bucket
            bucket = [[NSMutableArray alloc]init];
            //Add it to the top level
            [self.itineraryStopsBySection addObject:bucket];

            currentDateOnly = dateOnly;
            [bucket addObject:stop];
        }
    }
}
  */









// TODO Deal with this in a better fashion, needs the timezone info


// Throw this into the background so it doesn't block networking.
//
- (void)showError {
    NSBlockOperation *op = [NSBlockOperation blockOperationWithBlock:^{
        [[AnalyticsManager sharedInstance] logCategory:@"Expense" withName:@"Itinerary Lookup"];

        [UIView animateWithDuration:0.25 animations:^{
            self.waitView.alpha = 0;
        }];

        [self.activityIndicator stopAnimating];
        [self.waitView removeFromSuperview];

        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Error retrieving Itineraries"
                                                        message:@"Sorry! Something went wrong. Please try again later."
                                                       delegate:nil
                                              cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                              otherButtonTitles:nil];

        [alert show];
    }];

    [[NSOperationQueue mainQueue] addOperation:op];
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
//    return [self.itineraryStopsBySection count];
    return 3;
}

/*
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
//    return [super tableView:tableView titleForHeaderInSection:section];
    NSMutableArray *bucket = self.itineraryStopsBySection[section];
    ItineraryStop *stop = bucket[0];
    NSDate *date = stop.departureDate;

    unsigned int flags = NSYearCalendarUnit | NSMonthCalendarUnit | NSDayCalendarUnit;
    NSCalendar* calendar = [NSCalendar currentCalendar];

    NSDateComponents* components = [calendar components:flags fromDate:date];
    NSDate* dateOnly = [calendar dateFromComponents:components];


    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyy-MM-dd"];
    NSString *departureDate = [formatter stringFromDate:dateOnly];
    return departureDate;
}
*/


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if(section == HeaderSection)
    {
        if(self.itinerary == nil)
        {
            return 0;
        }
        return 1;
    }
    else if(section == InformationHeader)
    {
        if(self.itinerary != nil)
        {
            if(self.itinerary.hasFailures)
            {
                return 1;
            }
        }
        return 0;
    }
    else if(section == StopSection)
    {
        if(self.itinerary == nil)
        {
            return 0;
        }
        return [self.itinerary.stops count];
    }
    
    return 0;
}

- (CGFloat)tableView:(UITableView *)tableView estimatedHeightForHeaderInSection:(NSInteger)section
{
    return 0;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 0;
}

- (CGFloat)tableView:(UITableView *)tableView estimatedHeightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 100;
}

-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath{
    if(indexPath.section == HeaderSection)
    {
        if([ExSystem is8Plus])
        {
            return UITableViewAutomaticDimension;
        }

        ItineraryCell *cell = (ItineraryCell *) [tableView dequeueReusableCellWithIdentifier:@"ItineraryListCell"];
        CGFloat height = [cell bounds].size.height;
        return height;
    }
    else if(indexPath.section == InformationHeader)
    {
        if([ExSystem is8Plus])
        {
            return UITableViewAutomaticDimension;
        }

        ItineraryCell *cell = (ItineraryCell *) [tableView dequeueReusableCellWithIdentifier:@"ItineraryInformationCell"];
        CGFloat height = [cell bounds].size.height;

        UITextView *informationTextView = cell.informationText;
        if([informationTextView.text isEqualToString:@"Information"])
        {
            for (ItineraryStop *stop in self.itinerary.stops)
            {
                if(stop.isFailed)
                {
                    informationTextView.text = stop.statusTextLocalized;
                    break;
                }
            }
        }

        CGSize textViewSize = [informationTextView sizeThatFits:CGSizeMake(informationTextView.frame.size.width, FLT_MAX)];
        height = textViewSize.height + informationTextView.frame.origin.y; // TODO make this get the

        return height;

    }
    else if(indexPath.section == StopSection)
    {
        if([ExSystem is8Plus])
        {
            return UITableViewAutomaticDimension;
        }

        NSString *cellIdentifier = @"ItinStop2ProtoCell";
        id cellId = [self.tableView dequeueReusableCellWithIdentifier:cellIdentifier];
        CGFloat height = [cellId bounds].size.height;
        return height;
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
    if(indexPath.section == HeaderSection)
    {
        return [self cellForRowAtIndexPathHeader:indexPath tableView:tableView];
    }
    else if(indexPath.section == InformationHeader)
    {
        return [self cellForRowAtIndexPathInfo:indexPath tableView:tableView];
    }
    else if(indexPath.section == StopSection)
    {
        return [self cellForRowAtIndexPathStop:indexPath tableView:tableView];
    }
    return nil;
}

- (UITableViewCell *) cellForRowAtIndexPathInfo:(NSIndexPath *)indexPath tableView:(UITableView *)tableView
{
    ItineraryCell *cell = (ItineraryCell *) [tableView dequeueReusableCellWithIdentifier:@"ItineraryInformationCell" forIndexPath:indexPath];
    cell.clipsToBounds = YES;

    for (ItineraryStop *stop in self.itinerary.stops)
    {
        if(stop.isFailed)
        {
            cell.informationText.text = stop.statusTextLocalized;
            return cell;
        }
    }

    cell.informationText.text = @"Hello from your friends";

    return cell;
}

- (UITableViewCell *) cellForRowAtIndexPathHeader:(NSIndexPath *)indexPath tableView:(UITableView *)tableView
{
    Itinerary *itinerary = self.itinerary;
    ItineraryConfig *config = self.itineraryConfig;

    if(itinerary == nil)
    {
//TODO        Should be some sort of message
        return nil;
    }

    ItineraryCell *cell = (ItineraryCell *)[tableView dequeueReusableCellWithIdentifier:@"ItineraryListCell"];

    // Configure the cell...
    cell.itineraryName.text = itinerary.itinName;
//    cell.numberOfStops.text = [NSString stringWithFormat:@"%u Stops", [itinerary.stops count]];
//    cell.numberOfStops.text = @"";

    [ItineraryCell composeItineraryDateRange:itinerary cell:cell format:@"MMM dd"];

    cell.itinerary = itinerary;
    cell.itineraryConfig = config;
    cell.clipsToBounds = YES;

    NSLog(@"itinerary.tripLength = %@", itinerary.tripLength);
    NSString *tripLengthValue = [config.tripLengthListValues valueForKey:self.itinerary.tripLength];
    NSLog(@"tripLengthValue = %@", tripLengthValue);
    cell.itineraryTripLength.text = tripLengthValue;

    NSLog(@"cell.accessoryType = %d", cell.accessoryType);

    if([itinerary.areAllRowsLocked boolValue])
    {
        // Rows are locked, don't allow them to change
    }
    else if(![Itinerary isApproving:self.role])
    {
        UITapGestureRecognizer *singleTapRecogniser = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(headerTappedHandler:)];
        [singleTapRecogniser setDelegate:self];
        singleTapRecogniser.numberOfTouchesRequired = 1;
        singleTapRecogniser.numberOfTapsRequired = 1;
        [cell addGestureRecognizer:singleTapRecogniser];

        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    }

    return cell;

}

- (UITableViewCell *) cellForRowAtIndexPathStop:(NSIndexPath *)indexPath tableView:(UITableView *)tableView
{
    NSInteger *rowIndex = indexPath.row;

    // Get the data object
//    NSMutableArray *section = [self.itineraryStopsBySection objectAtIndex:sectionIndex];
//    ItineraryStop *stop = [section objectAtIndex:rowIndex];

    ItineraryStop *stop = [self.itinerary.stops objectAtIndex:rowIndex];

    // Get the appropriate prototype cell for display, so I could switch on something in the data cell.
    NSString *cellIdentifier = @"ItinStop2ProtoCell";
    ItineraryStopCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier forIndexPath:indexPath];

    //Localize


    cell.itineraryStop = stop;

    cell.departureCity.text = stop.departureLocation;
    cell.arrivalCity.text = stop.arrivalLocation;

    cell.arrivalDate.text = @"2014 01 23";
    cell.departureDate.text = @"2014 01 23";

    if([stop.stopNumber intValue] == 0)
    {
        NSInteger indexPlusOne = indexPath.row + 1;

        cell.stopNumber.text = [NSString stringWithFormat:@"%i", indexPlusOne];

        [cell.stopBadge updateBadgeCount:[NSNumber numberWithInteger:indexPlusOne]];

        [cell.stopBadge updateBadgeColor:[UIColor colorWithRed:0.0/255.0 green:120.0/255.0 blue:200.0/255.0 alpha:1]];
    }
    else
    {
        cell.stopNumber.text = [stop.stopNumber stringValue];

        [cell.stopBadge updateBadgeCount:stop.stopNumber];
//        int xxx = [stop.stopNumber intValue] + [@100 intValue];
//        [cell.stopBadge updateBadgeCount:[NSNumber numberWithInteger:(xxx)]];

        [cell.stopBadge updateBadgeColor:[UIColor colorWithRed:0.0/255.0 green:120.0/255.0 blue:200.0/255.0 alpha:1]];
    }

    cell.stopLabel.text = [Localizer getLocalizedText:@"Stop"];
    cell.FromLabel.text = [Localizer getLocalizedText:@"From"];
    cell.ToLabel.text = [Localizer getLocalizedText:@"To"];
    cell.RateLocationLabel.text = [Localizer getLocalizedText:@"Rate Location:"];

    UIColor *errorFlagColor = [UIColor colorWithRed:191.0/255.0 green:103.0/255.0 blue:103.0/255.0 alpha:1];

    if(stop.isFailed)
    {
        [cell.stopErrorIndicator setHidden:NO];

        [cell.triangleBadge setHidden:NO];
        [cell.triangleBadge updateBadgeColor:errorFlagColor];

    }
    else
    {
        [cell.stopErrorIndicator setHidden:YES];

        [cell.triangleBadge setHidden:YES];
        [cell.triangleBadge updateBadgeColor:errorFlagColor];
    }

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

    NSLog(@"departureDate = %@", departureDate);
    NSLog(@"arrivalDate = %@", arrivalDate);


//    cell.selectionStyle = UITableViewCellSelectionStyleBlue;
//    [cell addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(bob:)]];
    
    return cell;
}



//- (void)bob:(UITapGestureRecognizer*)gesture {
//    NSLog(@"gesture %@",[gesture debugDescription]);
//    [self performSegueWithIdentifier:@"SelectStopSegue" sender:self];
//}


// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    if(indexPath.section == HeaderSection)
    {
        return NO;
    }
    else if (indexPath.section == InformationHeader)
    {
        return NO;
    }
    else
    {

        // Get the data object
        ItineraryStop *stop = [self.itinerary.stops objectAtIndex:indexPath.row];
        if (stop.rowLocked)
        {
            return NO;
        }

        // Return NO if you do not want the specified item to be editable.
        return YES;
    }
}




// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        NSLog(@"commitEditingStyle = editingStyle = %d", editingStyle);
//        [self tableView:<#(UITableView *)tableView#> cellForRowAtIndexPath:<#(NSIndexPath *)indexPath#>]
        ItineraryStopCell *cell = [self.tableView cellForRowAtIndexPath:indexPath];

        ItineraryStop *stop = cell.itineraryStop;
        NSString *itinKey = self.itinerary.itinKey;

        NSLog(@"itinKey = %@", itinKey);

        void (^success)(NSString *) = ^(NSString *result)
        {
            BOOL *success = [ItineraryStop wasDeleteItineraryStopSuccessful:result];
            NSLog(@"Delete Stop : success = %p", success);
            // Reload Table
            //                                            [self updateTableView];
            [self.tableView beginUpdates];
    
            [self.itinerary.stops removeObjectAtIndex:indexPath.row];
    
            NSArray *arg = @[indexPath];
            [self.tableView deleteRowsAtIndexPaths:arg withRowAnimation:UITableViewRowAnimationFade];
    
            [self.tableView endUpdates];
    
        };
        void (^failure)(NSError *) = ^(NSError *error) {
            [self showError];
        };

        CXRequest *request = [ItineraryStop deleteItineraryStop:itinKey irKey:stop.irKey];
        [[CXClient sharedClient] performRequest:request success:success failure:failure];


//        [self.tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
        // TODO Delete the stop
    }

//    else if (editingStyle == UITableViewCellEditingStyleInsert) {
//        Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
//    }

}

#pragma mark - Navigation

//- (BOOL)shouldPerformSegueWithIdentifier:(NSString *)identifier sender:(id)sender{
//    NSLog(@"shouldPerformSegueWithIdentifier %@",identifier);
//    return YES;
//}

// In a story board-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.

    UINavigationController * navController = [segue destinationViewController];

    if([segue.identifier isEqualToString:@"SelectStopSegue"])
    {
        ItineraryStopDetailViewController *destinationController = nil;
        if([[segue destinationViewController] isKindOfClass:[ItineraryStopDetailViewController class]])
        {
            destinationController = (ItineraryStopDetailViewController *) [segue destinationViewController];
            [destinationController setOnSuccessfulSave:^(NSDictionary *dictionary) {
//                [self.navigationController popViewControllerAnimated:YES];
                [self updateTableView];
            }];
        }
        else
        {
            destinationController = (ItineraryStopDetailViewController *) navController.topViewController;
            [destinationController setOnSuccessfulSave:^(NSDictionary *dictionary) {
                [self dismissViewControllerAnimated:YES completion:nil];
                [self updateTableView];
            }];
        }



        destinationController.showHeaderText = ([self.itinerary.stops count] < 2); //Remove the header text if there are already two stops
        destinationController.itinerary = self.itinerary;
        destinationController.paramBag = self.paramBag;
        destinationController.itineraryConfig = self.itineraryConfig;

        ItineraryStopCell *cell = (ItineraryStopCell *)sender;
        destinationController.itineraryStop = cell.itineraryStop;


    }
    else if ([segue.identifier isEqualToString:@"AddStopSegue"])
    {
        ItineraryStopDetailViewController *destinationController = nil;
        if([[segue destinationViewController] isKindOfClass:[ItineraryStopDetailViewController class]])
        {
            destinationController = (ItineraryStopDetailViewController *) [segue destinationViewController];
            [destinationController setOnSuccessfulSave:^(NSDictionary *dictionary) {
                NSString *wasSingleDay = (NSString *)[dictionary valueForKey:@"wasSingleDay"];

                if([wasSingleDay isEqualToString:@"Y"])
                {
                    if (self.onSuccessfulSaveOfSingleDay)
                    {
                        self.onSuccessfulSaveOfSingleDay(dictionary);
                    }
                }
                else
                {
                    [self updateTableView];
                }
            }];
        }
        else
        {
            destinationController = (ItineraryStopDetailViewController *)navController.topViewController;

            [destinationController setOnSuccessfulSave:^(NSDictionary *dictionary) {
                NSString *wasSingleDay = (NSString *)[dictionary valueForKey:@"wasSingleDay"];

                if([wasSingleDay isEqualToString:@"Y"])
                {
                    //Perform another segue if single day?
                    [self.navigationController popViewControllerAnimated:YES];

                    if (self.onSuccessfulSaveOfSingleDay)
                    {
                        self.onSuccessfulSaveOfSingleDay(dictionary);
                    }
                }
                else
                {
                    [self dismissViewControllerAnimated:YES completion:nil];
                    [self updateTableView];
                }
            }];

        }


        destinationController.itineraryConfig = self.itineraryConfig;
        
        
        //        destinationController.showHeaderText = ([self.itineraryStops count] < 2); //Remove the header text if there are already two stops
        destinationController.showHeaderText = true; //Remove the header text if there are already two stops

        //Is there an itinerary, or are we creating the first stop
        if(self.itinerary == nil)
        {
            //Create a placeholder
            Itinerary *newItinerary = [Itinerary getNewItineraryRegular:self.itineraryConfig reportName:[Itinerary getReportName:self.paramBag] rptKey:[Itinerary getRptKey:self.paramBag]];
            self.itinerary = newItinerary;
        }

        destinationController.itinerary = self.itinerary;
        destinationController.paramBag = self.paramBag;

        ItineraryStop *newItineraryStop = [ItineraryStop getNewStop:self.itineraryConfig itinerary:self.itinerary];

        destinationController.itineraryStop = newItineraryStop;

    }
    else if ([segue.identifier isEqualToString:@"AllowanceAdjustmentsSegue"])
    {
        ItineraryAllowanceAdjustmentViewController *destinationController = nil;
        if([[segue destinationViewController] isKindOfClass:[ItineraryAllowanceAdjustmentViewController class]])
        {
            destinationController = (ItineraryAllowanceAdjustmentViewController *)[segue destinationViewController];
            [destinationController setOnSuccessfulSave:^(NSDictionary *dictionary)
            {
                NSLog(@"EXECUTE Allowance Adjustment");

                [self.navigationController popViewControllerAnimated:YES];

                [self updateTableView];
            }];
        }
        else
        {
            destinationController = (ItineraryAllowanceAdjustmentViewController *)navController.topViewController;
            [destinationController setOnSuccessfulSave:^(NSDictionary *dictionary)
            {
                NSLog(@"EXECUTE Allowance Adjustment");

                [self dismissViewControllerAnimated:YES completion:nil];

                [self updateTableView];
            }];

        }

        if(destinationController != nil)
        {

            destinationController.rptKey = self.itinerary.rptKey;
            destinationController.crnCode = self.itinerary.crnCode;
            destinationController.role = self.role;

            if ([self.itinerary.areAllRowsLocked isEqualToString:@"Y"])
            {
                destinationController.hideGenerateExpenseButton = YES;
            }

            if (![Itinerary isApproving:self.role])
            {
                destinationController.expandAllDays = YES;
            }


        }
    }
    else if ([segue.identifier isEqualToString:@"SelectSummarySegue"])
    {
        NSLog(@"++++++++segue = %@", segue);

        ItinerarySummaryViewController *destinationController = nil;
        if([[segue destinationViewController] isKindOfClass:[ItinerarySummaryViewController class]])
        {
            destinationController = (ItinerarySummaryViewController *)[segue destinationViewController];
            [destinationController setOnSuccessfulSave:^(NSDictionary *dictionary) {
//                [self dismissViewControllerAnimated:YES completion:nil];
                [self.navigationController popViewControllerAnimated:YES];
                [self updateTableView];
            }];
        }
        else
        {
            destinationController = (ItinerarySummaryViewController *)navController.topViewController;
            [destinationController setOnSuccessfulSave:^(NSDictionary *dictionary) {
                [self dismissViewControllerAnimated:YES completion:nil];
                [self updateTableView];
            }];
        }

        destinationController.itinerary = self.itinerary;
        destinationController.itineraryConfig = self.itineraryConfig;

    }
}



-(IBAction)unwindToList:(UIStoryboardSegue *)segue
{
    NSLog(@"Stop - unwindToList - segue.identifier = %@", segue.identifier);

    ItineraryStopDetailViewController *source = [segue sourceViewController];

    if([segue.identifier isEqualToString:@"ItineraryStopFromCancel"])
    {
        // Discard
    }
    else if ([segue.identifier isEqualToString:@"ItineraryStopFromDone"])
    {
        // Save the changes, or should this be done in a prepare for segue
        NSLog(@"self.itinerary.itinName = %@", self.itinerary.itinName);
        NSLog(@"source.itinerary.itinName = %@", source.itinerary.itinName);

    }

    //Reload
    [self updateTableView];
}

-(IBAction)unwindToListFromAllowanceAdjustment:(UIStoryboardSegue *)segue
{
    NSLog(@"unwindToListFromAllowanceAdjustment - segue.identifier = %@", segue.identifier);

    //Reload
    [self updateTableView];
}


-(IBAction)unwindFromAllowanceAdjustment:(UIStoryboardSegue *)segue
{
    NSLog(@"unwindFromAllowanceAdjustment - segue = %@", segue);

}


- (NSIndexPath *)tableView:(UITableView *)tableView willSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    ItineraryStop *stop = [self.itinerary.stops objectAtIndex:indexPath.row];
    if (stop.rowLocked)
    {
        return nil;
    }

    return indexPath;
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSectionXXX:(NSInteger)section
{
//    return [super tableView:tableView viewForHeaderInSection:section];
//    Itinerary *itinerary = (Itinerary *)[self.itineraries objectAtIndex:section];
    Itinerary *itinerary = self.itinerary;
    ItineraryConfig *config = self.itineraryConfig;

    if(itinerary == nil)
    {
//TODO        Should be some sort of message
        return nil;
    }

    ItineraryCell *cell = (ItineraryCell *)[tableView dequeueReusableCellWithIdentifier:@"ItineraryListCell"];

    // Configure the cell...
    cell.itineraryName.text = itinerary.itinName;
//    cell.numberOfStops.text = [NSString stringWithFormat:@"%u Stops", [itinerary.stops count]];
//    cell.numberOfStops.text = @"";


    [ItineraryCell composeItineraryDateRange:itinerary cell:cell format:@"MMM dd"];

    cell.itinerary = itinerary;
    cell.itineraryConfig = config;
    cell.clipsToBounds = YES;

    NSLog(@"itinerary.tripLength = %@", itinerary.tripLength);
    NSString *tripLengthValue = [config.tripLengthListValues valueForKey:self.itinerary.tripLength];
    NSLog(@"tripLengthValue = %@", tripLengthValue);
    cell.itineraryTripLength.text = tripLengthValue;

    NSLog(@"cell.accessoryType = %d", cell.accessoryType);

    if([itinerary.areAllRowsLocked boolValue])
    {
        // Rows are locked, don't allow them to change
    }
    else if(![Itinerary isApproving:self.role])
    {
        UITapGestureRecognizer *singleTapRecogniser = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(headerTappedHandler:)];
        [singleTapRecogniser setDelegate:self];
        singleTapRecogniser.numberOfTouchesRequired = 1;
        singleTapRecogniser.numberOfTapsRequired = 1;
        [cell addGestureRecognizer:singleTapRecogniser];

        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    }

    return cell;

}

- (void) headerTappedHandler:(UIGestureRecognizer *)gestureRecognizer
{
    ItineraryCell *cell = gestureRecognizer.view;
    [self performSegueWithIdentifier:@"SelectSummarySegue" sender:cell];
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
