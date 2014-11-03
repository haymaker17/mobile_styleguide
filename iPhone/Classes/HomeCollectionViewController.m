//
//  HomeCollectionViewController.m
//  ConcurMobile
//
//  Created by ernest cho on 11/25/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "HomeCollectionViewController.h"
#import "HomeCollectionView.h"

// SAMPLE CODE
//
// This class demonstrates how to use the HomeCollectionView
// This is implemented within iPadHome9VC
//
@interface HomeCollectionViewController ()
@property (nonatomic, readwrite, strong) IBOutlet HomeCollectionView *homeCollectionView;
@end

@implementation HomeCollectionViewController

- (id)init
{
    self = [super initWithNibName:@"HomeCollectionViewController" bundle:nil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    // Set the home collection view to show the correct layout
    //[self.homeCollectionView switchLayoutToExpenseOnly];
    //[self.homeCollectionView switchLayoutToTravelOnly];
    //[self.homeCollectionView disableFlightBooking];

    [self rotateHomeCollectionViewToInterfaceOrientation:self.interfaceOrientation];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    [super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
    [self rotateHomeCollectionViewToInterfaceOrientation:toInterfaceOrientation];
}

/**
 The layout of the HomeCollectionView could change quite a bit depending on orientation.
 */
- (void)rotateHomeCollectionViewToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation
{
    if (UIInterfaceOrientationIsPortrait(toInterfaceOrientation)) {
        [self.homeCollectionView switchToPortrait];
    } else {
        [self.homeCollectionView switchToLandscape];
    }
}


@end
