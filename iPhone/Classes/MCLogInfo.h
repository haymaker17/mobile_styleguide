//
//  MCLogInfo.h
//  ConcurMobile
//
//  Created by yiwen on 12/28/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MCLogging.h"

@interface MCLogInfo : NSObject {

	NSString * text;
	NSDate * timestamp;
	MCLogLevel level;
}

@property (strong, nonatomic) NSString * text;
@property (strong, nonatomic) NSDate * timestamp;
@property MCLogLevel level;
@end
