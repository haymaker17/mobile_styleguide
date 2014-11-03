//
//  IgniteItinDetailSocialOppDelegate.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/31/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntitySalesOpportunity.h"

@protocol IgniteItinDetailSocialOppDelegate <NSObject>
- (void) loadingSocialOppData;
- (void) socialOppDataReceived;
- (void) opportunitySelected:(EntitySalesOpportunity*) opp withFrame:(CGRect) frame;
- (BOOL) isOpportunityScheduled:(EntitySalesOpportunity*) opp;
- (void) addScheduledOpportunity:(EntitySalesOpportunity*) opp;
- (void) removeScheduledOpportunity:(EntitySalesOpportunity*) opp;
@end
