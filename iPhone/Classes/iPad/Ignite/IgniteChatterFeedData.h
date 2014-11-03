//
//  IgniteChatterFeedData.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/31/12.
//  Copyright 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "MsgResponderCommon.h"

@interface IgniteChatterFeedData : MsgResponder
{
    NSString                *itemId;
    NSString                *feedLabel;
    NSMutableDictionary     *photoDict; // Key = cache name, Value = url
}

@property (nonatomic, strong, readonly) NSManagedObjectContext  *managedObjectContext;

@property (nonatomic, strong) NSString                          *itemId;
@property (nonatomic, strong) NSString                          *feedLabel;
@property (nonatomic, strong) NSMutableDictionary               *photoDict;

-(void) preloadPhotos;

@end
