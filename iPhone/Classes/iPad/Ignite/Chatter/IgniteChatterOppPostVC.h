//
//  IgniteChatterOppPostVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/21/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IgniteChatterPostVC.h"
#import "IgniteChatterOppPostFeedDS.h"
#import "EntitySalesOpportunity.h"

@interface IgniteChatterOppPostVC : IgniteChatterPostVC 
{
    UITableView                     *tableList;
    IgniteChatterOppPostFeedDS      *dsFeed;
    EntitySalesOpportunity          *opportunity;
}

@property (nonatomic, strong) IBOutlet UITableView          *tableList;
@property (nonatomic, strong) IgniteChatterOppPostFeedDS    *dsFeed;
@property (nonatomic, strong) EntitySalesOpportunity        *opportunity;
@end
