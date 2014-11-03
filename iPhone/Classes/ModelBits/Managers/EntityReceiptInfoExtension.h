//
//  EntityReceiptInfoExtension.h
//  ConcurMobile
//
//  Created by charlottef on 3/14/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntityReceiptInfo.h"
#import "ReceiptStoreReceipt.h"

// Category EntityReceiptExtension adds methods to EntityReceipt
@interface EntityReceiptInfo (EntityReceiptInfoExtension)
{
}

+(EntityReceiptInfo *) makeNewInContext:(NSManagedObjectContext*)context;

+(void) deleteByImageId:(NSString*)imageId inContext:(NSManagedObjectContext*)context;

+(NSArray*) fetchAllInContext:(NSManagedObjectContext*)context;

+(EntityReceiptInfo *) fetchByImageId:(NSString*)imageId inContext:(NSManagedObjectContext*)context;

+(EntityReceiptInfo*) updateOrCreateFromReceiptStoreReceipt:(ReceiptStoreReceipt*)receiptInfo inContext:(NSManagedObjectContext*)context;

-(ReceiptStoreReceipt*) makeReceiptStoreReceipt;

@end
