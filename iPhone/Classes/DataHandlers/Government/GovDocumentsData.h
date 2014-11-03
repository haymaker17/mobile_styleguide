//
//  GovDocumentsData.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/15/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntityGovDocument.h"

@interface GovDocumentsData : MsgResponderCommon
{
    EntityGovDocument       *currentDoc;
    NSString                *filter;
    NSMutableDictionary     *existingDocs;  // Key - "TravelerId-DocName"
}

@property (nonatomic, strong) EntityGovDocument                 *currentDoc;
@property (nonatomic, strong) NSString                          *filter;
@property (nonatomic, strong, readonly) NSManagedObjectContext  *managedObjectContext;
@property (nonatomic, strong) NSMutableDictionary               *existingDocs;

- (Msg*) newMsg:(NSMutableDictionary*)parameterBag;
- (NSString*) keyForDoc:(EntityGovDocument*) doc;
- (void)cleanupAllRemainingObjects;
- (NSString*) serverEndPoint;

+ (void) fillDocListInfo:(EntityGovDocument*) doc withName:(NSString*) name withValue:(NSString*)value;
@end
