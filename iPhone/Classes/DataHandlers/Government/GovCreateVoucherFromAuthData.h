//
//  GovCreateVoucherFromAuthData.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/19/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "EntityGovDocument.h"
#import "ActionStatus.h"

@interface GovCreateVoucherFromAuthData : MsgResponderCommon
{
    NSString                    *docName; // Auth doc name
    NSString                    *docType;
    NSString                    *travid;
    NSString                    *returnDocName;
    NSString                    *returnDocType;
    NSString                    *gtmDocType;
    
    EntityGovDocument           *_voucherListInfo;
    ActionStatus                *status;
}

@property (nonatomic, strong) NSString                          *docName;
@property (nonatomic, strong) NSString                          *docType;
@property (nonatomic, strong) NSString                          *returnDocName;
@property (nonatomic, strong) NSString                          *returnDocType;
@property (nonatomic, strong) NSString                          *gtmDocType;
@property (nonatomic, strong) NSString                          *travid;
@property (nonatomic, strong) ActionStatus                      *status;

@property (nonatomic, strong, readonly) NSManagedObjectContext  *managedObjectContext;

-(Msg*) newMsg:(NSMutableDictionary*)parameterBag;


@end
