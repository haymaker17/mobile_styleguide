//
//  AbstractDataSource.m
//  PastDestinations
//
//  Created by Pavan Adavi on 6/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "AbstractDataSource.h"

@implementation AbstractDataSource

- (instancetype)init
{
    self = [super init];
    if (!self)
        return nil;
    
    return self;
}


- (NSArray *)indexPathsForItem:(id)object
{
    NSAssert(NO, @"Should be implemented by subclasses");
    return nil;
}

- (id)itemAtIndexPath:(NSIndexPath *)indexPath
{
    NSAssert(NO, @"Should be implemented by subclasses");
    return nil;
}

- (void)removeItemAtIndexPath:(NSIndexPath *)indexPath
{
    NSAssert(NO, @"Should be implemented by subclasses");
    return;
}

-(void)insertItemAtIndexPath:(id)item  indexPath:(NSIndexPath *)indexPath
{
    NSAssert(NO, @"Should be implemented by subclasses");
    return;
}

- (NSInteger)numberOfSections
{
    return 1;
}

- (void)resetContent
{
    NSAssert(NO, @"Should be implemented by subclasses");
    return;
}

- (void)loadContent
{
    NSAssert(NO, @"Should be implemented by subclasses");
    return;

}

-(void)setNeedsLoadContent
{
    NSAssert(NO, @"Optional May be implemented by subclasses");
    return;

}

-(AbstractDataSource *)dataSourceForSectionAtIndex:(NSInteger)sectionIndex
{
    return self;
}

- (NSString *)sectionIndexTitleForSectionName:(NSString *)sectionName
{
    NSAssert(NO, @"Optional - may be implemented by subclasses");
    return nil;
}


- (NSInteger)sectionForSectionIndexTitle:(NSString *)title atIndex:(NSInteger)sectionIndex
{
    NSAssert(NO, @"Optional - may be implemented by subclasses");
    return nil;
}

@end
