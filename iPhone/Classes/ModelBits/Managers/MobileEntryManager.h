//
//  MobileEntryManager.h
//  ConcurMobile
//
//  Created by charlottef on 10/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//
//  Change Log :
//      Rewritten by PavanA for MOB-12986
//

#import "EntityMobileEntry.h"
#import "BaseManager.h"

@interface MobileEntryManager : BaseManager
{
    NSString   *entityName;
}

+(MobileEntryManager*)sharedInstance;
-(id)init;
-(instancetype)initWithContext:(NSManagedObjectContext*)inContext;

-(EntityMobileEntry *) makeNew;
-(EntityMobileEntry *) fetchOrMake:(NSString *)key;


-(void) clearAll;
-(void) deleteByLocalId:(NSString*)localId;
-(void) deleteByKey:(NSString*)key;

-(void) deleteBypctKey:(NSString*)key;
-(void) deleteBycctKey:(NSString*)key;
-(void) deleteByrcKey:(NSString*)key;
-(void) deleteByEreceiptID:(NSString*)key;
-(void) deleteBySmartExpenseId:(NSString*)smartExpenseId;

-(NSArray*) fetchAll;


-(EntityMobileEntry *) fetchByLocalId:(NSString*)localId;
-(NSArray*) fetchMobileEntriesReferencingLocalReceiptImageId:(NSString*)localReceiptImageId ;
-(EntityMobileEntry *) fetchByKey:(NSString*)key;

-(EntityMobileEntry *) fetchBycctKey:(NSString*)key;
-(EntityMobileEntry *) fetchBypctKey:(NSString*)key;
-(EntityMobileEntry *) fetchByrcKey:(NSString*)key;
-(EntityMobileEntry *) fetchByEreceiptID:(NSString*)key;
-(EntityMobileEntry *) fetchByLocalId:(NSString*)localId;
-(EntityMobileEntry *) fetchBySmartExpenseId:(NSString*)smartExpenseId;

-(NSArray*) fetchMobileEntriesReferencingLocalReceiptImageId:(NSString*)localReceiptImageId;
-(NSArray*) fetchAllCorporateCardMobileEntries;
-(NSArray*) fetchAllPersonalCardMobileEntries;
-(NSArray*) fetchAllPersonalCardNames;
-(NSArray*) fetchAllExpenseItEntries;

+(NSString*) getKey:(EntityMobileEntry*)mobileEntry;
+(BOOL)isCorporateCardTransaction:(EntityMobileEntry*)mobileEntry;
+(BOOL)isCardTransaction:(EntityMobileEntry*)mobileEntry;
+(BOOL)isPersonalCardTransaction:(EntityMobileEntry*)mobileEntry;
+(BOOL)isCardAuthorizationTransaction:(EntityMobileEntry*)mobileEntry;
+(BOOL)isReceiptCapture:(EntityMobileEntry*)mobileEntry;
+(BOOL)isEreceipt:(EntityMobileEntry*)mobileEntry;
+(BOOL)isSmartMatched:(EntityMobileEntry*)mobileEntry;
+(BOOL)isSmartMatchedEReceipt:(EntityMobileEntry*)mobileEntry;

@end
