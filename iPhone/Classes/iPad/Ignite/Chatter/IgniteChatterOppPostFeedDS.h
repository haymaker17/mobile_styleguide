//
//  IgniteChatterOppPostFeedDS.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/22/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "IgniteItinDetailSocialFeedDS.h"

@interface IgniteChatterOppPostFeedDS : IgniteItinDetailSocialFeedDS
{
    NSString                *opportunityId;
    NSString                *feedEntryIdentifier;
}

@property (nonatomic, strong) NSString              *opportunityId;
@property (nonatomic, strong) NSString              *feedEntryIdentifier;

-(void) setSeedData:(NSManagedObjectContext *)con withOppId:(NSString *)oppId withTable:(UITableView *)tbl withDelegate:(id<IgniteItinDetailSocialFeedDelegate>) del;

@end
