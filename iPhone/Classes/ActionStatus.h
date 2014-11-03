//
//  ActionStatus.h
//  ConcurMobile
//
//  Created by yiwen on 4/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface ActionStatus : NSObject {
	NSString *status;
	NSString *errMsg;  // localized error message
}

@property (nonatomic, strong) NSString		*status;
@property (nonatomic, strong) NSString		*errMsg;


@end
