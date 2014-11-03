//
//  GovStampTMDocumentData.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/16/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ActionStatus.h"
#import "MsgResponderCommon.h"
#import "EntityGovDocument.h"

@interface GovStampTMDocumentData : MsgResponderCommon
{
    NSString            *docName;
    NSString            *docType;
    NSString            *sigkey;
    NSString            *stampName;
    NSString            *travid;
    
    NSString            *comments;
    NSString            *reasonCode;
    NSString            *returnTo;
    
    ActionStatus        *status;
    EntityGovDocument   *currentDoc;

}

@property (nonatomic, strong) NSString                          *docName;
@property (nonatomic, strong) NSString                          *docType;
@property (nonatomic, strong) NSString                          *sigkey;
@property (nonatomic, strong) NSString                          *stampName;
@property (nonatomic, strong) NSString                          *travid;

@property (nonatomic, strong) NSString                          *comments;
@property (nonatomic, strong) NSString                          *reasonCode;
@property (nonatomic, strong) NSString                          *returnTo;

@property (nonatomic, strong) ActionStatus                      *status;
@property (nonatomic, strong) EntityGovDocument                 *currentDoc;

@property (nonatomic, strong, readonly) NSManagedObjectContext  *managedObjectContext;

-(Msg*) newMsg:(NSMutableDictionary*)parameterBag;

@end
