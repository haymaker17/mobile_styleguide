//
//  EntityReceiptInfoExtension.m
//  ConcurMobile
//
//  Created by charlottef on 3/14/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "EntityReceiptInfoExtension.h"
#import "ReceiptStoreReceipt.h"
#import "CCDateUtilities.h"

@implementation EntityReceiptInfo (EntityReceiptInfoExtension)

#pragma mark - Creation and deletion

+(EntityReceiptInfo *) makeNewInContext:(NSManagedObjectContext*)context
{
    return [NSEntityDescription insertNewObjectForEntityForName:@"EntityReceiptInfo" inManagedObjectContext:context];
}

+(void) deleteByImageId:(NSString*)imageId inContext:(NSManagedObjectContext*)context
{
    EntityReceiptInfo *receiptInfo = [EntityReceiptInfo fetchByImageId:imageId inContext:context];
    if (receiptInfo != nil)
        [receiptInfo.managedObjectContext deleteObject:receiptInfo];
}

+(NSArray*) fetchAllInContext:(NSManagedObjectContext*)context
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityReceiptInfo" inManagedObjectContext:context];
    [fetchRequest setEntity:entity];
    
    NSError *error = nil;
    NSArray *aFetch = [context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}

+(EntityReceiptInfo *) fetchByImageId:(NSString*)imageId inContext:(NSManagedObjectContext*)context
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityReceiptInfo" inManagedObjectContext:context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(receiptId = %@)", imageId];
    [fetchRequest setPredicate:pred];
    
    NSError *error = nil;
    NSArray *aFetch = [context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    
    return nil;
}

+(EntityReceiptInfo*) updateOrCreateFromReceiptStoreReceipt:(ReceiptStoreReceipt*)receiptInfo inContext:(NSManagedObjectContext*)context
{
    EntityReceiptInfo *entity = [EntityReceiptInfo fetchByImageId:receiptInfo.receiptImageId inContext:context];
    if (entity == nil)
        entity = [EntityReceiptInfo makeNewInContext:context];
    
    entity.receiptId = receiptInfo.receiptImageId;
    entity.dateCreated = [CCDateUtilities formatDateForReceiptInfoEntity:receiptInfo.imageDate];
    entity.thumbUrl = receiptInfo.thumbUrl;
    entity.imageUrl = receiptInfo.imageUrl;
    entity.imageType = receiptInfo.fileType;
    
    return entity;
}

-(ReceiptStoreReceipt*) makeReceiptStoreReceipt
{
    ReceiptStoreReceipt* receipt = [[ReceiptStoreReceipt alloc] init];
    
    //receipt.fileName = ;
    receipt.fileType = self.imageType;
    receipt.imageDate = [CCDateUtilities formatDateToISO8601DateTimeInString:self.dateCreated];
    //receipt.imageOrigin = ;
    receipt.receiptImageId = self.receiptId;
    receipt.imageUrl = self.imageUrl;
    receipt.thumbUrl = self.thumbUrl;
    //receipt.thumbImage = ;
    //receipt.fullScreenImage = ;
    //receipt.commentTag = ;
    
    return receipt;
}

@end
