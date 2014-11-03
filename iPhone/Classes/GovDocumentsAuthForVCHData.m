//
//  GovDocumentsAuthForVCH.m
//  ConcurMobile
//
//  Created by Shifan Wu on 12/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovDocumentsAuthForVCHData.h"
#import "GovDocumentManager.h"

@implementation GovDocumentsAuthForVCHData
//@synthesize managedObjectContext=__managedObjectContext;

- (NSString *)getMsgIdKey
{
    return GOV_DOCUMENTS_AUTH_FOR_VCH;
}

- (NSString *)serverEndPoint
{
    return @"GetAuthForVchTMDocuments";
}

- (Msg *)newMsg:(NSMutableDictionary *)parameterBag
{
//  GET /Mobile/GovTravelManager/GetAuthForVchTMDocuments
//    self.filter = [parameterBag objectForKey:@"FILTER"];
//    if (![self.filter length])
//        self.filter = GOV_DOC_TYPE_AUTH_FOR_VCH;
    
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/%@",[ExSystem sharedInstance].entitySettings.uri, [self serverEndPoint]];
    
    Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

- (void)parserDidStartDocument:(NSXMLParser *)parser
{
    // This needs to be here to override default implementation of the super class
//    NSArray* allObj = [[GovDocumentManager sharedInstance] fetchDocumentsByDocType:GOV_DOC_TYPE_AUTH_FOR_VCH withContext:[self managedObjectContext]];
//    for (EntityGovDocument* doc in allObj)
//    {
//        [[self managedObjectContext] deleteObject:doc];
//    }
}

//-(void)parserDidEndDocument:(NSXMLParser *)parser
//{
//    [super parserDidEndDocument:parser];
//    [self cleanupAllRemainingObjects];
//    
//    [self saveContext];
//    
//    // remove observer
//    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
//    [[NSNotificationCenter defaultCenter] removeObserver:ad name:NSManagedObjectContextDidSaveNotification object:self.managedObjectContext];
//    
//    __managedObjectContext = nil;
//}

- (void)saveDocument
{
    if (self.currentDoc != nil)
    {
        self.currentDoc.authForVch = [NSNumber numberWithBool:YES];
        NSString *key = [self keyForDoc:self.currentDoc];
        EntityGovDocument* existingDoc = [self.existingDocs objectForKey:key];
        
        if (existingDoc != nil)
        {
            [GovDocumentManager copyFrom: self.currentDoc to:existingDoc];
            // Save authForVch flag to update existing and remove from existingDocs list
            [self.existingDocs removeObjectForKey:key];
            [[self managedObjectContext] deleteObject:self.currentDoc];
        }
    }
    self.currentDoc = nil;
}

- (void)cleanupAllRemainingObjects
{
    // Clear out authForVch flag for all still in dict
    for (EntityGovDocument* doc in self.existingDocs.allValues)
    {
        doc.authForVch = [NSNumber numberWithBool:NO];
    }
    
}

//- (void)saveContext
//{
//   NSError *error = nil;
//    NSManagedObjectContext *managedObjectContext = __managedObjectContext;
//    if (managedObjectContext != nil)
//    {
//        if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
//        {
//            /*
//             Replace this implementation with code to handle the error appropriately.
//             
//             abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. If it is not possible to recover from the error, display an alert panel that instructs the user to quit the application by pressing the Home button.
//             */
//           NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
//            abort();
//        }
//    }
//}

@end
