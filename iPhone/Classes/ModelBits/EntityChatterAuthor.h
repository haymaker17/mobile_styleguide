//
//  EntityChatterAuthor.h
//  ConcurMobile
//
//  Created by  on 8/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityChatterAbstractEntry;

@interface EntityChatterAuthor : NSManagedObject

@property (nonatomic, strong) NSString * identifier;
@property (nonatomic, strong) NSString * smallPhotoUrl;
@property (nonatomic, strong) NSString * companyName;
@property (nonatomic, strong) NSString * name;
@property (nonatomic, strong) EntityChatterAbstractEntry *entry;

@end
