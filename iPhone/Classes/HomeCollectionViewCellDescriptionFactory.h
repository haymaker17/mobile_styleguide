//
//  HomeCollectionViewCellDescriptionFactory.h
//  ConcurHomeCollectionView
//
//  Created by ernest cho on 11/19/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface HomeCollectionViewCellDescriptionFactory : NSObject

+ (NSMutableArray *)descriptionsForDefault;
+ (NSMutableArray *)descriptionsForExpenseOnly;
+ (NSMutableArray *)descriptionsForTravelOnlyWithRail:(BOOL)railEnabled;
+ (NSMutableArray *)descriptionsForApprovalOnly;
+ (NSMutableArray *)descriptionsForExpenseAndApprovalOnly;
+ (NSMutableArray *)descriptionsForExpenseAndTravelOnly;
+ (NSMutableArray *)descriptionsForTravelandApprovalWithRail:(BOOL)railEnabled;
+ (NSMutableArray *)descriptionsForGovOnly;

@end
