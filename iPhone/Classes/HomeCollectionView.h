//
//  HomeCollectionView.h
//
//  Created by ernest cho on 11/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "iPadHome9VC.h"

@interface HomeCollectionView : UIView <UICollectionViewDataSource, UICollectionViewDelegate>
// I don't use a protocol here cause really we only have one possible delegate for this, iPadHome9VC
@property (nonatomic, readwrite, strong) iPadHome9VC *delegate;
@property (nonatomic, readwrite, strong) GoviPadHome9VC *govDelegate;

@property (nonatomic, readwrite, strong) IBOutlet UIView* topLevelSubView;
@property (nonatomic, readwrite, strong) IBOutlet UICollectionView* collectionView;

- (void)switchToLandscape;
- (void)switchToPortrait;

- (void)switchLayoutToDefault;
- (void)switchLayoutToTravelOnly;
- (void)switchLayoutToExpenseOnly;
- (void)switchLayoutToApprovalOnly;
- (void)switchLayoutToExpenseAndApprovalOnly;
- (void)switchLayoutToExpenseAndTravelOnly;
- (void)switchLayoutToTravelAndApproval;
- (void)switchLayoutToGovOnly;

- (void)disableFlightBooking;
- (void)disableHotelBooking;
- (void)disableCarBooking;
- (void)disableRailBooking;
- (void)disableExpenses;
- (void)disableExpenseReports;
- (void)disableApprovals;

- (void)setExpensesCount:(NSNumber *)count;
- (void)setExpenseReportsCount:(NSNumber *)count;
- (void)setApprovalCount:(NSNumber *)count;
- (void)setTripsCount:(NSNumber *)count;
- (void)setAuthorizationCount:(NSNumber *)count;
- (void)setVoucherCount:(NSNumber *)count;
- (void)setStampDocumentCount:(NSNumber *)count;

@end
