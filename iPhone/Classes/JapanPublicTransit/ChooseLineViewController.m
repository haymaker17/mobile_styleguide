//
//  ChooseLineViewController.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/11/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ChooseLineViewController.h"
#import "CXClient.h"
#import "CXRequest.h"
#import "Line.h"
#import "LineSearchRequestFactory.h"
#import "RXMLElement.h"

@interface ChooseLineViewController ()

@property (strong, nonatomic) NSMutableArray *lines;
@property (strong, nonatomic) NSArray *filteredLines;

@end

@implementation ChooseLineViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
        // Custom initialization
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

    self.lines = [[NSMutableArray alloc] init];
    
    CXRequest *request = [LineSearchRequestFactory searchForLineByStation:self.stationKey];
    
    [[CXClient sharedClient] performRequest:request success:^(NSString *result) {
        RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
        
        [rootXML iterate:@"ListItems.ListItem" usingBlock:^(RXMLElement *item) {
            Line *line = [[Line alloc] init];
            
            line.key = [item child:@"Key"].text;
            line.name = [item child:@"Text"].text;
            
            [self.lines addObject:line];
        }];
        
        self.filteredLines = self.lines;
        
        [self.tableView reloadData];
    } failure:^(NSError *error) {
        NSLog(@"Error = %@", error);
    }];
    
    self.filteredLines = self.lines;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

#pragma mark - Business

- (NSArray *)filterLines:(NSArray *)lines withTerm:(NSString *)term {
    NSMutableArray *filteredLines = [[NSMutableArray alloc] init];
    
    NSCharacterSet *whitespace = [NSCharacterSet whitespaceCharacterSet];
    
    NSMutableArray *tokens = (NSMutableArray *)[term componentsSeparatedByCharactersInSet:whitespace];
    
    for (Line *line in lines) {
        if ([self line:line matchesAllTerms:tokens]) {
            [filteredLines addObject:line];
        }
    }
    
    return filteredLines;
}

- (BOOL)line:(Line *)line matchesAllTerms:(NSArray *)tokens {
    NSString *lineName = line.name;
    NSCharacterSet *whitespace = [NSCharacterSet whitespaceCharacterSet];
    NSMutableArray *lineTokens = (NSMutableArray *)[lineName componentsSeparatedByCharactersInSet:whitespace];
    
    for (NSString *token in tokens) {
        if (![token length]) {
            continue;
        }
        
        BOOL matchedLineToken = NO;
        
        for (NSString *lineToken in lineTokens) {
            if ([lineToken rangeOfString:token options:NSAnchoredSearch|NSCaseInsensitiveSearch].location == 0) {
                matchedLineToken = YES;
            }
        }
        
        if (matchedLineToken == NO) {
            return NO;
        }
    }
    
    return YES;
}

#pragma mark - UISearchBarDelegate

- (void)searchBar:(UISearchBar *)searchBar textDidChange:(NSString *)searchText {
    if (![searchText length]) {
        self.filteredLines = self.lines;
        [self.tableView reloadData];
        return;
    }
    
    self.filteredLines = [self filterLines:self.lines withTerm:searchText];
    
    [self.tableView reloadData];
}

#pragma mark - UITableViewDataSource

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView
                             dequeueReusableCellWithIdentifier:@"StationCell"];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"LineCell"];
    }
    
    NSInteger row = [indexPath row];
    
    Line *line = self.filteredLines[row];
    
    cell.textLabel.text = line.name;
    
    return cell;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [self.filteredLines count];
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    NSInteger row = [indexPath row];
    
    Line *line = self.filteredLines[row];
    
    if (line != nil) {
        [[NSNotificationCenter defaultCenter] postNotificationName:self.notificationName object:line];
    }
    
    [[self navigationController] popViewControllerAnimated:YES];
}

@end
