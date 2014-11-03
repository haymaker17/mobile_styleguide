//
//  SearchResultsViewController.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXClient.h"
#import "CXRequest.h"
#import "ExpenseCommuteViewController.h"
#import "JorudanSearchRequestFactory.h"
#import "Localizer.h"
#import "Route.h"
#import "RouteManager.h"
#import "RXMLElement.h"
#import "SearchResultRouteView.h"
#import "SearchResultsViewController.h"
#import "Segment.h"
#import "Station.h"

@interface SearchResultsViewController ()

@end

@implementation SearchResultsViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
    }
    
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // If iOS 7
    //
    if ([self respondsToSelector:@selector(setEdgesForExtendedLayout:)]) {
        self.edgesForExtendedLayout = UIRectEdgeNone;
    }
    
    self.navigationItem.title = [Localizer getLocalizedText:@"select_route"];
    
    [self.activityIndicator startAnimating];
    
    self.tripSynopsis.text = [self.routeSearchModel synopsis];
    self.tripMetadata.text = [self.routeSearchModel metadata];
        
    Station *firstStation = [self.routeSearchModel.stations objectAtIndex:0];
    Station *firstThroughStation = [self.routeSearchModel.stations objectAtIndex:1];
    Station *secondThroughStation = [self.routeSearchModel.stations objectAtIndex:2];
    Station *lastStation = [self.routeSearchModel.stations objectAtIndex:3];
    
    CXRequest *request = [JorudanSearchRequestFactory searchJorudanForDate:self.routeSearchModel.date
                                                               fromStation:firstStation
                                                                 toStation:lastStation
                                                               viaStation1:firstThroughStation
                                                               viaStation2:secondThroughStation
                                                              withSeatType:self.routeSearchModel.seatType
                                                               isRoundTrip:self.routeSearchModel.isRoundTrip
                                                              isIcCardFare:self.routeSearchModel.isIcCardFare];
    
    [[CXClient sharedClient] performRequest:request success:^(NSString *result) {
        RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
        
        self.cheapRoute = [self parseRoute:@"Cheap" fromElement:rootXML];
        self.easyRoute = [self parseRoute:@"Easy" fromElement:rootXML];
        self.fastRoute = [self parseRoute:@"Fast" fromElement:rootXML];
        self.otherRoute = [self parseRoute:@"Other" fromElement:rootXML];

//        NSLog(@"fast route = %@", self.fastRoute);
//        NSLog(@"easy route = %@", self.easyRoute);
//        NSLog(@"cheap route = %@", self.cheapRoute);
//        NSLog(@"other route = %@", self.otherRoute);
        
        [self.activityIndicator stopAnimating];
        
        self.tableView.hidden = NO;
        
        [self.tableView reloadData];
    } failure:^(NSError *error) {
        NSLog(@"Error = %@", error);
    }];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

#pragma mark - UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    int numSections = 4;
    
    // Sometimes there's no "other". Test for that here
    // and don't show that section if it's not present.
    //
    if ([self.otherRoute.segments count] == 0) {
        return 3;
    }
    
    return numSections;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    NSString *title = nil;
    
    switch (section) {
        case 0:
            title = [Localizer getLocalizedText:@"fast"];
            break;
        case 1:
            title = [Localizer getLocalizedText:@"easy"];
            break;
        case 2:
            title = [Localizer getLocalizedText:@"cheap"];
            break;
        default:
            title = [Localizer getLocalizedText:@"other"];
            break;
    }
    
    return title;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView
                             dequeueReusableCellWithIdentifier:@"SearchResultCell"];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"SearchResultCell"];
    }
    
    SearchResultRouteView *routeView = [[SearchResultRouteView alloc]
                                        initWithFrame:CGRectMake(0, 0, self.tableView.frame.size.width, 300)];
    
    [routeView addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(didTap:)]];
    
    switch ([indexPath section]) {
        case 0:
            routeView.route = self.fastRoute;
            break;
        case 1:
            routeView.route = self.easyRoute;
            break;
        case 2:
            routeView.route = self.cheapRoute;
            break;
        default:
            routeView.route = self.otherRoute;
            break;
    }

    [routeView sizeToFit];
    
    // Clean out the cell.
    //
    for (UIView *v in cell.contentView.subviews) {
        [v removeFromSuperview];
    }
    
    // Add our only child (route view).
    //
    [cell.contentView addSubview:routeView];
    
    return cell;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 1;
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [self.tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    Route *route = nil;
    
    switch ([indexPath section]) {
        case 0:
            route = self.fastRoute;
            break;
        case 1:
            route = self.easyRoute;
            break;
        case 2:
            route = self.cheapRoute;
            break;
        default:
            route = self.otherRoute;
            break;
    }
    
    route.entryType = @"RSRC";
    
    [[RouteManager sharedInstance] saveRecentSearchRoute:route withMaxHistory:10];
    
    self.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Back"]
                                                                             style:UIBarButtonItemStylePlain
                                                                            target:nil
                                                                            action:nil];
    
    ExpenseCommuteViewController *vc = [[ExpenseCommuteViewController alloc] init];
    
    RouteExpense *routeExpense = [[RouteExpense alloc] init];
    
    routeExpense.route = route;
    
    vc.routeExpense = routeExpense;
    
    [self.navigationController pushViewController:vc animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    CGFloat cellHeight = 0;
    
    Route *route = nil;
    
    switch ([indexPath section]) {
        case 0:
            route = self.fastRoute;
            break;
        case 1:
            route = self.easyRoute;
            break;
        case 2:
            route = self.cheapRoute;
            break;
        default:
            route = self.otherRoute;
            break;
    }
    
    cellHeight += ([route.segments count]) * 58;
    cellHeight += 26; // Last route
    cellHeight += 69; // Content other than the tableview.
    
    return cellHeight;
}

