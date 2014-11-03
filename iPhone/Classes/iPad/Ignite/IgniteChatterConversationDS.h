//
//  IgniteChatterConversationDS.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/10/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "IgniteChatterConversationDSDelegate.h"
#import "IgniteChatterConversationCellDelegate.h"
#import "EntityChatterFeedEntry.h"

@interface IgniteChatterConversationDS : NSObject <UITableViewDelegate, UITableViewDataSource, NSFetchedResultsControllerDelegate, IgniteChatterConversationCellDelegate>
{
    UITableView                             *tableList;
    id<IgniteChatterConversationDSDelegate> __weak _delegate;

    NSFetchedResultsController              *fetchedResultsController;
    NSManagedObjectContext                  *managedObjectContext;
    
    EntityChatterFeedEntry                  *feedEntry;
}

@property (nonatomic, strong) UITableView                               *tableList;
@property (nonatomic, weak) id<IgniteChatterConversationDSDelegate>   delegate;

@property (nonatomic, strong) NSFetchedResultsController                *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext                    *managedObjectContext;

@property (nonatomic, strong) EntityChatterFeedEntry                    *feedEntry;

-(void) setSeedData:(NSManagedObjectContext*)con withTable:(UITableView *)tbl withDelegate:(id<IgniteChatterConversationDSDelegate>) del withFeedEntry:(EntityChatterFeedEntry*)entry;

@end
