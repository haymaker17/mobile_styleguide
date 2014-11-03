//
//  EntityChatterAbstractEntry.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/17/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityChatterAttachment, EntityChatterAuthor;

@interface EntityChatterAbstractEntry : NSManagedObject

@property (nonatomic, strong) NSDate * createdDate;
@property (nonatomic, strong) NSNumber * likedByMe;
@property (nonatomic, strong) NSString * identifier;
@property (nonatomic, strong) NSString * type;
@property (nonatomic, strong) NSString * text;
@property (nonatomic, strong) NSNumber * totalLikes;
@property (nonatomic, strong) NSString * parentIdentifier;
@property (nonatomic, strong) EntityChatterAuthor *author;
@property (nonatomic, strong) EntityChatterAttachment *relAttachment;

@end
