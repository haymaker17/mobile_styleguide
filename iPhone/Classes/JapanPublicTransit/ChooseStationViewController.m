//
//  ChooseStationViewController.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/15/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ChooseStationViewController.h"
#import "CXClient.h"
#import "CXRequest.h"
#import "Localizer.h"
#import "RXMLElement.h"
#import "Station.h"
#import "StationSearchRequestFactory.h"

@interface ChooseStationViewController ()

@property (strong, nonatomic) NSMutableArray *stations;

@end

@implementation ChooseStationViewController

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    [self.searchBar becomeFirstResponder];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // If iOS 7
    //
    if ([self respondsToSelector:@selector(setEdgesForExtendedLayout:)]) {
        self.edgesForExtendedLayout = UIRectEdgeNone;
    }
    
    self.stations = [[NSMutableArray alloc] init];
    
    [self.searchBar setPlaceholder:[Localizer getLocalizedText:@"search_stations"]];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

#pragma mark - Business

- (void)startRequestForTerm:(NSString *)term {
    self.request = [StationSearchRequestFactory searchForStation:term];
    
    [[CXClient sharedClient] performRequest:self.request success:^(NSString *result) {
        [self.stations removeAllObjects];
        
        RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
        
        [rootXML iterate:@"ListItems.ListItem" usingBlock:^(RXMLElement *item) {
            Station *station = [[Station alloc] init];
            
            station.key = [item child:@"Key"].text;
            station.name = [item child:@"Text"].text;
            
            [self.stations addObject:station];
        }];

        [self.tableView reloadData];
    } failure:^(NSError *error) {
        NSLog(@"Error = %@", error);
    }];
}

#pragma mark - Utility Methods

// Checks that each search token matches the beginning of at least one part of
// the station name. If any search token fails to match then we return NO, which
// means that the search term did not match.
//
- (BOOL)station:(Station *)station matchesAllTerms:(NSArray *)tokens {
    NSString *stationName = station.name;
    NSCharacterSet *whitespace = [NSCharacterSet whitespaceCharacterSet];
    NSMutableArray *stationTokens = (NSMutableArray *)[stationName componentsSeparatedByCharactersInSet:whitespace];
    
    for (NSString *token in tokens) {
        if (![token length]) {
            continue;
        }
        
        BOOL matchedStationToken = NO;
        
        for (NSString *stationToken in stationTokens) {
            if ([stationToken rangeOfString:token options:NSAnchoredSearch|NSCaseInsensitiveSearch].location == 0) {
                matchedStationToken = YES;
            }
        }
        
        if (matchedStationToken == NO) {
            return NO;
        }
    }
    
    return YES;
}

- (NSArray *)filterStations:(NSArray *)stations withTerm:(NSString *)term {
    NSMutableArray *filteredStations = [[NSMutableArray alloc] init];
    
    NSCharacterSet *whitespace = [NSCharacterSet whitespaceCharacterSet];
    
    NSMutableArray *tokens = (NSMutableArray *)[term componentsSeparatedByCharactersInSet:whitespace];
    
    for (Station *station in stations) {
        if ([self station:station matchesAllTerms:tokens]) {
            [filteredStations addObject:station];
        }
    }
    
    return filteredStations;
}

#pragma mark - UISearchBarDelegate

- (void)searchBar:(UISearchBar *)searchBar textDidChange:(NSString *)searchText {
    if (![searchText length]) {
        [self.stations removeAllObjects];
        [self.tableView reloadData];
        return;
    }
    
    [self startRequestForTerm:searchText];
}

#pragma mark - UITableViewDataSource

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView
                             dequeueReusableCellWithIdentifier:@"StationCell"];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"StationCell"];
    }
    
    NSInteger row = [indexPath row];

    Station *station = self.stations[row];
    
    cell.textLabel.text = station.name;
    
    return cell;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [self.stations count];
    
    //return [self.filteredStations count];
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    NSInteger row = [indexPath row];
    
    Station *station = self.stations[row];

    if (station != nil) {
        [[NSNotificationCenter defaultCenter] postNotificationName:self.notificationName object:station];
    }
    
    [[self navigationController] popViewControllerAnimated:YES];
}

@end
