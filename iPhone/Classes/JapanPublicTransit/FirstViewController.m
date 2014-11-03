//
//  FirstViewController.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/12/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AddRouteSearchViewController.h"
#import "AddRouteManualViewController.h"
#import "AddRouteSearchViewController.h"
#import "AnalyticsManager.h"
#import "CXClient.h"
#import "ExpenseSavedRouteViewController.h"
#import "FavoriteRoutesRequestFactory.h"
#import "FirstViewController.h"
#import "JPTUtils.h"
#import "JorudanSearchRequestFactory.h"
#import "LineSearchRequestFactory.h"
#import "Localizer.h"
#import "Route.h"
#import "RouteExpenseManager.h"
#import "RouteManager.h"
#import "StationSearchRequestFactory.h"
#import "SearchResultsViewController.h"

NSInteger const TYPE_RECENT = 0;
NSInteger const TYPE_FAVORITES = 1;
NSInteger const TYPE_SAVED = 2;

NSInteger const ADD_ROUTE_SEARCH = 0;
NSInteger const ADD_ROUTE_MANUAL = 1;

@interface FirstViewController ()

@end

@implementation FirstViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
        UIBarButtonItem *addButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd
                                                                                   target:self
                                                                                   action:@selector(rightNavButtonTapped:)];
        
        [self.navigationItem setRightBarButtonItem:addButton];
        
        // add close button for ipad only
        if ([UIDevice isPad])
        {
            UIBarButtonItem *closeButton = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered
                                                                           target:self
                                                                           action:@selector(leftNavButtonTapped:)];
            [self.navigationItem setLeftBarButtonItem:closeButton];
        }
    }
    
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [[AnalyticsManager sharedInstance] clearImpressionPath];
    
    // If iOS 7
    //
    if ([self respondsToSelector:@selector(setEdgesForExtendedLayout:)]) {
        self.edgesForExtendedLayout = UIRectEdgeNone;
        self.typeControlBackground.backgroundColor = [UIColor whiteColor];
    }

    [self.typeControl setTitle:[Localizer getLocalizedText:@"recent"]
             forSegmentAtIndex:0];
    
    [self.typeControl setTitle:[Localizer getLocalizedText:@"favorites"]
             forSegmentAtIndex:1];
    
    [self.typeControl setTitle:[Localizer getLocalizedText:@"Saved"]
             forSegmentAtIndex:2];
    
    [self.addNewRouteButton setTitle:[Localizer getLocalizedText:@"add_new_route"] forState:UIControlStateNormal];
    [self.addNewRouteButton setTitle:[Localizer getLocalizedText:@"add_new_route"] forState:UIControlStateSelected];
    
    [self.typeControl setSelectedSegmentIndex:1];
    
    self.navigationItem.title = [Localizer getLocalizedText:@"routes"];
    
    [self.navigationController setToolbarHidden:YES animated:NO];

    self.editButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemEdit
                                                                                target:self
                                                                                action:@selector(toggleEditMode:)];
    
    self.doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone
                                                                                target:self
                                                                                action:@selector(toggleEditMode:)];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    [self typeChanged:nil];
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    
    [self hideContentWithDuration:0];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    [self.tableView reloadData];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    [self.navigationController setToolbarHidden:YES animated:YES];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

#pragma mark - UIActionSheetDelegate

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    switch (buttonIndex) {
        case ADD_ROUTE_MANUAL: {
            self.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc]
                                                     initWithTitle:[Localizer getLocalizedText:@"Back"]
                                                     style:UIBarButtonItemStylePlain
                                                     target:nil
                                                     action:nil];
            
            AddRouteManualViewController *vc = [[AddRouteManualViewController alloc] init];
            [self.navigationController pushViewController:vc animated:YES];
            break;
        }
        case ADD_ROUTE_SEARCH: {
            self.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc]
                                                     initWithTitle:[Localizer getLocalizedText:@"Back"]
                                                     style:UIBarButtonItemStylePlain
                                                     target:nil
                                                     action:nil];
            
            AddRouteSearchViewController *vc = [[AddRouteSearchViewController alloc] init];
            [self.navigationController pushViewController:vc animated:YES];
        }
        default:
            return;
    }
}

#pragma mark - Interface callbacks

- (IBAction)addNewRouteButtonTapped:(id)sender {
    [self addNewRoute];
}

