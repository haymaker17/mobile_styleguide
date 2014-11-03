//
//  EntityChatterAttachment.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/8/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityChatterAbstractEntry;

@interface EntityChatterAttachment : NSManagedObject

@property (nonatomic, strong) NSString * title;
@property (nonatomic, strong) NSString * identifier;
@property (nonatomic, strong) NSString * downloadUrl;
@property (nonatomic, strong) NSNumber * hasImagePreview;
@property (nonatomic, strong) EntityChatterAbstractEntry *relEntry;

@end
