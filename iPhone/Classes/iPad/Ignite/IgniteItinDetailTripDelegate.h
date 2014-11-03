//
//  IgniteItinDetailTripDelegate.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/31/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntitySegment.h"

@protocol IgniteItinDetailTripDelegate <NSObject>

-(void) segmentSelected:(EntitySegment*) segment withCell:(UITableViewCell*) cell;
@end
