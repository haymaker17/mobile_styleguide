//
//  GovDocumentManager.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/15/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BaseManager.h"
#import "EntityGovDocument.h"

extern NSString * const GOV_DOC_TYPE_AUTH;
extern NSString * const GOV_DOC_TYPE_VOUCHER;
//extern NSString * const GOV_DOC_TYPE_AUTH_FOR_VCH;
extern NSString * const GOV_DOC_TYPE_ALL;
extern NSString * const GOV_DOC_TYPE_STAMP;

@interface GovDocumentManager :  BaseManager

+(GovDocumentManager*)sharedInstance;
+(void) copyFrom:(EntityGovDocument*) src to:(EntityGovDocument*) dest;

-(EntityGovDocument*) fetchDocumentByDocName: (NSString*) docName withType:(NSString*)docType withTravelerId:(NSString*) travId;

-(NSArray*) fetchDocumentsByDocType:(NSString*) docType withContext:(NSManagedObjectContext*) context;

-(void) deleteAllByDocType:(NSString*) docType withContext:(NSManagedObjectContext*) context;

@end