- (IBAction)typeChanged:(id)sender {
    [self setEditingMode:NO];
    
    NSInteger selectedIndex = [self.typeControl selectedSegmentIndex];	
    
    switch (selectedIndex) {
        case TYPE_FAVORITES:
            [self switchToFavoriteRoutes:sender];
            break;
        case TYPE_RECENT:
            [self switchToRecentRoutes:sender];
            break;
        case TYPE_SAVED:
            [self switchToSavedExpenses:sender];
            break;
        default:
            break;
    }
}

- (void)rightNavButtonTapped:(id)sender {
    [self setEditingMode:NO];
    
    [self addNewRoute];
}

- (void)leftNavButtonTapped:(id)sender{
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)toggleEditMode:(id)sender {
    if (self.tableView.editing) {
        [self.tableView setEditing:NO animated:YES];
        
        self.toolbarItems = [NSArray arrayWithObjects:self.editButton, nil];
    } else {
        [self.tableView setEditing:YES animated:YES];
        
        self.toolbarItems = [NSArray arrayWithObjects:self.doneButton, nil];
    }
}

#pragma mark - Business logic

- (void)addNewRoute {
    UIActionSheet *newRouteActionSheet = [[UIActionSheet alloc]
                                          initWithTitle:nil
                                          delegate:self
                                          cancelButtonTitle:[Localizer getLocalizedText:@"Cancel"]
                                          destructiveButtonTitle:nil
                                          otherButtonTitles:[Localizer getLocalizedText:@"Search"],
                                          [Localizer getLocalizedText:@"manual"], nil];

    if (self.navigationController.toolbarHidden) {
        [newRouteActionSheet showInView:self.view];
    } else {
        [newRouteActionSheet showFromToolbar:self.navigationController.toolbar];
    }
}

- (BOOL)hasFavoriteRoutes {
    NSArray *favoriteRouteExpenses = [[RouteExpenseManager sharedInstance] fetchFavoriteRouteExpenses];
    
    return [favoriteRouteExpenses count] > 0;
}

- (BOOL)hasRecentSearches {
    NSArray *recentSearches = [[RouteManager sharedInstance] fetchRecentSearchRoutes];
    
    return [recentSearches count] > 0;
}

- (BOOL)hasSavedExpenses {
    NSArray *savedRoutes = [[RouteExpenseManager sharedInstance] fetchSavedExpenses];
    
    return [savedRoutes count] > 0;
}

- (void)hideContentWithDuration:(NSInteger)fadeDuration {
    [UIView animateWithDuration:fadeDuration animations:^{
        self.emptyView.alpha = 0;
        self.tableView.alpha = 0;
    }];
}

- (void)hideEmptyView {
    [UIView animateWithDuration:0.2 animations:^{
        self.emptyView.alpha = 0;
    }];
}

- (void)hideRoutesTable {
    [UIView animateWithDuration:0.1 animations:^{
        self.tableView.alpha = 0;
    }];
}

- (void)setEditingMode:(BOOL)arg {
    if (arg == NO) {
        self.toolbarItems = [NSArray arrayWithObjects:self.editButton, nil];
        
        [self.tableView setEditing:NO animated:YES];
    }
}

- (void)showEmptyViewWithMessage:(NSString *)message {
    [UIView animateWithDuration:0.2 animations:^{
        self.emptyView.alpha = 1;
    }];

    self.emptyViewMessage.text = message;
    
    [self.navigationController setToolbarHidden:YES animated:YES];
}

- (void)showFavoriteRoutes {
    [UIView animateWithDuration:0.1 animations:^{
        self.tableView.alpha = 1;
    }];
    
    self.toolbarItems = [NSArray arrayWithObjects:self.editButton, nil];
    
    [self.navigationController setToolbarHidden:NO animated:YES];
    
    if (self.favoriteRoutesSource == nil) {
        self.favoriteRoutesSource = [[FavoriteRoutesSource alloc] init];
        self.favoriteRoutesSource.favoriteRouteSourceDelegate = self;
        self.favoriteRoutesSource.delegate = self;
    }
    
    self.currentRoutesSource = self.favoriteRoutesSource;
    
    self.tableView.dataSource = self.currentRoutesSource;
    self.tableView.delegate = self.currentRoutesSource;
    
    [self.tableView reloadData];
}