#pragma mark - Gesture recognizers

- (void)didTap:(UIGestureRecognizer *)recognizer {
    CGPoint tapLocation = [recognizer locationInView:self.tableView];
    
    NSIndexPath *indexPath = [self.tableView indexPathForRowAtPoint:tapLocation];
    
    [self.tableView selectRowAtIndexPath:indexPath animated:YES scrollPosition:UITableViewScrollPositionNone];
    
    [self.tableView.delegate tableView:self.tableView didSelectRowAtIndexPath:indexPath];
}

#pragma mark - Business logic

- (Route *)parseRoute:(NSString *)routeType fromElement:(RXMLElement *)rootXML {
    Route *route = [[Route alloc] init];
    
    // Copy route query date and seat type over.
    //
    route.date = self.routeSearchModel.date;
    route.seatType = self.routeSearchModel.seatType;
    route.isRoundTrip = self.routeSearchModel.isRoundTrip;
    
    // MWS can return multiple routes per search category ("fast", "easy', etc). Since
    // we're only showing one route per category then we're going to grab the "Route"
    // child rather than iterate over all of them.
    //
    RXMLElement *routeElement = [[rootXML child:routeType] child:@"Route"];
    
    //[routeTypeElement iterate:@"Route" usingBlock:^(RXMLElement *routeElement) {
        route.fare = [[routeElement child:@"Fare"].text intValue];
        route.minutes = [[routeElement child:@"Minutes"].text intValue];
    
        [routeElement iterate:@"Segments.Segment" usingBlock:^(RXMLElement *segmentElement) {
            Segment *segment = [[Segment alloc] init];
            
            segment.fare = [[segmentElement child:@"Fare"].text intValue];
            segment.additionalCharge = [[segmentElement child:@"AdditionalCharge"].text intValue];
            segment.minutes = [[segmentElement child:@"Minutes"].text intValue];
            segment.fromStation = [self parseFromStationWithElement:segmentElement];
            segment.toStation = [self parseToStationWithElement:segmentElement];
            segment.line = [self parseLineWithElement:segmentElement];
            segment.fromIsCommuterPass = [[segmentElement child:@"FromIsCommuterPass"].text boolValue];
            segment.toIsCommuterPass = [[segmentElement child:@"ToIsCommuterPass"].text boolValue];
            
            [route addSegment:segment];
        }];
    //}];
    
    return route;
}

- (Line *)parseLineWithElement:(RXMLElement *)element {
    
    Line *line = [[Line alloc] init];
    
    line.name = [element child:@"LineName"].text;
    line.key = [element child:@"LineKey"].text;
    
    return line;
}

- (Station *)parseStationFromElement:(RXMLElement *)element
                            withName:(NSString *)name
                              andKey:(NSString *)key {
    
    Station *station = [[Station alloc] init];
    
    station.name = [element child:name].text;
    station.key = [element child:key].text;
    
    return station;
}

- (Station *)parseFromStationWithElement:(RXMLElement *)element {
    return [self parseStationFromElement:element
                                withName:@"FromStationName"
                                  andKey:@"FromStationKey"];
}

- (Station *)parseToStationWithElement:(RXMLElement *)element {
    return [self parseStationFromElement:element
                                withName:@"ToStationName"
                                  andKey:@"ToStationKey"];
}

@end
