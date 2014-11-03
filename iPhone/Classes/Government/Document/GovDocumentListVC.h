//
//  GovDocumentListVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/19/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileTableViewController.h"

@interface GovDocumentListVC : MobileTableViewController <NSFetchedResultsControllerDelegate, UIActionSheetDelegate>
{
    NSString        *filter;
}

@property (nonatomic, strong) NSString                      *filter;
@property (nonatomic, strong) NSFetchedResultsController    *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext        *managedObjectContext;

- (void)resetData;

@end
