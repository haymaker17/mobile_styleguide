//
//  TabBarViewController.m
//  ConcurMobile
//
//  Created by Shifan Wu on 11/14/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "TabBarViewController.h"
#import "TabBarCell.h"
#import "ExSystem.h"
#import "UIColor+ConcurColor.m"

@interface TabBarViewController ()

@property (nonatomic, strong) NSMutableArray *iconImgs;
@property (nonatomic, strong) NSMutableArray *iconImgs_iPad;
@property (nonatomic, strong) NSMutableDictionary *lblAction;

@end

NSString *const kImageBookAction = @"action_trip.png";
NSString *const kImageReceiptAction = @"action_camera.png";
NSString *const kImageExpenseAction = @"action_add.png";
NSString *const kImageMileageAction = @"action_mileage.png";

NSString *const kImageBookActionIpad = @"action_trip_ipad.png";
NSString *const kImageReceiptActionIpad = @"action_camera_ipad.png";
NSString *const kImageExpenseActionIpad = @"action_add_ipad.png";
NSString *const kImageMileageActionIpad = @"action_mileage_ipad.png";

@implementation TabBarViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(refreshView) name:NotificationOnLoginSuccess object:nil];
    // MOB-16587 - This is not so nice way of updating the tab bar for Car mileage data.
    // The tabbar gets notified if there are any personal car mileage carrates available.
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(addCarMileage) name:NotificationHasCarRatesData object:nil];
    
    [self refreshView];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:NotificationOnLoginSuccess object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:NotificationHasCarRatesData object:nil];

}

#pragma mark -
#pragma mark Rotation support
- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [self.collectionView reloadData];
}

#pragma mark - UICollectionViewDataSource
- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView
{
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section
{
    return [self.iconImgs count];
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
    TabBarCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"TabBarCell" forIndexPath:indexPath];
    NSString *imageName = @"";
    NSString *actionLabel = [self.iconImgs objectAtIndex:indexPath.row];
    
    // MOB-17433: fixes to enable accessibility for UIAutomation
    [cell setIsAccessibilityElement:YES];
    [cell setAccessibilityIdentifier:[self.lblAction objectForKey:actionLabel]];
    
    if ([UIDevice isPad])
        imageName = [self.iconImgs_iPad objectAtIndex:indexPath.row];
    else
        imageName = [self.iconImgs objectAtIndex:indexPath.row];
    cell.ivIconImage.image = [UIImage imageNamed: imageName];
    cell.lblAction.text = [self.lblAction objectForKey:actionLabel];
    [cell.lblLine setHidden:NO];
    
    // Hide white line for the last item
    if ([self.iconImgs count] == indexPath.row + 1) {
        [cell.lblLine setHidden:YES];
    }
    
    return cell;
}

#pragma mark - UICollectionViewDelegate

- (BOOL)collectionView:(UICollectionView *)collectionView shouldHighlightItemAtIndexPath:(NSIndexPath *)indexPath
{
    return YES;
}

- (void)collectionView:(UICollectionView *)colView didHighlightItemAtIndexPath:(NSIndexPath *)indexPath
{
    UICollectionViewCell* cell = [colView cellForItemAtIndexPath:indexPath];
    cell.contentView.backgroundColor = [UIColor brightBlueConcur];
}

