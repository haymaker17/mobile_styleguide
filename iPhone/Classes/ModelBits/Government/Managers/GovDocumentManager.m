//
//  GovDocumentManager.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/15/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovDocumentManager.h"

//All   - get both authorizations and vouchers
//AUTH  - get authorizations
//VCH   - get vouchers
//Stamp - used by view to display those needs current user's stamping.
NSString * const GOV_DOC_TYPE_AUTH = @"AUTH";
//NSString * const GOV_DOC_TYPE_AUTH_FOR_VCH = @"AUTH_FOR_VCH";
NSString * const GOV_DOC_TYPE_VOUCHER = @"VCH";
NSString * const GOV_DOC_TYPE_ALL = @"All";
NSString * const GOV_DOC_TYPE_STAMP = @"Stamp";

static GovDocumentManager *sharedInstance;

@implementation GovDocumentManager

+(GovDocumentManager*)sharedInstance
{
    if (sharedInstance != nil)
	{
		return sharedInstance;
	}
	else
	{
		@synchronized (self)
		{
			if (sharedInstance == nil)
			{
				sharedInstance = [[GovDocumentManager alloc] init];
			}
		}
		return sharedInstance;
	}

}

-(EntityGovDocument*) fetchDocumentByDocName: (NSString*) docName withType:(NSString*)docType withTravelerId:(NSString*) travId
{
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(docName = %@ and docType = %@ and travelerId = %@)", docName, docType, travId];
    return (EntityGovDocument*)[self fetchFirst:@"EntityGovDocument" withCondition:pred];

}

-(NSArray*) fetchDocumentsByDocType:(NSString*) docType withContext:(NSManagedObjectContext*) context
{
    NSPredicate *pred = [GOV_DOC_TYPE_ALL isEqualToString:docType]? nil:[NSPredicate predicateWithFormat:@"(docType = %@)", docType];
    return (NSArray*)[self fetch:@"EntityGovDocument" withCondition: pred withContext:context];
}

-(void) deleteAllByDocType:(NSString*) docType withContext:(NSManagedObjectContext*) context
{
    NSArray* allObj = [self fetchDocumentsByDocType:docType withContext:context];
    for(NSManagedObject * obj in allObj)
        [self deleteObj:obj withContext:context];
}

+(void) copyFrom:(EntityGovDocument*) src to:(EntityGovDocument*) dest
{
    dest.tripEndDate = src.tripEndDate;
    dest.travelerId = src.travelerId;
    dest.purposeCode = src.purposeCode;
    dest.docName = src.docName;
    dest.approveLabel = src.approveLabel;
    dest.docTypeLabel = src.docTypeLabel;
    dest.totalExpCost = src.totalExpCost;
    dest.tripBeginDate = src.tripBeginDate;
    dest.gtmDocType = src.gtmDocType;
    dest.docType = src.docType;
    dest.travelerName = src.travelerName;
    dest.needsStamping = src.needsStamping;
}
@end
