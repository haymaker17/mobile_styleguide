//
//  MoreMenuViewController.m
//  ConcurMobile
//
//  Created by ernest cho on 3/11/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MoreMenuViewController.h"
#import "MoreMenuSectionHeader.h"
#import "MoreMenuCell.h"

#import "MoreMenuData.h"
#import "MoreMenuSectionData.h"
#import "MoreMenuRowData.h"
#import "AdView.h"

@interface MoreMenuViewController ()

@property (strong, nonatomic) MoreMenuData *menuData;
@property (strong, nonatomic) AdView *adView;

@end

@implementation MoreMenuViewController


- (id)init
{
    self = [super init];
    if (self) {
        //self.menuData = [[MoreMenuData alloc] init];
        //self.adView = [[AdView alloc] init];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateMenuData) name:NotificationOnLoginSuccess object:nil];
    // MOB-16587 - This is not so nice way of updating the tab bar for Car mileage data.
    // The tabbar gets notified if there are any personal car mileage carrates available.
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateMenuData) name:NotificationHasCarRatesData object:nil];

    self.menuData = [[MoreMenuData alloc] init];
    self.adView = [[AdView alloc] init];
    if ([UIDevice isPad]) {
        [self.navigationController.navigationBar setHidden:YES];
    } else {
        [self setupNavBar];
    }
    [self setupToolbar];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self.navigationController setToolbarHidden:YES animated:NO];
}

-(void)updateMenuData
{
    self.menuData = [[MoreMenuData alloc] init];
    
    [self.tableView reloadData];
}

-(void)dealloc
{
    [[NSNotificationCenter defaultCenter]removeObserver:self name:NotificationOnLoginSuccess object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:NotificationHasCarRatesData object:nil];
}

- (CGSize)preferredContentSize{
    return CGSizeMake(320, 1024);
}
- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration{
    [super willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
    //self.navigationController.view.frame = CGRectMake(0, 20, self.preferredContentSize.width, self.parentViewController.view.bounds.size.height);
}

- (void)setupNavBar
{

    if ([UIDevice isPad]) {
        // MOB-16540 graphical issue with more menu on after returning from a trip detail or any other full screen. Clear the title.
        self.title = @"";
    } else {
        self.title = [Localizer getLocalizedText:@"Menu"];
        self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Home"] style:UIBarButtonItemStyleBordered target:self action:@selector(goBack)];
    }
}

- (void)goBack
{
    if ([UIDevice isPad])
    {
        if (self.tapHome)
            self.tapHome();
    }
    else
        [self dismissViewControllerWithDirection:@"fromRight"];
}

- (void) dismissViewControllerWithDirection:(NSString *) direction {
    
    [CATransaction begin];
    
    CATransition *transition = [CATransition animation];
    transition.type = kCATransitionPush;
    transition.subtype = direction;
    transition.duration = 0.25f;
    transition.fillMode = kCAFillModeForwards;
    transition.removedOnCompletion = YES;
    
    [[UIApplication sharedApplication].keyWindow.layer addAnimation:transition forKey:@"transition"];
    [self dismissViewControllerAnimated:NO completion:nil];
    [CATransaction commit];
}

-(void)setupToolbar
{
    if ([self.adView shouldShowAd]) {
        self.tableView.tableFooterView = self.adView;
    }
    [self.navigationController setToolbarHidden:YES animated:NO];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (BOOL)enablePullDownRefresh
{
    return NO;
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [self.menuData numberOfSections];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.menuData numberOfRowsInSection:section];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"MoreMenuCell";
    MoreMenuCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[MoreMenuCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
        
        cell.label.text = [self.menuData getTextForCell:indexPath];
        [cell.icon setImage:[self.menuData getImageForCell:indexPath]];
        
        // set the label's accessibility label for automation
        [cell.label setAccessibilityLabel:[NSString stringWithFormat:@"%@ Menu", cell.label.text]];
    }

    return cell;
}

// use custom section headers
- (UIView *) tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    MoreMenuSectionHeader *headerView = [[MoreMenuSectionHeader alloc] initWithFrame:CGRectMake(0, 0, tableView.bounds.size.width, 22)];
    headerView.title.text = [self tableView:tableView titleForHeaderInSection:section];
    
    return headerView;
}

// get section heights. empty section headers are not shown
- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    NSString *tmp = [self tableView:tableView titleForHeaderInSection:section];
    if (tmp == nil || [tmp length] == 0) {
        return 0.0;
    }
    return 22.0;
}

// return section header titles
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return [self.menuData getTitleForSection:section];
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    CGRect location = [tableView rectForRowAtIndexPath:indexPath];
    if ([UIDevice isPad]) {
        if ([self.menuData didSelectCell:indexPath withView:self.ipadHome atLocation:location]) {
            if (self.tapHome) {
                self.tapHome();
            }else{
                NSLog(@"MoreMenuViewController -- tapHome was not defined so what I am suppossed to do?");
            }
            
        }
    }else {
        [self.menuData didSelectCell:indexPath withView:self atLocation:location];
    }
    
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

@end
