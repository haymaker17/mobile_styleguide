//
//  EntitySplitSmartExpenses.h
//  ConcurMobile
//
//  Created by ernest cho on 8/16/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntitySplitSmartExpenses : NSManagedObject

@property (nonatomic, retain) NSString * expenseKey;
@property (nonatomic, retain) NSString * matchedMobileEntryKey;

@end
