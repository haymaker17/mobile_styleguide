//
//  HotelsNearMeListSectionInfo.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/9/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ConcreteDataSourceSectionInfo.h"

@interface ConcreteDataSourceSectionInfo ()
@property (nonatomic,strong) NSMutableArray *itemList;
@property (nonatomic,strong) NSString *sectionTitle;
@property (nonatomic,strong) NSString *sectionName;

@end

@implementation ConcreteDataSourceSectionInfo

-(id)init
{
    return [self initWithArray:nil];
}

-(instancetype)initWithArray:(NSArray *)items
{
    self = [super init];
    
    if (!self) {
        return  nil;
    }
    // Do initialization
    _itemList = items;
    return self;
}

-(NSString *)name
{
    return _sectionName;
}

-(NSUInteger)numberOfObjects
{
    return [_itemList count];
}

-(NSArray *)objects
{
    return [_itemList copy];
}

-(NSString *)indexTitle
{
    return _sectionTitle;
}

@end
