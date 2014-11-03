//
//  MobileTourCollectionVC.m
//  ConcurMobile
//
//  Created by Sally Yan on 2/19/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "MobileTourCollectionViewController.h"
#import "TourCell.h"
#import "MobileTourData.h"

@interface MobileTourCollectionViewController ()

@property (strong, nonatomic) NSString *localizedTitle;
@property (strong, nonatomic) MobileTourData *mobileTourData;
@property BOOL isOrientationChanged;

@end

static NSString *kCollectionViewCellIdentifier = @"TourCell";

@implementation MobileTourCollectionViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // registier cell
    }
    return self;
}


- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if ([self respondsToSelector:@selector(setAutomaticallyAdjustsScrollViewInsets:)]) {
        self.automaticallyAdjustsScrollViewInsets = NO;
    }
    
    // set up the data for collection view
    self.mobileTourData = [[MobileTourData alloc] init];
    if (self.onPageDidChange) {
        self.onPageDidChange(0);
    }
    
    // MOB-17704: do not allow the user to scoll the view when there is only one page
    if([self totalNumberOfPages] == 1){
        [self.collectionView setScrollEnabled:NO];
    }
    
    self.isOrientationChanged = NO;
    if([UIDevice isPad]){
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didRotate:) name:UIDeviceOrientationDidChangeNotification object:nil];
    }
}

-(void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIDeviceOrientationDidChangeNotification object:nil];
    
}

- (void)didRotate:(NSNotification *)notification
{
    self.isOrientationChanged = YES;        // bool flag to adjust the image offset for orientation
    [self.collectionView reloadData];
    [self.view setNeedsUpdateConstraints];
    [self.collectionView layoutIfNeeded];
    [self.collectionViewLayout invalidateLayout];
}

- (NSInteger)totalNumberOfPages{
    //since there is only 1 section
    if ([UIDevice isPhone]){
        return  [self.mobileTourData.introScreens_iPhone count];
    } else {
        return [self.mobileTourData.introScreens_iPad count];
    }
}

#pragma mark - UICollectionView Delegate Method

-(CGSize)collectionViewContentSize
{
    if ([UIDevice isPhone]){
        return CGSizeMake(280, 320);
    }
    else {
        if (UIDeviceOrientationIsPortrait(self.interfaceOrientation)){
            return CGSizeMake(650.0f, 718.0f);
        }
        else{
            return CGSizeMake(455, 493);
        }
    }
}

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView
{
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section
{
    return [self totalNumberOfPages];
}

- (UICollectionViewCell *) collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
    TourCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:kCollectionViewCellIdentifier forIndexPath:indexPath];
    NSDictionary *dictionary = nil;
    if ([UIDevice isPhone]){
        dictionary = [self.mobileTourData.introScreens_iPhone objectAtIndex:indexPath.row];
    }
    else {  // iPad
        dictionary = [self.mobileTourData.introScreens_iPad objectAtIndex:indexPath.row];
        if (UIDeviceOrientationIsPortrait(self.interfaceOrientation)){
            cell.textViewWidth.constant = 650.0f;
            cell.labelTitleWidth.constant = 650.0f;
        }
        else {
            cell.textViewWidth.constant = 445.0f;
            cell.labelTitleWidth.constant = 445.0f;
        }
    }

    // get the title and subtitle
    cell.labelTitle.text = dictionary[@"Title"];
    cell.labelSubTitle.text = dictionary[@"SubTitle"];
    cell.imageView.image = [UIImage imageNamed:dictionary[@"Image"]];
    
    // set up the labels' size
    cell.labelTitleHeight.constant = [self getLabelSize:cell.labelTitle].height;
    cell.labelSubTitileHeight.constant = [self getLabelSize:cell.labelSubTitle].height + 5;
    if ([UIDevice isPhone]){
        cell.coSubTitleBottom.constant = 20.0f;
    }
    else{
        cell.coSubTitleBottom.constant = 30.0f;
    }
    
    // adjust height of the view behind the text labels
    if ([UIDevice isPhone]){
        cell.textViewHeight.constant = cell.labelTitleHeight.constant + cell.labelSubTitileHeight.constant + 27;
    }
    else{   // the height should be taller for iPad
        cell.textViewHeight.constant = cell.labelTitleHeight.constant + cell.labelSubTitileHeight.constant + 37;
    }
    cell.coTitileTop.constant = 5;
    
    //MOB-17654: since the size for landscape and portrait is different, when orientation changes, need to adjust the offset
    //    to display the current page
    if (self.isOrientationChanged && indexPath.row == self.currentPageIndex) {
        if (UIDeviceOrientationIsPortrait(self.interfaceOrientation)){
            self.collectionView.contentOffset = CGPointMake(650.0 * self.currentPageIndex, 0.0);
        } else {
            self.collectionView.contentOffset = CGPointMake(450.0 * self.currentPageIndex - 5 , 0.0);
        }
        self.isOrientationChanged = NO;
    }
    return cell;
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout  *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath
{
    if ([UIDevice isPad]){
        if (UIDeviceOrientationIsLandscape(self.interfaceOrientation)){
            return CGSizeMake(445, 493);
        }
        else {
            return CGSizeMake(650, 718);
        }
    }
    return CGSizeMake(280.0, 320.0);
}

- (UIEdgeInsets)collectionView:
(UICollectionView *)collectionView layout:(UICollectionViewLayout*)collectionViewLayout insetForSectionAtIndex:(NSInteger)section {

    return UIEdgeInsetsMake(0,0,0,0);  // top, left, bottom, right
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView
{
    NSInteger indexPath = self.collectionView.contentOffset.x / self.collectionView.frame.size.width;
    self.currentPageIndex = indexPath;
    if (self.onPageDidChange) {
        self.onPageDidChange(indexPath);
    }
}

-(CGSize)getLabelSize:(UILabel*)label
{
    CGSize maxSize;
    maxSize = [self collectionViewContentSize];
    CGSize labelSize = [label.text sizeWithFont:label.font constrainedToSize:maxSize lineBreakMode:label.lineBreakMode];
    CGRect newFrame = label.frame;
    newFrame.size.height = labelSize.height;
    
    // reset the frame size
    [label setFrame:newFrame];
    
    return label.frame.size;
}


@end