- (void)showRecentRoutes {
    [UIView animateWithDuration:0.1 animations:^{
        self.tableView.alpha = 1;
    }];

    [self.navigationController setToolbarHidden:YES animated:YES];
    
    if (self.recentRoutesSource == nil) {
        self.recentRoutesSource = [[RecentRoutesSource alloc] init];
        self.recentRoutesSource.delegate = self;
    }
    
    self.currentRoutesSource = self.recentRoutesSource;
    
    self.tableView.dataSource = self.currentRoutesSource;
    self.tableView.delegate = self.currentRoutesSource;
    
    [self.tableView reloadData];
}

- (void)showSavedExpenses {
    [UIView animateWithDuration:0.1 animations:^{
        self.tableView.alpha = 1;
    }];
    
    self.toolbarItems = [NSArray arrayWithObjects:self.editButton, nil];
    
    [self.navigationController setToolbarHidden:NO animated:YES];
    
    if (self.savedRoutesSource == nil) {
        self.savedRoutesSource = [[SavedRoutesSource alloc] init];
        self.savedRoutesSource.savedRouteSourceDelegate = self;
        self.savedRoutesSource.delegate = self;
    }
    
    self.currentRoutesSource = self.savedRoutesSource;
    
    self.tableView.dataSource = self.currentRoutesSource;
    self.tableView.delegate = self.currentRoutesSource;
    
    [self.tableView reloadData];
}

- (void)switchToFavoriteRoutes:(id)sender {
    
    // We only want to log this stuff if action initiated by user, not by the system.
    //
    if (sender) {
        [[AnalyticsManager sharedInstance] logCategory:@"JPT"
                                              withName:@"Viewed"
                                               andType:@"Favorite"];
    }
    
    if ([self hasFavoriteRoutes]) {
        [self hideEmptyView];
        [self showFavoriteRoutes];
    } else {
        [self hideRoutesTable];
        [self showEmptyViewWithMessage:[self.currentRoutesSource emptyMessage]];
    }
}

- (void)switchToRecentRoutes:(id)sender {
    if (sender) {
        [[AnalyticsManager sharedInstance] logCategory:@"JPT"
                                              withName:@"Viewed"
                                               andType:@"Recent"];
    }
    
    if ([self hasRecentSearches]) {
        [self hideEmptyView];
        [self showRecentRoutes];
    } else {
        [self hideRoutesTable];
        [self showEmptyViewWithMessage:[self.currentRoutesSource emptyMessage]];
    }
}

- (void)switchToSavedExpenses:(id)sender {
    if (sender) {
        [[AnalyticsManager sharedInstance] logCategory:@"JPT"
                                              withName:@"Viewed"
                                               andType:@"Saved"];
    }
    
    if ([self hasSavedExpenses]) {
        [self hideEmptyView];
        [self showSavedExpenses];
    } else {
        [self hideRoutesTable];
        [self showEmptyViewWithMessage:[self.currentRoutesSource emptyMessage]];
    }
}

#pragma mark - RouteSourceDelegate

- (void)didDeleteLastItemForRouteSource:(AbstractRouteSource *)routeSource {
    if (self.tableView.editing) {
        [self.tableView setEditing:NO animated:YES];
        
        //[self.navigationController setToolbarHidden:YES animated:YES];
    }
    
    [self hideRoutesTable];
    [self showEmptyViewWithMessage:[self.currentRoutesSource emptyMessage]];
}

- (void)routeSource:(AbstractRouteSource *)routeSource didSelectRoute:(Route *)route {
    
    self.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Back"]
                                                                             style:UIBarButtonItemStylePlain
                                                                            target:nil
                                                                            action:nil];
    
    ExpenseSavedRouteViewController *vc = [[ExpenseSavedRouteViewController alloc] init];
    
    vc.routeExpense.route = route;
    
    [self.navigationController pushViewController:vc animated:YES];
}

#pragma mark - SavedRouteSourceDelegate

- (void)routeSource:(SavedRoutesSource *)routeSource didSelectSavedExpense:(RouteExpense *)routeExpense {
    
    self.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Back"]
                                                                             style:UIBarButtonItemStylePlain
                                                                            target:nil
                                                                            action:nil];
    
    ExpenseSavedRouteViewController *vc = [[ExpenseSavedRouteViewController alloc] init];
   
    vc.routeExpense = routeExpense;
    
    [self.navigationController pushViewController:vc animated:YES];
}

@end
