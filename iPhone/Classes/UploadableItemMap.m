//
//  UploadableItemMap.m
//  ConcurMobile
//
//  Created by charlottef on 10/31/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "UploadableItemMap.h"
#import "UploadableMobileEntry.h"
#import "UploadableReceipt.h"

@implementation UploadableItemMap

+(id<UploadableItem>) uploadableItemForEntityInstanceId:(NSString*)entityInstanceId entityTypeName:(NSString*)entityTypeName inContext:(NSManagedObjectContext*)context
{
    if ([entityTypeName isEqualToString:@"EntityMobileEntry"])
        return [[UploadableMobileEntry alloc] initWithEntityInstanceId:entityInstanceId inContext:context];
    else if ([entityTypeName isEqualToString:@"Receipt"])
        return [[UploadableReceipt alloc] initWithLocalReceiptImageId:entityInstanceId];
    
    NSString *itemDescription = [NSString stringWithFormat:@"EntityTypeName: %@, EntityInstanceId: %@", entityTypeName, entityInstanceId];
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"UploadableItemMap::uploadableItemForEntityInstanceId - Could not find item in queue matching %@", itemDescription] Level:MC_LOG_ERRO];
    return nil;
}

+(void) didDequeueEntityInstanceId:(NSString*)entityInstanceId entityTypeName:(NSString*)entityTypeName inContext:(NSManagedObjectContext*)context
{
    if ([entityTypeName isEqualToString:@"EntityMobileEntry"])
        [UploadableMobileEntry didDequeueEntityInstanceId:entityInstanceId inContext:context];
    else if ([entityTypeName isEqualToString:@"Receipt"])
        [UploadableReceipt didDequeueEntityInstanceId:entityInstanceId];
}

@end
