//
//  FindMeDelegate.h
//  ConcurMobile
//
//  Created by yiwen on 8/18/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@class FindMe;
@protocol FindMeDelegate <NSObject>

-(void) locationFound:(FindMe*) findMe;
-(void) locationNotFound:(NSString*) errMsg;

@end