- (void)collectionView:(UICollectionView *)colView didUnhighlightItemAtIndexPath:(NSIndexPath *)indexPath
{
    UICollectionViewCell* cell = [colView cellForItemAtIndexPath:indexPath];
    cell.contentView.backgroundColor = [UIColor blueConcur];
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath
{
    // Use icon image to determin the clicking event
    if ([[self.iconImgs objectAtIndex:indexPath.row] isEqualToString:kImageBookAction])
    {
        self.selectOption(@{@"Book":@"YES"});
    }
    else if ([[self.iconImgs objectAtIndex:indexPath.row] isEqualToString:kImageReceiptAction])
    {
        self.selectOption(@{@"Receipt":@"YES"});
    }
    else if ([[self.iconImgs objectAtIndex:indexPath.row] isEqualToString:kImageExpenseAction])
    {
        self.selectOption(@{@"Expense":@"YES"});
    }
    else if ([[self.iconImgs objectAtIndex:indexPath.row] isEqualToString:kImageMileageAction])
    {
        self.selectOption(@{@"Mileage":@"YES"});
    }
    
}

- (void)collectionView:(UICollectionView *)collectionView didDeselectItemAtIndexPath:(NSIndexPath *)indexPath
{
    UICollectionViewCell * cell = [collectionView  cellForItemAtIndexPath:indexPath];
    cell.backgroundColor = [UIColor blueConcur];
}

#pragma mark - UICollectionViewFlowLayout
- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath
{
    if ([self.iconImgs count] == 0) {
        return CGSizeMake(0, 0);
    }
    
    if ([UIDevice isPad])
    {
        return CGSizeMake(self.collectionView.frame.size.width/[self.iconImgs count], 100);
    }
    else
    {
        return CGSizeMake(self.collectionView.frame.size.width/[self.iconImgs count], 73);
    }
    
}

- (CGFloat)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout minimumInteritemSpacingForSectionAtIndex:(NSInteger)section
{
    return 0;
}

- (UIEdgeInsets)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout insetForSectionAtIndex:(NSInteger)section
{
    return UIEdgeInsetsMake(0, 0, 0, 0);
}

- (void)refreshView
{
    if ([[ExSystem sharedInstance] isExpenseAndApprovalOnlyUser])         // Expense and Approval ONLY
    {
        self.iconImgs = @[kImageReceiptAction, kImageExpenseAction].mutableCopy;
        self.iconImgs_iPad = @[kImageReceiptActionIpad, kImageExpenseActionIpad].mutableCopy;
        self.lblAction = @{kImageReceiptAction: [@"Receipt" localize], kImageExpenseAction: [@"Expense" localize]}.mutableCopy;
    }
    else if ([[ExSystem sharedInstance] isExpenseOnlyUser])                                                     // Expense ONLY
    {
        self.iconImgs = @[kImageReceiptAction, kImageExpenseAction].mutableCopy;
        self.iconImgs_iPad = @[kImageReceiptActionIpad, kImageExpenseActionIpad].mutableCopy;
        self.lblAction = @{kImageReceiptAction: [@"Receipt" localize], kImageExpenseAction: [@"Expense" localize]}.mutableCopy;
    }
    else if ([[ExSystem sharedInstance] isTravelOnly] || [[ExSystem sharedInstance] isApprovalOnlyUser] || [[ExSystem sharedInstance] isTravelAndApprovalOnlyUser])        // Travel or Approval ONLY or TravelAndApproval ONLY
    {
        self.iconImgs = @[].mutableCopy;
        self.iconImgs_iPad = @[].mutableCopy;
    }
    else if ([[ExSystem sharedInstance] isGovernment])
    {
        self.iconImgs = @[kImageBookAction, kImageExpenseAction].mutableCopy;
        self.iconImgs_iPad = @[kImageBookActionIpad, kImageExpenseActionIpad].mutableCopy;
        self.lblAction = @{kImageBookAction: [@"Book" localize], kImageExpenseAction: [@"Expense" localize]}.mutableCopy;
    }
    else                                                                                                        // Full feature user
    {
        //  MOB-16631 - Check if user has travelbooking before adding the book travel button to the list.
        // The below check uses the same logic used to determine the "+" button for trip list view.
        if ([[ExSystem sharedInstance] hasTravelBooking])
        {
            self.iconImgs = @[kImageBookAction, kImageReceiptAction, kImageExpenseAction].mutableCopy;
            self.iconImgs_iPad = @[kImageBookActionIpad, kImageReceiptActionIpad, kImageExpenseActionIpad].mutableCopy;
            self.lblAction = @{kImageBookAction: [@"Book" localize], kImageReceiptAction: [@"Receipt" localize], kImageExpenseAction: [@"Expense" localize]}.mutableCopy;
        }
        else
        {
        // MOB-17062 - Added checks for showing receipts and expense icon 
            self.iconImgs = @[].mutableCopy;
            self.iconImgs_iPad = @[].mutableCopy;
            self.lblAction =  [[NSMutableDictionary alloc]init];
            // MOB-17062 User roles: Expense user role is in mobile despite user does not have it
            if([[ExSystem sharedInstance] hasReceiptStore])
            {
                [self.iconImgs addObject:kImageReceiptAction];
                [self.iconImgs_iPad addObject:kImageReceiptActionIpad];
                [self.lblAction setObject:[@"Receipt" localize] forKey:kImageReceiptAction];
            }

            if([[ExSystem sharedInstance] isExpenseRelated])
            {
                [self.iconImgs addObject:kImageExpenseAction];
                [self.iconImgs_iPad addObject:kImageExpenseActionIpad];
                [self.lblAction setObject:[@"Expense" localize] forKey:kImageExpenseAction];
            }
        }
    }

    [self.collectionView reloadData];
}

/**
 Adds carmileage icon to the list and refresh collection.
 */
-(void)addCarMileage
{
        // Add car mileage on home.  I believe the server already checks to see if this is a valid setting for your roles.
        if ([[ExSystem sharedInstance] hasCarMileageOnHome] && ![self.iconImgs containsObject:kImageMileageAction]) {
            [self.iconImgs addObject:kImageMileageAction];
            [self.iconImgs_iPad addObject: kImageMileageActionIpad];
            [self.lblAction setObject:[@"Mileage" localize] forKey:kImageMileageAction];
        }
    [self.collectionView performBatchUpdates:^{
        [self.collectionView reloadSections:[NSIndexSet indexSetWithIndex:0]];
    } completion:nil];
}
@end
