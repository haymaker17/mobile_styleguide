//
//  UploadQueueDS.h
//  ConcurMobile
//
//  Created by Shifan Wu on 11/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "UploadQueueDSDelegate.h"

@interface UploadQueueDS : NSObject<NSFetchedResultsControllerDelegate, UITableViewDelegate, UITableViewDataSource>
{
    UITableView                         *tableList;
    
    NSFetchedResultsController          *fetchedResultsController;
    NSManagedObjectContext              *managedObjectContext;
    
    id<UploadQueueDSDelegate>   __weak _delegate;
}

@property (nonatomic, strong) UITableView                           *tableList;

@property (nonatomic, strong) NSFetchedResultsController            *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext                *managedObjectContext;

@property (nonatomic, weak) id<UploadQueueDSDelegate> delegate;

-(void) setSeedData:(UITableView *)tbl withDelegate:(id<UploadQueueDSDelegate>)del;

@end
