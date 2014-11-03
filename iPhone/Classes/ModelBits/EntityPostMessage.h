//
//  EntityPostMessage.h
//  ConcurMobile
//
//  Created by yiwen on 11/2/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityPostMessageBody;

@interface EntityPostMessage : NSManagedObject

@property (nonatomic, strong) NSString * header;
@property (nonatomic, strong) NSDate * creationDate;
@property (nonatomic, strong) NSString * contentType;
@property (nonatomic, strong) NSString * uuid;
@property (nonatomic, strong) NSString * reqFileInfo;
@property (nonatomic, strong) NSString * idKey;
@property (nonatomic, strong) NSString * uri;
@property (nonatomic, strong) NSNumber * retried;
@property (nonatomic, strong) EntityPostMessageBody *relPostMessageBody;

@end
