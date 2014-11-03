//
//  EntityMRU.h
//  ConcurMobile
//
//  Created by ernest cho on 8/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntityMRU : NSManagedObject

@property (nonatomic, retain) NSString * code;
@property (nonatomic, retain) NSString * value;
@property (nonatomic, retain) NSNumber * key;
@property (nonatomic, retain) NSDate * lastUsedDate;
@property (nonatomic, retain) NSString * type;
@property (nonatomic, retain) NSData * extra;

@end
