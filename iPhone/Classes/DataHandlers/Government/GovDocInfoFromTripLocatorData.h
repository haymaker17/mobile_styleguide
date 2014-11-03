//
//  GovDocInfoFromTripLocatorData.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 2/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntityGovDocument.h"

@interface GovDocInfoFromTripLocatorData : MsgResponderCommon
{
    NSString            *tanum;
    NSString            *travid; // If empty, default to current user
    NSString            *tripLocator;

    EntityGovDocument   *currentDoc;
    
    NSString            *currentDocName;
    NSString            *currentDocType;
}

@property (nonatomic, strong) NSString                          *tanum;
@property (nonatomic, strong) NSString                          *travid;
@property (nonatomic, strong) NSString                          *tripLocator;
@property (nonatomic, strong) EntityGovDocument                 *currentDoc;
@property (nonatomic, strong) NSString                          *currentDocName;
@property (nonatomic, strong) NSString                          *currentDocType;

@property (nonatomic, strong, readonly) NSManagedObjectContext  *managedObjectContext;

-(Msg*) newMsg:(NSMutableDictionary*)parameterBag;

@end
