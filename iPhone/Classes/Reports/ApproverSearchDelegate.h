//
//  ApproverSearchDelegate.h
//  ConcurMobile
//
//  Created by yiwen on 8/26/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ApproverInfo.h"

@protocol ApproverSearchDelegate <NSObject>

-(void) approverSelected:(ApproverInfo*) approver;

@end
