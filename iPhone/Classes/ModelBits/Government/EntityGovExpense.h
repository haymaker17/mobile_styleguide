//
//  EntityGovExpense.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/31/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntityGovExpense : NSManagedObject

@property (nonatomic, retain) NSString * expenseDesc;
@property (nonatomic, retain) NSDate * expDate;
@property (nonatomic, retain) NSDecimalNumber * amount;
@property (nonatomic, retain) NSString * ccExpId;
@property (nonatomic, retain) NSString * imageId;

@end
