//
//  HomeTableViewCellDescriptionFactory.h
//  ConcurMobile
//
//  Created by ernest cho on 12/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface HomeTableViewCellDescriptionFactory : NSObject

+ (NSMutableArray *)descriptionsForDefault;
+ (NSMutableArray *)descriptionsForExpenseOnly;
+ (NSMutableArray *)descriptionsForTravelOnlyWithRail:(BOOL)railEnabled;
+ (NSMutableArray *)descriptionsForApprovalOnly;
+ (NSMutableArray *)descriptionsForExpenseAndApprovalOnly;
+ (NSMutableArray *)descriptionsForExpenseAndTravelOnly;
+ (NSMutableArray *)descriptionsForTravelandApprovalWithRail:(BOOL)railEnabled;

@end
