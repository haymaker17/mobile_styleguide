//
//  GovDocumentsToStampData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//
//  Update the needsStamping flag is data is from server

#import "GovDocumentsToStampData.h"
#import "GovDocumentManager.h"

@implementation GovDocumentsToStampData


-(NSString *)getMsgIdKey
{
	return GOV_DOCUMENTS_TO_STAMP;
}

-(NSString*) serverEndPoint
{
    return @"GetTMDocuments";
}

- (void)saveDocument
{
    if (self.currentDoc != nil)
    {
        self.currentDoc.needsStamping = [NSNumber numberWithBool:YES];
        NSString *key = [self keyForDoc:self.currentDoc];
        EntityGovDocument* existingDoc = [self.existingDocs objectForKey:key];
        
        if (existingDoc != nil)
        {
            [GovDocumentManager copyFrom: self.currentDoc to:existingDoc];
            // Save needsStamping flag to update existing and remove from existingDocs list
            [self.existingDocs removeObjectForKey:key];
            [[self managedObjectContext] deleteObject:self.currentDoc];
        }
    }
    self.currentDoc = nil;
}

- (void)cleanupAllRemainingObjects
{
    // Clear out needsStamping flag for all still in dict
    for (EntityGovDocument* doc in self.existingDocs.allValues)
    {
        doc.needsStamping = [NSNumber numberWithBool:NO];
    }

}

@end
